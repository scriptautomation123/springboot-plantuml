# PlantUML Library Usage

## Library Mode

This project can be used in two ways:
1. **As a Spring Boot server** - Full web application with REST API
2. **As a library** - Direct import and use in your Java code

## Using as a Library

### Maven Dependency

Add this to your `pom.xml`:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>springboot-plantuml-server</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Note:** When used as a library dependency, Spring Boot dependencies are optional and won't be pulled into your project. Only the PlantUML core library is required.

### Basic Usage

#### 1. Generate PNG Diagram

```java
import com.example.plantuml.PlantUMLLibrary;

public class Example {
    public static void main(String[] args) throws Exception {
        PlantUMLLibrary library = new PlantUMLLibrary();
        
        String plantUMLCode = """
            @startuml
            Alice -> Bob: Hello
            Bob -> Alice: Hi there!
            @enduml
            """;
        
        byte[] pngData = library.generatePng(plantUMLCode);
        
        // Save to file
        Files.write(Path.of("diagram.png"), pngData);
    }
}
```

#### 2. Generate SVG Diagram

```java
import com.example.plantuml.PlantUMLLibrary;

PlantUMLLibrary library = new PlantUMLLibrary();

String plantUMLCode = """
    @startuml
    class User {
        +String name
        +String email
        +login()
        +logout()
    }
    @enduml
    """;

byte[] svgData = library.generateSvg(plantUMLCode);
Files.write(Path.of("diagram.svg"), svgData);
```

#### 3. Process Markdown with PlantUML Blocks

```java
import com.example.plantuml.PlantUMLLibrary;
import com.example.plantuml.PlantUMLLibrary.ProcessedMarkdown;
import com.example.plantuml.PlantUMLLibrary.DiagramInfo;

PlantUMLLibrary library = new PlantUMLLibrary();

String markdownContent = """
    # My Documentation
    
    ## Architecture Diagram
    
    ```plantuml
    @startuml
    [User] --> [Application]
    [Application] --> [Database]
    @enduml
    ```
    
    ## Sequence Diagram
    
    ```plantuml
    @startuml
    User -> System: Request
    System -> Database: Query
    Database -> System: Result
    System -> User: Response
    @enduml
    ```
    """;

ProcessedMarkdown result = library.processMarkdown(markdownContent);

// Access processed markdown
String processedContent = result.processedContent();
System.out.println("Processed markdown:\n" + processedContent);

// Access generated diagrams
for (DiagramInfo diagram : result.diagrams()) {
    System.out.println("Diagram: " + diagram.fileName());
    Files.write(Path.of(diagram.fileName()), diagram.data());
}

System.out.println("Total blocks processed: " + result.totalBlocks());
```

#### 4. Custom Configuration

```java
import com.example.plantuml.PlantUMLLibrary;
import net.sourceforge.plantuml.FileFormat;

// Create custom configuration
PlantUMLLibrary.Config config = new PlantUMLLibrary.Config()
    .withDefaultFormat(FileFormat.PNG)
    .withMaxBlocksPerFile(100)
    .withMaxCodeSize(100_000)
    .withValidation(true);

PlantUMLLibrary library = new PlantUMLLibrary(config);

// Use library with custom config
byte[] diagram = library.generatePng(plantUMLCode);
```

#### 5. Different Output Formats

```java
PlantUMLLibrary library = new PlantUMLLibrary();

String code = "@startuml\nAlice -> Bob\n@enduml";

// Generate different formats
byte[] png = library.generateDiagram(code, "PNG");
byte[] svg = library.generateDiagram(code, "SVG");
byte[] pdf = library.generateDiagram(code, "PDF");
byte[] eps = library.generateDiagram(code, "EPS");
```

### Complete Example

