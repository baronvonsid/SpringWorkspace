//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.04.28 at 07:48:45 PM BST 
//


package walla.datatypes.auto;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *         &lt;element name="StorageMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="StorageGBLimit" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="MonthlyUploadCap" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="UploadCount30Days" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="SizeGB" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="CompressedSizeGB" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="ImageCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="FormatRef" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence minOccurs="0">
 *                   &lt;element name="Format" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="SizeGB" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *                   &lt;element name="CompressedSizeGB" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *                   &lt;element name="ImageCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                   &lt;element name="Colour" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="UploadSourceRef" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence minOccurs="0">
 *                   &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="SizeGB" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *                   &lt;element name="CompressedSizeGB" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *                   &lt;element name="ImageCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                   &lt;element name="Colour" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="ImageYearRef" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence minOccurs="0">
 *                   &lt;element name="Year" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="SizeGB" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *                   &lt;element name="CompressedSizeGB" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *                   &lt;element name="ImageCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                   &lt;element name="Colour" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}long" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "storageMessage",
    "storageGBLimit",
    "monthlyUploadCap",
    "uploadCount30Days",
    "sizeGB",
    "compressedSizeGB",
    "imageCount",
    "formatRef",
    "uploadSourceRef",
    "imageYearRef"
})
@XmlRootElement(name = "AccountStorage", namespace = "http://ws.fotowalla.com/AccountStorage")
public class AccountStorage {

    @XmlElement(name = "StorageMessage", namespace = "http://ws.fotowalla.com/AccountStorage", required = true)
    protected String storageMessage;
    @XmlElement(name = "StorageGBLimit", namespace = "http://ws.fotowalla.com/AccountStorage")
    protected double storageGBLimit;
    @XmlElement(name = "MonthlyUploadCap", namespace = "http://ws.fotowalla.com/AccountStorage")
    protected int monthlyUploadCap;
    @XmlElement(name = "UploadCount30Days", namespace = "http://ws.fotowalla.com/AccountStorage")
    protected int uploadCount30Days;
    @XmlElement(name = "SizeGB", namespace = "http://ws.fotowalla.com/AccountStorage")
    protected double sizeGB;
    @XmlElement(name = "CompressedSizeGB", namespace = "http://ws.fotowalla.com/AccountStorage")
    protected double compressedSizeGB;
    @XmlElement(name = "ImageCount", namespace = "http://ws.fotowalla.com/AccountStorage")
    protected int imageCount;
    @XmlElement(name = "FormatRef", namespace = "http://ws.fotowalla.com/AccountStorage")
    protected List<AccountStorage.FormatRef> formatRef;
    @XmlElement(name = "UploadSourceRef", namespace = "http://ws.fotowalla.com/AccountStorage")
    protected List<AccountStorage.UploadSourceRef> uploadSourceRef;
    @XmlElement(name = "ImageYearRef", namespace = "http://ws.fotowalla.com/AccountStorage")
    protected List<AccountStorage.ImageYearRef> imageYearRef;
    @XmlAttribute(name = "id")
    protected Long id;

    /**
     * Gets the value of the storageMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStorageMessage() {
        return storageMessage;
    }

    /**
     * Sets the value of the storageMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStorageMessage(String value) {
        this.storageMessage = value;
    }

    /**
     * Gets the value of the storageGBLimit property.
     * 
     */
    public double getStorageGBLimit() {
        return storageGBLimit;
    }

    /**
     * Sets the value of the storageGBLimit property.
     * 
     */
    public void setStorageGBLimit(double value) {
        this.storageGBLimit = value;
    }

    /**
     * Gets the value of the monthlyUploadCap property.
     * 
     */
    public int getMonthlyUploadCap() {
        return monthlyUploadCap;
    }

    /**
     * Sets the value of the monthlyUploadCap property.
     * 
     */
    public void setMonthlyUploadCap(int value) {
        this.monthlyUploadCap = value;
    }

    /**
     * Gets the value of the uploadCount30Days property.
     * 
     */
    public int getUploadCount30Days() {
        return uploadCount30Days;
    }

    /**
     * Sets the value of the uploadCount30Days property.
     * 
     */
    public void setUploadCount30Days(int value) {
        this.uploadCount30Days = value;
    }

    /**
     * Gets the value of the sizeGB property.
     * 
     */
    public double getSizeGB() {
        return sizeGB;
    }

