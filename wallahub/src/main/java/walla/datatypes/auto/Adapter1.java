//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.12.04 at 06:54:52 PM GMT 
//


package walla.datatypes.auto;

import java.util.Calendar;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class Adapter1
    extends XmlAdapter<String, Calendar>
{


    public Calendar unmarshal(String value) {
        return (walla.utils.JaxbDateTimeConvert.parseDateTime(value));
    }

    public String marshal(Calendar value) {
        return (walla.utils.JaxbDateTimeConvert.printDateTime(value));
    }

}
