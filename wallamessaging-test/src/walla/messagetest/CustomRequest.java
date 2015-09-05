package walla.messagetest;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement; 
import javax.xml.bind.annotation.XmlAttribute;

@XmlRootElement(name = "WallaRequest")
public class CustomRequest {

	private String process;
	private long userId;
	private long entityId;
	
	public void setProcess(String process)
	{
		this.process = process;
	}
	
	@XmlAttribute(name = "process")
	public String getProcess()
	{
		return this.process;
	}
	
	public void setUserId(long userId)
	{
		this.userId = userId;
	}
	
	@XmlElement(name = "UserId")
	public long getUserId()
	{
		return this.userId;
	}
	
	public void setEntity(long entityId)
	{
		this.entityId = entityId;
	}
	
	@XmlElement(name = "EntityId")
	public long getEntityId()
	{
		return this.entityId;
	}
}