    /**
     * Sets the value of the sizeGB property.
     * 
     */
    public void setSizeGB(double value) {
        this.sizeGB = value;
    }

    /**
     * Gets the value of the compressedSizeGB property.
     * 
     */
    public double getCompressedSizeGB() {
        return compressedSizeGB;
    }

    /**
     * Sets the value of the compressedSizeGB property.
     * 
     */
    public void setCompressedSizeGB(double value) {
        this.compressedSizeGB = value;
    }

    /**
     * Gets the value of the imageCount property.
     * 
     */
    public int getImageCount() {
        return imageCount;
    }

    /**
     * Sets the value of the imageCount property.
     * 
     */
    public void setImageCount(int value) {
        this.imageCount = value;
    }

    /**
     * Gets the value of the formatRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the formatRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFormatRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AccountStorage.FormatRef }
     * 
     * 
     */
    public List<AccountStorage.FormatRef> getFormatRef() {
        if (formatRef == null) {
            formatRef = new ArrayList<AccountStorage.FormatRef>();
        }
        return this.formatRef;
    }

    /**
     * Gets the value of the uploadSourceRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the uploadSourceRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUploadSourceRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AccountStorage.UploadSourceRef }
     * 
     * 
     */
    public List<AccountStorage.UploadSourceRef> getUploadSourceRef() {
        if (uploadSourceRef == null) {
            uploadSourceRef = new ArrayList<AccountStorage.UploadSourceRef>();
        }
        return this.uploadSourceRef;
    }

