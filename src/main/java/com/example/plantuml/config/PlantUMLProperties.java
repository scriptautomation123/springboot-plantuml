package com.example.plantuml.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import java.util.List;

/**
 * Configuration properties for PlantUML server.
 * 
 * @author Principal Engineer
 */
@ConfigurationProperties(prefix = "plantuml.server")
@Validated
public record PlantUMLProperties(
    @NotNull Upload upload,
    @NotNull Processing processing,
    @NotNull Output output
) {
    
    public record Upload(
        @NotNull String maxFileSize,
        @NotEmpty List<String> allowedExtensions,
        @Positive int maxPlantumlBlocksPerFile
    ) {}
    
    public record Processing(
        @NotNull String defaultFormat,
        boolean enableCaching,
        @NotNull Duration cacheTtl,
        @NotNull Duration maxProcessingTime
    ) {}
    
    public record Output(
        @Positive int zipCompressionLevel,
        boolean svgEmbedFonts,
        boolean includeSourceInZip
    ) {}
}
