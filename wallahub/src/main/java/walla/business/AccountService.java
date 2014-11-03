package walla.business;

import java.util.*;
import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.db.*;
import walla.utils.*;


import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.xml.datatype.*;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.security.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Qualifier;
import org.apache.commons.lang3.*;

@Service("AccountService")
public class AccountService {

	private AccountDataHelperImpl accountDataHelper;
	private UtilityDataHelperImpl utilityDataHelper;
	private TagService tagService;
	private CategoryService categoryService;
	private GalleryService galleryService;
	private CachedData cachedData;
	
	private static final Logger meLogger = Logger.getLogger(AccountService.class);

	public AccountService()
	{
		String simon = System.getProperty("simon.simon");
	}
	
	//*************************************************************************************************************
	//***********************************  Web server synchronous methods *****************************************
	//*************************************************************************************************************
	
	/* Account setup sequence and status

		Main
		1 - initial details setup
		2 - live (email and banking done)
		3 - shutdown pending
		4 - closed
		
		Email
		0 - email not sent
		1 - email sent 
		2 - email not confirmed in timely manner
		3 - email confirmed
		
		Banking
		0 - not setup
		1 - details received
		2 - validated
		3 - details need to be re-setup
	 */
	
	//Create Account (Brief details) + Email.
	//Collect bank details and personal contact information (if paid account)
	//Send out confirmation email.
	//Receive confirmation email. (Link clicked)
	//Account open
	//Account close requested
	//Account close completed

