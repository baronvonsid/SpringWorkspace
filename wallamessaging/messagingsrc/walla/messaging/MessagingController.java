package walla.messaging;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;














import javax.annotation.Resource;
//import javax.jms.Message;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
//import org.springframework.integration.message.GenericMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.message.*;
import org.springframework.integration.annotation.Headers;
import org.springframework.integration.annotation.Payload;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import walla.business.AccountService;
import walla.business.CategoryService;
import walla.business.GalleryService;
import walla.business.ImageService;
import walla.business.TagService;
import walla.business.UtilityService;
import walla.datatypes.auto.RequestMessage;
import walla.datatypes.java.LogMethodDetail;
import walla.datatypes.java.SecurityEvent;
import walla.datatypes.java.UserEvent;
import walla.db.UtilityDataHelperImpl;
import walla.utils.UserTools;

//import walla.messagetest.CustomRequest;

public class MessagingController {

	private static final Logger meLogger = Logger.getLogger(MessagingController.class);

	@Resource(name="utilityDataHelper") 
	private UtilityDataHelperImpl utilityDataHelper;
	
	@Resource(name="accountServicePooled")
	private AccountService accountService;

	@Resource(name="categoryServicePooled")
	private CategoryService categoryService;
	
	@Resource(name="galleryServicePooled")
	private GalleryService galleryService;
	
	@Resource(name="imageServicePooled")
	private ImageService imageService;
	
	@Resource(name="tagServicePooled")
	private TagService tagService;
	
	@Resource(name="utilityServicePooled")
	private UtilityService utilityService;
	
	@ServiceActivator
	public void processLogMethodMessage(String message)
	{
		try 
		{
			meLogger.debug("processLogMethodMessage method called");
			meLogger.debug(message);
			
			LogMethodDetail detail = (LogMethodDetail)UserTools.XmlToObject(message, LogMethodDetail.class);
			utilityDataHelper.LogMethodCall(detail);
		} 
		catch (Exception ex) {meLogger.error(ex);}
	}
	
	@ServiceActivator
	public void processLogUserEventMessage(String message)
	{
		try
		{
			meLogger.debug("processLogUserEventMessage method called");
			meLogger.debug(message);
			
			UserEvent event = (UserEvent)UserTools.XmlToObject(message, UserEvent.class);
			utilityDataHelper.AddAction(event);
		}
		catch (Exception ex) {meLogger.error(ex);}
	}
	
	@ServiceActivator
	public void processLogSecEventMessage(String message)
	{
		try
		{
			meLogger.debug("processLogSecEventMessage method called");
			meLogger.debug(message);
			
			SecurityEvent event = (SecurityEvent)UserTools.XmlToObject(message, SecurityEvent.class);
			utilityDataHelper.AddSecurityAction(event);
		}
		catch (Exception ex) {meLogger.error(ex);}
	}
	
	@ServiceActivator
	public void processRequestMessage(String message)
	{
		try
		{
			meLogger.debug("processRequestMessage method called");
			meLogger.debug(message);
			
			RequestMessage request = (RequestMessage)UserTools.XmlToObject(message, RequestMessage.class);
			
			if (request.getWallaClass().equals("AccountService") && request.getMethod().equals("CheckUpdateAccountStatus"))
			{
				accountService.CheckUpdateAccountStatus(request.getUserId(), request.getRequestId());
			}
			else if (request.getWallaClass().equals("CategoryService") && request.getMethod().equals("CategoryRippleUpdate"))
			{
				categoryService.CategoryRippleUpdate(request.getUserId(), request.getIdOne(), request.getRequestId());
			}
			else if (request.getWallaClass().equals("CategoryService") && request.getMethod().equals("CategoryRippleDelete"))
			{
				long[] imageList = ArrayUtils.toPrimitive(request.getIdList().getIdRef().toArray(new Long[request.getIdList().getIdRef().size()]));
				categoryService.CategoryRippleDelete(request.getUserId(), imageList, request.getRequestId());
			}
			else if (request.getWallaClass().equals("GalleryService") && request.getMethod().equals("RefreshGalleryImages"))
			{
				galleryService.RefreshGalleryImages(request.getUserId(), request.getIdOne(), request.getRequestId());
			}
			else if (request.getWallaClass().equals("ImageService") && request.getMethod().equals("SetupNewImage"))
			{
				imageService.SetupNewImage(request.getUserId(), request.getIdOne(), request.getIdTwo(), request.getRequestId());
			}
			else if (request.getWallaClass().equals("ImageService") && request.getMethod().equals("ImageDeletePermanent"))
			{
				long[] imageList = ArrayUtils.toPrimitive(request.getIdList().getIdRef().toArray(new Long[request.getIdList().getIdRef().size()]));
				imageService.ImageDeletePermanent(request.getUserId(), imageList, request.getRequestId());
			}
			else if (request.getWallaClass().equals("TagService") && request.getMethod().equals("TagRippleUpdate"))
			{
				tagService.TagRippleUpdate(request.getUserId(), request.getIdOne(), request.getRequestId());
			}
			else if (request.getWallaClass().equals("TagService") && request.getMethod().equals("TagRippleDelete"))
			{
				tagService.TagRippleDelete(request.getUserId(), request.getIdOne(), request.getRequestId());
			}
			else if (request.getWallaClass().equals("TagService") && request.getMethod().equals("ReGenDynamicTags"))
			{
				tagService.ReGenDynamicTags(request.getUserId(), request.getRequestId());
			}
			else
			{
				throw new Exception("Unknown class and method to process.  Message: " + message);
			}
			
			meLogger.debug("ProcessRequestMessage method completed.  Class: " + request.getWallaClass() + " Method: " + request.getMethod());			
		}
		catch (Exception ex) {meLogger.error(ex);}
	}
	
