package com.example.plantuml.service;

import com.example.plantuml.config.PlantUMLProperties;
import com.example.plantuml.service.MarkdownProcessingService.DiagramInfo;
import com.example.plantuml.service.MarkdownProcessingService.ProcessedMarkdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Service for creating ZIP archives containing processed markdown and diagrams.
 * 
 * @author Principal Engineer
 */
@Service
public class ZipGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(ZipGenerationService.class);
    
    private final PlantUMLProperties properties;
    
    public ZipGenerationService(PlantUMLProperties properties) {
        this.properties = properties;
    }
    
    /**
     * Create a ZIP archive from processed markdown and diagrams.
     * 
     * @param processedMarkdown the processed markdown content and diagrams
     * @return ZIP file as byte array
     * @throws ZipGenerationException if ZIP creation fails
     */
    public byte[] createZipArchive(ProcessedMarkdown processedMarkdown) throws ZipGenerationException {
        logger.info("Creating ZIP archive for file: {}", processedMarkdown.originalFileName());
        
        try (ByteArrayOutputStream zipStream = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(zipStream)) {
            
            // Set compression level
            zipOut.setLevel(properties.output().zipCompressionLevel());
            
            // Add processed markdown file
            addProcessedMarkdownToZip(zipOut, processedMarkdown);
            
            // Add diagram files
            for (DiagramInfo diagram : processedMarkdown.diagrams()) {
                addDiagramToZip(zipOut, diagram);
            }
            
            // Optionally add source markdown if enabled
            if (properties.output().includeSourceInZip()) {
                addSourceMarkdownToZip(zipOut, processedMarkdown);
            }
            
            // Add metadata file
            addMetadataToZip(zipOut, processedMarkdown);
            
            zipOut.finish();
            byte[] result = zipStream.toByteArray();
            
            logger.info("Successfully created ZIP archive of {} bytes with {} entries", 
                       result.length, processedMarkdown.diagrams().size() + 1);
            
            return result;
            
        } catch (IOException e) {
            logger.error("Failed to create ZIP archive", e);
            throw new ZipGenerationException("Failed to create ZIP archive: " + e.getMessage(), e);
        }
    }
    
    /**
     * Add the processed markdown file to the ZIP.
     */
    private void addProcessedMarkdownToZip(ZipOutputStream zipOut, ProcessedMarkdown processedMarkdown) 
            throws IOException {
        
        String fileName = generateProcessedFileName(processedMarkdown.originalFileName());
        ZipEntry entry = new ZipEntry(fileName);
        entry.setComment("Processed markdown with PlantUML diagram references");
        
        zipOut.putNextEntry(entry);
        zipOut.write(processedMarkdown.processedContent().getBytes(StandardCharsets.UTF_8));
        zipOut.closeEntry();
        
        logger.debug("Added processed markdown: {}", fileName);
    }
    
    /**
     * Add a diagram file to the ZIP.
     */
    private void addDiagramToZip(ZipOutputStream zipOut, DiagramInfo diagram) throws IOException {
        ZipEntry entry = new ZipEntry(diagram.fileName());
        entry.setComment("Generated PlantUML diagram #" + diagram.blockNumber());
        
        zipOut.putNextEntry(entry);
        zipOut.write(diagram.data());
        zipOut.closeEntry();
        
        logger.debug("Added diagram: {} ({} bytes)", diagram.fileName(), diagram.data().length);
    }
    
    /**
     * Add the original source markdown to the ZIP for reference.
     */
    private void addSourceMarkdownToZip(ZipOutputStream zipOut, ProcessedMarkdown processedMarkdown) 
            throws IOException {
        
        String fileName = "source/" + processedMarkdown.originalFileName();
        ZipEntry entry = new ZipEntry(fileName);
        entry.setComment("Original markdown source file");
        
        zipOut.putNextEntry(entry);
        // Note: We would need the original content here, which we should pass through
        // For now, we'll add a placeholder
        String sourceNote = "# Original Source\n\nOriginal file: " + processedMarkdown.originalFileName() + 
                           "\nProcessed blocks: " + processedMarkdown.totalBlocks();
        zipOut.write(sourceNote.getBytes(StandardCharsets.UTF_8));
        zipOut.closeEntry();
        
        logger.debug("Added source reference: {}", fileName);
    }
    
    /**
     * Add metadata file to the ZIP.
     */
    private void addMetadataToZip(ZipOutputStream zipOut, ProcessedMarkdown processedMarkdown) 
            throws IOException {
        
        ZipEntry entry = new ZipEntry("metadata.txt");
        entry.setComment("Processing metadata");
        
        StringBuilder metadata = new StringBuilder();
        metadata.append("PlantUML Processing Metadata\n");
        metadata.append("============================\n\n");
        metadata.append("Original file: ").append(processedMarkdown.originalFileName()).append("\n");
        metadata.append("Processed on: ").append(java.time.Instant.now()).append("\n");
        metadata.append("Total PlantUML blocks: ").append(processedMarkdown.totalBlocks()).append("\n");
        metadata.append("Generated diagrams: ").append(processedMarkdown.diagrams().size()).append("\n");
        metadata.append("Output format: ").append(properties.processing().defaultFormat()).append("\n\n");
        
        metadata.append("Generated Files:\n");
        metadata.append("- ").append(generateProcessedFileName(processedMarkdown.originalFileName())).append("\n");
        for (DiagramInfo diagram : processedMarkdown.diagrams()) {
            metadata.append("- ").append(diagram.fileName()).append(" (").append(diagram.data().length).append(" bytes)\n");
        }
        
        zipOut.putNextEntry(entry);
        zipOut.write(metadata.toString().getBytes(StandardCharsets.UTF_8));
        zipOut.closeEntry();
        
        logger.debug("Added metadata file");
    }
    
    /**
     * Generate the processed markdown filename.
     */
    private String generateProcessedFileName(String originalFileName) {
        String baseName = removeFileExtension(originalFileName);
        return baseName + "_processed.md";
    }
    
    /**
     * Generate ZIP filename for download.
     */
    public String generateZipFileName(String originalFileName) {
        String baseName = removeFileExtension(originalFileName);
        return baseName + "_processed.zip";
    }
    
    /**
     * Remove file extension from filename.
     */
    private String removeFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(0, lastDotIndex) : fileName;
    }
    
    /**
     * Custom exception for ZIP generation errors.
     */
    public static class ZipGenerationException extends Exception {
        public ZipGenerationException(String message) {
            super(message);
        }
        
        public ZipGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
