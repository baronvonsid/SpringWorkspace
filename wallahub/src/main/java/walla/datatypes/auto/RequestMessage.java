//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.06.07 at 09:21:34 PM BST 
//


package walla.datatypes.auto;

import java.util.ArrayList;
import java.util.List;
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
 *       &lt;sequence minOccurs="0">
 *         &lt;element name="UserId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="WallaClass" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Method" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="RequestId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="IdOne" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="IdTwo" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="IdList">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="IdRef" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}long">
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
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
    "userId",
    "wallaClass",
    "method",
    "requestId",
    "idOne",
    "idTwo",
    "idList"
})
@XmlRootElement(name = "RequestMessage", namespace = "http://ws.fotowalla.com/RequestMessage")
public class RequestMessage {

    @XmlElement(name = "UserId", namespace = "http://ws.fotowalla.com/RequestMessage")
    protected Long userId;
    @XmlElement(name = "WallaClass", namespace = "http://ws.fotowalla.com/RequestMessage")
    protected String wallaClass;
    @XmlElement(name = "Method", namespace = "http://ws.fotowalla.com/RequestMessage")
    protected String method;
    @XmlElement(name = "RequestId", namespace = "http://ws.fotowalla.com/RequestMessage")
    protected String requestId;
    @XmlElement(name = "IdOne", namespace = "http://ws.fotowalla.com/RequestMessage")
    protected Long idOne;
    @XmlElement(name = "IdTwo", namespace = "http://ws.fotowalla.com/RequestMessage")
    protected Long idTwo;
    @XmlElement(name = "IdList", namespace = "http://ws.fotowalla.com/RequestMessage")
    protected RequestMessage.IdList idList;

    /**
     * Gets the value of the userId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the value of the userId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setUserId(Long value) {
        this.userId = value;
    }

    /**
     * Gets the value of the wallaClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWallaClass() {
        return wallaClass;
    }

    /**
     * Sets the value of the wallaClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWallaClass(String value) {
        this.wallaClass = value;
    }

    /**
     * Gets the value of the method property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the value of the method property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMethod(String value) {
        this.method = value;
    }

    /**
     * Gets the value of the requestId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Sets the value of the requestId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestId(String value) {
        this.requestId = value;
    }

    /**
     * Gets the value of the idOne property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getIdOne() {
        return idOne;
    }

    /**
     * Sets the value of the idOne property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setIdOne(Long value) {
        this.idOne = value;
    }

    /**
     * Gets the value of the idTwo property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getIdTwo() {
        return idTwo;
    }

    /**
     * Sets the value of the idTwo property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setIdTwo(Long value) {
        this.idTwo = value;
    }

    /**
     * Gets the value of the idList property.
     * 
     * @return
     *     possible object is
     *     {@link RequestMessage.IdList }
     *     
     */
    public RequestMessage.IdList getIdList() {
        return idList;
    }

    /**
     * Sets the value of the idList property.
     * 
     * @param value
     *     allowed object is
     *     {@link RequestMessage.IdList }
     *     
     */
    public void setIdList(RequestMessage.IdList value) {
        this.idList = value;
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
     *         &lt;element name="IdRef" maxOccurs="unbounded" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}long">
     *             &lt;/restriction>
     *           &lt;/simpleType>
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
        "idRef"
    })
    public static class IdList {

        @XmlElement(name = "IdRef", namespace = "http://ws.fotowalla.com/RequestMessage", type = Long.class)
        protected List<Long> idRef;

        /**
         * Gets the value of the idRef property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the idRef property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getIdRef().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Long }
         * 
         * 
         */
        public List<Long> getIdRef() {
            if (idRef == null) {
                idRef = new ArrayList<Long>();
            }
            return this.idRef;
        }

    }

}
