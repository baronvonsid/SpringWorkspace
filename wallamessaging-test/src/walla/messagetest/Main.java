package walla.messagetest;

import javax.jms.*;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {

	private static final Logger meLogger = Logger.getLogger(Main.class);
	
	public static void main(String[] args) throws JMSException, InterruptedException {

		meLogger.info("TestMessageProducer has begun.");
		
		try
		{
	    	Properties prop = new Properties();
	    	try {
	    		prop.load(new FileInputStream("resources\\project.properties"));
	    	} catch (IOException ex) {
	    		ex.printStackTrace();
	        }
	
			JmsHelper helper = new JmsHelper(prop.getProperty("jms.broker.url"), prop.getProperty("jms.broker.queue"));
			helper.CreateConn();
			
			
			String methodName = args[0];
			long userid = Long.parseLong(args[1]);
			long entityId = Long.parseLong(args[2]);
			String agg = args[3];
			
			helper.SendMultipleMessages(
					Integer.parseInt(prop.getProperty("jms.nummessages")), 
					Integer.parseInt(prop.getProperty("jms.msdelay")),
					userid,entityId,methodName,agg);
			
			
			
			/*
			if (args.length == 1)
			{
				long orderId = Long.parseLong(args[0].toString());
				helper.SendToTrading(orderId);
			}
			else
			{
				
			}
			*/
			
			
			helper.CloseConn();
		
			meLogger.info("TestMessageProducer has completed.");
			
			System.exit(0);
		}
		catch (Exception ex)
		{
			meLogger.error(ex);
		}
	}
}
