package com.example.plantuml;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple library facade for PlantUML diagram generation.
 * Can be used directly without Spring framework dependencies.
 * 
 * <p>Example usage:
 * <pre>
 * PlantUMLLibrary library = new PlantUMLLibrary();
 * 
 * // Generate a PNG diagram
 * byte[] png = library.generatePng("@startuml\nAlice -> Bob\n@enduml");
 * 
 * // Generate an SVG diagram
 * byte[] svg = library.generateSvg("@startuml\nAlice -> Bob\n@enduml");
 * 
 * // Process markdown with PlantUML blocks
 * ProcessedMarkdown result = library.processMarkdown(markdownContent);
 * </pre>
 * 
 * @author Principal Engineer
 */
public class PlantUMLLibrary {
    
    private static final Pattern PLANTUML_PATTERN = Pattern.compile(
        "```plantuml\\s*\\n(.*?)\\n```", 
        Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );
    
    private final Config config;
    
    /**
     * Create library with default configuration.
     */
    public PlantUMLLibrary() {
        this(new Config());
    }
    
    /**
     * Create library with custom configuration.
     * 
     * @param config the configuration to use
     */
    public PlantUMLLibrary(Config config) {
        this.config = config;
    }
    
    /**
     * Generate PlantUML diagram as PNG.
     * 
     * @param plantUMLCode the PlantUML source code
     * @return PNG image data
     * @throws PlantUMLException if generation fails
     */
    public byte[] generatePng(String plantUMLCode) throws PlantUMLException {
        return generateDiagram(plantUMLCode, FileFormat.PNG);
    }
    
    /**
     * Generate PlantUML diagram as SVG.
     * 
     * @param plantUMLCode the PlantUML source code
     * @return SVG image data
     * @throws PlantUMLException if generation fails
     */
    public byte[] generateSvg(String plantUMLCode) throws PlantUMLException {
        return generateDiagram(plantUMLCode, FileFormat.SVG);
    }
    
    /**
     * Generate PlantUML diagram with specified format.
     * 
     * @param plantUMLCode the PlantUML source code
     * @param format the output format (PNG, SVG, PDF, EPS)
     * @return diagram data
     * @throws PlantUMLException if generation fails
     */
    public byte[] generateDiagram(String plantUMLCode, String format) throws PlantUMLException {
        FileFormat fileFormat = parseFormat(format);
        return generateDiagram(plantUMLCode, fileFormat);
    }
    
    /**
     * Process markdown content containing PlantUML code blocks.
     * Extracts PlantUML blocks and generates diagrams.
     * 
     * @param markdownContent the markdown content to process
     * @return processed markdown with diagram information
     * @throws PlantUMLException if processing fails
     */
    public ProcessedMarkdown processMarkdown(String markdownContent) throws PlantUMLException {
        if (markdownContent == null || markdownContent.trim().isEmpty()) {
            throw new PlantUMLException("Markdown content cannot be empty");
        }
        
        List<DiagramInfo> diagrams = new ArrayList<>();
        Matcher matcher = PLANTUML_PATTERN.matcher(markdownContent);
        StringBuffer result = new StringBuffer();
        int blockCount = 0;
        
        while (matcher.find()) {
            blockCount++;
            
            if (blockCount > config.maxBlocksPerFile) {
                throw new PlantUMLException("Too many PlantUML blocks. Maximum: " + config.maxBlocksPerFile);
            }
            
            String plantUMLCode = matcher.group(1).trim();
            
            try {
                // Generate diagram
                byte[] diagramData = generateDiagram(plantUMLCode, config.defaultFormat);
                
                // Create diagram info
                String diagramFileName = String.format("diagram_%d.%s", 
                    blockCount, 
                    config.defaultFormat.name().toLowerCase()
                );
                
                DiagramInfo diagramInfo = new DiagramInfo(
                    diagramFileName,
                    diagramData,
                    plantUMLCode,
                    blockCount
                );
                diagrams.add(diagramInfo);
                
                // Replace PlantUML block with image reference
                String replacement = String.format("![PlantUML Diagram %d](%s)", 
                    blockCount, 
                    diagramFileName
                );
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
                
            } catch (PlantUMLException e) {
                // Keep original block but add error comment
                String errorComment = "<!-- PlantUML Error: " + e.getMessage() + " -->\n";
                String originalBlock = matcher.group(0);
                matcher.appendReplacement(result, 
                    Matcher.quoteReplacement(errorComment + originalBlock));
            }
        }
        
        matcher.appendTail(result);
        
        return new ProcessedMarkdown(result.toString(), diagrams, blockCount);
    }
    
    // Private helper methods
    
    private byte[] generateDiagram(String plantUMLCode, FileFormat format) throws PlantUMLException {
        if (plantUMLCode == null || plantUMLCode.trim().isEmpty()) {
            throw new PlantUMLException("PlantUML code cannot be empty");
        }
        
        if (config.validateCode) {
            validatePlantUMLCode(plantUMLCode);
        }
        
        try {
            SourceStringReader reader = new SourceStringReader(plantUMLCode);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            FileFormatOption formatOption = new FileFormatOption(format);
            reader.outputImage(outputStream, formatOption);
            
            byte[] result = outputStream.toByteArray();
            
            if (result.length == 0) {
                throw new PlantUMLException("PlantUML generation resulted in empty output");
            }
            
            return result;
            
        } catch (IOException e) {
            throw new PlantUMLException("Failed to generate diagram: " + e.getMessage(), e);
        }
    }
    
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
            throw new PlantUMLException("PlantUML code must contain @start/@end tags");
        }
        
        // Size limits
        if (code.length() > config.maxCodeSize) {
            throw new PlantUMLException("PlantUML code too large (max " + config.maxCodeSize + " bytes)");
        }
    }
    
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
    
    // Configuration class
    
    /**
     * Configuration for PlantUMLLibrary.
     * Use builder pattern for easy configuration.
     */
    public static class Config {
        private FileFormat defaultFormat = FileFormat.SVG;
        private int maxBlocksPerFile = 50;
        private int maxCodeSize = 50_000;
        private boolean validateCode = true;
        
        public Config() {}
        
        public Config withDefaultFormat(FileFormat format) {
            this.defaultFormat = format;
            return this;
        }
        
        public Config withMaxBlocksPerFile(int max) {
            this.maxBlocksPerFile = max;
            return this;
        }
        
        public Config withMaxCodeSize(int size) {
            this.maxCodeSize = size;
            return this;
        }
        
        public Config withValidation(boolean validate) {
            this.validateCode = validate;
            return this;
        }
    }
    
    // Data classes
    
    /**
     * Information about a generated diagram.
     */
    public record DiagramInfo(
        String fileName,
        byte[] data,
        String sourceCode,
        int blockNumber
    ) {}
    
    /**
     * Result of markdown processing.
     */
    public record ProcessedMarkdown(
        String processedContent,
        List<DiagramInfo> diagrams,
        int totalBlocks
    ) {}
    
    /**
     * Exception for PlantUML processing errors.
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