```java
import com.example.plantuml.PlantUMLLibrary;
import com.example.plantuml.PlantUMLLibrary.ProcessedMarkdown;
import com.example.plantuml.PlantUMLLibrary.DiagramInfo;
import java.nio.file.Files;
import java.nio.file.Path;

public class PlantUMLLibraryExample {
    
    public static void main(String[] args) {
        try {
            // Create library instance
            PlantUMLLibrary library = new PlantUMLLibrary();
            
            // Example 1: Generate simple sequence diagram
            generateSequenceDiagram(library);
            
            // Example 2: Generate class diagram
            generateClassDiagram(library);
            
            // Example 3: Process markdown file
            processMarkdownFile(library);
            
            System.out.println("All diagrams generated successfully!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void generateSequenceDiagram(PlantUMLLibrary library) 
            throws Exception {
        String code = """
            @startuml
            actor User
            participant "Web App" as App
            database "Database" as DB
            
            User -> App: Login Request
            App -> DB: Validate Credentials
            DB -> App: User Data
            App -> User: Login Success
            @enduml
            """;
        
        byte[] png = library.generatePng(code);
        Files.write(Path.of("sequence-diagram.png"), png);
        System.out.println("Generated: sequence-diagram.png");
    }
    
    private static void generateClassDiagram(PlantUMLLibrary library) 
            throws Exception {
        String code = """
            @startuml
            class Vehicle {
                +String brand
                +int year
                +start()
                +stop()
            }
            
            class Car extends Vehicle {
                +int doors
                +openTrunk()
            }
            
            class Motorcycle extends Vehicle {
                +boolean hasSidecar
            }
            @enduml
            """;
        
        byte[] svg = library.generateSvg(code);
        Files.write(Path.of("class-diagram.svg"), svg);
        System.out.println("Generated: class-diagram.svg");
    }
    
    private static void processMarkdownFile(PlantUMLLibrary library) 
            throws Exception {
        String markdown = """
            # Project Documentation
            
            ## System Architecture
            
            ```plantuml
            @startuml
            [Frontend] --> [Backend API]
            [Backend API] --> [Database]
            [Backend API] --> [Cache]
            @enduml
            ```
            """;
        
        ProcessedMarkdown result = library.processMarkdown(markdown);
        
        // Save processed markdown
        Files.writeString(Path.of("processed.md"), result.processedContent());
        
        // Save all diagrams
        for (DiagramInfo diagram : result.diagrams()) {
            Path path = Path.of(diagram.fileName());
            Files.createDirectories(path.getParent());
            Files.write(path, diagram.data());
            System.out.println("Generated: " + diagram.fileName());
        }
    }
}
```

## Configuration Options

### Config Builder Methods

| Method | Description | Default |
|--------|-------------|---------|
| `withDefaultFormat(FileFormat)` | Set default output format | `SVG` |
| `withMaxBlocksPerFile(int)` | Max PlantUML blocks per markdown file | `50` |
| `withMaxCodeSize(int)` | Max PlantUML code size in bytes | `50,000` |
| `withValidation(boolean)` | Enable/disable code validation | `true` |

### Supported Output Formats

- **PNG** - Portable Network Graphics (raster)
- **SVG** - Scalable Vector Graphics (vector)
- **PDF** - Portable Document Format
- **EPS** - Encapsulated PostScript

## Error Handling

```java
import com.example.plantuml.PlantUMLLibrary;
import com.example.plantuml.PlantUMLLibrary.PlantUMLException;

PlantUMLLibrary library = new PlantUMLLibrary();

try {
    byte[] diagram = library.generatePng(plantUMLCode);
    // Success
} catch (PlantUMLException e) {
    // Handle PlantUML-specific errors
    System.err.println("PlantUML Error: " + e.getMessage());
}
```

## Advantages of Library Mode

1. **No Spring Dependencies** - Lightweight, only PlantUML library is required
2. **Simple API** - Easy to use with builder pattern
3. **Zero Configuration** - Works out of the box with sensible defaults
4. **Type Safety** - Compile-time checking with records and enums
5. **Thread Safe** - Can be used in multi-threaded applications
6. **No Server Required** - Direct in-process diagram generation

## Server Mode vs Library Mode

### Server Mode (Spring Boot)
- Full web interface
- REST API endpoints
- File upload handling
- Caching and monitoring
- Security features
- Docker support

### Library Mode (Standalone)
- Direct Java integration
- No HTTP overhead
- Programmatic control
- Embeddable in applications
- Minimal dependencies
- No server startup time

Choose the mode that best fits your use case!
