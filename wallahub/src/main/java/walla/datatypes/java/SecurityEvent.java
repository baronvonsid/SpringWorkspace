package walla.datatypes.java;

import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement; 
import javax.xml.bind.annotation.XmlAttribute;

import walla.datatypes.auto.Gallery;

@XmlRootElement(name = "SecurityEvent")
public class SecurityEvent {

	private String action = "";
	private String extraInfo = "";
	private Date actionDate = null;
	
	private String objectProfileName = "";
	private String objectEmail = "";
	private String objectKey = "";
	private String objectGalleryName = "";
	
	private String requestCookie = "";
	private String requestHeader = "";
	private String requestRemoteAddress = "";
	private String requestLocalAddress = "";
	private String requestMethod = "";
	private String requestRemoteHost = "";
	private String requestRequestURL = "";
	
	private String sessionRemoteAddress = "";
	private int sessionFailedLogonCount = -1;
	private Date sessionFailedLogonLast = null;
	private String sessionProfileName = "";
	private long sessionUserId = -1;
	private long sessionUserAppId = -1;
	private int sessionPlatformId = -1;
	private int sessionAppId = -1;
	private String sessionGalleryTempKey = "";
	private String sessionGalleryName = "";
	private String sessionCustomSessionIds = "";
	private String sessionNonceKey = "";
	
	/**************************************/
	
	public void setAction(String value) { this.action = value; }
	
	@XmlAttribute(name = "Action") 
	public String getAction() { return this.action; }
	
	public void setExtraInfo(String value) { this.extraInfo = value; }
	
	@XmlElement(name = "ExtraInfo") 
	public String getExtraInfo() { return this.extraInfo; }
	
	public void setActionDate(Date value) { this.actionDate = value; }
	
	@XmlElement(name = "ActionDate") 
	public Date getActionDate() { return this.actionDate; }
	
	/**************************************/
	
	public void setObjectProfileName(String value) { this.objectProfileName = value; }
	
	@XmlElement(name = "ObjectProfileName") 
	public String getObjectProfileName() { return this.objectProfileName; }
	
	public void setObjectEmail(String value) { this.objectEmail = value; }
	
	@XmlElement(name = "ObjectEmail") 
	public String getObjectEmail() { return this.objectEmail; }
	
	public void setObjectKey(String value) { this.objectKey = value; }
	
	@XmlElement(name = "ObjectKey") 
	public String getObjectKey() { return this.objectKey; }
	
	public void setObjectGalleryName(String value) { this.objectGalleryName = value; }
	
	@XmlElement(name = "ObjectGalleryName") 
	public String getObjectGalleryName() { return this.objectGalleryName; }
	
	/**************************************/
	
	public void setRequestCookie(String value) { this.requestCookie = value; }
	
	@XmlElement(name = "RequestCookie") 
	public String getRequestCookie() { return this.requestCookie; }
	
	public void setRequestHeader(String value) { this.requestHeader = value; }
	
	@XmlElement(name = "RequestHeader") 
	public String getRequestHeader() { return this.requestHeader; }
	
	public void setRequestRemoteAddress(String value) { this.requestRemoteAddress = value; }
	
	@XmlElement(name = "RequestRemoteAddress") 
	public String getRequestRemoteAddress() { return this.requestRemoteAddress; }
	
	public void setRequestLocalAddress(String value) { this.requestLocalAddress = value; }
	
	@XmlElement(name = "RequestLocalAddress") 
	public String getRequestLocalAddress() { return this.requestLocalAddress; }
	
	public void setRequestMethod(String value) { this.requestMethod = value; }
	
	@XmlElement(name = "RequestMethod") 
	public String getRequestMethod() { return this.requestMethod; }
	
	public void setRequestRemoteHost(String value) { this.requestRemoteHost = value; }
	
	@XmlElement(name = "RequestRemoteHost") 
	public String getRequestRemoteHost() { return this.requestRemoteHost; }
	
	public void setRequestRequestURL(String value) { this.requestRequestURL = value; }
	
	@XmlElement(name = "RequestRequestURL") 
	public String getRequestRequestURL() { return this.requestRequestURL; }
	
	/**************************************/

	public void setSessionRemoteAddress(String value) { this.sessionRemoteAddress = value; }
	
	@XmlElement(name = "SessionRemoteAddress") 
	public String getSessionRemoteAddress() { return this.sessionRemoteAddress; }
	
	public void setSessionFailedLogonCount(int value) { this.sessionFailedLogonCount = value; }
	
	@XmlElement(name = "SessionFailedLogonCount") 
	public int getSessionFailedLogonCount() { return this.sessionFailedLogonCount; }
	
	public void setSessionFailedLogonLast(Date value) { this.sessionFailedLogonLast = value; }
	
	@XmlElement(name = "SessionFailedLogonLast") 
	public Date getSessionFailedLogonLast() { return this.sessionFailedLogonLast; }
	
	public void setSessionProfileName(String value) { this.sessionProfileName = value; }
	
	@XmlElement(name = "SessionProfileName") 
	public String getSessionProfileName() { return this.sessionProfileName; }
	
	public void setSessionUserId(long value) { this.sessionUserId = value; }
	
	@XmlElement(name = "SessionUserId") 
	public long getSessionUserId() { return this.sessionUserId; }
	
	public void setSessionUserAppId(long value) { this.sessionUserAppId = value; }
	
	@XmlElement(name = "SessionUserAppId") 
	public long getSessionUserAppId() { return this.sessionUserAppId; }
	
	public void setSessionPlatformId(int value) { this.sessionPlatformId = value; }
	
	@XmlElement(name = "SessionPlatformId") 
	public int getSessionPlatformId() { return this.sessionPlatformId; }
	
	public void setSessionAppId(int value) { this.sessionAppId = value; }
	
	@XmlElement(name = "SessionAppId") 
	public int getSessionAppId() { return this.sessionAppId; }
	
	public void setSessionGalleryTempKey(String value) { this.sessionGalleryTempKey = value; }
	
	@XmlElement(name = "SessionGalleryTempKey") 
	public String getSessionGalleryTempKey() { return this.sessionGalleryTempKey; }
	
	public void setSessionGalleryName(String value) { this.sessionGalleryName = value; }
	
	@XmlElement(name = "SessionGalleryName") 
	public String getSessionGalleryName() { return this.sessionGalleryName; }
	
	public void setSessionCustomSessionIds(String value) { this.sessionCustomSessionIds = value; }
	
	@XmlElement(name = "SessionCustomSessionIds") 
	public String getSessionCustomSessionIds() { return this.sessionCustomSessionIds; }
	
	public void setSessionNonceKey(String value) { this.sessionNonceKey = value; }
	
	@XmlElement(name = "SessionNonceKey") 
	public String getSessionNonceKey() { return this.sessionNonceKey; }


}
