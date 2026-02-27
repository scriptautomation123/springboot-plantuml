# Spring Boot PlantUML - Server & Library

[![Java 21](https://img.shields.io/badge/Java-21-blue.svg)](https://adoptium.net/)
[![Spring Boot 3.3.4](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PlantUML 1.2025.4](https://img.shields.io/badge/PlantUML-1.2025.4-orange.svg)](https://plantuml.com/)

Modern PlantUML diagram generation - use as a **Spring Boot server** or **standalone library**.

## 🎯 Two Ways to Use

### 1. Library Mode (NEW!)
Import directly into your Java code - **no Spring required**:

```java
import com.example.plantuml.PlantUMLLibrary;

PlantUMLLibrary library = new PlantUMLLibrary();
byte[] diagram = library.generatePng("@startuml\nAlice -> Bob\n@enduml");
Files.write(Path.of("diagram.png"), diagram);
```

**Perfect for:**
- Embedding in applications
- Build tools and generators
- Direct programmatic control
- Minimal dependencies

### 2. Server Mode
Full-featured web application with REST API:

```bash
mvn spring-boot:run
# Access at http://localhost:8080/plantuml/
```

**Perfect for:**
- Web-based diagram generation
- Team collaboration
- REST API integration
- File upload and processing

## 🚀 Quick Start

### Prerequisites
- Java 17+ (Java 21 recommended)
- Maven 3.6+

### Library Mode

The library is published automatically to **GitHub Packages** whenever a commit is pushed to `main`.

**1. Authenticate with GitHub Packages**

**Option A — GitHub Codespaces (recommended, zero secrets to manage)**

`GITHUB_TOKEN` is injected automatically into every Codespace with the
`read:packages` scope. The devcontainer `postCreateCommand` runs
`.devcontainer/setup-maven.sh`, which writes `~/.m2/settings.xml` from
that token automatically — no manual steps required.

If you need a personal access token instead (e.g. for wider scopes), create
a **Codespace secret** named `MAVEN_GITHUB_TOKEN` at
<https://github.com/settings/codespaces>. The setup script prefers
`MAVEN_GITHUB_TOKEN` over the auto-injected `GITHUB_TOKEN` when both are
present. Your token is **never committed to the repository**.

**Option B — local development**

Add your credentials to `~/.m2/settings.xml` on your local machine:
```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <!-- Create a Personal Access Token with read:packages scope at
           https://github.com/settings/tokens -->
      <password>YOUR_GITHUB_TOKEN</password>
    </server>
  </servers>
</settings>
```
> ⚠️ Never commit `~/.m2/settings.xml` or any file containing a raw token
> to source control.

**2. Add the GitHub Packages repository to your `pom.xml`:**
```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/scriptautomation123/springboot-plantuml</url>
  </repository>
</repositories>
```

**3. Add the dependency to your `pom.xml`:**
```xml
<dependency>
  <groupId>com.example</groupId>
  <artifactId>springboot-plantuml-server</artifactId>
  <version>1.0.0</version>
</dependency>
```

**4. Use in your code:**
```java
PlantUMLLibrary library = new PlantUMLLibrary();

// Generate PNG
byte[] png = library.generatePng("""
  @startuml
  Alice -> Bob: Hello
  Bob -> Alice: Hi!
  @enduml
  """);

// Generate SVG
byte[] svg = library.generateSvg(plantUMLCode);

// Process markdown with PlantUML blocks
ProcessedMarkdown result = library.processMarkdown(markdownContent);
```

**No Spring dependencies needed.**

### Server Mode

**1. Build and run:**
```bash
mvn clean package
java -jar target/springboot-plantuml-server-1.0.0.jar
```

**2. Access the application:**
- Web UI: http://localhost:8080/plantuml/
- API: http://localhost:8080/plantuml/api/

**3. Use the REST API:**
```bash
# Process markdown file
curl -X POST -F "markdown_file=@document.md" \
   -o result.zip \
   http://localhost:8080/plantuml/api/process-markdown

# Generate diagram
curl -X POST \
   -d "text=@startuml\nAlice->Bob\n@enduml" \
   -d "format=svg" \
   http://localhost:8080/plantuml/api/generate
```

## 📦 Features

### Library Mode
✅ **Zero Spring dependencies** - Just PlantUML library  
✅ **Simple API** - Easy to use, builder pattern for config  
✅ **Type safe** - Java records and enums  
✅ **Thread safe** - Use in multi-threaded apps  
✅ **Instant startup** - No server overhead  
✅ **Markdown processing** - Extract PlantUML from markdown  

### Server Mode
✅ **REST API** - Generate diagrams via HTTP  
✅ **Web Interface** - User-friendly file upload  
✅ **Security** - Input validation, CSRF protection  
✅ **Monitoring** - Actuator endpoints, Prometheus metrics  
✅ **Caching** - Improve performance  
✅ **Docker support** - Easy deployment  

## 🔧 Library Usage (Complete)

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

String processedContent = result.processedContent();
System.out.println("Processed markdown:\n" + processedContent);

for (DiagramInfo diagram : result.diagrams()) {
  Files.write(Path.of(diagram.fileName()), diagram.data());
}

System.out.println("Total blocks processed: " + result.totalBlocks());
```

#### 4. Custom Configuration

```java
import com.example.plantuml.PlantUMLLibrary;
import net.sourceforge.plantuml.FileFormat;

PlantUMLLibrary.Config config = new PlantUMLLibrary.Config()
  .withDefaultFormat(FileFormat.PNG)
  .withMaxBlocksPerFile(100)
  .withMaxCodeSize(100_000)
  .withValidation(true);

PlantUMLLibrary library = new PlantUMLLibrary(config);
byte[] diagram = library.generatePng(plantUMLCode);
```

#### 5. Different Output Formats

```java
PlantUMLLibrary library = new PlantUMLLibrary();

String code = "@startuml\nAlice -> Bob\n@enduml";

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
      PlantUMLLibrary library = new PlantUMLLibrary();
            
      generateSequenceDiagram(library);
      generateClassDiagram(library);
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
        
    Files.writeString(Path.of("processed.md"), result.processedContent());
        
    for (DiagramInfo diagram : result.diagrams()) {
      Path path = Path.of(diagram.fileName());
      Files.createDirectories(path.getParent());
      Files.write(path, diagram.data());
      System.out.println("Generated: " + diagram.fileName());
    }
  }
}
```

## ⚙️ Library Configuration Options

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

## ❗ Error Handling

```java
import com.example.plantuml.PlantUMLLibrary;
import com.example.plantuml.PlantUMLLibrary.PlantUMLException;

PlantUMLLibrary library = new PlantUMLLibrary();

try {
  byte[] diagram = library.generatePng(plantUMLCode);
} catch (PlantUMLException e) {
  System.err.println("PlantUML Error: " + e.getMessage());
}
```

## 🧱 Architecture

```
springboot-plantuml/
├── src/main/java/com/example/plantuml/
│   ├── PlantUMLLibrary.java           # ⭐ Standalone library (no Spring)
│   ├── PlantUMLServerApplication.java # Spring Boot main class
│   ├── service/                        # Business logic (Spring)
│   │   ├── PlantUMLService.java
│   │   ├── MarkdownProcessingService.java
│   │   └── ...
│   └── controller/                     # REST controllers (Spring)
├── examples/                           # Standalone examples
│   ├── StandaloneExample.java
│   └── README.md
├── pom.xml                             # Maven config (Spring = optional)
└── usage.md                            # Server documentation
```

**Key Design:**
- `PlantUMLLibrary` = Standalone, no Spring dependencies
- Spring dependencies marked as `<optional>true</optional>`
- Library users only get PlantUML, not Spring
- Server users get full Spring Boot stack

## 🔄 Library vs Server

| Aspect | Library Mode | Server Mode |
|--------|--------------|-------------|
| **Dependencies** | PlantUML only | Full Spring Boot |
| **Startup** | Instant | ~2-3 seconds |
| **Memory** | ~50MB | ~200MB |
| **Integration** | `import` in code | HTTP API |
| **Config** | Java builder | YAML files |
| **Use Case** | Embedded, tools | Service, web app |

## 🧪 Testing

```bash
# Run tests
mvn test

# Run with coverage
mvn test jacoco:report

# Integration tests
mvn integration-test
```

## 🐳 Docker

### Server Mode

```bash
# Build Docker image
mvn spring-boot:build-image

# Run container
docker run -p 8080:8080 plantuml-springboot-server:1.0.0
```

Or use Docker Compose:
```bash
docker-compose up
```

### Library Mode
Just use as a regular Java dependency - no Docker needed.

## ✅ Refactoring Summary (Library Option)

### What Was Done
- **Created `PlantUMLLibrary`**: standalone facade with zero Spring dependencies.
- **Made Spring dependencies optional** in `pom.xml` so library users only pull PlantUML.
- **Added examples** including a full standalone example.
- **Improved validation** with case-insensitive security checks.
- **Documented thoroughly** with usage guides and examples.

### Design Principles Applied
- **Not over-engineered**: single facade, no unnecessary abstractions.
- **Separation of concerns**: server mode unchanged, library mode independent.
- **Backward compatible**: no breaking changes for server users.
- **Security-conscious**: input validation and safe defaults.
- **Thread-safe**: safe for concurrent usage.

### Benefits Delivered

**For library users**
- Zero Spring dependencies
- Instant startup, minimal overhead
- Simple, type-safe API
- Markdown processing support

**For server users**
- No breaking changes
- Same REST endpoints and UI
- Same Docker and deployment flows

**For maintainers**
- One codebase, two modes
- Clear documentation and examples
- Easy to test independently

## 📄 License

See LICENSE file for details.

## 🤝 Contributing

Contributions welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## 🔗 Links

- [PlantUML Official Site](https://plantuml.com/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Maven Central](https://search.maven.org/)

---

**Built with ❤️ by Principal Engineers**
