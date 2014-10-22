package walla.messaging;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jms.Message;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

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

//import walla.messagetest.CustomRequest;

public class CustomProcessController {

	private static final Logger meLogger = Logger.getLogger(CustomProcessController.class);

    @ServiceActivator
    public void processAggMessage(ArrayList<String> messages) 
    {
    	//@Headers Map<String, Object> headers, 
    	meLogger.info("processMessage called with: " + messages.size() + " messages");
    	if (messages.size() > 0)
    	{
    		CustomRequest request = GetObjectFromXml(messages.get(0));
    		meLogger.info(request.getProcess() + " " + request.getUserId() + request.getEntityId());
    	}
    }
	
    @ServiceActivator
    public void processMessage(String message) 
    {
		CustomRequest request = GetObjectFromXml(message);
		meLogger.info(request.getProcess() + " " + request.getUserId() + request.getEntityId());
    }
    
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

	
	@ServiceActivator
	public GenericMessage RequestTriage2(List<GenericMessage> inboundMessage) {

		int ffff = 0;
		
		meLogger.info("RequestTriage2");
		
		//GenericMessage<CustomRequest> message = (GenericMessage)MessageBuilder.fromMessage(inboundMessage).setHeader("success", response).build();

		return null;
	}
	
	/*
	public void setSession(CrdSessionWrapper session) {
		this.sessionWrapper = session;
	}
	*/
}
