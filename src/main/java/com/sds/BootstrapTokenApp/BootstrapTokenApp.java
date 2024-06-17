package com.sds.BootstrapTokenApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class BootstrapTokenApp {

    public static void main(String[] args) {
        try {
            String inputFilePath = "ncp-assertion.xml";
            String outputFilePath = "bst.xml";

            if (!Files.exists(Paths.get(inputFilePath))) {
                System.out.println("Input file not found.");
                return;
            }

            // Read and parse the input XML file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new FileInputStream(inputFilePath)));
            document.getDocumentElement().normalize();

            // Process the assertion to create a bootstrap token
            String bootstrapToken = processAssertion(document);

            if (bootstrapToken != null) {
                // Write the bootstrap token to the output XML file
                writeBootstrapTokenToFile(bootstrapToken, outputFilePath);
                System.out.println("Bootstrap token generated and saved to " + outputFilePath);
            } else {
                System.out.println("Failed to generate bootstrap token.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String processAssertion(Document document) {
        try {
            // Extract the assertion element from the XML document
            NodeList nodeList = document.getElementsByTagName("saml:Assertion");
            if (nodeList.getLength() == 0) {
                System.out.println("Assertion not found in XML input.");
                return null;
            }
            Element assertionElement = (Element) nodeList.item(0);

            // Perform any required transformations on the assertion to create the bootstrap token
            // For this example, we will simply serialize the assertion element as the bootstrap token
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(assertionElement);
            FileOutputStream outputStream = new FileOutputStream("bootstrap-token.xml");
            StreamResult result = new StreamResult(outputStream);
            transformer.transform(source, result);

            // Read the serialized bootstrap token from the file
            String bootstrapToken = new String(Files.readAllBytes(Paths.get("bootstrap-token.xml")));
            Files.deleteIfExists(Paths.get("bootstrap-token.xml"));
            return bootstrapToken;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void writeBootstrapTokenToFile(String bootstrapToken, String outputFilePath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

             // Parse the bootstrap token as an XML document
             Document bootstrapTokenDoc = builder.parse(new InputSource(new StringReader(bootstrapToken)));

            // Import the node from the parsed document to the new document
            Node importedNode = document.importNode(bootstrapTokenDoc.getDocumentElement(), true);
            document.appendChild(importedNode);

            // Write the document to the output file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new FileOutputStream(outputFilePath));
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}