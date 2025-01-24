package com.sds.ehsdi;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

// Entry point for the application
// Calls the SoSiTokenMock class to generate a mock token
public class XmlProcessor {
    public static void main(String[] args) {
        
        if (args.length < 2) {
            System.err.println("Usage: java XmlProcessor <input path> <output path>");
        }

        File inputPath = new File(args[0]);
        File outputDir = new File(args[1]);
        
        if (!outputDir.exists()) {
            boolean created = outputDir.mkdirs(); // Try creating the directory
            if (!created) {
                System.err.println("Failed to create output directory: " + outputDir.getAbsolutePath());
                return;
            }
        }

        ParsedDataParser parser = new ParsedDataParser();
        Mapper mapper = new Mapper();
        XMLService xmlService = new XMLService();
        Writer writer = new Writer(outputDir);

        Map<String, String> metadata = new HashMap<>();

        try {
            if (inputPath.isFile()) {
                processFile(inputPath, outputDir, parser, mapper, xmlService, writer, metadata);
            } else if (inputPath.isDirectory()) {
                for (File file : inputPath.listFiles((dir, name) -> name.endsWith(".xml"))) {
                    System.out.println("Processing file: " + file.getName());
                    processFile(file, outputDir, parser, mapper, xmlService, writer, metadata);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            writer.writeMetadata(metadata);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void processFile(File inputFile, File outputDir, ParsedDataParser parser, Mapper mapper,
                                    XMLService xmlService, Writer writer, Map<String, String> metadata) throws Exception {
        ParsedData parsedData = parser.parse(inputFile);
        SOSIToken sosiToken = mapper.map(parsedData);

        String outputXml = xmlService.generateXml(sosiToken);

        File outputFile = new File(outputDir, inputFile.getName());
        writer.writeXml(outputFile.getName(), outputXml);

        metadata.put(inputFile.getName(), outputFile.getName());
    }
}
