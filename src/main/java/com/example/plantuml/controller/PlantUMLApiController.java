package com.example.plantuml.controller;

import com.example.plantuml.service.*;
import com.example.plantuml.service.MarkdownProcessingService.ProcessedMarkdown;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

/**
 * REST controller for PlantUML diagram generation and markdown processing.
 * 
 * @author Principal Engineer
 */
@RestController
@RequestMapping("/api")
@Validated
public class PlantUMLApiController {

    private static final Logger logger = LoggerFactory.getLogger(PlantUMLApiController.class);
    
    private final PlantUMLService plantUMLService;
    private final MarkdownProcessingService markdownService;
    private final ZipGenerationService zipService;
    private final FileValidationService validationService;
    
    public PlantUMLApiController(
            PlantUMLService plantUMLService,
            MarkdownProcessingService markdownService,
            ZipGenerationService zipService,
            FileValidationService validationService) {
        this.plantUMLService = plantUMLService;
        this.markdownService = markdownService;
        this.zipService = zipService;
        this.validationService = validationService;
    }
    
    /**
     * Generate PlantUML diagram from source code.
     * 
     * @param text PlantUML source code
     * @param format Output format (png, svg, pdf, eps)
     * @return Base64 encoded diagram
     */
    @PostMapping("/generate")
    @Timed(value = "plantuml.generate", description = "Time taken to generate PlantUML diagram")
    public ResponseEntity<String> generateDiagram(
            @RequestParam @NotNull String text,
            @RequestParam(defaultValue = "svg") String format) {
        
        try {
            logger.info("Generating PlantUML diagram, format: {}, size: {} chars", format, text.length());
            
            byte[] diagramData = plantUMLService.generateDiagram(text, format);
            String base64Data = Base64.getEncoder().encodeToString(diagramData);
            
            String contentType = switch (format.toLowerCase()) {
                case "png" -> "image/png";
                case "svg" -> "image/svg+xml";
                case "pdf" -> "application/pdf";
                case "eps" -> "application/postscript";
                default -> "application/octet-stream";
            };
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(base64Data);
                    
        } catch (PlantUMLService.PlantUMLException e) {
            logger.warn("Failed to generate PlantUML diagram: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Process markdown file and return ZIP with processed content and diagrams.
     * 
     * @param file Uploaded markdown file
     * @return ZIP file containing processed markdown and diagram images
     */
    @PostMapping("/process-markdown")
    @Timed(value = "plantuml.process.markdown", description = "Time taken to process markdown file")
    public ResponseEntity<byte[]> processMarkdown(
            @RequestParam("markdown_file") MultipartFile file) {
        
        try {
            logger.info("Processing markdown file: {}", file.getOriginalFilename());
            
            // Validate uploaded file
            validationService.validateUploadedFile(file);
            
            // Read file content
            String markdownContent = new String(file.getBytes(), "UTF-8");
            
            // Process markdown
            ProcessedMarkdown processed = markdownService.processMarkdown(
                markdownContent, 
                file.getOriginalFilename()
            );
            
            // Generate ZIP archive
            byte[] zipData = zipService.createZipArchive(processed);
            
            // Prepare response headers
            String downloadFileName = zipService.generateZipFileName(file.getOriginalFilename());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", downloadFileName);
            headers.setContentLength(zipData.length);
            
            logger.info("Successfully processed markdown file: {} -> {} bytes", 
                       file.getOriginalFilename(), zipData.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipData);
                    
        } catch (FileValidationService.FileValidationException e) {
            logger.warn("File validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("Validation error: " + e.getMessage()).getBytes());
                    
        } catch (MarkdownProcessingService.MarkdownProcessingException e) {
            logger.warn("Markdown processing failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("Processing error: " + e.getMessage()).getBytes());
                    
        } catch (ZipGenerationService.ZipGenerationException e) {
            logger.error("ZIP generation failed: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(("ZIP creation error: " + e.getMessage()).getBytes());
                    
        } catch (Exception e) {
            logger.error("Unexpected error processing markdown", e);
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Internal server error".getBytes());
        }
    }
    
    /**
     * Health check endpoint for the API.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("PlantUML API is healthy");
    }
}
