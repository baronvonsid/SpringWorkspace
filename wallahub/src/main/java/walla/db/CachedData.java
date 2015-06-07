package walla.db;

import walla.business.UtilityService;
import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.utils.UserTools;
import walla.utils.WallaException;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

@Repository
public class CachedData {

	private Date cacheUpdateTime = new Date();
	private List<Platform> platforms = null;
	private List<App> apps = null;
	private List<AppPlatform> appPlatforms = null;
	private List<Style> styles = null;
	private List<Presentation> presentations = null;
	private UtilityDataHelperImpl utilityDataHelper;
	private static final Logger meLogger = Logger.getLogger(CachedData.class);
	
	@Resource(name="utilityServicePooled")
	private UtilityService utilityService;
	
	public CachedData() {
		//Date cacheUpdateTime = new Date();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		cacheUpdateTime.setTime(cal.getTimeInMillis());
		
		if (meLogger.isDebugEnabled()) { meLogger.debug("CachedData object instantiated with the timestamp:" + cacheUpdateTime.toGMTString()); }
	}

	private synchronized void CheckAndUpdateCache(String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, -1);
	
			if (cacheUpdateTime.before(cal.getTime()))
			{
				//Cache is out of date, so retrieve the latest.
				platforms = utilityDataHelper.GetPlatformList(requestId);
				if (platforms == null || platforms.size() < 1)
					meLogger.error("No platforms could be retrieved from the database");
				
				apps = utilityDataHelper.GetAppList(requestId);
				if (apps == null || apps.size() < 1)
					meLogger.error("No apps could be retrieved from the database");
				
				appPlatforms = utilityDataHelper.GetAppPlatformList(requestId);
				if (appPlatforms == null || appPlatforms.size() < 1)
					meLogger.error("No app platforms could be retrieved from the database");
				
				styles = utilityDataHelper.GetStyleList(requestId);
				if (styles == null || styles.size() < 1)
					meLogger.error("No styles could be retrieved from the database");
				
				presentations = utilityDataHelper.GetPresentationList(requestId);
				if (presentations == null || presentations.size() < 1)
					meLogger.error("No presentations could be retrieved from the database");
				
				Calendar calNow = Calendar.getInstance();
				cacheUpdateTime.setTime(calNow.getTimeInMillis());
				
				if (meLogger.isDebugEnabled()) { meLogger.debug("Cache has now been refreshed.  New timestamp:" + cacheUpdateTime.toGMTString()); }
			}
		}
		finally { utilityService.LogMethod("CachedData","CheckAndUpdateCache", startMS, requestId, ""); }
	}
	
	public Platform GetPlatform(int platformId, String OSType, String machineType, int majorVersion, int minorVersion, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			CheckAndUpdateCache(requestId);
			
			//find platform object and return.
			for (Iterator<Platform> platformIterater = platforms.iterator(); platformIterater.hasNext();)
			{
				Platform currentPlatform = (Platform)platformIterater.next();
				
				if (platformId != 0)
				{
					//Specific lookup required.
					if (currentPlatform.getPlatformId() == platformId)
					{
						return currentPlatform;
					}
				}
				else
				{
					//Try to find based on logic.  Needs to be improved when additional apps loaded.
					if (machineType.equals(currentPlatform.getMachineType()) && majorVersion == currentPlatform.getMajorVersion() && minorVersion == currentPlatform.getMinorVersion())
					{
						return currentPlatform;
					}
				}
			}
			
			return null;
		}
		finally { utilityService.LogMethod("CachedData","GetPlatform", startMS, requestId, "PlatformId:" + platformId + " OSType:" + OSType + " Machine:" + machineType + " Version:" + majorVersion + "." + minorVersion); }
	}
	
	public App GetApp(int appId, String key, int majorVersion, int minorVersion, long crc, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			CheckAndUpdateCache(requestId);
			
			//find platform object and return.
			for (Iterator<App> appIterater = apps.iterator(); appIterater.hasNext();)
			{
				App currentApp = (App)appIterater.next();

				if (appId != 0)
				{
					if (currentApp.getAppId() == appId)
					{
						return currentApp;
					}
				}
				else
				{
					if (currentApp.getAppKey().equals(key) && currentApp.getMajorVersion() == majorVersion
							&& currentApp.getMinorVersion() == minorVersion && currentApp.getAppCRC() == crc)
					{
						return currentApp;
					}
				}
			}
			
			return null;
		}
		finally { utilityService.LogMethod("CachedData","GetApp", startMS, requestId, "AppId:" + appId + " Key:" + key); }
	}
	
	public boolean GetAppPlatformSupported(int appId, int platformId, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			CheckAndUpdateCache(requestId);
			
			//find platform object and return.
			for (Iterator<AppPlatform> iterater = appPlatforms.iterator(); iterater.hasNext();)
			{
				AppPlatform current = (AppPlatform)iterater.next();
				
				if (appId == current.getAppId() && platformId == current.getPlatformId())
					return true;
			}
			
			return false;
		}
		finally { utilityService.LogMethod("CachedData","GetAppPlatformSupported", startMS, requestId, "AppId:" + appId + " PlatformId:" + platformId); }
	}
	
	public List<Style> GetStyleList(String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			CheckAndUpdateCache(requestId);
			return styles;
		}
		finally { utilityService.LogMethod("CachedData","GetStyleList", startMS, requestId, ""); }
	}
	
	public Style GetStyle(int styleId, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			CheckAndUpdateCache(requestId);
			
			for (Iterator<Style> iterater = styles.iterator(); iterater.hasNext();)
			{
				Style current = (Style)iterater.next();

				if (current.getStyleId() == styleId)
				{
					return current;
				}
			}
			
			return null;
		}
		finally { utilityService.LogMethod("CachedData","GetApp", startMS, requestId, ""); }
	}
	
	public List<Presentation> GetPresentationList(String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			CheckAndUpdateCache(requestId);
			return presentations;
		}
		finally { utilityService.LogMethod("CachedData","GetPresentationList", startMS, requestId, ""); }
	}
	
	public Presentation GetPresentation(int presentationId, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			CheckAndUpdateCache(requestId);
			
			for (Iterator<Presentation> iterater = presentations.iterator(); iterater.hasNext();)
			{
				Presentation current = (Presentation)iterater.next();

				if (current.getPresentationId() == presentationId)
				{
					return current;
				}
			}
			
			return null;
		}
		finally { utilityService.LogMethod("CachedData","GetPresentation", startMS, requestId, String.valueOf(presentationId)); }
	}
	
	public void setUtilityDataHelper(UtilityDataHelperImpl utilityDataHelper)
	{
		this.utilityDataHelper = utilityDataHelper;
	}
	
}
