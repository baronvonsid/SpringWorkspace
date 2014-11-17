//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.11.15 at 01:33:42 PM GMT 
//


package walla.datatypes.auto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Desc" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ImageCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="LastChanged" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="SystemOwned" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}long" default="0" />
 *       &lt;attribute name="parentId" type="{http://www.w3.org/2001/XMLSchema}long" default="0" />
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}int" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "name",
    "desc",
    "imageCount",
    "lastChanged",
    "systemOwned"
})
@XmlRootElement(name = "Category", namespace = "http://ws.fotowalla.com/Category")
public class Category {

    @XmlElement(name = "Name", namespace = "http://ws.fotowalla.com/Category")
    protected String name;
    @XmlElement(name = "Desc", namespace = "http://ws.fotowalla.com/Category")
    protected String desc;
    @XmlElement(name = "ImageCount", namespace = "http://ws.fotowalla.com/Category")
    protected Integer imageCount;
    @XmlElement(name = "LastChanged", namespace = "http://ws.fotowalla.com/Category")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastChanged;
    @XmlElement(name = "SystemOwned", namespace = "http://ws.fotowalla.com/Category")
    protected Boolean systemOwned;
    @XmlAttribute(name = "id")
    protected Long id;
    @XmlAttribute(name = "parentId")
    protected Long parentId;
    @XmlAttribute(name = "version")
    protected Integer version;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the desc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Sets the value of the desc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDesc(String value) {
        this.desc = value;
    }

    /**
     * Gets the value of the imageCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getImageCount() {
        return imageCount;
    }

    /**
     * Sets the value of the imageCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setImageCount(Integer value) {
        this.imageCount = value;
    }

    /**
     * Gets the value of the lastChanged property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastChanged() {
        return lastChanged;
    }

    /**
     * Sets the value of the lastChanged property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastChanged(XMLGregorianCalendar value) {
        this.lastChanged = value;
    }

    /**
     * Gets the value of the systemOwned property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSystemOwned() {
        return systemOwned;
    }

    /**
     * Sets the value of the systemOwned property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSystemOwned(Boolean value) {
        this.systemOwned = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getId() {
        if (id == null) {
            return  0L;
        } else {
            return id;
        }
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setId(Long value) {
        this.id = value;
    }

    /**
     * Gets the value of the parentId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getParentId() {
        if (parentId == null) {
            return  0L;
        } else {
            return parentId;
        }
    }

    /**
     * Sets the value of the parentId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setParentId(Long value) {
        this.parentId = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getVersion() {
        if (version == null) {
            return  0;
        } else {
            return version;
        }
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setVersion(Integer value) {
        this.version = value;
    }

}
