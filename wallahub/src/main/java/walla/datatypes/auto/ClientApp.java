//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.11.10 at 07:37:12 PM GMT 
//


package walla.datatypes.auto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="WSKey" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="OS" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="MachineType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Major" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Minor" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "wsKey",
    "os",
    "machineType",
    "major",
    "minor"
})
@XmlRootElement(name = "ClientApp", namespace = "http://ws.fotowalla.com/ClientApp")
public class ClientApp {

    @XmlElement(name = "WSKey", namespace = "http://ws.fotowalla.com/ClientApp", required = true)
    protected String wsKey;
    @XmlElement(name = "OS", namespace = "http://ws.fotowalla.com/ClientApp", required = true)
    protected String os;
    @XmlElement(name = "MachineType", namespace = "http://ws.fotowalla.com/ClientApp", required = true)
    protected String machineType;
    @XmlElement(name = "Major", namespace = "http://ws.fotowalla.com/ClientApp")
    protected int major;
    @XmlElement(name = "Minor", namespace = "http://ws.fotowalla.com/ClientApp")
    protected int minor;

    /**
     * Gets the value of the wsKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWSKey() {
        return wsKey;
    }

    /**
     * Sets the value of the wsKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWSKey(String value) {
        this.wsKey = value;
    }

    /**
     * Gets the value of the os property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOS() {
        return os;
    }

    /**
     * Sets the value of the os property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOS(String value) {
        this.os = value;
    }

    /**
     * Gets the value of the machineType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMachineType() {
        return machineType;
    }

    /**
     * Sets the value of the machineType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMachineType(String value) {
        this.machineType = value;
    }

    /**
     * Gets the value of the major property.
     * 
     */
    public int getMajor() {
        return major;
    }

    /**
     * Sets the value of the major property.
     * 
     */
    public void setMajor(int value) {
        this.major = value;
    }

    /**
     * Gets the value of the minor property.
     * 
     */
    public int getMinor() {
        return minor;
    }

    /**
     * Sets the value of the minor property.
     * 
     */
    public void setMinor(int value) {
        this.minor = value;
    }

}
