package walla.business;

import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.db.*;
import walla.utils.*;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

@Service("UtilityService")
public class UtilityService {

	@Value( "${messaging.enabled}" ) private boolean messagingEnabled;
	
	@Resource(name="utilityDataHelper") private UtilityDataHelperImpl utilityDataHelper;
	
	@Resource(name="cachedData") private CachedData cachedData;
	
	private static final Logger meLogger = Logger.getLogger(UtilityService.class);

	//*************************************************************************************************************
	//***********************************  Web server synchronous methods *****************************************
	//*************************************************************************************************************

	public UtilityService()
	{
		meLogger.debug("UtilityService object instantiated.");
	}
	
	
	
	
	public void AddAction(ActionType actionType, long id, String action, String extraInfo)
	{
		try
		{
			UserEvent event = new UserEvent();
			event.setActionType(actionType.name());
			event.setAction(action);
			event.setId(id);
			event.setExtraInfo((extraInfo.length() > 200) ? extraInfo.substring(0,200) : extraInfo);
			event.setActionDate(new Date());

			if (!messagingEnabled)
			{
				utilityDataHelper.AddAction(event);
			}
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when adding an action");
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when adding an action", ex);
		}
	}
	
	public void AddActionSecurityAccount(String action, String extraInfo, Logon logon, HttpServletRequest request, CustomSessionState customSession)
	{
		String email = "";
		String profileName = "";
		String key = "";
		
		if (logon != null)
		{
			email  = (logon.getEmail() == null) ? "" : logon.getEmail(); 
			profileName = (logon.getProfileName() == null) ? "" : logon.getProfileName();
			key = (logon.getKey() == null) ? "" : logon.getKey();
		}

		AddActionSecurity(action, extraInfo, profileName, email, key, "", request, customSession);
	}
	
	public void AddActionSecurityGallery(String action, String extraInfo, GalleryLogon logon, HttpServletRequest request, CustomSessionState customSession)
	{
		String galleryName = "";
		String profileName = "";
		String key = "";
		
		if (logon != null)
		{
			galleryName  = (logon.getGalleryName() == null) ? "" : logon.getGalleryName(); 
			profileName = (logon.getProfileName() == null) ? "" : logon.getProfileName();
			key = (logon.getKey() == null) ? "" : logon.getKey(); 
		}
		
		AddActionSecurity(action, extraInfo, profileName, "", key, galleryName, request, customSession);
	}
	
	private void AddActionSecurity(String action, 
			String extraInfo,
			String objectProfileName,
			String objectEmail,
			String objectKey,
			String objectGalleryName,
			HttpServletRequest request, 
			CustomSessionState customSession)
	{
		try
		{
			SecurityEvent event = new SecurityEvent();
			
			event.setAction(action);
			event.setExtraInfo((extraInfo.length() > 200) ? extraInfo.substring(0,200) : extraInfo);
			event.setActionDate(new Date());
			
			event.setObjectProfileName(objectProfileName);
			event.setObjectEmail(objectEmail);
			event.setObjectKey(objectKey);
			event.setObjectGalleryName(objectGalleryName);
			
			String cookie = "";
			if (request.getCookies() != null)
			{
				StringBuilder cookieString = new StringBuilder();
				for (int i = 0; i < request.getCookies().length; i++)
				{
					Cookie current = request.getCookies()[i];
					cookieString.append(current.getName() + ":" + current.getValue() + "||");
				}
				cookie = (cookieString.length() > 500) ? cookieString.substring(0,500) : cookieString.toString();
			}
			
			String header = "";
			if (request.getHeaderNames() != null)
			{
				StringBuilder headerString = new StringBuilder();
		        Enumeration headerNames = request.getHeaderNames();
		        while (headerNames.hasMoreElements()) 
		        {
		            String headerName = (String)headerNames.nextElement();
		            headerString.append(headerName + ":" + request.getHeader(headerName) + "||");
		        }
		        header = (headerString.length() > 500) ? headerString.substring(0,500) : headerString.toString();
			}
			
			event.setRequestCookie(cookie);
			event.setRequestHeader(header);
			event.setRequestRemoteAddress((request.getRemoteAddr() != null) ? request.getRemoteAddr() : "");
			event.setRequestLocalAddress((request.getLocalAddr() != null) ? request.getLocalAddr() : "");
			event.setRequestMethod((request.getMethod() != null) ? request.getMethod() : "");
			event.setRequestRemoteHost((request.getRemoteHost() != null) ? request.getRemoteHost() : "");
			
			String requestUrl = (request.getRequestURL() != null) ? request.getRequestURL().toString() : "";
			event.setRequestRequestURL((requestUrl.length() > 200) ? requestUrl.substring(0,200) : requestUrl);
			
			event.setSessionRemoteAddress(customSession.getRemoteAddress());
			event.setSessionFailedLogonCount(customSession.getFailedLogonCount());
			event.setSessionFailedLogonLast(customSession.getFailedLogonLast());
			event.setSessionProfileName(customSession.getProfileName());
			event.setSessionUserId(customSession.getUserId());
			event.setSessionUserAppId(customSession.getUserAppId());
			event.setSessionPlatformId(customSession.getPlatformId());
			event.setSessionAppId(customSession.getAppId());
			event.setSessionGalleryTempKey(customSession.getGalleryTempKey());
			event.setSessionGalleryName(customSession.getGalleryName());
			
			StringBuilder sessionIdString = new StringBuilder();
			for (int i = 0; i < customSession.getCustomSessionIds().size(); i++)
				sessionIdString.append(customSession.getCustomSessionIds().get(i) + "||");
			
			String sessionIds = (sessionIdString.length() > 200) ? sessionIdString.substring(0,200) : sessionIdString.toString();
			
			event.setSessionCustomSessionIds(sessionIds);
			event.setSessionNonceKey(customSession.getNonceKey());
			
			if (!messagingEnabled)
			{
				utilityDataHelper.AddSecurityAction(event);
			}
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when adding an action");
		}
		catch (Exception ex) {
			meLogger.error("AddActionSecurity failed with an error", ex);
		}

	}
	

}
