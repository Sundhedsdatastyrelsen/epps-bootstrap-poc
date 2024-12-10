package com.sds.ehsdi;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class Writer {

    private final File outputDirectory;

    public Writer(File outputDirectory) {
        if (!outputDirectory.exists()) {
            if (!outputDirectory.mkdirs()) {
                throw new IllegalArgumentException("Unable to create output directory: " + outputDirectory.getAbsolutePath());
            }
        }
        if (!outputDirectory.isDirectory()) {
            throw new IllegalArgumentException("Output path is not a directory: " + outputDirectory.getAbsolutePath());
        }
        this.outputDirectory = outputDirectory;
    }
    
    public void writeMetadata(Map<String, String> metadata) throws IOException {
        File metadataFile = new File(outputDirectory, "metadata.json");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(metadataFile, metadata);
        System.out.println("Written metadata to: " + metadataFile.getAbsolutePath());
    }

    public void writeXml(String fileName, String xml) throws IOException {
        File outputFile = new File(outputDirectory, fileName);
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(xml.getBytes());
        }
        System.out.println("Written XML to: " + outputFile.getAbsolutePath());
    }
}
