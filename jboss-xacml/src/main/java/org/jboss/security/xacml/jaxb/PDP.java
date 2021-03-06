//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.07.06 at 03:19:27 PM CDT 
//

package org.jboss.security.xacml.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for PDP complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PDP">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:jboss:xacml:2.0}Policies"/>
 *         &lt;element ref="{urn:jboss:xacml:2.0}Locators"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PDP", propOrder =
{"policies", "locators"})
public class PDP
{

   @XmlElement(name = "Policies", required = false)
   protected PoliciesType policies;

   @XmlElement(name = "Locators", required = true)
   protected LocatorsType locators;

   /**
    * Gets the value of the policies property.
    * 
    * @return
    *     possible object is
    *     {@link PoliciesType }
    *     
    */
   public PoliciesType getPolicies()
   {
      return policies;
   }

   /**
    * Sets the value of the policies property.
    * 
    * @param value
    *     allowed object is
    *     {@link PoliciesType }
    *     
    */
   public void setPolicies(PoliciesType value)
   {
      this.policies = value;
   }

   /**
    * Gets the value of the locators property.
    * 
    * @return
    *     possible object is
    *     {@link LocatorsType }
    *     
    */
   public LocatorsType getLocators()
   {
      return locators;
   }

   /**
    * Sets the value of the locators property.
    * 
    * @param value
    *     allowed object is
    *     {@link LocatorsType }
    *     
    */
   public void setLocators(LocatorsType value)
   {
      this.locators = value;
   }

}
