package walla.business;

import java.text.SimpleDateFormat;
import java.util.*;

import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.db.*;
import walla.utils.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.xml.datatype.*;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.security.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Qualifier;
import org.apache.commons.lang3.*;

@Service("AccountService")
public class AccountService {

	@Resource(name="accountDataHelper")
	private AccountDataHelperImpl accountDataHelper;
	
	@Resource(name="utilityDataHelper")
	private UtilityDataHelperImpl utilityDataHelper;
	
	@Resource(name="tagServicePooled")
	private TagService tagService;
	
	@Resource(name="categoryServicePooled")
	private CategoryService categoryService;
	
	@Resource(name="galleryServicePooled")
	private GalleryService galleryService;
	
	@Resource(name="cachedData")
	private CachedData cachedData;
	
	@Resource(name="utilityServicePooled")
	private UtilityService utilityService;
	
	private static final Logger meLogger = Logger.getLogger(AccountService.class);

	public AccountService()
	{
		meLogger.debug("AccountService object instantiated.");
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
	 */
	
	//Create Account (Brief details) + Email.
	//Collect bank details and personal contact information (if paid account)
	//Send out confirmation email.
	//Receive confirmation email. (Link clicked)
	//Account open
	//Account close requested
	//Account close completed

	public void CreateAccount(Account account, CustomResponse customResponse, CustomSessionState customSession, String requestId)
	{
		String principleEmail = "";
		String secondaryEmail = "";
		String sql = "";
		
		long startMS = System.currentTimeMillis();
		try 
		{
			
			if (account.getEmails() != null && account.getEmails().getEmailRef() != null)
			{
				if (account.getEmails().getEmailRef().size() > 0)
				{
					//Construct update SQL statements
					for (Iterator<Account.Emails.EmailRef> emailIterater = account.getEmails().getEmailRef().iterator(); emailIterater.hasNext();)
					{
						Account.Emails.EmailRef emailRef = (Account.Emails.EmailRef)emailIterater.next();
						if (emailRef.getPrinciple())
							principleEmail = (emailRef.getAddress() == null) ? "" : emailRef.getAddress();
						
						if (emailRef.getSecondary())
							secondaryEmail = (emailRef.getAddress() == null) ? "" : emailRef.getAddress();
					}
				}
			}
			
			//Create new account
			if (!UserTools.ValidEmailAddress(principleEmail))
			{
				meLogger.warn("Account create failed, email doesn't fit a standard form.  Email:" + principleEmail);
				customResponse.setMessage("Email doesn't fit a standard form");
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return;
			}
			
			if (secondaryEmail.length() > 0 && !UserTools.ValidEmailAddress(secondaryEmail))
			{
				meLogger.warn("Account create failed, email doesn't fit a standard form.  Email:" + secondaryEmail);
				customResponse.setMessage("Email doesn't fit a standard form");
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return;
			}
			
			if (!UserTools.CheckPasswordStrength(account.getPassword()))
			{
				meLogger.warn("Account create failed, password does not meet minimum complexity rules.");
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
			
			sql = "SELECT COUNT(1) FROM [User] WHERE UPPER([ProfileName]) = UPPER(?) AND [status] IN (1,2,3,4)";
			int nameCount = (int)utilityDataHelper.GetValueParamString(sql, account.getProfileName(), requestId);
			
			if (nameCount > 0)
			{
				String error = "Profile name is already in use.  " + account.getProfileName();
				meLogger.error(error);
				customResponse.setMessage("Profile name is already in use");
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return;
			}

			//TODO check email is unique
			
			//TODO auto find timezone
			account.setTimezone("GMT");
			
			String salt = SecurityTools.GenerateSalt();
			String passwordHash = SecurityTools.GetHashedPassword(account.getPassword(), salt, 160, 1000);
			
			long newUserId = accountDataHelper.CreateAccount(account, passwordHash, salt, requestId);
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
			accountDataHelper.UpdateLogonState(newUserId, 0, null, requestId);
			
			//TODO decouple.
			accountDataHelper.AddEmail(newUserId, principleEmail, true, false, requestId);
			
			if (secondaryEmail.length() > 0)
				accountDataHelper.AddEmail(newUserId, secondaryEmail, false, true, requestId);
			
			meLogger.info("New user has been created.  Email: " + principleEmail + " UserId:" + newUserId);
			utilityService.AddAction(ActionType.Account, newUserId, "AcctNew", "");
			
			customResponse.setResponseCode(HttpStatus.CREATED.value());
		}
		catch (WallaException wallaEx) {
			customResponse.setResponseCode(wallaEx.getCustomStatus());
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally { utilityService.LogMethod("AccountService","CreateAccount", startMS, requestId, principleEmail); }
	}
	
	public void UpdateAccount(Account account, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try 
		{
			/*Get timezone from country*/
			
			accountDataHelper.UpdateAccount(account, requestId);
			customResponse.setResponseCode(HttpStatus.OK.value());
			
			utilityService.AddAction(ActionType.Account,account.getId(), "AcctEdit", "");
		}
		catch (WallaException wallaEx) {
			customResponse.setResponseCode(wallaEx.getCustomStatus());
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally { utilityService.LogMethod("AccountService","UpdateAccount", startMS, requestId, "" + account.getId()); }
	}

	public void CloseAccount(Account account, CustomResponse customResponse, CustomSessionState customSession, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try 
		{
			LogonState userStateDb = accountDataHelper.GetLogonState(account.getProfileName(), requestId);
			if (userStateDb == null)
			{
				String message = "Logon state could not be retrieved from the database.  ProfileName: " + account.getProfileName();
				meLogger.warn(message);
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
		    	return;
			}
			
			//Get a hash of the password attempt.
			String passwordAttemptHash = SecurityTools.GetHashedPassword(account.getPassword(), userStateDb.getSalt(), 160, 1000);

			if (!SecurityTools.SlowEquals(passwordAttemptHash.getBytes(), userStateDb.getPasswordHash().getBytes()))
			{
				String message = "Password didn't match, account close failed.";
				meLogger.warn(message);
				customResponse.setMessage(message);
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				Thread.sleep(2000);

				return;
			}

			//TODO Send an email for manual image deletions.
			
			accountDataHelper.UpdateAccountStatus(account.getId(), AccountStatus.Closing, requestId);
			customResponse.setResponseCode(HttpStatus.OK.value());
			
			utilityService.AddAction(ActionType.Account, account.getId(), "AcctClose", "");
		}
		catch (WallaException wallaEx) {
			customResponse.setResponseCode(wallaEx.getCustomStatus());
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally { utilityService.LogMethod("AccountService","CloseAccount", startMS, requestId, account.getProfileName()); }
	}
	
	public Account GetAccountMeta(long userId, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try 
		{
			Account account = accountDataHelper.GetAccountMeta(userId, requestId);
			if (account == null)
			{
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return null;
			}

			//TODO add logic for account message.  Change to use enum!
			switch (account.getStatus())
			{
				case 1:
					account.setAccountMessage("Account is being setup, you must validate your email address and bank details to complete the account opening.");
				break;
				case 3:
					String frozenReason = accountDataHelper.ShouldAccountBeLive(userId, requestId);
					
					if (frozenReason.compareTo("OK") == 0)
						accountDataHelper.UpdateAccountStatus(userId, AccountStatus.Live, requestId);
					else
						account.setAccountMessage("Account is currently frozen because:" + frozenReason + "  Which means that no new images can be uploaded.");
						
				break;
				case 4:
					Date now = new Date();
					Date then = account.getCloseDate().getTime();
					long diffInMillies = now.getTime() - then.getTime();
					long daysDiff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
					daysDiff = 30 - daysDiff;
					
					String closeDate = new SimpleDateFormat("dd MMM yyyy").format(then);
					account.setAccountMessage("Account is being closed down.  This was requested on: " + closeDate + ".  During this 30 day close down period, you must download your collection.  You have " + daysDiff + " days left.");
					break;
			}
			
			//TODO expand out password change date message.
			Date now = new Date();
			Date then = account.getPasswordChangeDate().getTime();
			
			long diffInMillies = now.getTime() - then.getTime();
			long daysDiff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
			
		    if (daysDiff > 60)
		    	account.setSecurityMessage("Your password should be changed periodically.  Its been " + daysDiff + " days since you have change it.");
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			return account;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		finally { utilityService.LogMethod("AccountService","GetAccountMeta", startMS, requestId, ""); }
	}
	
	public AccountStorage GetAccountStorage(long userId, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try 
		{
			AccountStorage accountStorage = accountDataHelper.GetAccountStorage(userId, requestId);
			if (accountStorage == null)
			{
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return null;
			}

			//TODO
			accountStorage.setStorageMessage("test message");

			customResponse.setResponseCode(HttpStatus.OK.value());
			return accountStorage;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		finally { utilityService.LogMethod("AccountService","GetAccountStorage", startMS, requestId, ""); }
	}
	
	public AccountActionSummary GetAccountActions(long userId, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try 
		{
			AccountActionSummary summary = accountDataHelper.GetAccountActions(userId, requestId);
			if (summary == null)
			{
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return null;
			}
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			return summary;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		finally { utilityService.LogMethod("AccountService","GetAccountActions", startMS, requestId, ""); }
	}
	
	public void EmailConfirm(String profileName, String requestValidationString, CustomResponse customResponse, String requestId)
	{
		//Return email address if correctly validated.
		long startMS = System.currentTimeMillis();
		String message;
		try
		{
			if (profileName.length() > 30 || profileName.contains(" ") || profileName.contains("'"))
			{
				message = "Profile name is not set correctly.  " + profileName;
				meLogger.error(message);
				customResponse.setMessage(message);
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return;
			}
			
			long userId = (long)utilityDataHelper.GetValueParamString("SELECT [UserId] FROM [User] WHERE ProfileName = ?", profileName, requestId);
			
			if (!accountDataHelper.ValidateEmailConfirm(userId, requestValidationString, customResponse, requestId))
			{
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				Thread.sleep(2000);
				return;
			}
			
			//Email is OK, so verify and check if the account should now be set as live.
			String email = customResponse.getMessage();

			accountDataHelper.UpdateEmail(userId, email, EmailAction.Verified, "", requestId);
			
			//TODO decouple.
			CheckUpdateAccountStatus(userId, requestId);
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			customResponse.setMessage("Email: " + email + " was validated.");
			
			utilityService.AddAction(ActionType.Account, userId, "EmailValid", email);
		}
		catch (WallaException wallaEx) {
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			customResponse.setMessage("Email address could not be validated, due to an unexpected system error.");
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			customResponse.setMessage("Email address could not be validated, due to an unexpected system error.");
		}
		finally { utilityService.LogMethod("AccountService","EmailConfirm", startMS, requestId, profileName); }
	}

	public void VerifyEmailRequest(long userId, String email, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			String validationString = UserTools.GetComplexString();
			accountDataHelper.UpdateEmail(userId, email, EmailAction.SetupValidation, validationString.substring(0,32), requestId);
			
			//TODO decouple
			SendVerifyEmail(userId, email, validationString, requestId);
			
			customResponse.setResponseCode(HttpStatus.OK.value());
		}
		catch (WallaException wallaEx) {
			customResponse.setResponseCode(wallaEx.getCustomStatus());
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally { utilityService.LogMethod("AccountService","UpdateEmailAction", startMS, requestId, email); }
	}
	
	public void UpdateEmailAction(long userId, String email, EmailAction action, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try 
		{
			accountDataHelper.UpdateEmail(userId, email, action, "", requestId);
			
			//TODO decouple.
			if (action == EmailAction.Verified)
				CheckUpdateAccountStatus(userId, requestId);
			
			
			customResponse.setResponseCode(HttpStatus.OK.value());
		}
		catch (WallaException wallaEx) {
			customResponse.setResponseCode(wallaEx.getCustomStatus());
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally { utilityService.LogMethod("AccountService","UpdateEmailAction", startMS, requestId, email); }
	}

	public void AddEmail(long userId, String email, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try 
		{
			if (!accountDataHelper.EmailIsUnique(userId, email, requestId))
			{
				customResponse.setResponseCode(HttpStatus.CONFLICT.value());
				customResponse.setMessage("Email address is not unique.");
				return;
			}
			
			accountDataHelper.AddEmail(userId, email, false, false, requestId);
			VerifyEmailRequest(userId, email, customResponse, requestId);
		}
		catch (WallaException wallaEx) {
			customResponse.setResponseCode(wallaEx.getCustomStatus());
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally { utilityService.LogMethod("AccountService","AddEmail", startMS, requestId, email); }
	}

	public long CreateUserApp(long userId, int appId, int platformId, UserApp proposedUserApp, CustomResponse customResponse, String requestId)
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
			
			long userAppId = accountDataHelper.FindExistingUserApp(userId, appId, platformId, proposedUserApp.getMachineName(), requestId);
			if (userAppId > 0)
			{
				customResponse.setResponseCode(HttpStatus.OK.value());
				return userAppId;
			}
			
			UserApp newUserApp = new UserApp();
			userAppId = utilityDataHelper.GetNewId("UserAppId", requestId);

			App app = cachedData.GetApp(appId, "", requestId);
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
			newUserApp.setTagId(tagService.CreateOrFindUserAppTag(userId, platformId, proposedUserApp.getMachineName(), requestId));
			
			//Create new auto upload category. 
			newUserApp.setUserAppCategoryId(categoryService.CreateOrFindUserAppCategory(userId, platformId, newUserApp.getMachineName(), requestId));
			
			//Default user category
			newUserApp.setUserDefaultCategoryId(categoryService.FindDefaultUserCategory(userId, requestId));
			
			//Get default gallery.
			newUserApp.setGalleryId(galleryService.GetDefaultGallery(userId, appId, requestId));
			
			if (proposedUserApp.getAutoUpload())
				newUserApp.setAutoUpload(true);
			
			if (proposedUserApp.getAutoUploadFolder() != null && !proposedUserApp.getAutoUploadFolder().isEmpty())
				newUserApp.setAutoUploadFolder(proposedUserApp.getAutoUploadFolder());
			
			if (proposedUserApp.getMainCopyFolder() != null && !proposedUserApp.getMainCopyFolder().isEmpty())
				newUserApp.setMainCopyFolder(proposedUserApp.getMainCopyFolder());
			
			accountDataHelper.CreateUserApp(userId, newUserApp, requestId);

			utilityService.AddAction(ActionType.UserApp, userAppId, "UserAppNew", "");
			
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
		finally { utilityService.LogMethod("AccountService","CreateUserApp", startMS, requestId, ""); }
	}

	public void UpdateUserApp(long userId, int appId, int platformId, UserApp updatedUserApp, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try 
		{
			UserApp userApp = accountDataHelper.GetUserApp(userId, updatedUserApp.getId(), requestId);
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
				updatedUserApp.setTagId(tagService.CreateOrFindUserAppTag(userId, platformId, userApp.getMachineName(), requestId));
				
				//Create new auto upload category. 
				updatedUserApp.setUserAppCategoryId(categoryService.CreateOrFindUserAppCategory(userId, platformId, userApp.getMachineName(), requestId));
			}
			
			accountDataHelper.UpdateUserApp(userId, updatedUserApp, requestId);

			utilityService.AddAction(ActionType.UserApp, updatedUserApp.getId(), "UserAppUpd", "");
			customResponse.setResponseCode(HttpStatus.OK.value());
		}
		catch (WallaException wallaEx) {
			customResponse.setResponseCode(wallaEx.getCustomStatus());
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally { utilityService.LogMethod("AccountService","UpdateUserApp", startMS, requestId, String.valueOf(updatedUserApp.getId())); }
	}
	
	public UserApp GetUserApp(long userId, int appId, int platformId, long userAppId, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try {
			UserApp userApp = accountDataHelper.GetUserApp(userId, userAppId, requestId);
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
				newUserApp.setAutoUpload(userApp.getAutoUpload());
				newUserApp.setAutoUploadFolder(userApp.getAutoUploadFolder());
				newUserApp.setThumbCacheSizeMB(userApp.getThumbCacheSizeMB());
				newUserApp.setMainCopyFolder(userApp.getMainCopyFolder());
				newUserApp.setMainCopyCacheSizeMB(userApp.getMainCopyCacheSizeMB());
				
				long newUserAppId = CreateUserApp(userId, appId, platformId, newUserApp, customResponse, requestId);
				
				userApp = accountDataHelper.GetUserApp(userId, newUserAppId, requestId);
				if (userApp == null)
				{
					meLogger.warn("GetUserApp didn't return the new UserApp object: " + newUserAppId);
					customResponse.setResponseCode(HttpStatus.NOT_FOUND.value());
					return null;
				}
			}
			else
			{
				utilityDataHelper.ExecuteSql("UPDATE [UserApp] SET LastUsed = dbo.GetDateNoMS() WHERE UserAppId = " + userApp.getId(), requestId);
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
		finally { utilityService.LogMethod("AccountService","GetUserApp", startMS, requestId, String.valueOf(userAppId)); }
	}
	
	public void UserAppBlockUnblock(long userId, long userAppId, boolean block, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try 
		{
			UserApp userApp = accountDataHelper.GetUserApp(userId, userAppId, requestId);
			if (userApp == null)
			{
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				customResponse.setMessage("Application settings could not be retrieved");
				return;
			}
			
			if (userApp.getBlocked() == block)
			{
				customResponse.setResponseCode(HttpStatus.CONFLICT.value());
				customResponse.setMessage("Application update requested is not valid");
				return;
			}
			
			accountDataHelper.UserAppBlockUnblock(userId, userAppId, block, requestId);
			customResponse.setResponseCode(HttpStatus.OK.value());
		}
		catch (WallaException wallaEx) {
			customResponse.setResponseCode(wallaEx.getCustomStatus());
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally { utilityService.LogMethod("AccountService","UserAppBlockUnblock", startMS, requestId, String.valueOf(userAppId)); }
	}	
	
	public boolean CheckProfileNameIsUnique(String profileName, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		boolean isUnique = false;
		try 
		{
			if (profileName.length() >30 || profileName.contains(" ") || profileName.contains("'"))
			{
				meLogger.error("Profile name is not set correctly.  " + profileName);
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return false;
			}
			
			String sql = "SELECT COUNT(1) FROM [User] WHERE UPPER([ProfileName]) = UPPER(?) AND [status] IN (1,2,3,4)";
			int nameCount = (int)utilityDataHelper.GetValueParamString(sql, profileName, requestId);
			if (nameCount == 0)
				isUnique = true;
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			return isUnique;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return false;
		}
		finally { utilityService.LogMethod("AccountService","CheckProfileNameIsUnique", startMS, requestId, profileName); }
	}

	public int GetPlatformId(ClientApp clientApp, CustomResponse customResponse, String requestId)
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
			
			Platform platform = cachedData.GetPlatform(0, OS, machine, major, minor, requestId);
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
		finally { utilityService.LogMethod("AccountService","GetPlatformId", startMS, requestId, ""); }
	}
	
	public int VerifyApp(ClientApp clientApp, CustomResponse customResponse, String requestId)
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
			
			App app = cachedData.GetApp(0, clientApp.getWSKey(), requestId);
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
		finally { utilityService.LogMethod("AccountService","VerifyApp", startMS, requestId, ""); }
	}

	public String GetNewUserToken(HttpServletRequest request, CustomSessionState customSession, CustomResponse customResponse, String requestId)
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
			
			utilityService.AddActionSecurityAccount("UserToken", newKey, null, request, customSession);
			
			return newKey;
		}
		catch (Exception ex) {
			utilityService.AddActionSecurityAccount("TokenErr", "", null, request, customSession);
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return "";
		}
		finally { utilityService.LogMethod("AccountService","GetNewUserToken", startMS, requestId, ""); }
	}
	
	public String GetLogonToken(HttpServletRequest request, CustomSessionState customSession, CustomResponse customResponse, String requestId)
	{
		//Only reads user data from DB, any state is held in the session object.
		long startMS = System.currentTimeMillis();
		String message = "";
		//String profileName = "";
		//String email = "";
		try
		{
			/*
			profileName = (logon.getProfileName() == null) ? "": logon.getProfileName();
			email = (logon.getEmail() == null) ? "": logon.getEmail();
			if (profileName.length() < 5 && email.length() < 5)
			{
				meLogger.warn("Profile name/email not supplied correctly.");
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return "";
			}
	    */
			
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
			    	message = "Subsequent logon token request too soon after previous failure. (session)";
			    	meLogger.warn(message);
			    	utilityService.AddActionSecurityAccount("LoginSoon", message, null, request, customSession);
			    	customResponse.setResponseCode(HttpStatus.FORBIDDEN.value());
			    	return "";
			    }
			}
			
			/*
			LogonState userStateDb = accountDataHelper.GetLogonState(profileName);
			if (userStateDb == null)
			{
				meLogger.warn("Logon state could not be retrieved from the database.  ProfileName: " + profileName + " Email:" + email);
		    	customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
		    	return "";
			}
			*/
			
			//Check DB state for last login information
			/*
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
			*/
			
			//Passed initial checks, so issue a key and update the custom session.
			String newKey = UserTools.GetComplexString();
			
			//TEMP overide for testing!!!!
			newKey = "12345678901234567890123456789012";
			
			synchronized(customSession) {
				customSession.setNonceKey(newKey);
				customSession.setAuthenticated(false);
				customSession.setGalleryViewer(false);
				//customSession.setProfileName(userStateDb.getProfileName());
				//customSession.setUserId(userStateDb.getUserId());
				customSession.setRemoteAddress(request.getRemoteAddr());
			}
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			utilityService.AddActionSecurityAccount("LoginToken", newKey, null, request, customSession);
			
			return newKey;
		}
		catch (Exception ex) {
			utilityService.AddActionSecurityAccount("TokenErr", "", null, request, customSession);
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return "";
		}
		finally { utilityService.LogMethod("AccountService","GetLogonToken", startMS, requestId, ""); }
	}

	public void LogonCheck(Logon logon, HttpServletRequest request, CustomSessionState customSession, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		String profileName = "";
		String password = "";
		String requestKey = "";
		String email = "";
		String message = "";
		try
		{
			//TODO add customResponse. coes and messages.
			
			profileName = (logon.getProfileName() == null) ? "": logon.getProfileName();
			email = (logon.getEmail() == null) ? "": logon.getEmail();
			password = (logon.getPassword() == null) ? "": logon.getPassword();
			
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
			    	message = "Subsequent logon request too soon after previous failure.  Please try again later.";
			    	meLogger.warn(message);
			    	utilityService.AddActionSecurityAccount("LoginSoon", message, logon, request, customSession);
			    	customResponse.setResponseCode(HttpStatus.FORBIDDEN.value());
			    	return;
			    }
			}

			if (customSession.getRemoteAddress().compareTo(request.getRemoteAddr()) != 0)
			{
		    	message = "IP address of the session has changed since the logon key was issued.";
		    	meLogger.warn(message);
				utilityService.AddActionSecurityAccount("LoginInval", message, logon, request, customSession);
		    	customResponse.setResponseCode(HttpStatus.FORBIDDEN.value());
		    	return;
			}
			
			if ((profileName == null && email == null) || password == null || requestKey == null)
			{
		    	message = "Not all the logon fields were supplied, logon failed.";
		    	meLogger.warn(message);
				utilityService.AddActionSecurityAccount("LoginInval", message, logon, request, customSession);
		    	customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
		    	return;
			}
		    
			if ((profileName.length() < 5 && email.length() < 5) || password.length() < 8 || requestKey.length() != 32)
			{
		    	message = "The logon fields supplied did meet minimum size, logon failed.  profileName:" + profileName + " password length:" + password.length() + " key:" + requestKey;
		    	meLogger.warn(message);
				utilityService.AddActionSecurityAccount("LoginInval", message, logon, request, customSession);
		    	customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
		    	return;
			}
			
		    //Check one-off logon key, matches between server and request.
			if (requestKey.compareTo(logon.getKey()) != 0)
			{
		    	message = "One off logon key, does not match request.  ServerKey:" + requestKey + " RequestKey:" + logon.getKey();
		    	meLogger.warn(message);
				utilityService.AddActionSecurityAccount("LoginInval", message, logon, request, customSession);
		    	customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
		    	return;
			}
			
			LogonState userStateDb = accountDataHelper.GetLogonState(profileName, requestId);
			if (userStateDb == null)
			{
		    	message = "Logon state could not be retrieved from the database.  ProfileName: " + profileName;
		    	meLogger.warn(message);
				utilityService.AddActionSecurityAccount("LoginInval", message, logon, request, customSession);
		    	customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
		    	return;
			}
			
			if (userStateDb.getFailedLogonCount() > 5)
			{
				Date now = new Date();
				long diffInMillies = now.getTime() - userStateDb.getFailedLogonLast().getTime();
				long minsDiff = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
				if (minsDiff < 30)
				{
			    	message = "Your account has been temporarily frozen, as the maximum allowed login attempts was breached.  Please wait " + (30-minsDiff) + " minutes and try again.";
			    	meLogger.warn(message);
					utilityService.AddActionSecurityAccount("LogFrozTry", message, logon, request, customSession);
			    	customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
			    	customResponse.setMessage(message);
			    	return;
				}
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
					customSession.setProfileName(userStateDb.getProfileName());
					customSession.setUserId(userStateDb.getUserId());
				}
				accountDataHelper.UpdateLogonState(userStateDb.getUserId(), 0, null, requestId);
				utilityService.AddActionSecurityAccount("LoginOK", logon.getProfileName(), logon, request, customSession);
				meLogger.debug("Logon successfull for User: " + logon.getProfileName());
				customResponse.setResponseCode(HttpStatus.OK.value());
			}
			else
			{
				int failCount = userStateDb.getFailedLogonCount() + 1;
				synchronized(customSession) 
				{
					customSession.setFailedLogonCount(failCount);
					customSession.setFailedLogonLast(new Date());
					customSession.setAuthenticated(false);
				}
				
				accountDataHelper.UpdateLogonState(userStateDb.getUserId(), failCount, new Date(), requestId);
				message = "Password didn't match, logon failed.";
				meLogger.warn(message);
				utilityService.AddActionSecurityAccount("LoginKO", "Password didn't match, logon failed.", logon, request, customSession);

				
				//Check for number of recent failures.  More than 5? then 10 seconds delay.
				if (customSession.getFailedLogonCount() > 5)
				{
					Thread.sleep(10000);
					customResponse.setMessage("The system has detected multiple logon failures, and will slow down subsequent attempts.  Please double check your credentials before proceeding.");				
				}
				else
				{
					Thread.sleep(2000);
					customResponse.setMessage("Login failed.  Please double check your credentials before proceeding.");				
				}

				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
			}
			
		}
		catch (Exception ex) {
			utilityService.AddActionSecurityAccount("LoginErr", "", logon, request, customSession);
			meLogger.error(ex);
			return;
		}
		finally { utilityService.LogMethod("AccountService","LogonCheck", startMS, requestId, profileName); }
	}

	public String GetAdminPassThroughToken(HttpServletRequest request, 
			CustomSessionState customSession, CustomResponse customResponse, String requestId)
	{
		//Only reads user data from DB, any state are held in the session object.
		long startMS = System.currentTimeMillis();
		String profileName = "";

		try
		{
			profileName = customSession.getProfileName();
			
			// salt 19 + 13
			String tempSalt = UserTools.GetComplexString().substring(0,19) + String.valueOf(System.currentTimeMillis());
			String keyFactors = customSession.getProfileName() + UserTools.GetIpAddress(request);
			
			// Length 28
			String token = SecurityTools.GetHashedPassword(keyFactors, tempSalt, 160, 1000);
			
			accountDataHelper.UpdateTempSalt(customSession.getUserId(), tempSalt, requestId);
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			
			utilityService.AddActionSecurityAccount("PassToken", token, null, request, customSession);
			
			return token;
		}
		catch (Exception ex) {
			utilityService.AddActionSecurityAccount("TokenErr", "", null, request, customSession);
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return "";
		}
		finally { utilityService.LogMethod("AccountService","GetAdminPassThroughToken", startMS, requestId, "ProfileName: " + profileName); }
	}
	
	public void AutoLoginAdminUser(String logonToken, String profileName, HttpServletRequest request, 
			CustomSessionState customSession, CustomResponse customResponse, String requestId)
	{
		//Only reads user data from DB, any state are held in the session object.
		long startMS = System.currentTimeMillis();
		boolean passedCheck = false;
		String message = "";
		
		try
		{
			logonToken = (logonToken == null) ? "" : logonToken;
			profileName = (profileName == null) ? "" : profileName;
			
			if (profileName.length() > 30)
			{
				message = "AutoLoginAdminUser was passed an invalid argument. UserName: " + profileName;
				meLogger.warn(message);
				utilityService.AddActionSecurityAccount("LoginInval", message, null, request, customSession);
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				customResponse.setMessage("Incorrect credentials received");
				return;
			}
			
			if (logonToken.length() != 28)
			{
				message = "LoginToken supplied are not valid.  User:" + profileName + " LogonToken:" + logonToken;
				meLogger.warn(message);
				utilityService.AddActionSecurityAccount("LoginInval", logonToken, null,request, customSession);
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				customResponse.setMessage("Incorrect credentials received");
				return;
			}

			if (customSession.getProfileName().length() > 0 && customSession.getProfileName().compareTo(profileName) != 0)
			{
				message = "Profile name supplied is different to profile on the session.  User:" + profileName;
				meLogger.warn(message);
				utilityService.AddActionSecurityAccount("LoginInval", message, null,request, customSession);
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				customResponse.setMessage("Incorrect credentials received");
				return;
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
			    	message = "Subsequent logon request too soon after previous failure.  Please try again later.";
			    	meLogger.warn(message);
			    	utilityService.AddActionSecurityAccount("LoginSoon", message, null, request, customSession);
			    	customResponse.setResponseCode(HttpStatus.FORBIDDEN.value());
			    	return;
			    }
			}
			
			LogonState logonState = accountDataHelper.GetLogonState(profileName, requestId);
			if (logonState == null)
			{
				message = "Logon state could not be retrieved from the database.  ProfileName: " + profileName;
				meLogger.warn(message);
				utilityService.AddActionSecurityAccount("LoginInval", message, null,request, customSession);
		    	customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
		    	customResponse.setMessage("Incorrect credentials received");
		    	return;
			}

			if (logonState.getFailedLogonCount() > 5)
			{
				Date now = new Date();
				long diffInMillies = now.getTime() - logonState.getFailedLogonLast().getTime();
				long minsDiff = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
				if (minsDiff < 30)
				{
					message = "Your account has been temporarily frozen, as the maximum allowed login attempts was breached.  Please wait " + (30-minsDiff) + " minutes and try again.";
			    	meLogger.warn(message);
			    	utilityService.AddActionSecurityAccount("LogFrozTry", message, null, request, customSession);
			    	customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
			    	customResponse.setMessage(message);
			    	return;
				}
			}
			
			String tempSalt = (logonState.getTempSalt() == null) ? "" : logonState.getTempSalt();
			
			if (tempSalt.length() != 32)
			{
				message = "Logon salt retrieved from the DB, was not the correct length: " + tempSalt;
				meLogger.warn(message);
				utilityService.AddActionSecurityAccount("LoginInval", message, null, request, customSession);
		    	customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
		    	return;
			}
			
			long saltTime = Long.valueOf(tempSalt.substring(19));
			long diffMS = System.currentTimeMillis() - saltTime;
			
		    if (diffMS > 3000)
		    {
		    	message = "Logon request too long after token was issued. " + String.valueOf(diffMS) + "ms";
		    	meLogger.warn(message);
		    	utilityService.AddActionSecurityAccount("LoginLong", message, null, request, customSession);
		    	customResponse.setResponseCode(HttpStatus.FORBIDDEN.value());
		    	customResponse.setMessage("Login not completed in a timely manner.");
		    	return;
		    }
			
			String keyFactors = profileName + UserTools.GetIpAddress(request);
			String logonTokenServer = SecurityTools.GetHashedPassword(keyFactors, tempSalt, 160, 1000);
			
			if (SecurityTools.SlowEquals(logonTokenServer.getBytes(), logonToken.getBytes()))
			{
				synchronized(customSession)
				{
					customSession.getCustomSessionIds().add(UserTools.GetComplexString());
					customSession.setFailedLogonCount(0);
					customSession.setFailedLogonLast(null);
					customSession.setAuthenticated(true);
					customSession.setAdmin(true);
					customSession.setGalleryViewer(false);
					customSession.setProfileName(logonState.getProfileName());
					customSession.setUserId(logonState.getUserId());
					customSession.setRemoteAddress(request.getRemoteAddr());
				}
				
				accountDataHelper.UpdateLogonState(logonState.getUserId(), 0, null, requestId);
				meLogger.debug("Admin logon successfull for User: " + customSession.getProfileName());
				customResponse.setResponseCode(HttpStatus.OK.value());
				utilityService.AddActionSecurityAccount("AutoLogOK", customSession.getProfileName(), null, request, customSession);
			}
			else
			{
				int failCount = logonState.getFailedLogonCount() + 1;
				synchronized(customSession) 
				{
					customSession.setFailedLogonCount(failCount);
					customSession.setFailedLogonLast(new Date());
					customSession.setAuthenticated(false);
				}
				
				accountDataHelper.UpdateLogonState(logonState.getUserId(), failCount, new Date(), requestId);
				message = "Pass through token didn't match, logon failed.  Profile: " + customSession.getProfileName() + " token: " + logonToken;
				meLogger.warn(message);
				utilityService.AddActionSecurityAccount("AutoLogKO", logonToken, null, request, customSession);
				
				//Check for number of recent failures.  More than 5? then 10 seconds delay.
				if (customSession.getFailedLogonCount() > 5)
				{
					Thread.sleep(10000);
					customResponse.setMessage("The system has detected multiple logon failures, and will slow down subsequent attempts.  Please double check your credentials before proceeding.");				
				}
				else
				{
					Thread.sleep(2000);
					customResponse.setMessage("Login failed.  Please double check your credentials before proceeding.");				
				}
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
			}
			
		}
		catch (Exception ex) {
			utilityService.AddActionSecurityAccount("LoginErr", "", null, request, customSession);
			meLogger.error(ex);
		}
		finally { utilityService.LogMethod("AccountService","AutoLoginAdminUser", startMS, requestId, profileName); }
	}
	
	public void ChangePassword(Logon logon, HttpServletRequest request, CustomResponse customResponse, CustomSessionState customSession, String requestId)
	{
		long startMS = System.currentTimeMillis();
		String message = "";
		try
		{
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
			    	message = "Subsequent logon request too soon after previous failure. (session)";
			    	meLogger.warn(message);
			    	utilityService.AddActionSecurityAccount("LoginSoon", message, logon, request, customSession);
			    	customResponse.setResponseCode(HttpStatus.FORBIDDEN.value());
			    	return;
			    }
			}
			
			if (!UserTools.CheckPasswordStrength(logon.getPassword()))
			{
				message = "Update security failed, new password does not meet minimum complexity rules.";
				meLogger.warn(message);
				utilityService.AddActionSecurityAccount("PassCmplx", message, logon, request, customSession);
				customResponse.setMessage(message);
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return;
			}
			
			LogonState userStateDb = accountDataHelper.GetLogonState(logon.getProfileName(), requestId);
			if (userStateDb == null)
			{
				message = "Logon state could not be retrieved from the database.  ProfileName: " + logon.getProfileName();
				meLogger.warn(message);
				utilityService.AddActionSecurityAccount("LoginInval", message, logon, request, customSession);
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
		    	return;
			}
			
			String passwordAttemptHash = SecurityTools.GetHashedPassword(logon.getOldPassword(), userStateDb.getSalt(), 160, 1000);
			if (!SecurityTools.SlowEquals(passwordAttemptHash.getBytes(), userStateDb.getPasswordHash().getBytes()))
			{
				message = "Current password is not correct, update password failed.";
				meLogger.warn(message);
				utilityService.AddActionSecurityAccount("PassUpdKO", message, logon, request, customSession);
				customResponse.setMessage(message);
				
				int failCount = Math.max(userStateDb.getFailedLogonCount(), customSession.getFailedLogonCount()) + 1;
				synchronized(customSession) 
				{
					customSession.setFailedLogonCount(failCount);
					customSession.setFailedLogonLast(new Date());
				}
				
				accountDataHelper.UpdateLogonState(userStateDb.getUserId(), failCount, new Date(), requestId);

				//Check for number of recent failures.  More than 5? then 10 seconds delay.
				if (customSession.getFailedLogonCount() > 5)
					Thread.sleep(10000);
				else
					Thread.sleep(1000);
				
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
			}
			else
			{
				//Create new salt and password hash
				String salt = SecurityTools.GenerateSalt();
				String newPasswordHash = SecurityTools.GetHashedPassword(logon.getPassword(), salt, 160, 1000);
				
				synchronized(customSession) 
				{
					customSession.setFailedLogonCount(0);
					customSession.setFailedLogonLast(null);
				}
				
				accountDataHelper.UpdatePassword(userStateDb.getUserId(), newPasswordHash, salt, requestId);
				meLogger.debug("Change password successfull for User: " + logon.getProfileName());
				
				utilityService.AddAction(ActionType.Account, userStateDb.getUserId(), "PassUpdOK", "");
				utilityService.AddActionSecurityAccount("PassUpdOK", logon.getProfileName(), logon, request, customSession);
				customResponse.setResponseCode(HttpStatus.OK.value());
			}
		}
		catch (Exception ex) {
			utilityService.AddActionSecurityAccount("PassUpdErr", "", logon, request, customSession);
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally { utilityService.LogMethod("AccountService","ChangePassword", startMS, requestId, logon.getProfileName()); }
	}
	
	//*************************************************************************************************************
	//*************************************  Messaging initiated methods ******************************************
	//*************************************************************************************************************
	

	public void CheckUpdateAccountStatus(long userId, String requestId) 
	{
		try
		{
			if (accountDataHelper.ShouldAccountBeLive(userId, requestId).compareTo("OK") == 0)
			{
				//Account should be switched to live from either init or frozen.
				accountDataHelper.UpdateAccountStatus(userId, AccountStatus.Live, requestId);
			}
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when Checking/Updating account to be live.");
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when Checking/Updating account to be live.", ex);
		}
	}
	
	public void SendVerifyEmail(long userId, String email, String validationString, String requestId) 
	{
		try
		{
			//TODO actually send email.
		}
		//catch (WallaException wallaEx) {
		//	meLogger.error("Unexpected error when trying to process SendEmailConfirmation");
		//}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces SendEmailConfirmation", ex);
		}
	}
	
	/*
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
	
	public void setUtilityService(UtilityService utilityService)
	{
		this.utilityService = utilityService;
	}
	*/
	
}
