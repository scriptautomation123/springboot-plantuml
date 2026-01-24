# PlantUML Library Examples

This directory contains standalone examples demonstrating how to use the PlantUML library without Spring Boot.

## Overview

The `StandaloneExample.java` demonstrates various use cases:

1. **Simple PNG Diagram** - Generate basic sequence diagrams
2. **SVG Class Diagram** - Create vector graphics diagrams
3. **Multiple Formats** - Generate the same diagram in different formats
4. **Markdown Processing** - Extract and process PlantUML blocks from markdown
5. **Custom Configuration** - Use builder pattern for custom settings

## Running the Examples

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Option 1: Using Maven Exec Plugin

```bash
# Build the project first
mvn clean package -DskipTests

# Run the example using Maven
mvn exec:java -Dexec.mainClass="com.example.plantuml.examples.StandaloneExample"
```

### Option 2: Direct Java Execution

```bash
# Build the project
mvn clean package -DskipTests

# Run directly with Java
java -cp "target/springboot-plantuml-server-1.0.0.jar" \
     com.example.plantuml.examples.StandaloneExample
```

### Option 3: Copy and Use in Your Project

Simply copy the example code and adapt it to your needs:

```java
import com.example.plantuml.PlantUMLLibrary;

PlantUMLLibrary library = new PlantUMLLibrary();
byte[] png = library.generatePng("@startuml\nAlice -> Bob\n@enduml");
Files.write(Path.of("diagram.png"), png);
```

## Output

When you run the examples, diagrams will be generated in an `output/` directory:

```
output/
├── example1-sequence.png
├── example2-class-diagram.svg
├── example3-architecture.png
├── example3-architecture.svg
├── example4-documentation.md
├── diagrams/
│   ├── diagram_1.svg
│   └── diagram_2.svg
└── example5-state-diagram.png
```

## Key Features Demonstrated

### No Spring Dependencies
The examples work without any Spring framework - just plain Java with the PlantUML library.

### Simple API
```java
// Create library instance
PlantUMLLibrary library = new PlantUMLLibrary();

// Generate diagrams
byte[] png = library.generatePng(plantUMLCode);
byte[] svg = library.generateSvg(plantUMLCode);
byte[] diagram = library.generateDiagram(plantUMLCode, "PNG");
```

### Markdown Processing
```java
// Process markdown with PlantUML blocks
ProcessedMarkdown result = library.processMarkdown(markdownContent);

// Access processed content and diagrams
String markdown = result.processedContent();
List<DiagramInfo> diagrams = result.diagrams();
```

### Configuration
```java
// Custom configuration with builder pattern
PlantUMLLibrary.Config config = new PlantUMLLibrary.Config()
    .withDefaultFormat(FileFormat.PNG)
    .withMaxBlocksPerFile(100)
    .withMaxCodeSize(100_000)
    .withValidation(true);

PlantUMLLibrary library = new PlantUMLLibrary(config);
```

## Integration in Your Project

### Step 1: Add Dependency

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>springboot-plantuml-server</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Step 2: Import and Use

```java
import com.example.plantuml.PlantUMLLibrary;
import java.nio.file.Files;
import java.nio.file.Path;

public class MyApplication {
    public static void main(String[] args) throws Exception {
        PlantUMLLibrary library = new PlantUMLLibrary();
        
        String code = "@startuml\nAlice -> Bob: Hello\n@enduml";
        byte[] diagram = library.generatePng(code);
        
        Files.write(Path.of("output.png"), diagram);
        System.out.println("Diagram generated!");
    }
}
```

That's it! No Spring configuration, no application context, no server startup.

## Comparison: Library vs Server Mode

| Feature | Library Mode | Server Mode |
|---------|-------------|-------------|
| Dependencies | PlantUML only | Full Spring Boot |
| Startup time | Instant | 2-3 seconds |
| Memory usage | Minimal | ~200MB |
| Integration | Direct import | HTTP API |
| Configuration | Java code | application.yml |
| Best for | Embedded use | Standalone service |

## Additional Resources

- [LIBRARY-USAGE.md](../LIBRARY-USAGE.md) - Complete library usage guide
- [usage.md](../usage.md) - Server mode documentation
- [PlantUML Official Docs](https://plantuml.com) - PlantUML syntax reference

## Support

For questions or issues:
1. Check the examples in this directory
2. Review LIBRARY-USAGE.md for detailed documentation
3. Check PlantUML syntax at plantuml.com
4. Open an issue on GitHub
