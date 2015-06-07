package walla.datatypes.java;

public class AppPlatform {

	private int appId;
	private int platformId;
	
	public AppPlatform() {
		// TODO Auto-generated constructor stub
	}

	public void setPlatformId(int platformId)
	{
		this.platformId = platformId;
	}
	
	public int getPlatformId()
	{
		return this.platformId;
	}
	
	public void setAppId(int appId)
	{
		this.appId = appId;
	}
	
	public int getAppId()
	{
		return this.appId;
	}
}
