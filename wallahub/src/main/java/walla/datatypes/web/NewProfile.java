package walla.datatypes.web;

import javax.validation.constraints.*;

public class NewProfile {

	public NewProfile()
	{
		String simon = "smon";
		
	}
	
	@NotNull
	@Size(min=5, max=30)
	private String profileName = "";
	
	@Size(min=0, max=100)
	private String description = "";
	
	@NotNull
	@Size(min=8, max=30)
	private String password = "";
	
	@NotNull
	@Size(min=6, max=100)
	private String email = "";
	
	@NotNull
	@Max(10)
	private int accountType;
	
	private String key = "";
	
	public String getProfileName()
	{
		return this.profileName;
	}
	
	public void setProfileName(String value)
	{
		this.profileName = value;
	}
	
	public String getDescription()
	{
		return this.description;
	}
	
	public void setDescription(String value)
	{
		this.description = value;
	}
	
	public String getPassword()
	{
		return this.password;
	}
	
	public void setPassword(String value)
	{
		this.password = value;
	}
	
	public String getEmail()
	{
		return this.email;
	}
	
	public void setEmail(String value)
	{
		this.email = value;
	}
	
	public int getAccountType()
	{
		return this.accountType;
	}
	
	public void setAccountType(int value)
	{
		this.accountType = value;
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
