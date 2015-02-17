package walla.datatypes.web;

import java.util.Calendar;
import java.util.Date;

import javax.validation.constraints.*;

public class AccountSettings {

	/*
		  <element name="Account">
		    <complexType>
		      <sequence maxOccurs="1" minOccurs="0">
		        <element type="string" name="ProfileName"/>
		        <element type="string" name="Desc"/>
		        <element type="string" name="Country"/>
		        <element type="string" name="Timezone"/>
		        <element type="boolean" name="Newsletter"/>
		        <element type="string" name="Password"/>
		        <element type="int" name="Status" /> 
		        <!-- 		
			        1 - initial details setup
					2 - live (email and banking done)
					3 - shutdown pending
					4 - closed
				-->
				
				<element type="int" name="AccountType" />
				<element type="string" name="AccountTypeName" />
		        <element type="date" name="OpenDate" />
		        <element type="date" name="CloseDate" />
		        <element type="string" name="Key" />
		
		        <element name="Emails">
		          <complexType>
		            <sequence>
		              <element name="EmailRef" maxOccurs="unbounded" minOccurs="0">
		              <complexType>
		              	<sequence maxOccurs="1" minOccurs="0">
							<element type="string" name="Address" />
							<element type="boolean" name="Principle" />
							<element type="boolean" name="Secondary" />
							<element type="boolean" name="Verified" />
						</sequence>
		              </complexType>
		              </element>
		            </sequence>
		          </complexType>
		        </element>
		        
		        <element name="StorageSummary">
		          <complexType>
		            <sequence>
					  <element type="double" name="StorageGBLimit" />
					  <element type="int" name="MonthlyUploadCap" />
					  <element type="int" name="UploadCount30Days" />
		              <element type="double" name="SizeGB" />
		              <element type="double" name="CompressedSizeGB" />
		              <element type="int" name="ImageCount" />
		              <element name="FormatRef" maxOccurs="unbounded" minOccurs="0">
			              <complexType>
			              	<sequence maxOccurs="1" minOccurs="0">
		                      <element type="string" name="Format" />
		                      <element type="double" name="SizeGB"/>
		                      <element type="double" name="CompressedSizeGB"/>
		                      <element type="int" name="ImageCount" />
		                    </sequence>
			              </complexType>
		              </element>
		              <element name="UploadSourceRef" maxOccurs="unbounded" minOccurs="0">
			              <complexType>
			              	<sequence maxOccurs="1" minOccurs="0">
		                      <element type="string" name="Name" />
		                      <element type="double" name="SizeGB"/>
		                      <element type="double" name="CompressedSizeGB"/>
		                      <element type="int" name="ImageCount" />
		                    </sequence>
			              </complexType>
		              </element>
		              <element name="ImageYearRef" maxOccurs="unbounded" minOccurs="0">
			              <complexType>
			              	<sequence maxOccurs="1" minOccurs="0">
		                      <element type="string" name="Year" />
		                      <element type="double" name="SizeGB"/>
		                      <element type="double" name="CompressedSizeGB"/>
		                      <element type="int" name="ImageCount" />
		                    </sequence>
			              </complexType>
		              </element>
		            </sequence>
		          </complexType>
		        </element>
		
		        
		        
		       </sequence>
		      <attribute type="long" name="id" default="0"/>
		      <attribute type="int" name="version" default="0"/>
		    </complexType>
		  </element>
		</schema>
	*/
	@Size(min=5, max=30)
	private String profileName = "";
	
	@Size(min=0, max=200)
	private String description = "";
	
	@Size(min=0, max=50)
	private String country = "";

	private String timezone = "";
	
	private boolean newsletter;
	
	private String accountMessage = null;
	
	private String accountTypeName;
	private Date openDate = null;

	private long id;
	private int version;
	
	private String currentPassword = "";
	
	public String getProfileName()
	{ return this.profileName; }
	
	public void setProfileName(String value)
	{ this.profileName = value; }
	
	public String getDescription()
	{ return this.description; }
	
	public void setDescription(String value)
	{ this.description = value; }
	
	public String getCountry()
	{ return this.country; }
	
	public void setCountry(String value)
	{ this.country = value; }
	
	public String getTimezone()
	{ return this.timezone; }
	
	public void setTimezone(String value)
	{ this.timezone = value; }
	
	public boolean isNewsletter()
	{ return this.newsletter; }
	
	public void setNewsletter(Boolean value)
	{ this.newsletter = value; }

	public String getAccountMessage()
	{ return this.accountMessage; }
	
	public void setAccountMessage(String value)
	{ this.accountMessage = value; }
	
	public String getAccountTypeName()
	{ return this.accountTypeName; }
	
	public void setAccountTypeName(String value)
	{ this.accountTypeName = value; }
	
	public Date getOpenDate()
	{ return this.openDate; }
	
	public void setOpenDate(Date value)
	{ this.openDate = value; }

	public long getId()
	{ return this.id; }
	
	public void setId(long value)
	{ this.id = value; }
	
	public int getVersion()
	{ return this.version; }
	
	public void setVersion(int value)
	{ this.version = value; }
	
	public String getCurrentPassword()
	{ return this.currentPassword; }
	
	public void setCurrentPassword(String value)
	{ this.currentPassword = value; }
}
