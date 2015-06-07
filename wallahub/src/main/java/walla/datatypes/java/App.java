package walla.datatypes.java;

public class App {

	private int appId;
	private String name;
	private String appKey;
	private int majorVersion;
	private int minorVersion;
	private long appCRC;
	private int status;
	private String userMessage;
	private int defaultFetchSize;
	private int defaultThumbCacheMB;
	private int defaultMainCopyCacheMB;
	private int defaultGalleryType;
	
	public App() {
	}

	public void setAppId(int appId)
	{
		this.appId = appId;
	}
	
	public int getAppId()
	{
		return this.appId;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setAppKey(String appKey)
	{
		this.appKey = appKey;
	}
	
	public String getAppKey()
	{
		return this.appKey;
	}

	public void setMajorVersion(int majorVersion)
	{
		this.majorVersion = majorVersion;
	}
	
	public int getMajorVersion()
	{
		return this.majorVersion;
	}
	
	public void setMinorVersion(int minorVersion)
	{
		this.minorVersion = minorVersion;
	}
	
	public int getMinorVersion()
	{
		return this.minorVersion;
	}
	
	public void setAppCRC(long appCRC)
	{
		this.appCRC = appCRC;
	}
	
	public long getAppCRC()
	{
		return this.appCRC;
	}
	
	public void setStatus(int status)
	{
		this.status = status;
	}
	
	public int getStatus()
	{
		return this.status;
	}
	
	public void setUserMessage(String userMessage)
	{
		this.userMessage = userMessage;
	}
	
	public String getUserMessage()
	{
		return this.userMessage;
	}
	
	public void setDefaultFetchSize(int defaultFetchSize)
	{
		this.defaultFetchSize = defaultFetchSize;
	}
	
	public int getDefaultFetchSize()
	{
		return this.defaultFetchSize;
	}
	
	public void setDefaultThumbCacheMB(int defaultThumbCacheMB)
	{
		this.defaultThumbCacheMB = defaultThumbCacheMB;
	}
	
	public int getDefaultThumbCacheMB()
	{
		return this.defaultThumbCacheMB;
	}
	
	public void setDefaultMainCopyCacheMB(int defaultMainCopyCacheMB)
	{
		this.defaultMainCopyCacheMB = defaultMainCopyCacheMB;
	}
	
	public int getDefaultMainCopyCacheMB()
	{
		return this.defaultMainCopyCacheMB;
	}
	
	public void setDefaultGalleryType(int defaultGalleryType)
	{
		this.defaultGalleryType = defaultGalleryType;
	}
	
	public int getDefaultGalleryType()
	{
		return this.defaultGalleryType;
	}
	
}
