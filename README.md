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

[📚 Library Documentation](LIBRARY-USAGE.md) | [💡 Examples](examples/README.md)

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

[📖 Server Documentation](usage.md)

## 🚀 Quick Start

### Prerequisites
- Java 17+ (Java 21 recommended)
- Maven 3.6+

### Library Mode

**1. Add to your `pom.xml`:**
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>springboot-plantuml-server</artifactId>
    <version>1.0.0</version>
</dependency>
```

**2. Use in your code:**
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

**No Spring dependencies needed!** See [LIBRARY-USAGE.md](LIBRARY-USAGE.md) for complete guide.

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

See [usage.md](usage.md) for server documentation.

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

## 🔧 Configuration

### Library Configuration

```java
PlantUMLLibrary.Config config = new PlantUMLLibrary.Config()
    .withDefaultFormat(FileFormat.PNG)
    .withMaxBlocksPerFile(100)
    .withMaxCodeSize(100_000)
    .withValidation(true);

PlantUMLLibrary library = new PlantUMLLibrary(config);
```

### Server Configuration

Edit `src/main/resources/application.yml`:

```yaml
plantuml:
  upload:
    max-file-size: 10MB
    allowed-extensions: [.md, .markdown]
    max-plantuml-blocks-per-file: 50
  processing:
    default-format: SVG
```

## 📋 Examples

### Library Examples

```java
// Example 1: Simple diagram generation
PlantUMLLibrary library = new PlantUMLLibrary();
byte[] png = library.generatePng("@startuml\nAlice -> Bob\n@enduml");
Files.write(Path.of("diagram.png"), png);

// Example 2: Process markdown
String markdown = """
    # Documentation
    ```plantuml
    @startuml
    [User] --> [System]
    @enduml
    ```
    """;
ProcessedMarkdown result = library.processMarkdown(markdown);

// Example 3: Custom format
byte[] svg = library.generateDiagram(code, "SVG");
byte[] pdf = library.generateDiagram(code, "PDF");
```

More examples in [examples/](examples/) directory.

### Server Examples

```bash
# Health check
curl http://localhost:8080/plantuml/api/health

# Generate diagram
curl -X POST \
  -d "text=@startuml\nclass User\n@enduml" \
  -d "format=png" \
  http://localhost:8080/plantuml/api/generate

# Process markdown
curl -X POST \
  -F "markdown_file=@README.md" \
  -o output.zip \
  http://localhost:8080/plantuml/api/process-markdown
```

## 🏗️ Architecture

```
springboot-plantuml/
├── src/main/java/com/example/plantuml/
│   ├── PlantUMLLibrary.java          # ⭐ Standalone library (no Spring)
│   ├── PlantUMLServerApplication.java # Spring Boot main class
│   ├── service/                       # Business logic (Spring)
│   │   ├── PlantUMLService.java
│   │   ├── MarkdownProcessingService.java
│   │   └── ...
│   └── controller/                    # REST controllers (Spring)
├── examples/                          # Standalone examples
│   ├── StandaloneExample.java
│   └── README.md
├── pom.xml                            # Maven config (Spring = optional)
├── LIBRARY-USAGE.md                   # Library documentation
└── usage.md                           # Server documentation
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

Choose based on your needs!

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
Just use as a regular Java dependency - no Docker needed!

## 📚 Documentation

- **[LIBRARY-USAGE.md](LIBRARY-USAGE.md)** - Complete library guide
- **[usage.md](usage.md)** - Server usage guide  
- **[examples/](examples/)** - Code examples
- **[PlantUML Docs](https://plantuml.com/)** - PlantUML syntax

## 🛠️ Development

```bash
# Build
mvn clean package

# Run in dev mode
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Format code
mvn spotless:apply

# Check dependencies
mvn dependency:tree
```

## 📄 License

See LICENSE file for details.

## 🤝 Contributing

Contributions welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## 💡 Why This Project?

**Problem:** Existing PlantUML solutions require running a server or complex setup.

**Solution:** This project offers flexibility:
- **Need a library?** Import and use directly in code
- **Need a service?** Deploy as a web application

One codebase, two modes - choose what fits your needs!

## 🔗 Links

- [PlantUML Official Site](https://plantuml.com/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Maven Central](https://search.maven.org/)

---

**Built with ❤️ by Principal Engineers**
