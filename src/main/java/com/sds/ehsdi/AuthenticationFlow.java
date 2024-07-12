package com.sds.ehsdi;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

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
            Files.write(new File("BST.xml").toPath(), bootstrapToken.getBytes());
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

    private static String createBootstrapToken(ParsedData parsedData, String soapHeaderContent) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();


            // Root Element
            Element root = document.createElement("BootstrapToken");
            document.appendChild(root);

            // Attributes
            for (String attributeName : parsedData.getAttributes().keySet()) {
                String sanitizedAttributeName = sanitizeXMLName(attributeName);
                Element attributeElement = document.createElement(sanitizedAttributeName);
                for (String value : parsedData.getAttributes().get(attributeName)) {
                    Element valueElement = document.createElement("Value");
                    valueElement.appendChild(document.createTextNode(value));
                    attributeElement.appendChild(valueElement);
                }
                root.appendChild(attributeElement);
            }

            // Permissions
            // Element permissionsElement = document.createElement("Permissions");
            // for (String permission : parsedData.getPermissions()) {
            //     Element permissionElement = document.createElement(permission);
            //     permissionElement.appendChild(document.createTextNode(permission));
            //     permissionsElement.appendChild(permissionElement);
            // }
            // root.appendChild(permissionsElement);

            // TODO: Add the relevant data from soap header to the bootstrap token such as origin country, etc.

            // Convert to String
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));

            return writer.getBuffer().toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private static String sanitizeXMLName(String name) {
        // Replace invalid characters with an underscore
        return name.replaceAll("[^a-zA-Z0-9\\-_.:]", "_");
    }
}