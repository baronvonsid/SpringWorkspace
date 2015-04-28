//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.04.28 at 07:48:45 PM BST 
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
 *       &lt;sequence>
 *         &lt;element name="ImageUploadRef" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="imageId" type="{http://www.w3.org/2001/XMLSchema}long" />
 *                 &lt;attribute name="status" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                 &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="error" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *                 &lt;attribute name="errorMessage" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="lastUpdated" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
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
    "imageUploadRef"
})
@XmlRootElement(name = "UploadStatusList", namespace = "http://ws.fotowalla.com/UploadStatusList")
public class UploadStatusList {

    @XmlElement(name = "ImageUploadRef", namespace = "http://ws.fotowalla.com/UploadStatusList")
    protected List<UploadStatusList.ImageUploadRef> imageUploadRef;

    /**
     * Gets the value of the imageUploadRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the imageUploadRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getImageUploadRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link UploadStatusList.ImageUploadRef }
     * 
     * 
     */
    public List<UploadStatusList.ImageUploadRef> getImageUploadRef() {
        if (imageUploadRef == null) {
            imageUploadRef = new ArrayList<UploadStatusList.ImageUploadRef>();
        }
        return this.imageUploadRef;
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
     *       &lt;attribute name="imageId" type="{http://www.w3.org/2001/XMLSchema}long" />
     *       &lt;attribute name="status" type="{http://www.w3.org/2001/XMLSchema}int" />
     *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="error" type="{http://www.w3.org/2001/XMLSchema}boolean" />
     *       &lt;attribute name="errorMessage" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="lastUpdated" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class ImageUploadRef {

        @XmlAttribute(name = "imageId")
        protected Long imageId;
        @XmlAttribute(name = "status")
        protected Integer status;
        @XmlAttribute(name = "name")
        protected String name;
        @XmlAttribute(name = "error")
        protected Boolean error;
        @XmlAttribute(name = "errorMessage")
        protected String errorMessage;
        @XmlAttribute(name = "lastUpdated")
        @XmlJavaTypeAdapter(Adapter1 .class)
        @XmlSchemaType(name = "dateTime")
        protected Calendar lastUpdated;

        /**
         * Gets the value of the imageId property.
         * 
         * @return
         *     possible object is
         *     {@link Long }
         *     
         */
        public Long getImageId() {
            return imageId;
        }

        /**
         * Sets the value of the imageId property.
         * 
         * @param value
         *     allowed object is
         *     {@link Long }
         *     
         */
        public void setImageId(Long value) {
            this.imageId = value;
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
         * Gets the value of the error property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean getError() {
            return error;
        }

        /**
         * Sets the value of the error property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setError(Boolean value) {
            this.error = value;
        }

        /**
         * Gets the value of the errorMessage property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getErrorMessage() {
            return errorMessage;
        }

        /**
         * Sets the value of the errorMessage property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setErrorMessage(String value) {
            this.errorMessage = value;
        }

        /**
         * Gets the value of the lastUpdated property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public Calendar getLastUpdated() {
            return lastUpdated;
        }

        /**
         * Sets the value of the lastUpdated property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLastUpdated(Calendar value) {
            this.lastUpdated = value;
        }

    }

}
