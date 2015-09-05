package walla.messagetest;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import javax.swing.text.Document;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class JmsHelper {

	String url;
	String subject;
	Connection connection;
	Session session;
	Destination destination;
	
	private static final Logger meLogger = Logger.getLogger(JmsHelper.class);

	
	public JmsHelper(String brokerUrl, String queue) throws JMSException
	{
		url = brokerUrl;
		subject = queue;
	}
	
	public void CreateConn() throws JMSException
	{
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        connection = connectionFactory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        destination = session.createQueue(subject);
	}
	
	public void CloseConn() throws JMSException
	{
		connection.stop();
		session.close();
	}
	
	public void SendMultipleMessages(int numMessages, int msDelay, long userId, long entityId, String process, String agg) throws InterruptedException, JMSException
	{
		for (int i = 0; i < numMessages; i++)
		{
	        MessageProducer producer = session.createProducer(destination);
	        
	        CustomRequest request = new CustomRequest();
	        request.setEntity(entityId);
	        request.setProcess(process);
	        request.setUserId(userId);
	        
	        TextMessage message = session.createTextMessage(GetXml(request));
	        String corr = userId + entityId + process;
	        
	        message.setStringProperty("correlationId", corr);
	        message.setStringProperty("aggIt", agg);
	        
	        message.setJMSCorrelationID(corr);

	        producer.send(message);
	        
	        meLogger.info("Message has been sent.  Count:" + i);
	        
			Thread.sleep(msDelay);
		}
	}
	
	/*
	public void SendToTrading(long orderId) throws JMSException
	{
        MessageProducer producer = session.createProducer(destination);
        
        CustomRequest request = new CustomRequest();
        request.setOrderId(orderId);
        request.setProcess("SendToTrading");
        
        TextMessage message = session.createTextMessage(GetXml(request));
        producer.send(message);
	}
	*/
	
	private String GetXml(CustomRequest request)
	{
		try
		{
	        JAXBContext context = JAXBContext.newInstance(CustomRequest.class);
	        Marshaller marshaller = context.createMarshaller();
	        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
	        StringWriter st = new StringWriter(); 
	        marshaller.marshal(request, st);
	        
	        return st.toString(); 
		}
		catch (Exception ex)
		{
			meLogger.error(ex);
			return "";
		}
	}
	
}
