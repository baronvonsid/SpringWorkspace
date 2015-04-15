package walla.datatypes.java;

import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement; 
import javax.xml.bind.annotation.XmlAttribute;

import walla.datatypes.auto.Gallery;

@XmlRootElement(name = "UserEvent")
public class UserEvent {

	private String actionType; 
	private String action = "";
	private long id = -1;
	private String extraInfo = "";
	private Date actionDate = null;
	
	
	/**************************************/
	
	public void setActionType(String value) { this.actionType = value; }
	
	@XmlAttribute(name = "ActionType") 
	public String getActionType() { return this.actionType; }
	
	public void setAction(String value) { this.action = value; }
	
	@XmlAttribute(name = "Action") 
	public String getAction() { return this.action; }
	
	@XmlElement(name = "Id") 
	public long getId() { return this.id; }

	public void setId(long value) { this.id = value; }
	
	public void setExtraInfo(String value) { this.extraInfo = value; }
	
	@XmlElement(name = "ExtraInfo") 
	public String getExtraInfo() { return this.extraInfo; }
	
	public void setActionDate(Date value) { this.actionDate = value; }
	
	@XmlElement(name = "ActionDate") 
	public Date getActionDate() { return this.actionDate; }

}
