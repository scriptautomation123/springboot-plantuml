package com.example.plantuml.examples;

import com.example.plantuml.PlantUMLLibrary;
import com.example.plantuml.PlantUMLLibrary.ProcessedMarkdown;
import com.example.plantuml.PlantUMLLibrary.DiagramInfo;
import net.sourceforge.plantuml.FileFormat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Standalone example demonstrating PlantUML library usage without Spring.
 * This example shows how to use the PlantUMLLibrary class directly in any Java application.
 * 
 * <p>To run this example:
 * <ol>
 *   <li>Build the project: mvn clean package -DskipTests</li>
 *   <li>Run: mvn exec:java -Dexec.mainClass="com.example.plantuml.examples.StandaloneExample"</li>
 * </ol>
 * 
 * @author Principal Engineer
 */
public class StandaloneExample {
    
    public static void main(String[] args) {
        System.out.println("=== PlantUML Library Standalone Example ===\n");
        
        try {
            // Create output directory
            Path outputDir = Paths.get("output");
            Files.createDirectories(outputDir);
            System.out.println("Output directory: " + outputDir.toAbsolutePath() + "\n");
            
            // Example 1: Simple PNG diagram
            example1_SimplePngDiagram(outputDir);
            
            // Example 2: SVG diagram
            example2_SvgDiagram(outputDir);
            
            // Example 3: Multiple formats
            example3_MultipleFormats(outputDir);
            
            // Example 4: Process markdown with PlantUML blocks
            example4_ProcessMarkdown(outputDir);
            
            // Example 5: Custom configuration
            example5_CustomConfiguration(outputDir);
            
            System.out.println("\n=== All examples completed successfully! ===");
            System.out.println("Check the 'output' directory for generated diagrams.");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Example 1: Generate a simple PNG sequence diagram.
     */
    private static void example1_SimplePngDiagram(Path outputDir) throws Exception {
        System.out.println("Example 1: Simple PNG Diagram");
        System.out.println("-------------------------------");
        
        PlantUMLLibrary library = new PlantUMLLibrary();
        
        String plantUMLCode = """
            @startuml
            actor User
            participant "Web Application" as App
            database Database
            
            User -> App: Login Request
            App -> Database: Validate Credentials
            Database -> App: User Info
            App -> User: Welcome Page
            @enduml
            """;
        
        byte[] pngData = library.generatePng(plantUMLCode);
        Path outputFile = outputDir.resolve("example1-sequence.png");
        Files.write(outputFile, pngData);
        
        System.out.println("✓ Generated: " + outputFile.getFileName());
        System.out.println("  Size: " + pngData.length + " bytes\n");
    }
    
    /**
     * Example 2: Generate an SVG class diagram.
     */
    private static void example2_SvgDiagram(Path outputDir) throws Exception {
        System.out.println("Example 2: SVG Class Diagram");
        System.out.println("-----------------------------");
        
        PlantUMLLibrary library = new PlantUMLLibrary();
        
        String plantUMLCode = """
            @startuml
            abstract class Animal {
                +String name
                +int age
                +makeSound()
            }
            
            class Dog extends Animal {
                +String breed
                +bark()
            }
            
            class Cat extends Animal {
                +boolean indoor
                +meow()
            }
            
            class Owner {
                +String name
                +List<Animal> pets
                +adoptPet(Animal)
            }
            
            Owner "1" *-- "*" Animal
            @enduml
            """;
        
        byte[] svgData = library.generateSvg(plantUMLCode);
        Path outputFile = outputDir.resolve("example2-class-diagram.svg");
        Files.write(outputFile, svgData);
        
        System.out.println("✓ Generated: " + outputFile.getFileName());
        System.out.println("  Size: " + svgData.length + " bytes\n");
    }
    
    /**
     * Example 3: Generate the same diagram in multiple formats.
     */
    private static void example3_MultipleFormats(Path outputDir) throws Exception {
        System.out.println("Example 3: Multiple Output Formats");
        System.out.println("-----------------------------------");
        
        PlantUMLLibrary library = new PlantUMLLibrary();
        
        String plantUMLCode = """
            @startuml
            [Frontend] --> [API Gateway]
            [API Gateway] --> [Auth Service]
            [API Gateway] --> [User Service]
            [API Gateway] --> [Order Service]
            [User Service] --> [Database]
            [Order Service] --> [Database]
            @enduml
            """;
        
        // Generate PNG
        byte[] png = library.generateDiagram(plantUMLCode, "PNG");
        Files.write(outputDir.resolve("example3-architecture.png"), png);
        System.out.println("✓ Generated: example3-architecture.png (" + png.length + " bytes)");
        
        // Generate SVG
        byte[] svg = library.generateDiagram(plantUMLCode, "SVG");
        Files.write(outputDir.resolve("example3-architecture.svg"), svg);
        System.out.println("✓ Generated: example3-architecture.svg (" + svg.length + " bytes)\n");
    }
    
    /**
     * Example 4: Process markdown content with PlantUML blocks.
     */
    private static void example4_ProcessMarkdown(Path outputDir) throws Exception {
        System.out.println("Example 4: Process Markdown with PlantUML Blocks");
        System.out.println("------------------------------------------------");
        
        PlantUMLLibrary library = new PlantUMLLibrary();
        
        String markdownContent = """
            # Project Architecture
            
            This document describes our system architecture.
            
            ## System Overview
            
            The system consists of the following components:
            
            ```plantuml
            @startuml
            package "Frontend" {
                [React App]
            }
            
            package "Backend" {
                [Spring Boot API]
                [Authentication Service]
            }
            
            package "Data Layer" {
                database "PostgreSQL"
                database "Redis Cache"
            }
            
            [React App] --> [Spring Boot API]
            [Spring Boot API] --> [Authentication Service]
            [Spring Boot API] --> [PostgreSQL]
            [Spring Boot API] --> [Redis Cache]
            @enduml
            ```
            
            ## User Flow
            
            Here's how users interact with the system:
            
            ```plantuml
            @startuml
            actor User
            
            User -> "React App": Open Application
            "React App" -> "Spring Boot API": Request Data
            "Spring Boot API" -> "PostgreSQL": Query Database
            "PostgreSQL" -> "Spring Boot API": Return Results
            "Spring Boot API" -> "React App": JSON Response
            "React App" -> User: Display Data
            @enduml
            ```
            """;
        
        ProcessedMarkdown result = library.processMarkdown(markdownContent);
        
        // Save processed markdown
        Path markdownFile = outputDir.resolve("example4-documentation.md");
        Files.writeString(markdownFile, result.processedContent());
        System.out.println("✓ Generated: " + markdownFile.getFileName());
        
        // Save all diagrams
        for (DiagramInfo diagram : result.diagrams()) {
            Path diagramPath = outputDir.resolve(diagram.fileName());
            Files.createDirectories(diagramPath.getParent());
            Files.write(diagramPath, diagram.data());
            System.out.println("  - Diagram: " + diagram.fileName() + " (" + diagram.data().length + " bytes)");
        }
        
        System.out.println("  Total blocks processed: " + result.totalBlocks() + "\n");
    }
    
    /**
     * Example 5: Use custom configuration.
     */
    private static void example5_CustomConfiguration(Path outputDir) throws Exception {
        System.out.println("Example 5: Custom Configuration");
        System.out.println("--------------------------------");
        
        // Create library with custom configuration
        PlantUMLLibrary.Config config = new PlantUMLLibrary.Config()
            .withDefaultFormat(FileFormat.PNG)        // Use PNG by default
            .withMaxBlocksPerFile(100)                // Allow up to 100 blocks
            .withMaxCodeSize(100_000)                 // Increase max code size
            .withValidation(true);                    // Enable validation
        
        PlantUMLLibrary library = new PlantUMLLibrary(config);
        
        String plantUMLCode = """
            @startuml
            state "Idle" as idle
            state "Processing" as processing
            state "Complete" as complete
            state "Error" as error
            
            [*] -> idle
            idle -> processing : Start
            processing -> complete : Success
            processing -> error : Failure
            complete -> [*]
            error -> idle : Retry
            error -> [*] : Cancel
            @enduml
            """;
        
        byte[] diagram = library.generatePng(plantUMLCode);
        Path outputFile = outputDir.resolve("example5-state-diagram.png");
        Files.write(outputFile, diagram);
        
        System.out.println("✓ Generated: " + outputFile.getFileName());
        System.out.println("  Using custom configuration");
        System.out.println("  Default format: PNG");
        System.out.println("  Max blocks: 100");
        System.out.println("  Max code size: 100,000 bytes");
        System.out.println("  Validation: enabled\n");
    }
}