	@ServiceActivator
	public void processEmailMessage(String message)
	{
		try
		{
			meLogger.debug("processEmailMessage method called, not implemented");
			meLogger.debug(message);
			meLogger.debug(message);
		}
		catch (Exception ex) {meLogger.error(ex);}
	}
	
    @ServiceActivator
    public void processAggMessages(ArrayList<String> messages) 
    {
    	try
    	{
    		meLogger.debug("processAggMessages method with: " + messages.size() + " messages");
	    	if (messages.size() > 0)
	    	{
	    		processRequestMessage(messages.get(0));
	    		if (messages.size() > 1)
	    		{
	    			//TODO log relationship to processed message.
	    		}
	    	}
    	}
		catch (Exception ex) {meLogger.error(ex);}
    }
    

    


}












/*
@ServiceActivator
public GenericMessage RequestTriage2(List<GenericMessage> inboundMessage) {

	int ffff = 0;
	
	meLogger.info("RequestTriage2");
	
	//GenericMessage<CustomRequest> message = (GenericMessage)MessageBuilder.fromMessage(inboundMessage).setHeader("success", response).build();

	return null;
}
*/

/*
public void setSession(CrdSessionWrapper session) {
	this.sessionWrapper = session;
}
*/

/*
private CustomRequest GetObjectFromXml(String request)
{
	try
	{
        JAXBContext context = JAXBContext.newInstance(CustomRequest.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        //unmarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	
        StringReader st = new StringReader(request);
        CustomRequest customRequest = (CustomRequest)unmarshaller.unmarshal(st);
        //marshaller.marshal(request, st);
        
        return customRequest; 
	}
	catch (Exception ex)
	{
		meLogger.error(ex);
		return null;
	}
}


@ServiceActivator
public GenericMessage<CustomRequest> RequestTriage(GenericMessage<CustomRequest> inboundMessage) {

	// Switch process
	String response = "false";

	CustomRequest request = inboundMessage.getPayload();
	
	meLogger.debug("RequestTriage() has received a message with: EntityId-" + request.getEntityId() + " Process-" + request.getProcess());
	
	if (request.getProcess().equalsIgnoreCase("UpdateGallery")) 
	{
		response = "true";
	}
	else if (request.getProcess().equalsIgnoreCase("UpdateTag")) 
	{
		response = "true";
	}
	else
	{
		meLogger.error("RequestTriage() cannot process the message with: EntityId-" + request.getEntityId() + " ProcessName-" 
				+ request.getProcess() + " because there is no custom method defined for the process name.");
	}
	
	meLogger.info("The message with EntityId-" + request.getEntityId() + " ProcessName-" 
			+ request.getProcess() + " and was returned the state:" + response + " from the custom process method.");

	GenericMessage<CustomRequest> message = (GenericMessage)MessageBuilder.fromMessage(inboundMessage).setHeader("success", response).build();

	return message;
}
*/
