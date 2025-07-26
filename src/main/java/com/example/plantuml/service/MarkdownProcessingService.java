package com.example.plantuml.service;

import com.example.plantuml.config.PlantUMLProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for processing Markdown files containing PlantUML blocks.
 * Extracts PlantUML code blocks and generates corresponding diagrams.
 * 
 * @author Principal Engineer
 */
@Service
public class MarkdownProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(MarkdownProcessingService.class);
    
    private static final Pattern PLANTUML_PATTERN = Pattern.compile(
        "```plantuml\\s*\\n(.*?)\\n```", 
        Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );
    
    private final PlantUMLService plantUMLService;
    private final PlantUMLProperties properties;
    
    public MarkdownProcessingService(PlantUMLService plantUMLService, PlantUMLProperties properties) {
        this.plantUMLService = plantUMLService;
        this.properties = properties;
    }
    
    /**
     * Process markdown content and extract PlantUML blocks.
     * 
     * @param markdownContent the original markdown content
     * @param fileName the original file name for reference
     * @return processed markdown with diagram references
     * @throws MarkdownProcessingException if processing fails
     */
    public ProcessedMarkdown processMarkdown(String markdownContent, String fileName) 
            throws MarkdownProcessingException {
        
        validateInput(markdownContent, fileName);
        
        List<DiagramInfo> diagrams = new ArrayList<>();
        Matcher matcher = PLANTUML_PATTERN.matcher(markdownContent);
        StringBuffer result = new StringBuffer();
        int blockCount = 0;
        
        logger.info("Processing markdown file: {}", fileName);
        
        while (matcher.find()) {
            blockCount++;
            
            if (blockCount > properties.upload().maxPlantumlBlocksPerFile()) {
                throw new MarkdownProcessingException(
                    "Too many PlantUML blocks. Maximum allowed: " + 
                    properties.upload().maxPlantumlBlocksPerFile()
                );
            }
            
            String plantUMLCode = matcher.group(1).trim();
            logger.debug("Processing PlantUML block #{}: {} characters", blockCount, plantUMLCode.length());
            
            try {
                // Generate diagram
                byte[] diagramData = plantUMLService.generateDiagram(
                    plantUMLCode, 
                    properties.processing().defaultFormat()
                );
                
                // Create diagram info
                String diagramFileName = generateDiagramFileName(fileName, blockCount);
                DiagramInfo diagramInfo = new DiagramInfo(
                    diagramFileName,
                    diagramData,
                    plantUMLCode,
                    blockCount
                );
                diagrams.add(diagramInfo);
                
                // Replace PlantUML block with image reference
                String replacement = generateImageReference(diagramFileName, blockCount);
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
                
                logger.debug("Successfully processed block #{}", blockCount);
                
            } catch (PlantUMLService.PlantUMLException e) {
                logger.warn("Failed to process PlantUML block #{}: {}", blockCount, e.getMessage());
                
                // Keep original block but add error comment
                String errorComment = "<!-- PlantUML Error: " + e.getMessage() + " -->\n";
                String originalBlock = matcher.group(0);
                matcher.appendReplacement(result, 
                    Matcher.quoteReplacement(errorComment + originalBlock));
            }
        }
        
        matcher.appendTail(result);
        
        logger.info("Processed {} PlantUML blocks from file: {}", blockCount, fileName);
        
        return new ProcessedMarkdown(
            result.toString(),
            diagrams,
            fileName,
            blockCount
        );
    }
    
    /**
     * Validate input parameters.
     */
    private void validateInput(String markdownContent, String fileName) throws MarkdownProcessingException {
        if (markdownContent == null || markdownContent.trim().isEmpty()) {
            throw new MarkdownProcessingException("Markdown content cannot be empty");
        }
        
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new MarkdownProcessingException("File name cannot be empty");
        }
        
        // Check if file extension is allowed
        boolean isAllowed = properties.upload().allowedExtensions().stream()
            .anyMatch(ext -> fileName.toLowerCase().endsWith(ext.toLowerCase()));
            
        if (!isAllowed) {
            throw new MarkdownProcessingException(
                "File extension not allowed. Allowed extensions: " + 
                properties.upload().allowedExtensions()
            );
        }
        
        // Content size validation
        if (markdownContent.length() > 1_000_000) { // 1MB text limit
            throw new MarkdownProcessingException("Markdown content too large (max 1MB)");
        }
    }
    
    /**
     * Generate diagram file name based on original file name and block number.
     */
    private String generateDiagramFileName(String originalFileName, int blockNumber) {
        String baseName = removeFileExtension(originalFileName);
        String extension = properties.processing().defaultFormat().toLowerCase();
        return String.format("diagrams/%s_diagram_%d.%s", baseName, blockNumber, extension);
    }
    
    /**
     * Generate markdown image reference.
     */
    private String generateImageReference(String diagramFileName, int blockNumber) {
        return String.format("![PlantUML Diagram %d](%s)", blockNumber, diagramFileName);
    }
    
    /**
     * Remove file extension from filename.
     */
    private String removeFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(0, lastDotIndex) : fileName;
    }
    
    /**
     * Represents a processed PlantUML diagram.
     */
    public record DiagramInfo(
        String fileName,
        byte[] data,
        String sourceCode,
        int blockNumber
    ) {}
    
    /**
     * Represents the result of markdown processing.
     */
    public record ProcessedMarkdown(
        String processedContent,
        List<DiagramInfo> diagrams,
        String originalFileName,
        int totalBlocks
    ) {}
    
    /**
     * Custom exception for markdown processing errors.
     */
    public static class MarkdownProcessingException extends Exception {
        public MarkdownProcessingException(String message) {
            super(message);
        }
        
        public MarkdownProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