	public void CreateAccount(Account account, CustomResponse customResponse, CustomSessionState customSession)
	{
		String email = "";
		long startMS = System.currentTimeMillis();
		try 
		{
			email = (account.getEmail() == null) ? "" : account.getEmail();
			
			//Create new account
			if (!UserTools.ValidEmailAddress(email))
			{
				meLogger.warn("Account create failed, email doesn't fit a standard form.  Email:" + email);
				customResponse.setMessage("Email doesn't fit a standard form");
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return;
			}
			
			if (!UserTools.CheckPasswordStrength(account.getPassword()))
			{
				meLogger.warn("Account create failed, password does not meet minimum complexity rules." + account.getPassword());
				customResponse.setMessage("Password does not meet minimum complexity rules");
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return;
			}
			
			if (account.getProfileName().length() >30 || account.getProfileName().contains(" "))
			{
				meLogger.error("Profile name is not set correctly.  " + account.getProfileName());
				customResponse.setMessage("Profile name is not set correctly");
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return;
			}
			
			if (!accountDataHelper.ProfileNameIsUnique(account.getProfileName()))
			{
				String error = "Profile name is already in use.  " + account.getProfileName();
				meLogger.error(error);
				customResponse.setMessage("Profile name is already in use");
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return;
			}

			//check email is unique
			
			String salt = SecurityTools.GenerateSalt();
			String passwordHash = SecurityTools.GetHashedPassword(account.getPassword(), salt, 160, 1000);
			
			long newUserId = accountDataHelper.CreateAccount(account, passwordHash, salt);
			if (newUserId == 0)
			{
				meLogger.warn("User could not be created.");
				customResponse.setMessage("User could not be created");
				customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return;
			}

			//New User created OK, auto-login session.
			synchronized(customSession) 
			{
				customSession.getCustomSessionIds().add(UserTools.GetComplexString());
				customSession.setFailedLogonCount(0);
				customSession.setFailedLogonLast(null);
				customSession.setAuthenticated(true);
				customSession.setAdmin(true);
				customSession.setProfileName(account.getProfileName());
				customSession.setUserId(newUserId);
			}
			accountDataHelper.UpdateLogonState(newUserId, 0, null);
			
			//TODO decouple.
			SendEmailConfirm(newUserId);
			
			meLogger.info("New user has been created.  Email: " + account.getEmail() + " UserId:" + newUserId);
			customResponse.setResponseCode(HttpStatus.CREATED.value());
		}
		catch (WallaException wallaEx) {
			customResponse.setResponseCode(wallaEx.getCustomStatus());
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally { UserTools.LogMethod("CreateAccount", meLogger, startMS, email); }
	}
	
	public int UpdateAccount(Account account)
	{
		long startMS = System.currentTimeMillis();
		try 
		{
			accountDataHelper.UpdateAccount(account);
			return HttpStatus.OK.value();
		}
		catch (WallaException wallaEx) {
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
		finally { UserTools.LogMethod("UpdateAccount", meLogger, startMS, account.getProfileName()); }
	}

	public Account GetAccount(long userId, CustomResponse customResponse)
	{
		long startMS = System.currentTimeMillis();
		try 
		{
			Account account = accountDataHelper.GetAccount(userId);
			if (account == null)
			{
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return null;
			}

			customResponse.setResponseCode(HttpStatus.OK.value());
			return account;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		finally { UserTools.LogMethod("GetAccount", meLogger, startMS, String.valueOf(userId)); }
	}
	
	public int AckEmailConfirm(String profileName, String requestValidationString)
	{
		//TODO - expire email validation string
		long startMS = System.currentTimeMillis();
		try
		{
			if (profileName.length() >30 || profileName.contains(" "))
			{
				meLogger.error("Profile name is not set correctly.  " + profileName);
				return HttpStatus.BAD_REQUEST.value();
			}
			
			String sql = "SELECT [UserId] FROM [dbo].[User] WHERE [ProfileName] = '" + profileName + "'";
			long userId = utilityDataHelper.GetLong(sql);
			
			sql = "SELECT [ValidationString] FROM [dbo].[User] WHERE [EmailStatus] = 1 AND [UserId] = " + userId;
			String serverValidationString = utilityDataHelper.GetString(sql);
			if (serverValidationString.equals(requestValidationString))
			{
				accountDataHelper.UpdateEmailStatus(userId, 3, "");;
				//Check if banking is all done and if so, mark the account as Live.
				/*
				sql = "SELECT [BankingStatus] FROM [User] WHERE UserId = " + userId;
				int bankingStatus = utilityDataHelper.GetInt(sql);
				if (bankingStatus == 2)
				{
					accountDataHelper.UpdateMainStatus(userId, 2);
				}
				*/
				return HttpStatus.OK.value();
			}
			else
			{
				meLogger.error("AckEmailConfirm - Validation string didn't match for account: " + userId + " requestValidationString:" + requestValidationString);
				return HttpStatus.BAD_REQUEST.value();
			}
		}
		catch (WallaException wallaEx) {
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
		finally { UserTools.LogMethod("AckEmailConfirm", meLogger, startMS, profileName); }
	}

	
	
	
	
	public long CreateUserApp(long userId, int appId, int platformId, UserApp proposedUserApp, CustomResponse customResponse)
	{
		long startMS = System.currentTimeMillis();
		try {
			meLogger.debug("CreateUserApp() begins. UserId:" + userId);
			
			if (proposedUserApp.getMachineName() == null || proposedUserApp.getMachineName().isEmpty())
			{
				meLogger.warn("CreateUserApp didn't receive a machine name, this is mandatory.");
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return 0;
			}
			
			long userAppId = accountDataHelper.FindExistingUserApp(userId, appId, platformId, proposedUserApp.getMachineName());
			if (userAppId > 0)
			{
				customResponse.setResponseCode(HttpStatus.CREATED.value());
				return userAppId;
			}
			
			UserApp newUserApp = new UserApp();
			userAppId = utilityDataHelper.GetNewId("UserAppId");

			App app = cachedData.GetApp(appId, "");
			newUserApp.setId(userAppId);
			newUserApp.setAppId(appId);
			newUserApp.setPlatformId(platformId);
			newUserApp.setFetchSize(app.getDefaultFetchSize());
			newUserApp.setThumbCacheSizeMB(app.getDefaultThumbCacheMB());
			newUserApp.setMainCopyCacheSizeMB(app.getDefaultMainCopyCacheMB());
			newUserApp.setAutoUpload(false);
			newUserApp.setAutoUploadFolder("");
			newUserApp.setMainCopyFolder("");
			newUserApp.setMachineName(proposedUserApp.getMachineName());

			//Create or find new userapp tag (system owned).
			newUserApp.setTagId(tagService.CreateOrFindUserAppTag(userId, platformId, proposedUserApp.getMachineName()));
			
			//Create new auto upload category. 
			newUserApp.setUserAppCategoryId(categoryService.CreateOrFindUserAppCategory(userId, platformId, newUserApp.getMachineName()));
			
			//Default user category
			newUserApp.setUserDefaultCategoryId(categoryService.FindDefaultUserCategory(userId));
			
			//Get default gallery.
			newUserApp.setGalleryId(galleryService.GetDefaultGallery(userId, appId));
			
			if (proposedUserApp.isAutoUpload())
				newUserApp.setAutoUpload(true);
			
			if (proposedUserApp.getAutoUploadFolder() != null && !proposedUserApp.getAutoUploadFolder().isEmpty())
				newUserApp.setAutoUploadFolder(proposedUserApp.getAutoUploadFolder());
			
			if (proposedUserApp.getMainCopyFolder() != null && !proposedUserApp.getMainCopyFolder().isEmpty())
				newUserApp.setMainCopyFolder(proposedUserApp.getMainCopyFolder());
			
			accountDataHelper.CreateUserApp(userId, newUserApp);

			customResponse.setResponseCode(HttpStatus.CREATED.value());
			return userAppId;
		}
		catch (WallaException wallaEx) {
			customResponse.setResponseCode(wallaEx.getCustomStatus());
			return 0;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return 0;
		}
		finally { UserTools.LogMethod("CreateUserApp", meLogger, startMS, String.valueOf(userId)); }
	}

	public void UpdateUserApp(long userId, int appId, int platformId, UserApp updatedUserApp, CustomResponse customResponse)
	{
		long startMS = System.currentTimeMillis();
		try 
		{
			UserApp userApp = accountDataHelper.GetUserApp(userId, updatedUserApp.getId());
			if (userApp == null)
			{
				meLogger.warn("UpdateUserApp didn't return a valid UserApp object");
				customResponse.setResponseCode(HttpStatus.NOT_FOUND.value());
				return;
			}
			
			if (platformId != userApp.getPlatformId())
			{
				meLogger.warn("Account update failed, platforms do not match.  PlatformId:" + platformId);
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return;
			}
			
			if (appId != updatedUserApp.getAppId())
			{
				meLogger.warn("Account update failed, apps do not match.  AppId:" + appId);
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return;
			}
			
			//Ensure correct platformId and appId is used
			updatedUserApp.setPlatformId(platformId);
			updatedUserApp.setAppId(appId);
			
			if (!userApp.getMachineName().equalsIgnoreCase(updatedUserApp.getMachineName()))
			{
				//Create or find new userapp tag (system owned).
				updatedUserApp.setTagId(tagService.CreateOrFindUserAppTag(userId, platformId, userApp.getMachineName()));
				
				//Create new auto upload category. 
				updatedUserApp.setUserAppCategoryId(categoryService.CreateOrFindUserAppCategory(userId, platformId, userApp.getMachineName()));
			}
			
			accountDataHelper.UpdateUserApp(userId, updatedUserApp);

			customResponse.setResponseCode(HttpStatus.OK.value());
		}
		catch (WallaException wallaEx) {
			customResponse.setResponseCode(wallaEx.getCustomStatus());
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally { UserTools.LogMethod("UpdateUserApp", meLogger, startMS, String.valueOf(userId) + " " + String.valueOf(updatedUserApp.getId())); }
	}
	
	public UserApp GetUserApp(long userId, int appId, int platformId, long userAppId, CustomResponse customResponse)
	{
		long startMS = System.currentTimeMillis();
		try {
			UserApp userApp = accountDataHelper.GetUserApp(userId, userAppId);
			if (userApp == null)
			{
				String error = "GetUserApp didn't return a valid UserApp object using id: " + userAppId;
				meLogger.warn(error);
				customResponse.setResponseCode(HttpStatus.NOT_FOUND.value());
				return null;
			}
			
			//Check the userapp is still relevent for the platform.
			if (userApp.getPlatformId() != platformId)
			{
				//Register new userapp, the platform has changed.  This could either be an upgrade, name change or copying config.
				//Use existing app as a starting point.
				meLogger.info("Platforms don't match, create a new platform. UserAppId:" + userAppId + " PlatformId:" + platformId);
				
				UserApp newUserApp = new UserApp();
				newUserApp.setAutoUpload(userApp.isAutoUpload());
				newUserApp.setAutoUploadFolder(userApp.getAutoUploadFolder());
				newUserApp.setThumbCacheSizeMB(userApp.getThumbCacheSizeMB());
				newUserApp.setMainCopyFolder(userApp.getMainCopyFolder());
				newUserApp.setMainCopyCacheSizeMB(userApp.getMainCopyCacheSizeMB());
				
				long newUserAppId = CreateUserApp(userId, appId, platformId, newUserApp, customResponse);
				
				userApp = accountDataHelper.GetUserApp(userId, newUserAppId);
				if (userApp == null)
				{
					meLogger.warn("GetUserApp didn't return the new UserApp object: " + newUserAppId);
					customResponse.setResponseCode(HttpStatus.NOT_FOUND.value());
					return null;
				}
			}
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			return userApp;
		}
		catch (WallaException wallaEx) {
			customResponse.setResponseCode(wallaEx.getCustomStatus());
			return null;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		finally { UserTools.LogMethod("GetUserApp", meLogger, startMS, String.valueOf(userId) + " " + String.valueOf(userAppId)); }
	}
	
	public boolean CheckProfileNameIsUnique(String profileName, CustomResponse customResponse)
	{
		long startMS = System.currentTimeMillis();
		try 
		{
			if (profileName.length() >30 || profileName.contains(" "))
			{
				meLogger.error("Profile name is not set correctly.  " + profileName);
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return false;
			}
			
			boolean isUnique = accountDataHelper.ProfileNameIsUnique(profileName);
			customResponse.setResponseCode(HttpStatus.OK.value());
			return isUnique;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return false;
		}
		finally { UserTools.LogMethod("CheckProfileNameIsUnique", meLogger, startMS, profileName); }
	}

	public int GetPlatformId(ClientApp clientApp, CustomResponse customResponse)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			String OS = (clientApp.getOS() == null) ? "" : clientApp.getOS();
			String machine = (clientApp.getMachineType() == null) ? "" : clientApp.getMachineType();
			int major = clientApp.getMajor();
			int minor = clientApp.getMinor();
			
			if (OS == null || machine == null || OS.length() < 1 || machine.length() < 1)
			{
				meLogger.warn("Valid OS and machines not supplied.  OS:" + OS + " machine:" + machine);
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return 0;
			}
			
			Platform platform = cachedData.GetPlatform(0, OS, machine, major, minor);
			if (platform == null)
			{
				meLogger.info("Platform not found. OS:" + OS + " machine:" + machine);
				customResponse.setResponseCode(HttpStatus.NOT_ACCEPTABLE.value());
				return 0;
			}
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			return platform.getPlatformId();
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return 0;
		}
		finally { UserTools.LogMethod("GetPlatformId", meLogger, startMS, ""); }
	}
	
	public int VerifyApp(ClientApp clientApp, CustomResponse customResponse)
	{
		//Check for key existing in Walla	
		//If not, then send back not found message
		//If exists - but retired, then send back not acceptable message
		//Else send back OK.
		long startMS = System.currentTimeMillis();
		try
		{
			String key = clientApp.getWSKey();
			if (key == null || key.length() < 10)
			{
				meLogger.warn("Valid key not supplied.  Key:" + clientApp.getWSKey());
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return 0;
			}
			
			App app = cachedData.GetApp(0, clientApp.getWSKey());
			if (app == null)
			{
				meLogger.info("App not found.  Key:" + clientApp.getWSKey());
				customResponse.setResponseCode(HttpStatus.NOT_ACCEPTABLE.value());
				return 0;
			}
			
			if (app.getStatus() != 1)
			{
				meLogger.info("App not enabled.  Key:" + clientApp.getWSKey());
				customResponse.setResponseCode(HttpStatus.NOT_ACCEPTABLE.value());
				return 0;
			}
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			return app.getAppId();
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return 0;
		}
		finally { UserTools.LogMethod("VerifyApp", meLogger, startMS, ""); }
	}

	public String GetNewUserToken(HttpServletRequest request, CustomSessionState customSession, CustomResponse customResponse)
	{
		//Only reads user data from DB, any state is held in the session object.
		long startMS = System.currentTimeMillis();

		try
		{
			//Add safe guards.  eg - number of requests per minute
			
			//Passed initial checks, so issue a key and update the custom session.
			String newKey = UserTools.GetComplexString();
			
			//TEMP overide for testing!!!!
			newKey = "12345678901234567890123456789012";
			
			synchronized(customSession) {
				customSession.setNonceKey(newKey);
				customSession.setHuman(false);
				customSession.setRemoteAddress(request.getRemoteAddr());
			}
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			
			return newKey;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return "";
		}
		finally { UserTools.LogMethod("GetNewUserToken", meLogger, startMS, ""); }
	}
	
	public String GetLogonToken(Logon logon, HttpServletRequest request, CustomSessionState customSession, CustomResponse customResponse)
	{
		//Only reads user data from DB, any state is held in the session object.
		long startMS = System.currentTimeMillis();
		String profileName = "";
		String email = "";
		try
		{
			profileName = (logon.getProfileName() == null) ? "": logon.getProfileName();
			email = (logon.getEmail() == null) ? "": logon.getEmail();
			if (profileName.length() < 5 && email.length() < 5)
			{
				meLogger.warn("Profile name/email not supplied correctly.");
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return "";
			}
	    
			//Head off unauthorised attempts if they come from the same session.
			Date failedLogonLast = customSession.getFailedLogonLast();
			if (failedLogonLast != null)
			{
			    Calendar calendar = Calendar.getInstance();
			    calendar.setTime(failedLogonLast);
			    
			    //If less than five failed logons, ensure a retry is not done within 2 seconds.  Otherwise its a 30 second delay.
			    if (customSession.getFailedLogonCount() <= 5)
				    calendar.add(Calendar.SECOND, 2);
			    else
			    	calendar.add(Calendar.SECOND, 30);
				
			    if (calendar.getTime().after(new Date()))
			    {
			    	meLogger.warn("Subsequent logon token request too soon after previous failure. (session)");
			    	customResponse.setResponseCode(HttpStatus.FORBIDDEN.value());
			    	return "";
			    }
			}
			
			LogonState userStateDb = accountDataHelper.GetLogonState(profileName, email);
			if (userStateDb == null)
			{
				meLogger.warn("Logon state could not be retrieved from the database.  ProfileName: " + profileName + " Email:" + email);
		    	customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
		    	return "";
			}
			
			//Check DB state for last login information
			failedLogonLast = userStateDb.getFailedLogonLast();
			if (failedLogonLast != null)
			{
			    Calendar calendar = Calendar.getInstance();
			    calendar.setTime(failedLogonLast);
			    
			    //If less than five failed logons, ensure a retry is not done within 2 seconds.  Otherwise its a 30 second delay.
			    if (userStateDb.getFailedLogonCount() <= 5)
				    calendar.add(Calendar.SECOND, 2);
			    else
			    	calendar.add(Calendar.SECOND, 30);
				
			    if (calendar.getTime().after(new Date()))
			    {
			    	meLogger.warn("Subsequent logon token request too soon after previous failure. (db)");
			    	customResponse.setResponseCode(HttpStatus.FORBIDDEN.value());
			    	return "";
			    }
			}
			
			//Passed initial checks, so issue a key and update the custom session.
			String newKey = UserTools.GetComplexString();
			
			//TEMP overide for testing!!!!
			newKey = "12345678901234567890123456789012";
			
			synchronized(customSession) {
				customSession.setNonceKey(newKey);
				customSession.setProfileName(userStateDb.getProfileName());
				customSession.setUserId(userStateDb.getUserId());
				customSession.setRemoteAddress(request.getRemoteAddr());
			}
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			
			return newKey;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return "";
		}
		finally { UserTools.LogMethod("GetLogonToken", meLogger, startMS, "ProfileName: " + profileName + " Email:" + email); }
	}
	
	
	public boolean LogonCheck(Logon logon, HttpServletRequest request, CustomSessionState customSession)
	{
		long startMS = System.currentTimeMillis();
		String profileName = "";
		String password = "";
		String requestKey = "";
		String email = "";
		try
		{
			profileName = logon.getProfileName();
			password = logon.getPassword();
			email = logon.getEmail();
			
			synchronized(customSession) {
				requestKey = customSession.getNonceKey();
				customSession.setNonceKey("");
			}
			
			Date failedLogonLast = customSession.getFailedLogonLast();
			if (failedLogonLast != null)
			{
			    Calendar calendar = Calendar.getInstance();
			    calendar.setTime(failedLogonLast);
			    
			    //If less than five failed logons, ensure a retry is not done within 2 seconds.  Otherwise its a 30 second delay.
			    if (customSession.getFailedLogonCount() <= 5)
				    calendar.add(Calendar.SECOND, 2);
			    else
			    	calendar.add(Calendar.SECOND, 30);
				
			    if (calendar.getTime().after(new Date()))
			    {
			    	meLogger.warn("Subsequent logon request too soon after previous failure. (session)");
			    	return false;
			    }
			}

			if (customSession.getRemoteAddress().compareTo(request.getRemoteAddr()) != 0)
			{
				meLogger.warn("IP address of the session has changed since the logon key was issued..");
				return false;
			}
			
			if ((profileName == null && email == null) || password == null || requestKey == null)
			{
				meLogger.warn("Not all the logon fields were supplied, logon failed.");
				return false;
			}
		    
			if ((profileName.length() < 5 && email.length() < 5) || password.length() < 8 || requestKey.length() != 32)
			{
				meLogger.warn("The logon fields supplied did meet minimum size, logon failed.  profileName:" + profileName + " password length:" + password.length() + " key:" + requestKey);
				return false;
			}
			
		    //Check one-off logon key, matches between server and request.
			if (requestKey.compareTo(logon.getKey()) != 0)
			{
				meLogger.warn("One off logon key, does not match request.  ServerKey:" + requestKey + " RequestKey:" + logon.getKey());
				return false;
			}
			
			LogonState userStateDb = accountDataHelper.GetLogonState(profileName, email);
			if (userStateDb == null)
			{
				meLogger.warn("Logon state could not be retrieved from the database.  ProfileName: " + profileName);
		    	return false;
			}
			
			if (userStateDb.getProfileName().compareTo(customSession.getProfileName()) != 0)
			{
				meLogger.warn("Custom session user name does not match the request username.  Request name:" + profileName + " Session Name:" + customSession.getProfileName());
				return false;
			}

			//Get a hash of the password attempt.
			String passwordAttemptHash = SecurityTools.GetHashedPassword(logon.getPassword(), userStateDb.getSalt(), 160, 1000);

			if (SecurityTools.SlowEquals(passwordAttemptHash.getBytes(), userStateDb.getPasswordHash().getBytes()))
			{
				synchronized(customSession) 
				{
					customSession.getCustomSessionIds().add(UserTools.GetComplexString());
					customSession.setFailedLogonCount(0);
					customSession.setFailedLogonLast(null);
					customSession.setAuthenticated(true);
					customSession.setAdmin(true);
				}
				accountDataHelper.UpdateLogonState(userStateDb.getUserId(), 0, null);
				meLogger.debug("Logon successfull for User: " + logon.getProfileName());
				
				return true;
			}
			else
			{
				int failCount = Math.max(userStateDb.getFailedLogonCount(), customSession.getFailedLogonCount()) + 1;
				synchronized(customSession) 
				{
					customSession.setFailedLogonCount(failCount);
					customSession.setFailedLogonLast(new Date());
					customSession.setAuthenticated(false);
				}
				
				accountDataHelper.UpdateLogonState(userStateDb.getUserId(), failCount, new Date());
				meLogger.warn("Password didn't match, logon failed.");

				//Check for number of recent failures.  More than 5? then 10 seconds delay.
				if (customSession.getFailedLogonCount() > 5)
					Thread.sleep(10000);
				else
					Thread.sleep(1000);
				
				return false;
			}
			
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return false;
		}
		finally { UserTools.LogMethod("LogonCheck", meLogger, startMS, profileName); }
	}

	//TODO
	public boolean ChangePassword(Logon logon)
	{
		try
		{
			//Check logon key, profle name, password are all OK.
			
			//Validate old password
			
			//Create new salt and password hash
			String salt = SecurityTools.GenerateSalt();
			String passwordHash = SecurityTools.GetHashedPassword(logon.getPassword(), salt, 160, 1000);
			
			
			//Create new password hash
			
			//Save to DB
			
			return true;
		}
		catch (Exception ex)
		{
			return false;
		}
	}
	
	//*************************************************************************************************************
	//*************************************  Messaging initiated methods ******************************************
	//*************************************************************************************************************
	
	public void SendEmailConfirm(long userId) 
	{
		try
		{
			String validationString = UserTools.GetComplexString();
			accountDataHelper.UpdateEmailStatus(userId, 1, validationString.substring(0,32));
			
			//TODO actually send email.
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process SendEmailConfirmation");
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces SendEmailConfirmation", ex);
		}
	}
	
	public void setAccountDataHelper(AccountDataHelperImpl accountDataHelper)
	{
		this.accountDataHelper = accountDataHelper;
	}
	
	public void setTagService(TagService tagService)
	{
		this.tagService = tagService;
	}
	
	public void setCachedData(CachedData cachedData)
	{
		this.cachedData = cachedData;
	}
	
	public void setCategoryService(CategoryService categoryService)
	{
		this.categoryService = categoryService;
	}
	
	public void setGalleryService(GalleryService galleryService)
	{
		this.galleryService = galleryService;
	}
	
	public void setUtilityDataHelper(UtilityDataHelperImpl utilityDataHelper)
	{
		this.utilityDataHelper = utilityDataHelper;
	}
}
