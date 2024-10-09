package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    private static String LOG_FILE_PATH;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis(); // Start time measurement

        try {
            // Load configuration
            JsonNode config = FileUtils.loadConfigJson("src/main/resources/config.json");
            LOG_FILE_PATH = config.get("logging").get("logFilePath").asText();
            FileUtils.setLogFilePath(LOG_FILE_PATH);

            // Use paths as needed
            String inputFilePath = config.get("dataValidation").get("inputFile").get("path").asText();
            String outputFilePath = config.get("dataValidation").get("outputFile").get("path").asText();

            // Validate files
            FileUtils.validateFiles(inputFilePath, outputFilePath, config);

            // Check if input file path is local or remote
            if (isLocalFile(inputFilePath)) {
                // Process CSV file
                FileUtils.processCsvWithRetries(inputFilePath, outputFilePath, config);
            } else {
                // Use SFTP to fetch and process file

                SftpUtils.processSftpFile(inputFilePath, outputFilePath, config);
            }

            // Convert string to HashMap for other API usage
            String exampleString = "key1:value1;key2:value2;key3:value3";
            HashMap<String, String> resultMap = FileUtils.stringToHashMap(exampleString);
            System.out.println("Converted HashMap: " + resultMap);

            // Notify successful completion
            System.out.println("Processing completed successfully. Check the log for details.");

        } catch (Exception e) {
            System.out.println("Error occurred during processing: " + e.getMessage());
            System.out.println("Please check the log file for more details: " + LOG_FILE_PATH);
            try {
                FileUtils.logError("Error occurred: " + e.getMessage());
            } catch (IOException ioException) {
                System.out.println("Logging failed: " + ioException.getMessage());
            }
        } finally {
            long endTime = System.currentTimeMillis(); // End time measurement
            long duration = endTime - startTime; // Calculate duration
            System.out.println("Time taken to process the input: " + duration + " milliseconds");
        }
    }

    private static boolean isLocalFile(String path) {
        return Files.exists(Paths.get(path)); // Returns true if file exists locally
    }
}
