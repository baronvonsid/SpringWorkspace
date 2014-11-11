//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.11.10 at 07:37:12 PM GMT 
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
 *         &lt;element name="ProfileName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Desc" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Email" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Password" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Status" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="AccountType" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="AccountTypeName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="OpenDate" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="CloseDate" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="StorageGBLimit" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="StorageGBCurrent" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="TotalImages" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="MonthlyUploadCap" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="UploadCount30Days" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Key" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "profileName",
    "desc",
    "email",
    "password",
    "status",
    "accountType",
    "accountTypeName",
    "openDate",
    "closeDate",
    "storageGBLimit",
    "storageGBCurrent",
    "totalImages",
    "monthlyUploadCap",
    "uploadCount30Days",
    "key"
})
@XmlRootElement(name = "Account", namespace = "http://ws.fotowalla.com/Account")
public class Account {

    @XmlElement(name = "ProfileName", namespace = "http://ws.fotowalla.com/Account")
    protected String profileName;
    @XmlElement(name = "Desc", namespace = "http://ws.fotowalla.com/Account")
    protected String desc;
    @XmlElement(name = "Email", namespace = "http://ws.fotowalla.com/Account")
    protected String email;
    @XmlElement(name = "Password", namespace = "http://ws.fotowalla.com/Account")
    protected String password;
    @XmlElement(name = "Status", namespace = "http://ws.fotowalla.com/Account")
    protected Integer status;
    @XmlElement(name = "AccountType", namespace = "http://ws.fotowalla.com/Account")
    protected Integer accountType;
    @XmlElement(name = "AccountTypeName", namespace = "http://ws.fotowalla.com/Account")
    protected String accountTypeName;
    @XmlElement(name = "OpenDate", namespace = "http://ws.fotowalla.com/Account")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar openDate;
    @XmlElement(name = "CloseDate", namespace = "http://ws.fotowalla.com/Account")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar closeDate;
    @XmlElement(name = "StorageGBLimit", namespace = "http://ws.fotowalla.com/Account")
    protected Double storageGBLimit;
    @XmlElement(name = "StorageGBCurrent", namespace = "http://ws.fotowalla.com/Account")
    protected Double storageGBCurrent;
    @XmlElement(name = "TotalImages", namespace = "http://ws.fotowalla.com/Account")
    protected Integer totalImages;
    @XmlElement(name = "MonthlyUploadCap", namespace = "http://ws.fotowalla.com/Account")
    protected Integer monthlyUploadCap;
    @XmlElement(name = "UploadCount30Days", namespace = "http://ws.fotowalla.com/Account")
    protected Integer uploadCount30Days;
    @XmlElement(name = "Key", namespace = "http://ws.fotowalla.com/Account")
    protected String key;
    @XmlAttribute(name = "id")
    protected Long id;
    @XmlAttribute(name = "version")
    protected Integer version;

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
     * Gets the value of the email property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the value of the email property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmail(String value) {
        this.email = value;
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
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setStatus(Integer value) {
        this.status = value;
    }

    /**
     * Gets the value of the accountType property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAccountType() {
        return accountType;
    }

    /**
     * Sets the value of the accountType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAccountType(Integer value) {
        this.accountType = value;
    }

    /**
     * Gets the value of the accountTypeName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccountTypeName() {
        return accountTypeName;
    }

    /**
     * Sets the value of the accountTypeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccountTypeName(String value) {
        this.accountTypeName = value;
    }

    /**
     * Gets the value of the openDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getOpenDate() {
        return openDate;
    }

    /**
     * Sets the value of the openDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setOpenDate(XMLGregorianCalendar value) {
        this.openDate = value;
    }

    /**
     * Gets the value of the closeDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCloseDate() {
        return closeDate;
    }

    /**
     * Sets the value of the closeDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCloseDate(XMLGregorianCalendar value) {
        this.closeDate = value;
    }

    /**
     * Gets the value of the storageGBLimit property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getStorageGBLimit() {
        return storageGBLimit;
    }

    /**
     * Sets the value of the storageGBLimit property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setStorageGBLimit(Double value) {
        this.storageGBLimit = value;
    }

    /**
     * Gets the value of the storageGBCurrent property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getStorageGBCurrent() {
        return storageGBCurrent;
    }

    /**
     * Sets the value of the storageGBCurrent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setStorageGBCurrent(Double value) {
        this.storageGBCurrent = value;
    }

    /**
     * Gets the value of the totalImages property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTotalImages() {
        return totalImages;
    }

    /**
     * Sets the value of the totalImages property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTotalImages(Integer value) {
        this.totalImages = value;
    }

    /**
     * Gets the value of the monthlyUploadCap property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMonthlyUploadCap() {
        return monthlyUploadCap;
    }

    /**
     * Sets the value of the monthlyUploadCap property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMonthlyUploadCap(Integer value) {
        this.monthlyUploadCap = value;
    }

    /**
     * Gets the value of the uploadCount30Days property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getUploadCount30Days() {
        return uploadCount30Days;
    }

    /**
     * Sets the value of the uploadCount30Days property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setUploadCount30Days(Integer value) {
        this.uploadCount30Days = value;
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
