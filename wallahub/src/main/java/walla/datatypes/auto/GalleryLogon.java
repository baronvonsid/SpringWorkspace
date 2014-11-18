//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.11.18 at 08:38:44 PM GMT 
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
 *         &lt;element name="UserId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="ProfileName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="GalleryName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Password" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PasswordHash" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="GallerySalt" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="TempSalt" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ComplexUrl" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="AccessType" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Key" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "userId",
    "profileName",
    "galleryName",
    "password",
    "passwordHash",
    "gallerySalt",
    "tempSalt",
    "complexUrl",
    "accessType",
    "key"
})
@XmlRootElement(name = "GalleryLogon", namespace = "http://ws.fotowalla.com/GalleryLogon")
public class GalleryLogon {

    @XmlElement(name = "UserId", namespace = "http://ws.fotowalla.com/GalleryLogon")
    protected long userId;
    @XmlElement(name = "ProfileName", namespace = "http://ws.fotowalla.com/GalleryLogon", required = true)
    protected String profileName;
    @XmlElement(name = "GalleryName", namespace = "http://ws.fotowalla.com/GalleryLogon", required = true)
    protected String galleryName;
    @XmlElement(name = "Password", namespace = "http://ws.fotowalla.com/GalleryLogon", required = true)
    protected String password;
    @XmlElement(name = "PasswordHash", namespace = "http://ws.fotowalla.com/GalleryLogon", required = true)
    protected String passwordHash;
    @XmlElement(name = "GallerySalt", namespace = "http://ws.fotowalla.com/GalleryLogon", required = true)
    protected String gallerySalt;
    @XmlElement(name = "TempSalt", namespace = "http://ws.fotowalla.com/GalleryLogon", required = true)
    protected String tempSalt;
    @XmlElement(name = "ComplexUrl", namespace = "http://ws.fotowalla.com/GalleryLogon", required = true)
    protected String complexUrl;
    @XmlElement(name = "AccessType", namespace = "http://ws.fotowalla.com/GalleryLogon")
    protected int accessType;
    @XmlElement(name = "Key", namespace = "http://ws.fotowalla.com/GalleryLogon", required = true)
    protected String key;

    /**
     * Gets the value of the userId property.
     * 
     */
    public long getUserId() {
        return userId;
    }

    /**
     * Sets the value of the userId property.
     * 
     */
    public void setUserId(long value) {
        this.userId = value;
    }

    /**
     * Gets the value of the profileName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProfileName() {
        return profileName;
    }

    /**
     * Sets the value of the profileName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProfileName(String value) {
        this.profileName = value;
    }

    /**
     * Gets the value of the galleryName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGalleryName() {
        return galleryName;
    }

    /**
     * Sets the value of the galleryName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGalleryName(String value) {
        this.galleryName = value;
    }

    /**
     * Gets the value of the password property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(String value) {
        this.password = value;
    }

    /**
     * Gets the value of the passwordHash property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the value of the passwordHash property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPasswordHash(String value) {
        this.passwordHash = value;
    }

    /**
     * Gets the value of the gallerySalt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGallerySalt() {
        return gallerySalt;
    }

    /**
     * Sets the value of the gallerySalt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGallerySalt(String value) {
        this.gallerySalt = value;
    }

    /**
     * Gets the value of the tempSalt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTempSalt() {
        return tempSalt;
    }

    /**
     * Sets the value of the tempSalt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTempSalt(String value) {
        this.tempSalt = value;
    }

    /**
     * Gets the value of the complexUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComplexUrl() {
        return complexUrl;
    }

    /**
     * Sets the value of the complexUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComplexUrl(String value) {
        this.complexUrl = value;
    }

    /**
     * Gets the value of the accessType property.
     * 
     */
    public int getAccessType() {
        return accessType;
    }

    /**
     * Sets the value of the accessType property.
     * 
     */
    public void setAccessType(int value) {
        this.accessType = value;
    }

    /**
     * Gets the value of the key property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the value of the key property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKey(String value) {
        this.key = value;
    }

}
