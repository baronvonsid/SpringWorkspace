package walla.datatypes.java;

import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement; 
import javax.xml.bind.annotation.XmlAttribute;

import walla.datatypes.auto.Gallery;

@XmlRootElement(name = "LogMethodDetail")
public class LogMethodDetail {

	private String logMethodType; 
	private String session = "";
	private String requestId = "";
	private long userId = -1;
	private long duration = 0;
	
	private Date startDate = null;
	private String object = "";
	private String method = "";
	private String requestPath = "";
	private String params = "";
	private String response = "";
	
	/**************************************/
	
	public void setLogMethodType(String value) { this.logMethodType = value; }
	
	@XmlAttribute(name = "LogMethodType") 
	public String getLogMethodType() { return this.logMethodType; }
	
	
	public void setSession(String value) { this.session = value; }
	
	@XmlAttribute(name = "Session") 
	public String getSession() { return this.session; }
	
	
	public void setRequestId(String value) { this.requestId = value; }
	
	@XmlAttribute(name = "RequestId") 
	public String getRequestId() { return this.requestId; }	
	
	
	@XmlElement(name = "UserId") 
	public long getUserId() { return this.userId; }

	public void setUserId(long value) { this.userId = value; }
	
	
	@XmlElement(name = "Duration") 
	public long getDuration() { return this.duration; }

	public void setDuration(long value) { this.duration = value; }

	
	public void setObject(String value) { this.object = value; }
	
	@XmlElement(name = "Object") 
	public String getObject() { return this.object; }
	
	
	public void setMethod(String value) { this.method = value; }
	
	@XmlElement(name = "Method") 
	public String getMethod() { return this.method; }	
	
	
	public void setStartDate(Date value) { this.startDate = value; }
	
	@XmlElement(name = "StartDate") 
	public Date getStartDate() { return this.startDate; }

	
	public void setRequestPath(String value) { this.requestPath = value; }
	
	@XmlElement(name = "RequestPath") 
	public String getRequestPath() { return this.requestPath; }	
	
	
	public void setParams(String value) { this.params = value; }
	
	@XmlElement(name = "Params") 
	public String getParams() { return this.params; }	
	
	
	public void setResponse(String value) { this.response = value; }
	
	@XmlElement(name = "Response") 
	public String getResponse() { return this.response; }	
}
