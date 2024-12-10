package com.sds.ehsdi;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ParsedDataParser {

    public ParsedData parse(File xmlFile) throws Exception {
        ParsedData parsedData = new ParsedData();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlFile);

        System.out.println(document.getDocumentElement().getTagName());

        // Handle both namespaces: saml and saml2
        Element assertion = getAssertionElement(document);
        if (assertion == null) {
            throw new IllegalArgumentException(
                    "The input XML does not contain a valid 'Assertion' element (namespace mismatch).");
        }

        // Extract Assertion attributes
        parsedData.setId(assertion.getAttribute("ID"));
        parsedData.setIssueInstant(assertion.getAttribute("IssueInstant"));
        parsedData.setVersion(assertion.getAttribute("Version"));

        // Extract Issuer
        Element issuer = (Element) assertion.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:assertion", "Issuer").item(0);
        parsedData.setIssuer(issuer.getTextContent());

        // Extract Signature
        ParsedData.Signature signature = new ParsedData.Signature();
        Element signatureElement = (Element) document.getElementsByTagName("ds:Signature").item(0);
        Element signedInfo = (Element) signatureElement.getElementsByTagName("ds:SignedInfo").item(0);
        signature.setSignatureMethodAlgorithm(signedInfo.getElementsByTagName("ds:SignatureMethod").item(0)
                .getAttributes().getNamedItem("Algorithm").getNodeValue());
        signature.setDigestMethodAlgorithm(signedInfo.getElementsByTagName("ds:DigestMethod").item(0).getAttributes()
                .getNamedItem("Algorithm").getNodeValue());
        signature.setDigestValue(signedInfo.getElementsByTagName("ds:DigestValue").item(0).getTextContent());
        signature
                .setSignatureValue(signatureElement.getElementsByTagName("ds:SignatureValue").item(0).getTextContent());
        signature.setCertificate(signatureElement.getElementsByTagName("ds:X509Certificate").item(0).getTextContent());
        parsedData.setSignature(signature);

        // Extract Subject
        ParsedData.Subject subject = new ParsedData.Subject();
        Element subjectElement = (Element) document.getElementsByTagName("saml:Subject").item(0);
        subject.setNameIdFormat(subjectElement.getElementsByTagName("saml:NameID").item(0).getAttributes()
                .getNamedItem("Format").getNodeValue());
        subject.setNameIdValue(subjectElement.getElementsByTagName("saml:NameID").item(0).getTextContent());
        subject.setConfirmationMethod(subjectElement.getElementsByTagName("saml:SubjectConfirmation").item(0)
                .getAttributes().getNamedItem("Method").getNodeValue());
        parsedData.setSubject(subject);

        // Extract Conditions
        ParsedData.Conditions conditions = new ParsedData.Conditions();
        Element conditionsElement = (Element) document.getElementsByTagName("saml:Conditions").item(0);
        conditions.setNotBefore(conditionsElement.getAttribute("NotBefore"));
        conditions.setNotOnOrAfter(conditionsElement.getAttribute("NotOnOrAfter"));
        parsedData.setConditions(conditions);

        // Extract Attributes
        List<ParsedData.Attribute> attributes = new ArrayList<>();
        NodeList attributeNodes = document.getElementsByTagName("saml:Attribute");
        for (int i = 0; i < attributeNodes.getLength(); i++) {
            Element attributeElement = (Element) attributeNodes.item(i);
            ParsedData.Attribute attribute = new ParsedData.Attribute();
            attribute.setFriendlyName(attributeElement.getAttribute("FriendlyName"));
            attribute.setName(attributeElement.getAttribute("Name"));
            NodeList values = attributeElement.getElementsByTagName("saml:AttributeValue");
            List<String> valueList = new ArrayList<>();
            for (int j = 0; j < values.getLength(); j++) {
                valueList.add(values.item(j).getTextContent());
            }
            attribute.setValues(valueList);
            attributes.add(attribute);
        }
        parsedData.setAttributes(attributes);

        // Extract Permissions
        List<String> permissions = new ArrayList<>();
        attributes.stream()
                .filter(attr -> "urn:oasis:names:tc:xspa:1.0:subject:hl7:permission".equals(attr.getName()))
                .forEach(attr -> permissions.addAll(attr.getValues()));
        parsedData.setPermissions(permissions);

        return parsedData;
    }

    private Element getAssertionElement(Document document) {

        // DEBUG
        // NodeList allElements = document.getElementsByTagName("*");
        // for (int i = 0; i < allElements.getLength(); i++) {
        //     Element element = (Element) allElements.item(i);
        //     System.out.println("Element: " + element.getTagName() + ", Namespace: " + element.getNamespaceURI());
        // }

        // Try "saml" namespace
        Element assertion = (Element) document
                .getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:assertion", "Assertion").item(0);
        if (assertion != null) {
            return assertion;
        }

        // Try "saml2" namespace
        assertion = (Element) document.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:assertion", "Assertion")
                .item(0);
        return assertion;
    }
}
