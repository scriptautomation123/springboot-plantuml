package com.example.plantuml;

import com.example.plantuml.config.PlantUMLProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main Spring Boot application class for the PlantUML server.
 * 
 * @author Principal Engineer
 */
@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(PlantUMLProperties.class)
public class PlantUMLServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlantUMLServerApplication.class, args);
    }
}
