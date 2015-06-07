package walla.business;

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
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

@Service("GalleryService")
public class GalleryService {

	@Resource(name="galleryDataHelper")
	private GalleryDataHelperImpl galleryDataHelper;
	
	@Resource(name="utilityDataHelper")
	private UtilityDataHelperImpl utilityDataHelper;
	
	@Resource(name="cachedData")
	private CachedData cachedData;
	
	@Resource(name="utilityServicePooled")
	private UtilityService utilityService;
	
	@Value( "${messaging.enabled}" ) private boolean messagingEnabled;
	
	private static final Logger meLogger = Logger.getLogger(GalleryService.class);

	//*************************************************************************************************************
	//***********************************  Web server synchronous methods *****************************************
	//*************************************************************************************************************
	
	public GalleryService()
	{
		meLogger.debug("GalleryService object instantiated.");
	}
	
	public int CreateUpdateGallery(long userId, Gallery newGallery, String galleryName, long userAppId, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try {
			Gallery existingGallery = galleryDataHelper.GetGalleryMeta(userId, galleryName, requestId);
			if (existingGallery == null)
			{
				if (!newGallery.getName().equals(galleryName))
				{
					meLogger.warn("Create Gallery failed, names don't match.");
					return HttpStatus.CONFLICT.value(); 
				}
				
				long newGalleryId = utilityDataHelper.GetNewId("GalleryId", requestId);
				
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

				galleryDataHelper.CreateGallery(userId, newGallery, newGalleryId, passwordHash, gallerySalt, UserTools.GetComplexString(), requestId);
				
				if (messagingEnabled)
				{
					RequestMessage requestMessage = utilityService.BuildRequestMessage(userId, "GalleryService", "RefreshGalleryImages", requestId, newGalleryId, 0, null);
					utilityService.SendMessageToQueue(QueueTemplate.Agg, requestMessage, "GALRFH");
				}
				else
					RefreshGalleryImages(userId, newGalleryId, requestId);
				
				utilityService.AddAction(ActionType.UserApp, userAppId, "GalAdd", "");
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
				if (newGallery.getAccessType() == 1 && !newGallery.getPassword().equals(".........."))
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
				
				galleryDataHelper.UpdateGallery(userId, newGallery, passwordHash, gallerySalt, requestId);

				if (messagingEnabled)
				{
					RequestMessage requestMessage = utilityService.BuildRequestMessage(userId, "GalleryService", "RefreshGalleryImages", requestId, newGallery.getId(), 0, null);
					utilityService.SendMessageToQueue(QueueTemplate.Agg, requestMessage, "GALRFH");
				}
				else
					RefreshGalleryImages(userId, newGallery.getId(), requestId);
				
				
				utilityService.AddAction(ActionType.UserApp, userAppId, "GalUpd", "");
				
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
		finally {utilityService.LogMethod("GalleryService","CreateUpdateGallery", startMS, requestId, galleryName);}
	}

	public int DeleteGallery(long userId, Gallery gallery, String galleryName, long userAppId, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try {

			if (!gallery.getName().equals(galleryName))
			{
				meLogger.warn("DeleteGallery failed, names don't match.");
				return HttpStatus.CONFLICT.value(); 
			}
			
			galleryDataHelper.DeleteGallery(userId, gallery.getId(), gallery.getVersion(), galleryName, requestId);

			utilityService.AddAction(ActionType.UserApp, userAppId, "GalDel", "");
			return HttpStatus.OK.value();
		}
		catch (WallaException wallaEx) {
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
		finally {utilityService.LogMethod("GalleryService","DeleteGallery", startMS, requestId, galleryName);}
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
		finally {utilityService.LogMethod("GalleryService","GetUserForGallery", startMS, requestId, userName + " " + galleryName);}
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
	
	public Gallery GetGalleryMeta(long userId, String galleryName, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try {
			//Get gallery list for response.
			Gallery gallery = galleryDataHelper.GetGalleryMeta(userId, galleryName, requestId);
			if (gallery == null)
			{
				meLogger.warn("GetGalleryMeta didn't return a valid Gallery object");
				customResponse.setResponseCode(HttpStatus.NOT_FOUND.value());
				return null;
			}

			if (gallery.getAccessType() == 1)
				gallery.setPassword("...........");
			
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
		finally {utilityService.LogMethod("GalleryService","GetGalleryMeta", startMS, requestId, galleryName);}
	}

	public GalleryList GetGalleryListForUser(long userId, Date clientVersionTimestamp, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try {
			GalleryList galleryList = null;
			Date lastUpdate = galleryDataHelper.LastGalleryListUpdate(userId, requestId);
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
			galleryList = galleryDataHelper.GetUserGalleryList(userId, requestId);
			if (galleryList!= null)
			{
				Calendar lastUpdateCalendar = Calendar.getInstance();
				lastUpdateCalendar.setTime(lastUpdate);
				galleryList.setLastChanged(lastUpdateCalendar);
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
		finally {utilityService.LogMethod("GalleryService","GetGalleryListForUser", startMS, requestId, "");}
	}

	public long GetDefaultGallery(long userId, int appId, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			App app = cachedData.GetApp(appId, "", 0,0,0,requestId);
			
			String sql = "SELECT [GalleryId] FROM [Gallery] WHERE [SystemOwned] = 1 "
					+ "AND [GalleryType] = " + app.getDefaultGalleryType() + " AND [UserId] = " + userId;
			
			long galleryId = utilityDataHelper.GetLong(sql, requestId);
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
		finally { utilityService.LogMethod("GalleryService","GetDefaultGallery", startMS, requestId, String.valueOf(appId)); }
	}
	
	public GalleryOption GetGalleryOption(long userId, Date clientVersionTimestamp, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try {
			Date latestDate = new Date();
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.YEAR, -10);
			latestDate.setTime(cal.getTimeInMillis());
			
			//Get Presentation and Style objects from memory.
			List<Presentation> presentations = cachedData.GetPresentationList(requestId);
			if (presentations == null)
			{
				meLogger.debug("No gallery options list generated because available presentations could not be retrieved.");
				customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return null;
			}
			
			List<Style> style = cachedData.GetStyleList(requestId);
			if (style == null)
			{
				meLogger.debug("No gallery options list generated because available styles could not be retrieved.");
				customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return null;
			}
			
			GalleryOption options = new GalleryOption();
			options.setPresentation(new GalleryOption.Presentation());
			
			for (Iterator<Presentation> iterater = presentations.iterator(); iterater.hasNext();)
			{
				Presentation current = (Presentation)iterater.next();

				GalleryOption.Presentation.PresentationRef ref = new GalleryOption.Presentation.PresentationRef();
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

			
			options.setStyle(new GalleryOption.Style());
			for (Iterator<Style> iterater = style.iterator(); iterater.hasNext();)
			{
				Style current = (Style)iterater.next();

				GalleryOption.Style.StyleRef ref = new GalleryOption.Style.StyleRef();
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
				Calendar latestCalendar = Calendar.getInstance();
				latestCalendar.setTime(latestDate);
				options.setLastChanged(latestCalendar);
				
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
		finally {utilityService.LogMethod("GalleryService","GetGalleryOption", startMS, requestId, "");}
	}

	public Gallery GetGallerySections(long userId, Gallery requestGallery, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try {
			long newTempGalleryId = utilityDataHelper.GetNewId("TempGalleryId", requestId);
			
			Gallery gallery = galleryDataHelper.GetGallerySections(userId, requestGallery, newTempGalleryId, requestId);

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
		finally {utilityService.LogMethod("GalleryService","GetGallerySections", startMS, requestId, String.valueOf(requestGallery.getId()));}
	}
	
	public Presentation GetPresentation(int presentationId, String requestId) throws WallaException
	{
		return cachedData.GetPresentation(presentationId, requestId);
	}
	
	public Style GetStyle(int styleId, String requestId) throws WallaException
	{
		return cachedData.GetStyle(styleId, requestId);
	}
	
	public String GetGalleryPassThroughToken(String galleryName, HttpServletRequest request, 
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
			String keyFactors = galleryName + customSession.getProfileName() + UserTools.GetIpAddress(request);
			
			// Length 28
			String token = SecurityTools.GetHashedPassword(keyFactors, tempSalt, 160, 1000);
			
			galleryDataHelper.UpdateTempSalt(customSession.getUserId(), galleryName, tempSalt, requestId);
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			utilityService.AddActionSecurityGallery("GalToken", token, null, request, customSession);
			
			return token;
		}
		catch (Exception ex) {
			utilityService.AddActionSecurityGallery("GalTokErr", galleryName, null, request, customSession);
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return "";
		}
		finally { utilityService.LogMethod("GalleryService","GetGalleryLogonToken", startMS, requestId, profileName); }
	}
	
	public boolean AutoLoginGalleryUser(boolean useTokenOnly, int accessType, String logonToken, String complexUrl, String password, String profileName, String galleryName, HttpServletRequest request, 
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
			galleryName = (galleryName == null) ? "" : galleryName;
			complexUrl = (complexUrl == null) ? "" : complexUrl;
			password = (password == null) ? "" : password;
			
			if (galleryName.length() > 30 || profileName.length() > 30)
			{
				message = "LoginGalleryUser was passed an invalid argument. UserName: " + profileName + " GalleryName:" + galleryName;
				meLogger.warn(message);
				utilityService.AddActionSecurityGallery("GalInval", message, null, request, customSession);
				return GalleryLoginFailReturn(customSession);
			}
			
			//Request validations.
			if (useTokenOnly && logonToken.length() != 28)
			{
				message = "LoginToken supplied are not valid.  User:" + profileName + " Gallery:" + galleryName + " LogonToken:" + logonToken;
				meLogger.warn(message);
				utilityService.AddActionSecurityGallery("GalInval", message, null, request, customSession);
				return GalleryLoginFailReturn(customSession);
			}
			else
			{
				if (accessType == 0 || accessType == 1)
				{
					message = "LoginGalleryUser was passed an invalid request to login into a private gallery. UserName: " + profileName + " GalleryName:" + galleryName;
					meLogger.warn(message);
					utilityService.AddActionSecurityGallery("GalInval", message, null, request, customSession);
					return GalleryLoginFailReturn(customSession);
				}
				else if (accessType == 2 && complexUrl.length() != 32)
				{
					message = "ComplexUrl supplied is not valid.  User:" + profileName + " Gallery:" + galleryName + " ComplexUrl:" + complexUrl;
					meLogger.warn(message);
					utilityService.AddActionSecurityGallery("GalURLKO", message, null, request, customSession);
					return GalleryLoginFailReturn(customSession);
				}
			}

			GalleryLogon galleryLogon = galleryDataHelper.GetGalleryLogonDetail(profileName, galleryName, complexUrl, requestId);
			if (galleryLogon == null)
			{
				message = "Gallery could not be retrieved for logon.  User:" + profileName + " Gallery:" + galleryName + " ComplexUrl:" + complexUrl;
				meLogger.warn(message);
				utilityService.AddActionSecurityGallery("GalInval", message, null, request, customSession);
				return GalleryLoginFailReturn(customSession);
			}

			if (!useTokenOnly && accessType != galleryLogon.getAccessType())
			{
				message = "Gallery logon, access type mismatch.  User:" + profileName + " Gallery:" + galleryName + " ComplexUrl:" + complexUrl;
				meLogger.warn(message);
				utilityService.AddActionSecurityGallery("GalInval", message, galleryLogon, request, customSession);
				return GalleryLoginFailReturn(customSession);
			}
			
			//This request requires a login token to be validated.
			if (useTokenOnly)
			{
				//String salt = utilityDataHelper.GetString("SELECT [TempLoginSalt] FROM [Gallery] WHERE [Name] = '" + galleryName + "' AND [UserId]=" + userId);
				String tempSalt = (galleryLogon.getTempSalt() == null) ? "" : galleryLogon.getTempSalt();
				
				if (tempSalt.length() != 32)
				{
					message = "Logon salt retrieved from the DB, was not the correct length: " + tempSalt;
					meLogger.warn(message);
					utilityService.AddActionSecurityGallery("GalInval", message, galleryLogon, request, customSession);
					return GalleryLoginFailReturn(customSession);
				}
				
				long saltTime = Long.valueOf(tempSalt.substring(19));
				long diffMS = System.currentTimeMillis() - saltTime;
				
			    if (diffMS > 3000)
			    {
			    	message = "Logon request too long after token was issued";
			    	meLogger.warn(message);
			    	utilityService.AddActionSecurityGallery("GalLogLong", message, galleryLogon, request, customSession);
			    	return GalleryLoginFailReturn(customSession);
			    }
				
				String keyFactors = galleryName + profileName + UserTools.GetIpAddress(request);
				String logonTokenServer = SecurityTools.GetHashedPassword(keyFactors, tempSalt, 160, 1000);
				
				if (SecurityTools.SlowEquals(logonTokenServer.getBytes(), logonToken.getBytes()))
					passedCheck = true;
				else
				{
					message = "Gallery logon failed, tokens didn't match for User: " + profileName + " Gallery: " + galleryName;
					meLogger.warn(message);
				}
			}
			else
			{
				if (accessType != 2)
				{
					message = "Logon request failed, because the gallery is not marked as accessible via a complex url.  User: " + profileName + " Gallery: " + galleryName;
					meLogger.warn(message);
					utilityService.AddActionSecurityGallery("GalInval", message, galleryLogon, request, customSession);
			    	return GalleryLoginFailReturn(customSession);
				}
				
				if (complexUrl.equals(galleryLogon.getComplexUrl()))
				{
					utilityService.AddActionSecurityGallery("GalURLOK", galleryLogon.getComplexUrl(), galleryLogon, request, customSession);
					passedCheck = true;
				}
				else
				{
					message = "Gallery logon failed, url did not match.  User: " + profileName + " Gallery: " + galleryName + " ComplexUrl:" + complexUrl;
					meLogger.warn(message);
					utilityService.AddActionSecurityGallery("GalURLKO", message, null, request, customSession);
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
				
				utilityService.AddActionSecurityGallery("GalLogOK", galleryName, galleryLogon, request, customSession);
				return true;
			}
			else
			{
				utilityService.AddActionSecurityGallery("GalLogKO", message, galleryLogon, request, customSession);
				return GalleryLoginFailReturn(customSession);
			}
		}
		catch (Exception ex) {
			utilityService.AddActionSecurityGallery("GalErr", galleryName, null, request, customSession);
			meLogger.error(ex);
			return false;
		}
		finally { utilityService.LogMethod("GalleryService","AutoLoginGalleryUser", startMS, requestId, galleryName); }
	}
	
	public boolean LoginGalleryUser(GalleryLogon requestLogon, HttpServletRequest request, CustomSessionState customSession, String requestId)
	{
		long startMS = System.currentTimeMillis();
		String profileName = "";
		String galleryName = "";
		String password = "";
		String requestKey = "";
		String serverKey = "";
		String message = "";
		
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
			    	message = "Subsequent logon request too soon after previous failure. (session)";
			    	meLogger.warn(message);
			    	utilityService.AddActionSecurityGallery("GalLogSoon", message, requestLogon, request, customSession);
			    	return GalleryLoginFailReturn(customSession);
			    }
			}

			if (profileName.length() < 1 || password.length() < 1 || requestKey.length() < 1)
			{
				message = "Not all the gallery logon fields were supplied, logon failed.  Profile: " + profileName + " Gallery: " + galleryName;
				meLogger.warn(message);
				utilityService.AddActionSecurityGallery("GalInval", galleryName, requestLogon, request, customSession);
				return GalleryLoginFailReturn(customSession);
			}
			
			if (customSession.getRemoteAddress().compareTo(request.getRemoteAddr()) != 0)
			{
				message = "IP address of the session has changed since the logon key was issued. Profile: " + profileName + " Gallery: " + galleryName;
				meLogger.warn(message);
				utilityService.AddActionSecurityGallery("GalInval", message, requestLogon, request, customSession);
				return GalleryLoginFailReturn(customSession);
			}
			
			if (!galleryName.equals(customSession.getGalleryName()))
			{
				message = "Gallery name mismatch, logon failed.  Request Name: " + galleryName + " Server Name: " + customSession.getGalleryName();
				meLogger.warn(message);
				utilityService.AddActionSecurityGallery("GalInval", message, requestLogon, request, customSession);
				return GalleryLoginFailReturn(customSession);
			}
			
			if (!profileName.equals(customSession.getProfileName()))
			{
				message = "Profile name mismatch, logon failed.  Request Name: " + profileName + " Server Name: " + customSession.getProfileName();
				meLogger.warn(message);
				utilityService.AddActionSecurityGallery("GalInval", message, requestLogon, request, customSession);
				return GalleryLoginFailReturn(customSession);
			}
			
		    //Check one-off logon key, matches between server and request.
			if (requestKey.compareTo(serverKey) != 0)
			{
				message = "One off logon key, does not match request.  Profile: " + profileName + " Gallery: " + galleryName + " ServerKey:" + serverKey + " RequestKey:" + requestKey;
				meLogger.warn(message);
				utilityService.AddActionSecurityGallery("GalInval", message, requestLogon, request, customSession);
				return GalleryLoginFailReturn(customSession);
			}

			GalleryLogon galleryLogon = galleryDataHelper.GetGalleryLogonDetail(profileName, galleryName, "", requestId);
			if (galleryLogon == null)
			{
				message = "Gallery could not be retrieved for logon.  User:" + profileName + " Gallery:" + galleryName;
				meLogger.warn(message);
				utilityService.AddActionSecurityGallery("GalInval", message, requestLogon, request, customSession);
				return GalleryLoginFailReturn(customSession);
			}

			if (galleryLogon.getAccessType() != 1)
			{
				message = "Gallery logon, access type is incorrect, only those which require password access can be validated here.  User:" + profileName + " Gallery:" + galleryName;
				meLogger.warn(message);
				utilityService.AddActionSecurityGallery("GalInval", message, requestLogon, request, customSession);
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
				utilityService.AddActionSecurityGallery("GalLogOK", galleryName, requestLogon, request, customSession);
				return true;
			}
			else
			{
				utilityService.AddActionSecurityGallery("GalLogKO", galleryName, requestLogon, request, customSession);
				return GalleryLoginFailReturn(customSession);
			}
		}
		catch (Exception ex) {
			meLogger.error(ex);
			utilityService.AddActionSecurityGallery("GallErr", galleryName, requestLogon, request, customSession);
			return false;
		}
		finally { utilityService.LogMethod("GalleryService","LoginGalleryUser", startMS, requestId, galleryName); }
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
	
	public String GetGalleryUserLogonToken(String profileName, String galleryName, HttpServletRequest request, CustomSessionState customSession, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		String message = "";
		
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
			    	message = "Subsequent gallery user logon token request too soon after previous failure. (session)";
			    	meLogger.warn(message);
			    	utilityService.AddActionSecurityGallery("GalLogSoon", message, null, request, customSession);
			    	customResponse.setResponseCode(HttpStatus.FORBIDDEN.value());
			    	return "";
			    }
			}
			
			GalleryLogon galleryLogon = galleryDataHelper.GetGalleryLogonDetail(profileName, galleryName, "", requestId);
			if (galleryLogon == null)
			{
				message = "Gallery could not be retrieved for logon.  User:" + profileName + " Gallery:" + galleryName;
				meLogger.warn(message);
				utilityService.AddActionSecurityGallery("GalInval", message, null, request, customSession);
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return "";
			}
			
			if (galleryLogon.getAccessType() != 1)
			{
				//This gallery is not setup for login access.
				message = "This gallery is not setup for login access.  AccessType:" + galleryLogon.getAccessType();
		    	meLogger.warn(message);
		    	utilityService.AddActionSecurityGallery("GalInval", message, null, request, customSession);
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
			
			utilityService.AddActionSecurityGallery("GalToken", newKey, null, request, customSession);
			
			return newKey;
		}
		catch (Exception ex) {
			utilityService.AddActionSecurityGallery("GalErr", galleryName, null, request, customSession);
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return "";
		}
		finally { utilityService.LogMethod("GalleryService","GetGalleryUserLogonToken", startMS, requestId, galleryName); }
	}
	
	public int GetGalleryAccessType(String profileName, String galleryName, CustomResponse customResponse, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			GalleryLogon galleryLogon = galleryDataHelper.GetGalleryLogonDetail(profileName, galleryName, "", requestId);
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
		finally { utilityService.LogMethod("GalleryService","GetGalleryAccessType", startMS, requestId, galleryName); }
	}
	
	//*************************************************************************************************************
	//*************************************  Messaging initiated methods ******************************************
	//*************************************************************************************************************

	public void RefreshGalleryImages(long userId, long galleryId, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			galleryDataHelper.RegenerateGalleryImages(userId, galleryId, requestId);
		}
		catch (WallaException wallaEx) {
			meLogger.error("RefreshGalleryImages failed with an error");
		}
		catch (Exception ex) {
			meLogger.error("RefreshGalleryImages failed with an error", ex);
		}
		finally {utilityService.LogMethod("GalleryService","RefreshGalleryImages", startMS, requestId, String.valueOf(galleryId));}
	}
	
}
