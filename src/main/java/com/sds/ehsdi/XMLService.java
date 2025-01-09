package com.sds.ehsdi;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.List;

public class XMLService {

    public String generateXml(SOSIToken sosiToken) throws Exception {
        // Create a new XML document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
    
        // Create root element and declare namespaces
        Element assertion = document.createElementNS("urn:oasis:names:tc:SAML:2.0:assertion", "Assertion");
        assertion.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // Declare xsi namespace
        assertion.setAttribute("ID", sosiToken.getId());
        assertion.setAttribute("IssueInstant", sosiToken.getIssueInstant());
        assertion.setAttribute("Version", sosiToken.getVersion());
        document.appendChild(assertion);
    
        // Add Issuer
        Element issuer = document.createElement("Issuer");
        issuer.setTextContent(sosiToken.getIssuer());
        assertion.appendChild(issuer);

        // Add Signature
        SOSIToken.Signature signature = sosiToken.getSignature();
        if (signature != null) {
            Element signatureElement = document.createElementNS("http://www.w3.org/2000/09/xmldsig#", "Signature");
            assertion.appendChild(signatureElement);

            Element signedInfo = document.createElement("SignedInfo");
            signatureElement.appendChild(signedInfo);

            Element signatureMethod = document.createElement("SignatureMethod");
            signatureMethod.setAttribute("Algorithm", signature.getSignatureMethodAlgorithm());
            signedInfo.appendChild(signatureMethod);

            Element digestMethod = document.createElement("DigestMethod");
            digestMethod.setAttribute("Algorithm", signature.getDigestMethodAlgorithm());
            signedInfo.appendChild(digestMethod);

            Element digestValue = document.createElement("DigestValue");
            digestValue.setTextContent(signature.getDigestValue());
            signedInfo.appendChild(digestValue);

            Element signatureValue = document.createElement("SignatureValue");
            signatureValue.setTextContent(signature.getSignatureValue());
            signatureElement.appendChild(signatureValue);

            Element keyInfo = document.createElement("KeyInfo");
            signatureElement.appendChild(keyInfo);

            Element x509Data = document.createElement("X509Data");
            keyInfo.appendChild(x509Data);

            Element x509Certificate = document.createElement("X509Certificate");
            x509Certificate.setTextContent(signature.getCertificate());
            x509Data.appendChild(x509Certificate);
        }

        // Add Subject
        SOSIToken.Subject subject = sosiToken.getSubject();
        if (subject != null) {
            Element subjectElement = document.createElement("Subject");
            assertion.appendChild(subjectElement);

            Element nameId = document.createElement("NameID");
            nameId.setAttribute("Format", subject.getNameIdFormat());
            nameId.setTextContent(subject.getNameIdValue());
            subjectElement.appendChild(nameId);

            Element subjectConfirmation = document.createElement("SubjectConfirmation");
            subjectConfirmation.setAttribute("Method", subject.getConfirmationMethod());
            subjectElement.appendChild(subjectConfirmation);
        }

        // Add Conditions
        SOSIToken.Conditions conditions = sosiToken.getConditions();
        if (conditions != null) {
            Element conditionsElement = document.createElement("Conditions");
            conditionsElement.setAttribute("NotOnOrAfter", conditions.getNotOnOrAfter());
            assertion.appendChild(conditionsElement);

            for (String audience : conditions.getAudiences()) {
                Element audienceRestriction = document.createElement("AudienceRestriction");
                Element audienceElement = document.createElement("Audience");
                audienceElement.setTextContent(audience);
                audienceRestriction.appendChild(audienceElement);
                conditionsElement.appendChild(audienceRestriction);
            }
        }

        // Add AttributeStatement
        List<SOSIToken.Attribute> attributes = sosiToken.getAttributes();
        if (attributes != null && !attributes.isEmpty()) {
            Element attributeStatement = document.createElement("AttributeStatement");
            assertion.appendChild(attributeStatement);

            for (SOSIToken.Attribute attribute : attributes) {
                Element attributeElement = document.createElement("Attribute");
                attributeElement.setAttribute("FriendlyName", attribute.getFriendlyName());
                attributeElement.setAttribute("Name", attribute.getName());
                attributeStatement.appendChild(attributeElement);
            
                if ("urn:oasis:names:tc:xspa:1.0:subject:purposeofuse".equals(attribute.getName())) {
                    Element purposeOfUse = document.createElementNS("urn:hl7-org:v3", "PurposeOfUse");
                    purposeOfUse.setAttribute("xsi:type", "CE");
                    purposeOfUse.setAttribute("code", "TREATMENT");
                    purposeOfUse.setAttribute("codeSystem", "urn:oasis:names:tc:xspa:1.0");
                    attributeElement.appendChild(purposeOfUse);
                } else {
                    for (String value : attribute.getValues()) {
                        Element attributeValue = document.createElement("AttributeValue");
                        attributeValue.setTextContent(value);
                        attributeElement.appendChild(attributeValue);
                    }
                }
            }
        }

        // Convert document to string
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));

        return writer.toString();
    }
}
