package walla.datatypes.java;

import java.util.Date;

public class Presentation {

	private int presentationId;
	private String name;
	private String desc;
	private String jspName;
	private String cssExtension;
	private int maxSections;
	private int maxImagesInSection;
	private int thumbWidth;
	private int thumbHeight;
	private boolean optionGalleryName;
	private boolean optionGalleryDesc;
	private boolean optionImageName;
	private boolean optionImageDesc;
	private boolean optionGroupingDesc;
	private Date lastUpdated;
	
	public Presentation() {
		// TODO Auto-generated constructor stub
	}
	
	public void setPresentationId(int presentationId)
	{
		this.presentationId = presentationId;
	}
	
	public int getPresentationId()
	{
		return this.presentationId;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setDesc(String desc)
	{
		this.desc = desc;
	}
	
	public String getDesc()
	{
		return this.desc;
	}
	
	public void setJspName(String jspName)
	{
		this.jspName = jspName;
	}
	
	public String getJspName()
	{
		return this.jspName;
	}
	
	public void setCssExtension(String cssExtension)
	{
		this.cssExtension = cssExtension;
	}
	
	public String getCssExtension()
	{
		return this.cssExtension;
	}
	
	public void setMaxSections(int maxSections)
	{
		this.maxSections = maxSections;
	}
	
	public int getMaxSections()
	{
		return this.maxSections;
	}
	
	public void setMaxImagesInSection(int maxImagesInSection)
	{
		this.maxImagesInSection = maxImagesInSection;
	}
	
	public int getMaxImagesInSection()
	{
		return this.maxImagesInSection;
	}
	
	public void setThumbWidth(int thumbWidth)
	{
		this.thumbWidth = thumbWidth;
	}
	
	public int getThumbWidth()
	{
		return this.thumbWidth;
	}
	
	public void setThumbHeight(int thumbHeight)
	{
		this.thumbHeight = thumbHeight;
	}
	
	public int getThumbHeight()
	{
		return this.thumbHeight;
	}

	public void setOptionGalleryName(boolean optionGalleryName)
	{
		this.optionGalleryName = optionGalleryName;
	}
	
	public boolean getOptionGalleryName()
	{
		return this.optionGalleryName;
	}
	
	public void setOptionGalleryDesc(boolean optionGalleryDesc)
	{
		this.optionGalleryDesc = optionGalleryDesc;
	}
	
	public boolean getOptionGalleryDesc()
	{
		return this.optionGalleryDesc;
	}
	
	public void setOptionImageName(boolean optionImageName)
	{
		this.optionImageName = optionImageName;
	}
	
	public boolean getOptionImageName()
	{
		return this.optionImageName;
	}

	public void setOptionImageDesc(boolean optionImageDesc)
	{
		this.optionImageDesc = optionImageDesc;
	}
	
	public boolean getOptionImageDesc()
	{
		return this.optionImageDesc;
	}
	
	public void setOptionGroupingDesc(boolean optionGroupingDesc)
	{
		this.optionGroupingDesc = optionGroupingDesc;
	}
	
	public boolean getOptionGroupingDesc()
	{
		return this.optionGroupingDesc;
	}
	
	public void setLastUpdated(Date lastUpdated)
	{
		this.lastUpdated = lastUpdated;
	}
	
	public Date getLastUpdated()
	{
		return this.lastUpdated;
	}
	
	
}
