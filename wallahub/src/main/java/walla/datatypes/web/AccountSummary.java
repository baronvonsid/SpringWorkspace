package walla.datatypes.web;

import java.util.Calendar;
import javax.validation.constraints.*;

public class AccountSummary {

	/*
	  <element name="Account">
    <complexType>
      <sequence maxOccurs="1" minOccurs="0">
        <element type="string" name="ProfileName"/>
        <element type="string" name="Desc"/>
        <element type="string" name="Email"/>
        <element type="string" name="Password"/>
        <element type="int" name="Status" /> <!-- 1 - Initial details received, 2 - Email confirmed, 
        											3 - billing confirmed, 4 - billing issue
        											5 - live, 6 - shutdown pending, 7 closed. -->
        <element type="int" name="AccountType" />
        <element type="string" name="AccountTypeName" />
        <element type="date" name="OpenDate" />
        <element type="date" name="CloseDate" />
        <element type="double" name="StorageGBLimit" />
        <element type="double" name="StorageGBCurrent" />
        <element type="int" name="TotalImages" />
        <element type="int" name="MonthlyUploadCap" />
        <element type="int" name="UploadCount30Days" />
        <element type="string" name="Key" />
       </sequence>
      <attribute type="long" name="id" default="0"/>
      <attribute type="int" name="version" default="0"/>
	*/
	private String profileName = "";
	
	@Size(min=0, max=100)
	private String description = "";
	
	private String email = "";
	private String accountStatus = "";
	private String accountTypeName;
	private Calendar openDate = null;
	private Calendar closeDate = null;
	private int totalImages;
	private int monthlyUploadCap;
	private int uploadCount30Days;
	private long id;
	private int version;
	
	public String getProfileName()
	{ return this.profileName; }
	
	public void setProfileName(String value)
	{ this.profileName = value; }
	
	public String getDescription()
	{ return this.description; }
	
	public void setDescription(String value)
	{ this.description = value; }
	
	public String getEmail()
	{ return this.email; }
	
	public void setEmail(String value)
	{ this.email = value; }
	
	public String getAccountStatus()
	{ return this.accountStatus; }
	
	public void setAccountStatus(String value)
	{ this.accountStatus = value; }
	
	public String getAccountTypeName()
	{ return this.accountTypeName; }
	
	public void setAccountTypeName(String value)
	{ this.accountTypeName = value; }
	
	public Calendar getOpenDate()
	{ return this.openDate; }
	
	public void setOpenDate(Calendar value)
	{ this.openDate = value; }
	
	public Calendar getCloseDate()
	{ return this.closeDate; }
	
	public void setCloseDate(Calendar value)
	{ this.closeDate = value; }
	
	public int getTotalImages()
	{ return this.totalImages; }
	
	public void setTotalImages(int value)
	{ this.totalImages = value; }
	
	public int getMonthlyUploadCap()
	{ return this.monthlyUploadCap; }
	
	public void setMonthlyUploadCap(int value)
	{ this.monthlyUploadCap = value; }
	
	public int getUploadCount30Days()
	{ return this.uploadCount30Days; }
	
	public void setUploadCount30Days(int value)
	{ this.uploadCount30Days = value; }
	
	public long getId()
	{ return this.id; }
	
	public void setId(long value)
	{ this.id = value; }
	
	public int getVersion()
	{ return this.version; }
	
	public void setVersion(int value)
	{ this.version = value; }
}
