package com.example.plantuml.exception;

import com.example.plantuml.service.FileValidationService;
import com.example.plantuml.service.MarkdownProcessingService;
import com.example.plantuml.service.PlantUMLService;
import com.example.plantuml.service.ZipGenerationService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.ModelAndView;

import java.time.Instant;


/**
 * Global exception handler for the PlantUML application.
 * Provides centralized error handling with appropriate responses for both web and API requests.
 * 
 * @author Principal Engineer
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle PlantUML service exceptions.
     */
    @ExceptionHandler(PlantUMLService.PlantUMLException.class)
    public ResponseEntity<ErrorResponse> handlePlantUMLException(
            PlantUMLService.PlantUMLException ex,
            HttpServletRequest request) {
        
        logger.warn("PlantUML processing error: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "PLANTUML_ERROR",
            "PlantUML processing failed: " + ex.getMessage(),
            request.getRequestURI(),
            Instant.now()
        );
        
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }
    
    /**
     * Handle markdown processing exceptions.
     */
    @ExceptionHandler(MarkdownProcessingService.MarkdownProcessingException.class)
    public ResponseEntity<ErrorResponse> handleMarkdownProcessingException(
            MarkdownProcessingService.MarkdownProcessingException ex,
            HttpServletRequest request) {
        
        logger.warn("Markdown processing error: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "MARKDOWN_ERROR",
            "Markdown processing failed: " + ex.getMessage(),
            request.getRequestURI(),
            Instant.now()
        );
        
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }
    
    /**
     * Handle file validation exceptions.
     */
    @ExceptionHandler(FileValidationService.FileValidationException.class)
    public ResponseEntity<ErrorResponse> handleFileValidationException(
            FileValidationService.FileValidationException ex,
            HttpServletRequest request) {
        
        logger.warn("File validation error: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            "File validation failed: " + ex.getMessage(),
            request.getRequestURI(),
            Instant.now()
        );
        
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }
    
    /**
     * Handle ZIP generation exceptions.
     */
    @ExceptionHandler(ZipGenerationService.ZipGenerationException.class)
    public ResponseEntity<ErrorResponse> handleZipGenerationException(
            ZipGenerationService.ZipGenerationException ex,
            HttpServletRequest request) {
        
        logger.error("ZIP generation error: {}", ex.getMessage(), ex);
        
        ErrorResponse error = new ErrorResponse(
            "ZIP_ERROR",
            "ZIP file generation failed",
            request.getRequestURI(),
            Instant.now()
        );
        
        return ResponseEntity.internalServerError()
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }
    
    /**
     * Handle file upload size exceeded exceptions.
     */
    @ExceptionHandler({MaxUploadSizeExceededException.class, MultipartException.class})
    public ResponseEntity<ErrorResponse> handleFileUploadException(
            Exception ex,
            HttpServletRequest request) {
        
        logger.warn("File upload error: {}", ex.getMessage());
        
        String message = ex instanceof MaxUploadSizeExceededException 
            ? "File size exceeds maximum allowed limit"
            : "File upload error: " + ex.getMessage();
        
        ErrorResponse error = new ErrorResponse(
            "UPLOAD_ERROR",
            message,
            request.getRequestURI(),
            Instant.now()
        );
        
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }
    
    /**
     * Handle validation exceptions.
     */
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            jakarta.validation.ConstraintViolationException ex,
            HttpServletRequest request) {
        
        logger.warn("Validation error: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            "Input validation failed: " + ex.getMessage(),
            request.getRequestURI(),
            Instant.now()
        );
        
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }
    
    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public Object handleGenericException(Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error", ex);
        
        // For web requests, return error page
        if (isWebRequest(request)) {
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("error", "An unexpected error occurred");
            mav.addObject("status", 500);
            mav.addObject("timestamp", Instant.now());
            return mav;
        }
        
        // For API requests, return JSON error
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_ERROR",
            "An unexpected error occurred",
            request.getRequestURI(),
            Instant.now()
        );
        
        return ResponseEntity.internalServerError()
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }
    
    /**
     * Determine if the request is a web request (HTML) vs API request (JSON).
     */
    private boolean isWebRequest(HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        String contentType = request.getContentType();
        String uri = request.getRequestURI();
        
        // If it's an API endpoint, treat as API request
        if (uri.startsWith("/api/")) {
            return false;
        }
        
        // If client accepts HTML, treat as web request
        if (acceptHeader != null && acceptHeader.contains("text/html")) {
            return true;
        }
        
        // If content type indicates form submission, treat as web request
        if (contentType != null && contentType.contains("multipart/form-data")) {
            return true;
        }
        
        // Default to API request
        return false;
    }
    
    /**
     * Standardized error response format.
     */
    public record ErrorResponse(
        String code,
        String message,
        String path,
        Instant timestamp
    ) {}
}
