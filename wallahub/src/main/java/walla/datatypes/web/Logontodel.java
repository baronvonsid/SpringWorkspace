package walla.datatypes.web;

import javax.validation.constraints.*;

public class Logontodel {

	@Size(min=5, max=30)
	private String profileName = "";

	@Size(min=6, max=100)
	private String email = "";
	
	@Size(min=8, max=30)
	private String password = "";
	
	private boolean isAdmin;
	
	private String key = "";
	
	public String getProfileName()
	{
		return this.profileName;
	}
	
	public void setProfileName(String value)
	{
		this.profileName = value;
	}

	public String getEmail()
	{
		return this.email;
	}
	
	public void setEmail(String value)
	{
		this.email = value;
	}
	
	public String getPassword()
	{
		return this.password;
	}
	
	public void setPassword(String value)
	{
		this.password = value;
	}

	public boolean isAdmin()
	{
		return this.isAdmin;
	}
	
	public void setAdmin(boolean value)
	{
		this.isAdmin = value;
	}
	
	public String getKey()
	{
		return this.key;
	}
	
	public void setKey(String value)
	{
		this.key = value;
	}
}
