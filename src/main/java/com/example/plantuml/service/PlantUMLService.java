package com.example.plantuml.service;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Service for generating PlantUML diagrams.
 * Provides caching and security features for diagram generation.
 * 
 * @author Principal Engineer
 */
@Service
public class PlantUMLService {

    private static final Logger logger = LoggerFactory.getLogger(PlantUMLService.class);
    
    /**
     * Generate a diagram from PlantUML source code.
     * 
     * @param plantUMLCode the PlantUML source code
     * @param format the output format (PNG, SVG)
     * @return the generated diagram as byte array
     * @throws PlantUMLException if diagram generation fails
     */
    @Cacheable(value = "plantuml-diagrams", key = "#plantUMLCode.hashCode() + '_' + #format")
    public byte[] generateDiagram(String plantUMLCode, String format) throws PlantUMLException {
        if (plantUMLCode == null || plantUMLCode.trim().isEmpty()) {
            throw new PlantUMLException("PlantUML code cannot be empty");
        }
        
        validatePlantUMLCode(plantUMLCode);
        
        FileFormat fileFormat = parseFormat(format);
        String cacheKey = generateCacheKey(plantUMLCode, format);
        
        logger.debug("Generating PlantUML diagram with cache key: {}", cacheKey);
        
        try {
            SourceStringReader reader = new SourceStringReader(plantUMLCode);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            FileFormatOption formatOption = new FileFormatOption(fileFormat);
            reader.outputImage(outputStream, formatOption);
            
            byte[] result = outputStream.toByteArray();
            
            if (result.length == 0) {
                throw new PlantUMLException("PlantUML generation resulted in empty output");
            }
            
            logger.debug("Successfully generated diagram of {} bytes", result.length);
            return result;
            
        } catch (IOException e) {
            logger.error("Failed to generate PlantUML diagram", e);
            throw new PlantUMLException("Failed to generate diagram: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate SVG diagram (convenience method).
     */
    public byte[] generateSvg(String plantUMLCode) throws PlantUMLException {
        return generateDiagram(plantUMLCode, "SVG");
    }
    
    /**
     * Generate PNG diagram (convenience method).
     */
    public byte[] generatePng(String plantUMLCode) throws PlantUMLException {
        return generateDiagram(plantUMLCode, "PNG");
    }
    
    /**
     * Validate PlantUML code for security and syntax.
     */
    private void validatePlantUMLCode(String code) throws PlantUMLException {
        // Security: Check for potentially dangerous directives
        if (code.contains("!include") && code.contains("..")) {
            throw new PlantUMLException("Path traversal in !include directive not allowed");
        }
        
        if (code.contains("!define") && code.contains("java")) {
            throw new PlantUMLException("Java execution in !define not allowed");
        }
        
        // Basic syntax validation
        if (!code.contains("@start") || !code.contains("@end")) {
            throw new PlantUMLException("PlantUML code must contain @startuml/@startsalt and @enduml/@endsalt tags");
        }
        
        // Size limits
        if (code.length() > 50_000) {
            throw new PlantUMLException("PlantUML code too large (max 50KB)");
        }
    }
    
    /**
     * Parse and validate the output format.
     */
    private FileFormat parseFormat(String format) throws PlantUMLException {
        if (format == null) {
            return FileFormat.SVG;
        }
        
        return switch (format.toUpperCase()) {
            case "PNG" -> FileFormat.PNG;
            case "SVG" -> FileFormat.SVG;
            case "EPS" -> FileFormat.EPS;
            case "PDF" -> FileFormat.PDF;
            default -> throw new PlantUMLException("Unsupported format: " + format);
        };
    }
    
    /**
     * Generate cache key for diagram caching.
     */
    private String generateCacheKey(String plantUMLCode, String format) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = plantUMLCode + ":" + format;
            byte[] hash = digest.digest(input.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple hash
            return String.valueOf((plantUMLCode + format).hashCode());
        }
    }
    
    /**
     * Custom exception for PlantUML processing errors.
     */
    public static class PlantUMLException extends Exception {
        public PlantUMLException(String message) {
            super(message);
        }
        
        public PlantUMLException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
