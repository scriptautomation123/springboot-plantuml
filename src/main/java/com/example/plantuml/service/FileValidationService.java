package com.example.plantuml.service;

import com.example.plantuml.config.PlantUMLProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service for validating uploaded files and ensuring security.
 * 
 * @author Principal Engineer
 */
@Service
public class FileValidationService {

    private static final Logger logger = LoggerFactory.getLogger(FileValidationService.class);
    
    // Common malicious patterns to check for
    private static final List<Pattern> MALICIOUS_PATTERNS = Arrays.asList(
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("data:text/html", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<script", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onload=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onerror=", Pattern.CASE_INSENSITIVE)
    );
    
    // File signature checks (magic numbers)
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    
    private final PlantUMLProperties properties;
    
    public FileValidationService(PlantUMLProperties properties) {
        this.properties = properties;
    }
    
    /**
     * Validate uploaded file for security and format compliance.
     * 
     * @param file the uploaded file
     * @throws FileValidationException if validation fails
     */
    public void validateUploadedFile(MultipartFile file) throws FileValidationException {
        if (file == null || file.isEmpty()) {
            throw new FileValidationException("File is empty or null");
        }
        
        validateFileName(file.getOriginalFilename());
        validateFileSize(file.getSize());
        validateFileContent(file);
        
        logger.info("File validation passed for: {}", file.getOriginalFilename());
    }
    
    /**
     * Validate file name for security issues.
     */
    private void validateFileName(String fileName) throws FileValidationException {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new FileValidationException("File name cannot be empty");
        }
        
        // Normalize filename
        String normalizedName = fileName.trim();
        
        // Check for path traversal attempts
        if (normalizedName.contains("..") || normalizedName.contains("/") || normalizedName.contains("\\")) {
            throw new FileValidationException("File name contains invalid path characters");
        }
        
        // Check for null bytes
        if (normalizedName.contains("\0")) {
            throw new FileValidationException("File name contains null bytes");
        }
        
        // Check filename length
        if (normalizedName.length() > 255) {
            throw new FileValidationException("File name too long (max 255 characters)");
        }
        
        // Check allowed extensions
        boolean hasValidExtension = properties.upload().allowedExtensions().stream()
            .anyMatch(ext -> normalizedName.toLowerCase().endsWith(ext.toLowerCase()));
            
        if (!hasValidExtension) {
            throw new FileValidationException(
                "File extension not allowed. Allowed extensions: " + 
                properties.upload().allowedExtensions()
            );
        }
        
        // Check for potentially dangerous filenames
        String lowerName = normalizedName.toLowerCase();
        if (lowerName.startsWith("con.") || lowerName.startsWith("prn.") || 
            lowerName.startsWith("aux.") || lowerName.startsWith("nul.")) {
            throw new FileValidationException("Reserved file name not allowed");
        }
    }
    
    /**
     * Validate file size against configured limits.
     */
    private void validateFileSize(long fileSize) throws FileValidationException {
        if (fileSize <= 0) {
            throw new FileValidationException("File size must be greater than 0");
        }
        
        // Parse max file size (e.g., "10MB")
        long maxSizeBytes = parseSize(properties.upload().maxFileSize());
        
        if (fileSize > maxSizeBytes) {
            throw new FileValidationException(
                String.format("File size (%d bytes) exceeds maximum allowed (%s)", 
                             fileSize, properties.upload().maxFileSize())
            );
        }
    }
    
    /**
     * Validate file content for security issues.
     */
    private void validateFileContent(MultipartFile file) throws FileValidationException {
        try {
            byte[] content = file.getBytes();
            
            // Check if file is actually text (allow BOM)
            if (!isTextFile(content)) {
                throw new FileValidationException("File does not appear to be a text file");
            }
            
            // Convert to string for content analysis
            String textContent = new String(content, "UTF-8");
            
            // Check for malicious patterns
            for (Pattern pattern : MALICIOUS_PATTERNS) {
                if (pattern.matcher(textContent).find()) {
                    throw new FileValidationException("File contains potentially malicious content");
                }
            }
            
            // Validate markdown structure
            validateMarkdownStructure(textContent);
            
        } catch (IOException e) {
            throw new FileValidationException("Failed to read file content: " + e.getMessage());
        }
    }
    
    /**
     * Check if the file content appears to be text.
     */
    private boolean isTextFile(byte[] content) {
        if (content.length == 0) {
            return true; // Empty file is considered text
        }
        
        // Skip BOM if present
        int startIndex = 0;
        if (content.length >= 3 && 
            content[0] == UTF8_BOM[0] && 
            content[1] == UTF8_BOM[1] && 
            content[2] == UTF8_BOM[2]) {
            startIndex = 3;
        }
        
        // Check for null bytes (binary indicator)
        for (int i = startIndex; i < Math.min(content.length, 1024); i++) {
            if (content[i] == 0) {
                return false;
            }
        }
        
        // Check character distribution
        int printableChars = 0;
        int totalChars = Math.min(content.length - startIndex, 1024);
        
        for (int i = startIndex; i < startIndex + totalChars; i++) {
            byte b = content[i];
            if (b >= 32 && b <= 126 || b == 9 || b == 10 || b == 13) {
                printableChars++;
            }
        }
        
        // Require at least 95% printable characters
        return (double) printableChars / totalChars >= 0.95;
    }
    
    /**
     * Validate basic markdown structure.
     */
    private void validateMarkdownStructure(String content) throws FileValidationException {
        // Check for excessively long lines (potential DoS)
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.length() > 10000) {
                throw new FileValidationException("File contains excessively long lines");
            }
        }
        
        // Count PlantUML blocks
        int plantumlBlocks = countPlantUMLBlocks(content);
        if (plantumlBlocks > properties.upload().maxPlantumlBlocksPerFile()) {
            throw new FileValidationException(
                String.format("Too many PlantUML blocks (%d). Maximum allowed: %d", 
                             plantumlBlocks, properties.upload().maxPlantumlBlocksPerFile())
            );
        }
    }
    
    /**
     * Count PlantUML blocks in content.
     */
    private int countPlantUMLBlocks(String content) {
        Pattern pattern = Pattern.compile("```plantuml", Pattern.CASE_INSENSITIVE);
        return (int) pattern.matcher(content).results().count();
    }
    
    /**
     * Parse size string (e.g., "10MB") to bytes.
     */
    private long parseSize(String sizeString) {
        if (sizeString == null || sizeString.trim().isEmpty()) {
            return 0;
        }
        
        String size = sizeString.trim().toUpperCase();
        long multiplier = 1;
        
        if (size.endsWith("KB")) {
            multiplier = 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("MB")) {
            multiplier = 1024 * 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("GB")) {
            multiplier = 1024 * 1024 * 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("B")) {
            size = size.substring(0, size.length() - 1);
        }
        
        try {
            return Long.parseLong(size.trim()) * multiplier;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid size format: " + sizeString);
        }
    }
    
    /**
     * Custom exception for file validation errors.
     */
    public static class FileValidationException extends Exception {
        public FileValidationException(String message) {
            super(message);
        }
        
        public FileValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
