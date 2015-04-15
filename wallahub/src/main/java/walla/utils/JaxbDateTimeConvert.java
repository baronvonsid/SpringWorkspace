package walla.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

public class JaxbDateTimeConvert {

    public static Calendar parseDateTime(String input) {
        //DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    	Calendar cal = DatatypeConverter.parseDateTime(input);
    	return cal;

    }
	
    // crazy hack because the 'Z' formatter produces an output incompatible with the xsd:dateTime
    public static String printDateTime(Calendar input) {
    	
    	return DatatypeConverter.printDateTime(input);
        //DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        //DateFormat tzFormatter = new SimpleDateFormat("Z");
        //String timezone = tzFormatter.format(dt);
        //return formatter.format(dt) + timezone.substring(0, 3) + ":"
         //       + timezone.substring(3);
    }
}
