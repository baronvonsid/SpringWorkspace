package walla.datatypes.java;

public class Platform {

	private int platformId;
	private String operatingSystem;
	private String machineType;
	private String shortName;
	private int majorVersion;
	private int minorVersion;
	
	public Platform() {
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
	
	public void setOperatingSystem(String operatingSystem)
	{
		this.operatingSystem = operatingSystem;
	}
	
	public String getOperatingSystem()
	{
		return this.operatingSystem;
	}
	
	public void setMachineType(String machineType)
	{
		this.machineType = machineType;
	}
	
	public String getMachineType()
	{
		return this.machineType;
	}
	
	public void setShortName(String shortName)
	{
		this.shortName = shortName;
	}
	
	public String getShortName()
	{
		return this.shortName;
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
}