    /**
     * Gets the value of the imageYearRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the imageYearRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getImageYearRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AccountStorage.ImageYearRef }
     * 
     * 
     */
    public List<AccountStorage.ImageYearRef> getImageYearRef() {
        if (imageYearRef == null) {
            imageYearRef = new ArrayList<AccountStorage.ImageYearRef>();
        }
        return this.imageYearRef;
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
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence minOccurs="0">
     *         &lt;element name="Format" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="SizeGB" type="{http://www.w3.org/2001/XMLSchema}double"/>
     *         &lt;element name="CompressedSizeGB" type="{http://www.w3.org/2001/XMLSchema}double"/>
     *         &lt;element name="ImageCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="Colour" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
        "format",
        "sizeGB",
        "compressedSizeGB",
        "imageCount",
        "colour"
    })
    public static class FormatRef {

        @XmlElement(name = "Format", namespace = "http://ws.fotowalla.com/AccountStorage")
        protected String format;
        @XmlElement(name = "SizeGB", namespace = "http://ws.fotowalla.com/AccountStorage")
        protected Double sizeGB;
        @XmlElement(name = "CompressedSizeGB", namespace = "http://ws.fotowalla.com/AccountStorage")
        protected Double compressedSizeGB;
        @XmlElement(name = "ImageCount", namespace = "http://ws.fotowalla.com/AccountStorage")
        protected Integer imageCount;
        @XmlElement(name = "Colour", namespace = "http://ws.fotowalla.com/AccountStorage")
        protected String colour;

        /**
         * Gets the value of the format property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getFormat() {
            return format;
        }

        /**
         * Sets the value of the format property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setFormat(String value) {
            this.format = value;
        }

        /**
         * Gets the value of the sizeGB property.
         * 
         * @return
         *     possible object is
         *     {@link Double }
         *     
         */
        public Double getSizeGB() {
            return sizeGB;
        }

        /**
         * Sets the value of the sizeGB property.
         * 
         * @param value
         *     allowed object is
         *     {@link Double }
         *     
         */
        public void setSizeGB(Double value) {
            this.sizeGB = value;
        }

        /**
         * Gets the value of the compressedSizeGB property.
         * 
         * @return
         *     possible object is
         *     {@link Double }
         *     
         */
        public Double getCompressedSizeGB() {
            return compressedSizeGB;
        }

        /**
         * Sets the value of the compressedSizeGB property.
         * 
         * @param value
         *     allowed object is
         *     {@link Double }
         *     
         */
        public void setCompressedSizeGB(Double value) {
            this.compressedSizeGB = value;
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
         * Gets the value of the colour property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getColour() {
            return colour;
        }

        /**
         * Sets the value of the colour property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setColour(String value) {
            this.colour = value;
        }

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
     *         &lt;element name="Year" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="SizeGB" type="{http://www.w3.org/2001/XMLSchema}double"/>
     *         &lt;element name="CompressedSizeGB" type="{http://www.w3.org/2001/XMLSchema}double"/>
     *         &lt;element name="ImageCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="Colour" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
        "year",
        "sizeGB",
        "compressedSizeGB",
        "imageCount",
        "colour"
    })
    public static class ImageYearRef {

        @XmlElement(name = "Year", namespace = "http://ws.fotowalla.com/AccountStorage")
        protected String year;
        @XmlElement(name = "SizeGB", namespace = "http://ws.fotowalla.com/AccountStorage")
        protected Double sizeGB;
        @XmlElement(name = "CompressedSizeGB", namespace = "http://ws.fotowalla.com/AccountStorage")
        protected Double compressedSizeGB;
        @XmlElement(name = "ImageCount", namespace = "http://ws.fotowalla.com/AccountStorage")
        protected Integer imageCount;
        @XmlElement(name = "Colour", namespace = "http://ws.fotowalla.com/AccountStorage")
        protected String colour;

        /**
         * Gets the value of the year property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getYear() {
            return year;
        }

        /**
         * Sets the value of the year property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setYear(String value) {
            this.year = value;
        }

        /**
         * Gets the value of the sizeGB property.
         * 
         * @return
         *     possible object is
         *     {@link Double }
         *     
         */
        public Double getSizeGB() {
            return sizeGB;
        }

        /**
         * Sets the value of the sizeGB property.
         * 
         * @param value
         *     allowed object is
         *     {@link Double }
         *     
         */
        public void setSizeGB(Double value) {
            this.sizeGB = value;
        }

        /**
         * Gets the value of the compressedSizeGB property.
         * 
         * @return
         *     possible object is
         *     {@link Double }
         *     
         */
        public Double getCompressedSizeGB() {
            return compressedSizeGB;
        }

        /**
         * Sets the value of the compressedSizeGB property.
         * 
         * @param value
         *     allowed object is
         *     {@link Double }
         *     
         */
        public void setCompressedSizeGB(Double value) {
            this.compressedSizeGB = value;
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
         * Gets the value of the colour property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getColour() {
            return colour;
        }

        /**
         * Sets the value of the colour property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setColour(String value) {
            this.colour = value;
        }

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
     *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="SizeGB" type="{http://www.w3.org/2001/XMLSchema}double"/>
     *         &lt;element name="CompressedSizeGB" type="{http://www.w3.org/2001/XMLSchema}double"/>
     *         &lt;element name="ImageCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="Colour" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
        "name",
        "sizeGB",
        "compressedSizeGB",
        "imageCount",
        "colour"
    })
    public static class UploadSourceRef {

        @XmlElement(name = "Name", namespace = "http://ws.fotowalla.com/AccountStorage")
        protected String name;
        @XmlElement(name = "SizeGB", namespace = "http://ws.fotowalla.com/AccountStorage")
        protected Double sizeGB;
        @XmlElement(name = "CompressedSizeGB", namespace = "http://ws.fotowalla.com/AccountStorage")
        protected Double compressedSizeGB;
        @XmlElement(name = "ImageCount", namespace = "http://ws.fotowalla.com/AccountStorage")
        protected Integer imageCount;
        @XmlElement(name = "Colour", namespace = "http://ws.fotowalla.com/AccountStorage")
        protected String colour;

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
         * Gets the value of the sizeGB property.
         * 
         * @return
         *     possible object is
         *     {@link Double }
         *     
         */
        public Double getSizeGB() {
            return sizeGB;
        }

        /**
         * Sets the value of the sizeGB property.
         * 
         * @param value
         *     allowed object is
         *     {@link Double }
         *     
         */
        public void setSizeGB(Double value) {
            this.sizeGB = value;
        }

        /**
         * Gets the value of the compressedSizeGB property.
         * 
         * @return
         *     possible object is
         *     {@link Double }
         *     
         */
        public Double getCompressedSizeGB() {
            return compressedSizeGB;
        }

        /**
         * Sets the value of the compressedSizeGB property.
         * 
         * @param value
         *     allowed object is
         *     {@link Double }
         *     
         */
        public void setCompressedSizeGB(Double value) {
            this.compressedSizeGB = value;
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
         * Gets the value of the colour property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getColour() {
            return colour;
        }

        /**
         * Sets the value of the colour property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setColour(String value) {
            this.colour = value;
        }

    }

}
