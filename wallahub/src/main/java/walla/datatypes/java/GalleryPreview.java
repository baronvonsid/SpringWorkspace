package walla.datatypes.java;

import java.util.Date;

import walla.datatypes.auto.Gallery;

public class GalleryPreview {
	
	private String key;
	private String profileName;
	private Gallery gallery;
	private Date lastUpdated;
	
	public GalleryPreview() {}
	

	public String getKey()
	{
		return this.key;
	}
	
	public void setKey(String value)
	{
		this.key = value;
	}
	
	public String getProfileName()
	{
		return this.profileName;
	}
	
	public void setProfileName(String value)
	{
		this.profileName = value;
	}
	
	public Gallery getGallery()
	{
		return this.gallery;
	}
	
	public void setGallery(Gallery value)
	{
		this.gallery = value;
	}
	
	public void setLastUpdated(Date value)
	{
		this.lastUpdated = value;
	}
	
	public Date getLastUpdated()
	{
		return this.lastUpdated;
	}
	
	
}
