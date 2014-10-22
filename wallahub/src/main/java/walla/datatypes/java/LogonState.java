package walla.datatypes.java;

import java.util.Date;

public class LogonState {

	private long userId = -1;
	private String profileName = "";
	private int failedLogonCount = 0;
	private Date failedLogonLast = null;
	private String salt = "";
	private String passwordHash = "";
	
	public long getUserId()
	{
		return this.userId;
	}
	
	public void setUserId(long value)
	{
		this.userId = value;
	}
    
	public String getProfileName()
	{
		return this.profileName;
	}
	
	public void setProfileName(String value)
	{
		this.profileName = value;
	}
	
	public int getFailedLogonCount()
	{
		return this.failedLogonCount;
	}
	
	public void setFailedLogonCount(int value)
	{
		this.failedLogonCount = value;
	}

	public Date getFailedLogonLast()
	{
		return this.failedLogonLast;
	}
	
	public void setFailedLogonLast(Date value)
	{
		this.failedLogonLast = value;
	}
	
	public String getSalt()
	{
		return this.salt;
	}
	
	public void setSalt(String value)
	{
		this.salt = value;
	}
	
	public String getPasswordHash()
	{
		return this.passwordHash;
	}
	
	public void setPasswordHash(String value)
	{
		this.passwordHash = value;
	}
}
