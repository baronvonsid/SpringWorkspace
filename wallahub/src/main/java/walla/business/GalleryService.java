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
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Qualifier;

@Service("GalleryService")
public class GalleryService {

	private GalleryDataHelperImpl galleryDataHelper;
	private UtilityDataHelperImpl utilityDataHelper;
	private CachedData cachedData;
	
	private static final Logger meLogger = Logger.getLogger(GalleryService.class);

	//*************************************************************************************************************
	//***********************************  Web server synchronous methods *****************************************
	//*************************************************************************************************************
	
	public int CreateUpdateGallery(long userId, Gallery newGallery, String galleryName)
	{
		long startMS = System.currentTimeMillis();
		try {
			Gallery existingGallery = galleryDataHelper.GetGalleryMeta(userId, galleryName);
			if (existingGallery == null)
			{
				if (!newGallery.getName().equals(galleryName))
				{
					meLogger.warn("Create Gallery failed, names don't match.");
					return HttpStatus.CONFLICT.value(); 
				}
				
				long newGalleryId = utilityDataHelper.GetNewId("GalleryId");
				
				String passwordHash = "";
				String gallerySalt = "";
				if (newGallery.getAccessType() == 1)
				{
					gallerySalt = SecurityTools.GenerateSalt();
					String password = newGallery.getPassword() == null ? "" : newGallery.getPassword();
					if (newGallery.getPassword().length() < 1)
					{
						meLogger.warn("Gallery defined as password controlled, but no password supplied.  Gallery not created.");
						return HttpStatus.BAD_REQUEST.value(); 
					}
					
					passwordHash = SecurityTools.GetHashedPassword(newGallery.getPassword(), gallerySalt, 160, 1000);
				}

				galleryDataHelper.CreateGallery(userId, newGallery, newGalleryId, passwordHash, gallerySalt, UserTools.GetComplexString());
				
				//TODO switch to messaging.
				RefreshGalleryImages(userId, newGalleryId);
				
				return HttpStatus.CREATED.value();
			}
			else
			{
				if (newGallery.getId() == null || newGallery.getVersion() == null)
				{
					meLogger.warn("Update Gallery failed, ids and versions weren't supplied.");
					return HttpStatus.CONFLICT.value(); 
				}
				
				if (existingGallery.getId().longValue() != newGallery.getId().longValue())
				{
					meLogger.warn("Update Gallery failed, ids don't match.");
					return HttpStatus.CONFLICT.value(); 
				}
				
				if (existingGallery.getVersion().intValue() != newGallery.getVersion().intValue())
				{
					meLogger.warn("Update Gallery failed, record versions don't match.");
					return HttpStatus.CONFLICT.value(); 
				}
				
				String passwordHash = "";
				String gallerySalt = "";
				if (newGallery.getAccessType() == 1 && !newGallery.getPassword().equals("XXXXXXXXXX"))
				{
					//New password supplied, so generate password hash
					gallerySalt = SecurityTools.GenerateSalt();
					String password = newGallery.getPassword() == null ? "" : newGallery.getPassword();
					if (newGallery.getPassword().length() < 1)
					{
						meLogger.warn("Gallery defined as password controlled, but no password supplied.  Gallery not updated.");
						return HttpStatus.BAD_REQUEST.value(); 
					}
					passwordHash = SecurityTools.GetHashedPassword(newGallery.getPassword(), gallerySalt, 160, 1000);
				}
				
				galleryDataHelper.UpdateGallery(userId, newGallery, passwordHash, gallerySalt);

				//TODO switch to messaging.
				RefreshGalleryImages(userId, newGallery.getId());
				
				if (!existingGallery.getName().equals(galleryName))
				{
					return HttpStatus.MOVED_PERMANENTLY.value();
				}
				else
				{
					return HttpStatus.OK.value();
				}
			}
		}
		catch (WallaException wallaEx) {
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
		finally {UserTools.LogMethod("CreateUpdateGallery", meLogger, startMS, String.valueOf(userId) + " " + galleryName);}
	}

	public int DeleteGallery(long userId, Gallery gallery, String galleryName)
	{
		long startMS = System.currentTimeMillis();
		try {

			if (!gallery.getName().equals(galleryName))
			{
				meLogger.warn("DeleteGallery failed, names don't match.");
				return HttpStatus.CONFLICT.value(); 
			}
			
			galleryDataHelper.DeleteGallery(userId, gallery.getId(), gallery.getVersion(), galleryName);

			return HttpStatus.OK.value();
		}
		catch (WallaException wallaEx) {
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
		finally {UserTools.LogMethod("DeleteGallery", meLogger, startMS, String.valueOf(userId) + " " + galleryName);}
	}
	
	/*
	public long GetUserForGallery(String userName, String galleryName, String urlComplex)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			if (galleryName.length() > 30 || urlComplex.length() > 36 || userName.length() > 30)
			{
				String message = "GetUserForGallery was passed an invalid argument. UserName: " + userName + " GalleryName:" + galleryName + " UrlComplex:" + urlComplex;
				meLogger.warn(message);
				return -1;
			}

			return galleryDataHelper.GetGalleryUserId(userName, galleryName, urlComplex);
		}
		catch (WallaException wallaEx) {
			return -1;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return -1;
		}
		finally {UserTools.LogMethod("GetUserForGallery", meLogger, startMS, userName + " " + galleryName);}
	}
	*/
	
	public void ResetGallerySectionForPreview(Gallery gallery)
	{
		if (gallery.getSections() != null)
		{
			if (gallery.getSections().getSectionRef().size() > 0)
			{
				for (int i = 0; i < gallery.getSections().getSectionRef().size(); i++)
				{
					Gallery.Sections.SectionRef current = gallery.getSections().getSectionRef().get(i);
					current.setId((long)(i+1));
				}
				
				//TODO sort these bad boys by sequence. 
			}
		}
	}
	
	public Gallery GetGalleryMeta(long userId, String galleryName, CustomResponse customResponse)
	{
		long startMS = System.currentTimeMillis();
		try {
			//Get gallery list for response.
			Gallery gallery = galleryDataHelper.GetGalleryMeta(userId, galleryName);
			if (gallery == null)
			{
				meLogger.warn("GetGalleryMeta didn't return a valid Gallery object");
				customResponse.setResponseCode(HttpStatus.NOT_FOUND.value());
				return null;
			}

			if (gallery.getAccessType() == 1)
				gallery.setPassword("XXXXXXXXXX");
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			return gallery;
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
		finally {UserTools.LogMethod("GetGalleryMeta", meLogger, startMS, String.valueOf(userId) + " " + galleryName);}
	}

	public GalleryList GetGalleryListForUser(long userId, Date clientVersionTimestamp, CustomResponse customResponse)
	{
		long startMS = System.currentTimeMillis();
		try {
			GalleryList galleryList = null;
			Date lastUpdate = galleryDataHelper.LastGalleryListUpdate(userId);
			if (lastUpdate == null)
			{
				meLogger.warn("Last updated date for gallery could not be retrieved.");
				customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return null;
			}
			
			//Check if tag list changed
			if (clientVersionTimestamp != null)
			{
				if (!lastUpdate.after(clientVersionTimestamp) || lastUpdate.equals(clientVersionTimestamp))
				{
					meLogger.debug("No gallery list generated because server timestamp (" + lastUpdate.toString() + ") is not later than client timestamp (" + clientVersionTimestamp.toString() + ")");
					customResponse.setResponseCode(HttpStatus.NOT_MODIFIED.value());
					return null;
				}
			}
			
			//Get tag list for response.
			galleryList = galleryDataHelper.GetUserGalleryList(userId);
			if (galleryList!= null)
			{
				GregorianCalendar gregory = new GregorianCalendar();
				gregory.setTime(lastUpdate);
				XMLGregorianCalendar xmlOldGreg = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregory);
				
				galleryList.setLastChanged(xmlOldGreg);
			}
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			
			return galleryList;
		}
		catch (WallaException wallaEx) {
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		finally {UserTools.LogMethod("GetGalleryListForUser", meLogger, startMS, String.valueOf(userId));}
	}

	public long GetDefaultGallery(long userId, int appId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			App app = cachedData.GetApp(appId, "");
			
			String sql = "SELECT [GalleryId] FROM [Gallery] WHERE [SystemOwned] = 1 "
					+ "AND [GalleryType] = " + app.getDefaultGalleryType() + " AND [UserId] = " + userId;
			
			long galleryId = utilityDataHelper.GetLong(sql);
			if (galleryId > 0)
			{
				return galleryId;
			}
			
			return 0;
		}
		catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			throw ex;
		}
		finally { UserTools.LogMethod("GetDefaultGallery", meLogger, startMS, String.valueOf(userId) + " " + String.valueOf(appId)); }
	}
	
	public GalleryOptions GetGalleryOptions(long userId, Date clientVersionTimestamp, CustomResponse customResponse)
	{
		long startMS = System.currentTimeMillis();
		try {
			Date latestDate = new Date();
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.YEAR, -10);
			latestDate.setTime(cal.getTimeInMillis());
			
			//Get Presentation and Style objects from memory.
			List<Presentation> presentations = cachedData.GetPresentationList();
			if (presentations == null)
			{
				meLogger.debug("No gallery options list generated because available presentations could not be retrieved.");
				customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return null;
			}
			
			List<Style> style = cachedData.GetStyleList();
			if (style == null)
			{
				meLogger.debug("No gallery options list generated because available styles could not be retrieved.");
				customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return null;
			}
			
			GalleryOptions options = new GalleryOptions();
			options.setPresentation(new GalleryOptions.Presentation());
			
			for (Iterator<Presentation> iterater = presentations.iterator(); iterater.hasNext();)
			{
				Presentation current = (Presentation)iterater.next();

				GalleryOptions.Presentation.PresentationRef ref = new GalleryOptions.Presentation.PresentationRef();
				ref.setPresentationId(current.getPresentationId());
				ref.setName(current.getName());
				ref.setDescription(current.getDesc());
				ref.setJspName(current.getJspName());
				ref.setCssExtension(current.getCssExtension());
				ref.setMaxSections(current.getMaxSections());
				ref.setMaxImagesInSection(current.getMaxImagesInSection());
				
				ref.setOptionGalleryName(current.getOptionGalleryName());
				ref.setOptionGalleryDesc(current.getOptionGalleryDesc());
				ref.setOptionImageName(current.getOptionImageName());
				ref.setOptionImageDesc(current.getOptionImageDesc());
				ref.setOptionGroupingDesc(current.getOptionGroupingDesc());
				
				if (current.getLastUpdated().after(latestDate) || current.getLastUpdated().equals(latestDate))
				{
					latestDate = current.getLastUpdated();
				}
				
				options.getPresentation().getPresentationRef().add(ref);
			}

			
			options.setStyle(new GalleryOptions.Style());
			for (Iterator<Style> iterater = style.iterator(); iterater.hasNext();)
			{
				Style current = (Style)iterater.next();

				GalleryOptions.Style.StyleRef ref = new GalleryOptions.Style.StyleRef();
				ref.setStyleId(current.getStyleId());
				ref.setName(current.getName());
				ref.setDescription(current.getDesc());
				ref.setCssFolder(current.getCssFolder());
				
				options.getStyle().getStyleRef().add(ref);
				
				if (current.getLastUpdated().after(latestDate) || current.getLastUpdated().equals(latestDate))
				{
					latestDate = current.getLastUpdated();
				}
			}
			
			if (clientVersionTimestamp == null || latestDate.after(clientVersionTimestamp))
			{
				GregorianCalendar oldGreg = new GregorianCalendar();
				oldGreg.setTime(latestDate);
				XMLGregorianCalendar xmlOldGreg = DatatypeFactory.newInstance().newXMLGregorianCalendar(oldGreg);
				options.setLastChanged(xmlOldGreg);
				
				customResponse.setResponseCode(HttpStatus.OK.value());
				return options;
			}
			else
			{
				if (meLogger.isDebugEnabled()) {meLogger.debug("No gallery options list generated because client list is up to date.");}
				customResponse.setResponseCode(HttpStatus.NOT_MODIFIED.value());
				return null;
			}
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		finally {UserTools.LogMethod("GetGalleryOptions", meLogger, startMS, "");}
	}

	public Gallery GetGallerySections(long userId, Gallery requestGallery, CustomResponse customResponse)
	{
		long startMS = System.currentTimeMillis();
		try {
			long newTempGalleryId = utilityDataHelper.GetNewId("TempGalleryId");
			
			Gallery gallery = galleryDataHelper.GetGallerySections(userId, requestGallery, newTempGalleryId);

			customResponse.setResponseCode(HttpStatus.OK.value());
			return gallery;
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
		finally {UserTools.LogMethod("GetGallerySections", meLogger, startMS, String.valueOf(userId));}
	}
	
	public Presentation GetPresentation(int presentationId) throws WallaException
	{
		return cachedData.GetPresentation(presentationId);
	}
	
	public Style GetStyle(int styleId) throws WallaException
	{
		return cachedData.GetStyle(styleId);
	}
	
	public String GetGalleryLogonToken(String galleryName, HttpServletRequest request, 
			CustomSessionState customSession, CustomResponse customResponse)
	{
		//Only reads user data from DB, any state are held in the session object.
		long startMS = System.currentTimeMillis();
		String profileName = "";

		try
		{
			profileName = customSession.getProfileName();
			
			
			// salt 19 + 13
			String tempSalt = UserTools.GetComplexString().substring(0,19) + String.valueOf(System.currentTimeMillis());
			String keyFactors = galleryName + customSession.getProfileName() + UserTools.GetIpAddress(request);
			// Length 28
			String token = SecurityTools.GetHashedPassword(keyFactors, tempSalt, 160, 1000);
			
			galleryDataHelper.UpdateTempSalt(customSession.getUserId(), galleryName, tempSalt);
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			
			return token;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return "";
		}
		finally { UserTools.LogMethod("GetGalleryLogonToken", meLogger, startMS, "ProfileName: " + profileName); }
	}
	
	public boolean AutoLoginGalleryUser(boolean useTokenOnly, int accessType, String logonToken, String complexUrl, String password, String profileName, String galleryName, HttpServletRequest request, 
			CustomSessionState customSession, CustomResponse customResponse)
	{
		//Only reads user data from DB, any state are held in the session object.
		long startMS = System.currentTimeMillis();
		boolean passedCheck = false;

		try
		{
			logonToken = (logonToken == null) ? "" : logonToken;
			profileName = (profileName == null) ? "" : profileName;
			galleryName = (galleryName == null) ? "" : galleryName;
			complexUrl = (complexUrl == null) ? "" : complexUrl;
			password = (password == null) ? "" : password;
			
			if (galleryName.length() > 30 || profileName.length() > 30)
			{
				meLogger.warn("LoginGalleryUser was passed an invalid argument. UserName: " + profileName + " GalleryName:" + galleryName);
				return GalleryLoginFailReturn(customSession);
			}
			
			//Request validations.
			if (useTokenOnly && logonToken.length() != 28)
			{
				meLogger.warn("LoginToken supplied are not valid.  User:" + profileName + " Gallery:" + galleryName + " LogonToken:" + logonToken);
				return GalleryLoginFailReturn(customSession);
			}
			else
			{
				if (accessType == 0 || accessType == 1)
				{
					meLogger.warn("LoginGalleryUser was passed an invalid request to login into a private gallery. UserName: " + profileName + " GalleryName:" + galleryName);
					return GalleryLoginFailReturn(customSession);
				}
				else if (accessType == 2 && complexUrl.length() != 32)
				{
					meLogger.warn("ComplexUrl supplied is not valid.  User:" + profileName + " Gallery:" + galleryName + " ComplexUrl:" + complexUrl);
					return GalleryLoginFailReturn(customSession);
				}
			}

			GalleryLogon galleryLogon = galleryDataHelper.GetGalleryLogonDetail(profileName, galleryName, complexUrl);
			if (galleryLogon == null)
			{
				meLogger.warn("Gallery could not be retrieved for logon.  User:" + profileName + " Gallery:" + galleryName + " ComplexUrl:" + complexUrl);
				return GalleryLoginFailReturn(customSession);
			}

			if (!useTokenOnly && accessType != galleryLogon.getAccessType())
			{
				meLogger.warn("Gallery logon, access type mismatch.  User:" + profileName + " Gallery:" + galleryName + " ComplexUrl:" + complexUrl);
				return GalleryLoginFailReturn(customSession);
			}
			
			//This request requires a login token to be validated.
			if (useTokenOnly)
			{
				//String salt = utilityDataHelper.GetString("SELECT [TempLoginSalt] FROM [Gallery] WHERE [Name] = '" + galleryName + "' AND [UserId]=" + userId);
				String tempSalt = (galleryLogon.getTempSalt() == null) ? "" : galleryLogon.getTempSalt();
				
				if (tempSalt.length() != 32)
				{
					meLogger.warn("Logon salt retrieved from the DB, was not the correct length: " + tempSalt);
					return GalleryLoginFailReturn(customSession);
				}
				
				long saltTime = Long.valueOf(tempSalt.substring(19));
				long diffMS = System.currentTimeMillis() - saltTime;
				
			    if (diffMS > 10000)
			    {
			    	meLogger.warn("Logon request too long after token was issued.");
			    	return GalleryLoginFailReturn(customSession);
			    }
				
				String keyFactors = galleryName + profileName + UserTools.GetIpAddress(request);
				String logonTokenServer = SecurityTools.GetHashedPassword(keyFactors, tempSalt, 160, 1000);
				
				if (SecurityTools.SlowEquals(logonTokenServer.getBytes(), logonToken.getBytes()))
					passedCheck = true;
				else
					meLogger.warn("Gallery logon failed, tokens didn't match for User: " + profileName + " Gallery: " + galleryName);
			}
			else
			{
				if (accessType != 2)
				{
					meLogger.warn("Logon request failed, because the gallery is not marked as accessible via a complex url.  User: " + profileName + " Gallery: " + galleryName);
			    	return GalleryLoginFailReturn(customSession);
				}
				
				if (complexUrl.equals(galleryLogon.getComplexUrl()))
					passedCheck = true;
				else
				{
					meLogger.warn("Gallery logon failed, url did not match.  User: " + profileName + " Gallery: " + galleryName + " ComplexUrl:" + complexUrl);
					return GalleryLoginFailReturn(customSession);
				}
			}
			
			if (passedCheck)
			{
				synchronized(customSession) 
				{
					customSession.getCustomSessionIds().add(UserTools.GetComplexString());
					customSession.setAdmin(false);
					customSession.setUserId(galleryLogon.getUserId());
					customSession.setProfileName(profileName);
					customSession.setGalleryName(galleryName);
					customSession.setFailedLogonCount(0);
					customSession.setFailedLogonLast(null);
					customSession.setAuthenticated(true);
					customSession.setGalleryViewer(true);
					customSession.setRemoteAddress(UserTools.GetIpAddress(request));
				}
				meLogger.debug("Gallery logon successfull for User: " + profileName + " Gallery: " + galleryName);
				return true;
			}
			else
				return GalleryLoginFailReturn(customSession);
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return false;
		}
		finally { UserTools.LogMethod("AutoLoginGalleryUser", meLogger, startMS, "ProfileName: " + profileName + " Gallery: " + galleryName); }
	}
	
	
	public boolean LoginGalleryUser(GalleryLogon requestLogon, HttpServletRequest request, CustomSessionState customSession)
	{
		long startMS = System.currentTimeMillis();
		String profileName = "";
		String galleryName = "";
		String password = "";
		String requestKey = "";
		String serverKey = "";

		try
		{
			profileName = requestLogon.getProfileName() == null ? "" : requestLogon.getProfileName();
			password = requestLogon.getPassword() == null ? "" : requestLogon.getPassword();
			galleryName = requestLogon.getGalleryName() == null ? "" : requestLogon.getGalleryName();
			requestKey = requestLogon.getKey() == null ? "" : requestLogon.getKey();
			
			synchronized(customSession) {
				serverKey = customSession.getNonceKey() == null ? "" : customSession.getNonceKey();
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
			    	return GalleryLoginFailReturn(customSession);
			    }
			}

			if (profileName.length() < 1 || password.length() < 1 || requestKey.length() < 1)
			{
				meLogger.warn("Not all the gallery logon fields were supplied, logon failed.  Profile: " + profileName + " Gallery: " + galleryName);
				return GalleryLoginFailReturn(customSession);
			}
			
			if (customSession.getRemoteAddress().compareTo(request.getRemoteAddr()) != 0)
			{
				meLogger.warn("IP address of the session has changed since the logon key was issued. Profile: " + profileName + " Gallery: " + galleryName);
				return GalleryLoginFailReturn(customSession);
			}
			
			if (!galleryName.equals(customSession.getGalleryName()))
			{
				meLogger.warn("Gallery name mismatch, logon failed.  Request Name: " + galleryName + " Server Name: " + customSession.getGalleryName());
				return GalleryLoginFailReturn(customSession);
			}
			
			if (!profileName.equals(customSession.getProfileName()))
			{
				meLogger.warn("Profile name mismatch, logon failed.  Request Name: " + profileName + " Server Name: " + customSession.getProfileName());
				return GalleryLoginFailReturn(customSession);
			}
			
		    //Check one-off logon key, matches between server and request.
			if (requestKey.compareTo(serverKey) != 0)
			{
				meLogger.warn("One off logon key, does not match request.  Profile: " + profileName + " Gallery: " + galleryName + " ServerKey:" + serverKey + " RequestKey:" + requestKey);
				return GalleryLoginFailReturn(customSession);
			}

			GalleryLogon galleryLogon = galleryDataHelper.GetGalleryLogonDetail(profileName, galleryName, "");
			if (galleryLogon == null)
			{
				meLogger.warn("Gallery could not be retrieved for logon.  User:" + profileName + " Gallery:" + galleryName);
				return GalleryLoginFailReturn(customSession);
			}

			if (galleryLogon.getAccessType() != 1)
			{
				meLogger.warn("Gallery logon, access type is incorrect, only those which require password access can be validated here.  User:" + profileName + " Gallery:" + galleryName);
				return GalleryLoginFailReturn(customSession);
			}

			//Get a hash of the password attempt.
			String passwordAttemptHash = SecurityTools.GetHashedPassword(password, galleryLogon.getGallerySalt(), 160, 1000);

			if (SecurityTools.SlowEquals(galleryLogon.getPasswordHash().getBytes(), passwordAttemptHash.getBytes()))
			{
				synchronized(customSession) 
				{
					customSession.getCustomSessionIds().add(UserTools.GetComplexString());
					customSession.setAdmin(false);
					customSession.setUserId(galleryLogon.getUserId());
					customSession.setFailedLogonCount(0);
					customSession.setFailedLogonLast(null);
					customSession.setAuthenticated(true);
					customSession.setGalleryViewer(true);
				}
				meLogger.debug("Gallery logon successfull for User: " + profileName + " Gallery: " + galleryName);
				return true;
			}
			else
				return GalleryLoginFailReturn(customSession);
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return false;
		}
		finally { UserTools.LogMethod("LoginGalleryUser", meLogger, startMS, "Profile:" + profileName + " Gallery:" + galleryName); }
	}
	
	private boolean GalleryLoginFailReturn(CustomSessionState customSession) throws InterruptedException
	{
		synchronized(customSession) 
		{
			customSession.setFailedLogonCount(customSession.getFailedLogonCount() + 1);
			customSession.setFailedLogonLast(new Date());
			customSession.setAuthenticated(false);
		}
		
		//Check for number of recent failures.  More than 5? then 10 seconds delay.
		if (customSession.getFailedLogonCount() > 5)
			Thread.sleep(30000);
		else
			Thread.sleep(1000);
		
		return false;
	}
	
	public String GetGalleryUserLogonToken(String profileName, String galleryName, HttpServletRequest request, CustomSessionState customSession, CustomResponse customResponse)
	{
		long startMS = System.currentTimeMillis();
		try
		{
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
			    	meLogger.warn("Subsequent gallery user logon token request too soon after previous failure. (session)");
			    	customResponse.setResponseCode(HttpStatus.FORBIDDEN.value());
			    	return "";
			    }
			}
			
			GalleryLogon galleryLogon = galleryDataHelper.GetGalleryLogonDetail(profileName, galleryName, "");
			if (galleryLogon == null)
			{
				meLogger.warn("Gallery could not be retrieved for logon.  User:" + profileName + " Gallery:" + galleryName);
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return "";
			}
			
			if (galleryLogon.getAccessType() != 1)
			{
				//This gallery is not setup for login access.
		    	meLogger.warn("This gallery is not setup for login access.  AccessType:" + galleryLogon.getAccessType());
		    	customResponse.setResponseCode(HttpStatus.FORBIDDEN.value());
		    	return "";
			}
			
			//Passed initial checks, so issue a key and update the custom session.
			String newKey = UserTools.GetComplexString();
			
			//TEMP overide for testing!!!!
			newKey = "12345678901234567890123456789012";

			synchronized(customSession) {
				customSession.setNonceKey(newKey);
				customSession.setProfileName(profileName);
				customSession.setGalleryName(galleryName);
				customSession.setAuthenticated(false);
				customSession.setGalleryViewer(true);
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
		finally { UserTools.LogMethod("GetGalleryUserLogonToken", meLogger, startMS, "ProfileName: " + profileName + " GalleryName:" + galleryName); }
	}
	
	public int GetGalleryAccessType(String profileName, String galleryName, CustomResponse customResponse) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			GalleryLogon galleryLogon = galleryDataHelper.GetGalleryLogonDetail(profileName, galleryName, "");
			if (galleryLogon == null)
			{
				meLogger.warn("Gallery could not be retrieved for logon.  User:" + profileName + " Gallery:" + galleryName);
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return -1;
			}
			
			
			return galleryLogon.getAccessType();
			
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return -1;
		}
		finally { UserTools.LogMethod("GetGalleryAccessType", meLogger, startMS, "ProfileName: " + profileName + " GalleryName:" + galleryName); }
	}
	
	//*************************************************************************************************************
	//*************************************  Messaging initiated methods ******************************************
	//*************************************************************************************************************

	public void RefreshGalleryImages(long userId, long galleryId)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			galleryDataHelper.RegenerateGalleryImages(userId, galleryId);
		}
		catch (WallaException wallaEx) {
			meLogger.error("RefreshGalleryImages failed with an error");
		}
		catch (Exception ex) {
			meLogger.error("RefreshGalleryImages failed with an error", ex);
		}
		finally {UserTools.LogMethod("RefreshGalleryImages", meLogger, startMS, String.valueOf(userId) + " " + String.valueOf(galleryId));}
	}
	
	public void setGalleryDataHelper(GalleryDataHelperImpl galleryDataHelper)
	{
		this.galleryDataHelper = galleryDataHelper;
	}
	
	public void setCachedData(CachedData cachedData)
	{
		this.cachedData = cachedData;
	}
	
	public void setUtilityDataHelper(UtilityDataHelperImpl utilityDataHelper)
	{
		this.utilityDataHelper = utilityDataHelper;
	}
}
