package com.sds.ehsdi;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AuthenticationFlow {

    // Snatched from SEB example XML - TODO: replace with actual data when acquired
    private static final String SIGNATUREVALUECERT = "cfYNh4nL1xaAo31ag1puLvP1l54M1LtvVubpwF2UKe3tVZS27EsIjFpRpDJgR5ng29xWIoTD6KDYq8WbbjQjpYEiTOSJEFC16/onscu6VXTilfbV6ncmyhHBzuzocbF+C5PraHgFKfOdgi0irWwswT8V16i3NuBq5zDNzITMHOaVmYJSJVPdkospPVryeJ7kRCI9xpovL257HSakgzs6fctkISooB3LnwoXOb5mKAqDcrYQHCjh62uNitUYRXEZ94WVyd3AWo2OTFnSxxc32NahJvTOSjNiBTjfXZwnEmgbtbW7cWQaNfqKFYnCi+Z5YMc8D+Y/dD5Cb08Ee/hWaD7034rvxFinLP/AxgBlxb/1pviQpsCQRH1IPxopPMno1yFjN19+s1nkZJmDiEW3+pb+qy0XjY3JtT6rklInVdfg2hPJYWCG/zj4EWF/YfFxqHrOvJdjNE+1MJ9LkXYG612SbpcX/4T5ojENGiyHgi6EWlk7NRkKNJsxjE7yGOY2M";
    private static final String X509CERTIFICATESTRING = "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";

    public static void main(String[] args) {

        // Simulate receiving XML assertion and SOAP header
        if (args.length < 2) {
            System.out.println("Usage: java -jar AuthenticationFlow.jar <xml-assertion> <soap-header>");
            System.exit(1);
        }

        String ncpAssertionFilePath = args[0];
        String soapHeaderFilePath = args[1];

        try {
            // Read the XML assertion from the file
            String ncpAssertion = new String(Files.readAllBytes(new File(ncpAssertionFilePath).toPath()));

            // Read the SOAP header from the json file
            String soapHeaderContent = new String(Files.readAllBytes(new File(soapHeaderFilePath).toPath()));

            // Parse the XML assertion
            ParsedData dataFromAssertion = parseXmlAssertion(ncpAssertion);

            // Create the bootstrap token
            String bootstrapToken = createBootstrapToken(dataFromAssertion, soapHeaderContent);

            // Write the bootstrap token to the BST.xml file
            Files.write(new File("NCP-BST.xml").toPath(), bootstrapToken.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ParsedData parseXmlAssertion(String xmlAssertion) {
        ParsedData parsedData = new ParsedData();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new java.io.ByteArrayInputStream(xmlAssertion.getBytes("UTF-8")));
            Element rootElement = document.getDocumentElement();
            
            NodeList attributeNodes = rootElement.getElementsByTagName("saml2:Attribute");
            for (int i = 0; i < attributeNodes.getLength(); i++) {
                Element attributeElement = (Element) attributeNodes.item(i);
                String attributeName = attributeElement.getAttribute("FriendlyName");
                NodeList attributeValues = attributeElement.getElementsByTagName("saml2:AttributeValue");
                List<String> values = new ArrayList<>();
                for (int j = 0; j < attributeValues.getLength(); j++) {
                    values.add(attributeValues.item(j).getTextContent());
                }
                parsedData.addAttribute(attributeName, values);
            }

            NodeList permissionNodes = rootElement.getElementsByTagName("saml2:Attribute");
            for (int i = 0; i < permissionNodes.getLength(); i++) {
                Element attributeElement = (Element) permissionNodes.item(i);
                if (attributeElement.getAttribute("FriendlyName").equals("Hl7 Permissions")) {
                    NodeList attributeValues = attributeElement.getElementsByTagName("saml2:AttributeValue");
                    for (int j = 0; j < attributeValues.getLength(); j++) {
                        parsedData.addPermission(attributeValues.item(j).getTextContent());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parsedData;
    }

    private static String createBootstrapToken(ParsedData dataFromAssertion, String soapHeaderContent) {
        try {
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        // Root Element
        Element assertion = document.createElementNS("urn:oasis:names:tc:SAML:2.0:assertion", "Assertion");
        assertion.setAttribute("ID", "_c74ef50a-9212-4460-83bb-a3a938ccd976"); // TODO: Generate a unique ID, using this generated one for now
        assertion.setAttribute("IssueInstant", "2024-08-20T11:23:59Z"); // TODO: Generate the current time in the correct format
        assertion.setAttribute("Version", "2.0");
        document.appendChild(assertion);

        // Issuer Element
        Element issuer = document.createElement("Issuer");
        issuer.appendChild(document.createTextNode("https://www.ncpdk.sds.dk")); // TODO: Exchange with actual issuer when decided what the value is
        assertion.appendChild(issuer);

        // Signature Element
        addSignatureElement(document, assertion);
        
        // Subject Element
        Element subject = document.createElement("Subject");
        Element nameID = document.createElement("NameID");
        nameID.setAttribute("Format", "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");
        nameID.appendChild(document.createTextNode("https://data.gov.dk/model/core/eid/professional/uuid/9dcdad2d-29e5-43c7-8abb-98a25d6e2c47"));
        subject.appendChild(nameID);

        Element subjectConfirmation = document.createElement("SubjectConfirmation");
        subjectConfirmation.setAttribute("Method", "urn:oasis:names:tc:SAML:2.0:cm:holder-of-key");

        Element subjectConfirmationData = document.createElementNS("http://www.w3.org/2000/09/xmldsig#", "SubjectConfirmationData");
        subjectConfirmationData.setAttribute("p4:type", "KeyInfoConfirmationDataType");
        subjectConfirmationData.setAttributeNS("xmlns:p4", "xmlns:p4", "http://www.w3.org/2001/XMLSchema-instance");

        Element keyInfo = createKeyInfo(document);
        subjectConfirmationData.appendChild(keyInfo);
        subjectConfirmation.appendChild(subjectConfirmationData);
        subject.appendChild(subjectConfirmation);

        assertion.appendChild(subject);

        // Conditions Element
        Element conditions = document.createElement("Conditions");
        conditions.setAttribute("NotOnOrAfter", "2024-08-20T23:23:59Z");
        Element audienceRestriction = document.createElement("AudienceRestriction");
        Element audience = document.createElement("Audience");
        audience.appendChild(document.createTextNode("https://sts.sosi.dk/"));
        audienceRestriction.appendChild(audience);
        conditions.appendChild(audienceRestriction);
        assertion.appendChild(conditions);

         // AttributeStatement Element
        Element attributeStatement = document.createElement("AttributeStatement");
        
        // Adding predefined attributes from SEB example to the AttributeStatement
        addAttribute(document, attributeStatement, "dk:gov:saml:attribute:SpecVer", "urn:oasis:names:tc:SAML:2.0:attrname-format:basic", "DK-SAML-2.0");
        addAttribute(document, attributeStatement, "dk:gov:saml:attribute:AssuranceLevel", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "3");
        addAttribute(document, attributeStatement, "urn:oid:2.5.4.10", "urn:oasis:names:tc:SAML:2.0:attrname-format:basic", "GLOBETEAM A/S");
        addAttribute(document, attributeStatement, "dk:gov:saml:attribute:CprNumberIdentifier", "urn:oasis:names:tc:SAML:2.0:attrname-format:basic", "0408801885");
        addAttribute(document, attributeStatement, "dk:gov:saml:attribute:CvrNumberIdentifier", "urn:oasis:names:tc:SAML:2.0:attrname-format:basic", "25959701");
        addAttribute(document, attributeStatement, "dk:gov:saml:attribute:RidNumberIdentifier", "urn:oasis:names:tc:SAML:2.0:attrname-format:basic", "11086796");
        
        
        // TODO: Add the attributes from the parsed data to the AttributeStatement
        for (Map.Entry<String, List<String>> entry : dataFromAssertion.getAttributes().entrySet()) {
            String attributeName = sanitizeXMLName(entry.getKey());
            Element attributeElement = document.createElement("Attribute");
            attributeElement.setAttribute("Name", attributeName);
            attributeElement.setAttribute("NameFormat", "urn:oasis:names:tc:SAML:2.0:attrname-format:basic");
            for (String value : entry.getValue()) {
                Element attributeValue = document.createElement("AttributeValue");
                attributeValue.appendChild(document.createTextNode(value));
                attributeElement.appendChild(attributeValue);
            }
            attributeStatement.appendChild(attributeElement);
        }

        // Add permissions as attributes
        if (!dataFromAssertion.getPermissions().isEmpty()) {
            Element permissionsAttribute = document.createElement("Attribute");
            permissionsAttribute.setAttribute("Name", "urn:oasis:names:tc:xspa:1.0:subject:hl7:permission");
            permissionsAttribute.setAttribute("NameFormat", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri");
            for (String permission : dataFromAssertion.getPermissions()) {
                Element attributeValue = document.createElement("AttributeValue");
                attributeValue.appendChild(document.createTextNode(permission));
                permissionsAttribute.appendChild(attributeValue);
            }
        }

        assertion.appendChild(attributeStatement);


        // Convert the document to a string
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
        
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void addSignatureElement(Document document, Element assertion) {
        Element signature = document.createElementNS("http://www.w3.org/2000/09/xmldsig#", "Signature");

        Element signedInfo = document.createElement("SignedInfo");

        Element canonicalizationMethod = document.createElement("CanonicalizationMethod");
        canonicalizationMethod.setAttribute("Algorithm", "http://www.w3.org/2001/10/xml-exc-c14n#");
        signedInfo.appendChild(canonicalizationMethod);

        Element signatureMethod = document.createElement("SignatureMethod");
        signatureMethod.setAttribute("Algorithm", "http://www.w3.org/2001/04/xmldsig-more#rsa-sha1"); // TODO: Check if this is the correct algorithm, it's probably not, 256?
        signedInfo.appendChild(signatureMethod);
        
        Element reference = document.createElement("Reference");
        reference.setAttribute("URI", "#_c74ef50a-9212-4460-83bb-a3a938ccd976"); // TODO: replace with a dynamic value
        
        // Transforms
        Element transforms = document.createElement("Transforms");
        Element transformEnvelopedSignature = document.createElement("Transform");
        transforms.appendChild(transformEnvelopedSignature);
        Element transformC14n = document.createElement("Transform");
        transformC14n.setAttribute("Algorithm", "http://www.w3.org/2001/10/xml-exc-c14n#");
        transforms.appendChild(transformC14n);

        reference.appendChild(transforms);

        // DigestMethod
        Element digestMethod = document.createElement("DigestMethod");
        digestMethod.setAttribute("Algorithm", "http://www.w3.org/2001/04/xmlenc#sha1"); // TODO: Check if this is the correct algorithm, it's probably not, 256?
        reference.appendChild(digestMethod);

        // DigestValue
        Element digestValue = document.createElement("DigestValue");
        digestValue.appendChild(document.createTextNode("onLP8pTMBvQpcODQZqEO/ZT2Eo0=")); // TODO: Calculate the digest value
        reference.appendChild(digestValue);

        signedInfo.appendChild(reference);
        signature.appendChild(signedInfo);

        // SignatureValue
        Element signatureValue = document.createElement("SignatureValue");
        signatureValue.appendChild(document.createTextNode(SIGNATUREVALUECERT));
        signature.appendChild(signatureValue);

        signature.appendChild(createKeyInfo(document));

        assertion.appendChild(signature);
    }

    private static Element createKeyInfo (Document document) {
        // KeyInfo
        Element keyInfo = document.createElement("KeyInfo");
        Element x509Data = document.createElement("X509Data");
        Element x509Certificate = document.createElement("X509Certificate");
        x509Certificate.appendChild(document.createTextNode(X509CERTIFICATESTRING));
        x509Data.appendChild(x509Certificate);
        keyInfo.appendChild(x509Data);
        return keyInfo;
    }

    private static void addAttribute(Document document, Element parent, String name, String format, String value) {
        Element attribute = document.createElement("Attribute");
        attribute.setAttribute("Name", name);
        attribute.setAttribute("NameFormat", format);
        Element attributeValue = document.createElement("AttributeValue");
        attributeValue.appendChild(document.createTextNode(value));
        attribute.appendChild(attributeValue);
        parent.appendChild(attribute);
    }

    
    private static String sanitizeXMLName(String name) {
        // Replace invalid characters with an underscore
        return name.replaceAll("[^a-zA-Z0-9\\-_.:]", "_");
    }
}