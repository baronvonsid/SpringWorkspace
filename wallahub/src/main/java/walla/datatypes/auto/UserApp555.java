//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.11.03 at 09:17:09 PM GMT 
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
 *         &lt;element name="PlatformId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="LastUsed" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="TagId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="CategoryId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="GalleryId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="ThumbCacheSizeMB" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="MainCopyCacheSizeMB" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="ImageFetchSize" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="MainCopyFolder" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="AutoUploadFolder" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}long" default="0" />
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
    "platformId",
    "lastUsed",
    "tagId",
    "categoryId",
    "galleryId",
    "thumbCacheSizeMB",
    "mainCopyCacheSizeMB",
    "imageFetchSize",
    "mainCopyFolder",
    "autoUploadFolder"
})
@XmlRootElement(name = "UserApp555", namespace = "http://ws.fotowalla.com/UserApp")
public class UserApp555 {

    @XmlElement(name = "PlatformId", namespace = "http://ws.fotowalla.com/UserApp")
    protected Integer platformId;
    @XmlElement(name = "LastUsed", namespace = "http://ws.fotowalla.com/UserApp")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar lastUsed;
    @XmlElement(name = "TagId", namespace = "http://ws.fotowalla.com/UserApp", defaultValue = "0")
    protected Long tagId;
    @XmlElement(name = "CategoryId", namespace = "http://ws.fotowalla.com/UserApp", defaultValue = "0")
    protected Long categoryId;
    @XmlElement(name = "GalleryId", namespace = "http://ws.fotowalla.com/UserApp", defaultValue = "0")
    protected Long galleryId;
    @XmlElement(name = "ThumbCacheSizeMB", namespace = "http://ws.fotowalla.com/UserApp")
    protected Integer thumbCacheSizeMB;
    @XmlElement(name = "MainCopyCacheSizeMB", namespace = "http://ws.fotowalla.com/UserApp")
    protected Integer mainCopyCacheSizeMB;
    @XmlElement(name = "ImageFetchSize", namespace = "http://ws.fotowalla.com/UserApp")
    protected Integer imageFetchSize;
    @XmlElement(name = "MainCopyFolder", namespace = "http://ws.fotowalla.com/UserApp")
    protected String mainCopyFolder;
    @XmlElement(name = "AutoUploadFolder", namespace = "http://ws.fotowalla.com/UserApp")
    protected String autoUploadFolder;
    @XmlAttribute(name = "id")
    protected Long id;
    @XmlAttribute(name = "version")
    protected Integer version;

    /**
     * Gets the value of the platformId property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPlatformId() {
        return platformId;
    }

    /**
     * Sets the value of the platformId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPlatformId(Integer value) {
        this.platformId = value;
    }

    /**
     * Gets the value of the lastUsed property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastUsed() {
        return lastUsed;
    }

    /**
     * Sets the value of the lastUsed property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastUsed(XMLGregorianCalendar value) {
        this.lastUsed = value;
    }

    /**
     * Gets the value of the tagId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getTagId() {
        return tagId;
    }

    /**
     * Sets the value of the tagId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTagId(Long value) {
        this.tagId = value;
    }

    /**
     * Gets the value of the categoryId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCategoryId() {
        return categoryId;
    }

    /**
     * Sets the value of the categoryId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCategoryId(Long value) {
        this.categoryId = value;
    }

    /**
     * Gets the value of the galleryId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getGalleryId() {
        return galleryId;
    }

    /**
     * Sets the value of the galleryId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setGalleryId(Long value) {
        this.galleryId = value;
    }

    /**
     * Gets the value of the thumbCacheSizeMB property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getThumbCacheSizeMB() {
        return thumbCacheSizeMB;
    }

    /**
     * Sets the value of the thumbCacheSizeMB property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setThumbCacheSizeMB(Integer value) {
        this.thumbCacheSizeMB = value;
    }

    /**
     * Gets the value of the mainCopyCacheSizeMB property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMainCopyCacheSizeMB() {
        return mainCopyCacheSizeMB;
    }

    /**
     * Sets the value of the mainCopyCacheSizeMB property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMainCopyCacheSizeMB(Integer value) {
        this.mainCopyCacheSizeMB = value;
    }

    /**
     * Gets the value of the imageFetchSize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getImageFetchSize() {
        return imageFetchSize;
    }

    /**
     * Sets the value of the imageFetchSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setImageFetchSize(Integer value) {
        this.imageFetchSize = value;
    }

    /**
     * Gets the value of the mainCopyFolder property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMainCopyFolder() {
        return mainCopyFolder;
    }

    /**
     * Sets the value of the mainCopyFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMainCopyFolder(String value) {
        this.mainCopyFolder = value;
    }

    /**
     * Gets the value of the autoUploadFolder property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAutoUploadFolder() {
        return autoUploadFolder;
    }

    /**
     * Sets the value of the autoUploadFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAutoUploadFolder(String value) {
        this.autoUploadFolder = value;
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
