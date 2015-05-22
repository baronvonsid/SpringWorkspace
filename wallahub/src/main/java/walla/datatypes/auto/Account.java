//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.05.22 at 10:07:49 PM BST 
//


package walla.datatypes.auto;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


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
 *         &lt;element name="Country" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Timezone" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Newsletter" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="Password" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Status" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="AccountMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="AccountType" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="AccountTypeName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="PasswordChangeDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="OpenDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="CloseDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="Key" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SecurityMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Emails">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="EmailRef" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence minOccurs="0">
 *                             &lt;element name="Address" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="Principle" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *                             &lt;element name="Secondary" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *                             &lt;element name="Verified" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
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
    "country",
    "timezone",
    "newsletter",
    "password",
    "status",
    "accountMessage",
    "accountType",
    "accountTypeName",
    "passwordChangeDate",
    "openDate",
    "closeDate",
    "key",
    "securityMessage",
    "emails"
})
@XmlRootElement(name = "Account", namespace = "http://ws.fotowalla.com/Account")
public class Account {

    @XmlElement(name = "ProfileName", namespace = "http://ws.fotowalla.com/Account")
    protected String profileName;
    @XmlElement(name = "Desc", namespace = "http://ws.fotowalla.com/Account")
    protected String desc;
    @XmlElement(name = "Country", namespace = "http://ws.fotowalla.com/Account")
    protected String country;
    @XmlElement(name = "Timezone", namespace = "http://ws.fotowalla.com/Account")
    protected String timezone;
    @XmlElement(name = "Newsletter", namespace = "http://ws.fotowalla.com/Account")
    protected Boolean newsletter;
    @XmlElement(name = "Password", namespace = "http://ws.fotowalla.com/Account")
    protected String password;
    @XmlElement(name = "Status", namespace = "http://ws.fotowalla.com/Account")
    protected Integer status;
    @XmlElement(name = "AccountMessage", namespace = "http://ws.fotowalla.com/Account")
    protected String accountMessage;
    @XmlElement(name = "AccountType", namespace = "http://ws.fotowalla.com/Account")
    protected Integer accountType;
    @XmlElement(name = "AccountTypeName", namespace = "http://ws.fotowalla.com/Account")
    protected String accountTypeName;
    @XmlElement(name = "PasswordChangeDate", namespace = "http://ws.fotowalla.com/Account", type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Calendar passwordChangeDate;
    @XmlElement(name = "OpenDate", namespace = "http://ws.fotowalla.com/Account", type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Calendar openDate;
    @XmlElement(name = "CloseDate", namespace = "http://ws.fotowalla.com/Account", type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Calendar closeDate;
    @XmlElement(name = "Key", namespace = "http://ws.fotowalla.com/Account")
    protected String key;
    @XmlElement(name = "SecurityMessage", namespace = "http://ws.fotowalla.com/Account")
    protected String securityMessage;
    @XmlElement(name = "Emails", namespace = "http://ws.fotowalla.com/Account")
    protected Account.Emails emails;
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
     * Gets the value of the country property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the value of the country property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountry(String value) {
        this.country = value;
    }

    /**
     * Gets the value of the timezone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * Sets the value of the timezone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimezone(String value) {
        this.timezone = value;
    }

    /**
     * Gets the value of the newsletter property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean getNewsletter() {
        return newsletter;
    }

    /**
     * Sets the value of the newsletter property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNewsletter(Boolean value) {
        this.newsletter = value;
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
     * Gets the value of the accountMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccountMessage() {
        return accountMessage;
    }

    /**
     * Sets the value of the accountMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccountMessage(String value) {
        this.accountMessage = value;
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
     * Gets the value of the passwordChangeDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Calendar getPasswordChangeDate() {
        return passwordChangeDate;
    }

    /**
     * Sets the value of the passwordChangeDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPasswordChangeDate(Calendar value) {
        this.passwordChangeDate = value;
    }

    /**
     * Gets the value of the openDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Calendar getOpenDate() {
        return openDate;
    }

    /**
     * Sets the value of the openDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOpenDate(Calendar value) {
        this.openDate = value;
    }

    /**
     * Gets the value of the closeDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Calendar getCloseDate() {
        return closeDate;
    }

    /**
     * Sets the value of the closeDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCloseDate(Calendar value) {
        this.closeDate = value;
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
     * Gets the value of the securityMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSecurityMessage() {
        return securityMessage;
    }

    /**
     * Sets the value of the securityMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSecurityMessage(String value) {
        this.securityMessage = value;
    }

    /**
     * Gets the value of the emails property.
     * 
     * @return
     *     possible object is
     *     {@link Account.Emails }
     *     
     */
    public Account.Emails getEmails() {
        return emails;
    }

    /**
     * Sets the value of the emails property.
     * 
     * @param value
     *     allowed object is
     *     {@link Account.Emails }
     *     
     */
    public void setEmails(Account.Emails value) {
        this.emails = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getId() {
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
    public Integer getVersion() {
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
     *         &lt;element name="EmailRef" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence minOccurs="0">
     *                   &lt;element name="Address" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="Principle" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
     *                   &lt;element name="Secondary" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
     *                   &lt;element name="Verified" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
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
        "emailRef"
    })
    public static class Emails {

        @XmlElement(name = "EmailRef", namespace = "http://ws.fotowalla.com/Account")
        protected List<Account.Emails.EmailRef> emailRef;

        /**
         * Gets the value of the emailRef property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the emailRef property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getEmailRef().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Account.Emails.EmailRef }
         * 
         * 
         */
        public List<Account.Emails.EmailRef> getEmailRef() {
            if (emailRef == null) {
                emailRef = new ArrayList<Account.Emails.EmailRef>();
            }
            return this.emailRef;
        }


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
         *         &lt;element name="Address" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="Principle" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
         *         &lt;element name="Secondary" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
         *         &lt;element name="Verified" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
            "address",
            "principle",
            "secondary",
            "verified"
        })
        public static class EmailRef {

            @XmlElement(name = "Address", namespace = "http://ws.fotowalla.com/Account")
            protected String address;
            @XmlElement(name = "Principle", namespace = "http://ws.fotowalla.com/Account")
            protected Boolean principle;
            @XmlElement(name = "Secondary", namespace = "http://ws.fotowalla.com/Account")
            protected Boolean secondary;
            @XmlElement(name = "Verified", namespace = "http://ws.fotowalla.com/Account")
            protected Boolean verified;

            /**
             * Gets the value of the address property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getAddress() {
                return address;
            }

            /**
             * Sets the value of the address property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setAddress(String value) {
                this.address = value;
            }

            /**
             * Gets the value of the principle property.
             * 
             * @return
             *     possible object is
             *     {@link Boolean }
             *     
             */
            public Boolean getPrinciple() {
                return principle;
            }

            /**
             * Sets the value of the principle property.
             * 
             * @param value
             *     allowed object is
             *     {@link Boolean }
             *     
             */
            public void setPrinciple(Boolean value) {
                this.principle = value;
            }

            /**
             * Gets the value of the secondary property.
             * 
             * @return
             *     possible object is
             *     {@link Boolean }
             *     
             */
            public Boolean getSecondary() {
                return secondary;
            }

            /**
             * Sets the value of the secondary property.
             * 
             * @param value
             *     allowed object is
             *     {@link Boolean }
             *     
             */
            public void setSecondary(Boolean value) {
                this.secondary = value;
            }

            /**
             * Gets the value of the verified property.
             * 
             * @return
             *     possible object is
             *     {@link Boolean }
             *     
             */
            public Boolean getVerified() {
                return verified;
            }

            /**
             * Sets the value of the verified property.
             * 
             * @param value
             *     allowed object is
             *     {@link Boolean }
             *     
             */
            public void setVerified(Boolean value) {
                this.verified = value;
            }

        }

    }

}
