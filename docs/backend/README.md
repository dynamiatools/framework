# Backend Developer Documentation

Welcome to the DynamiaTools Backend Developer Documentation. This comprehensive guide will help you understand the architecture, core concepts, and best practices for building enterprise applications with DynamiaTools.

## 📚 Documentation Overview

This documentation is organized into the following sections:

### Getting Started
- **[Architecture Overview](./ARCHITECTURE.md)** - Understand the layered architecture and design principles of DynamiaTools
- **[Core Modules Reference](./CORE_MODULES.md)** - Detailed explanation of each platform core module
- **[Extensions Guide](./EXTENSIONS.md)** - Pre-built enterprise extensions and their purposes

### Development & Best Practices
- **[Development Patterns](./DEVELOPMENT_PATTERNS.md)** - Common patterns, anti-patterns, and best practices
- **[Advanced Topics](./ADVANCED_TOPICS.md)** - Spring integration, modularity, custom extensions, and security
- **[Examples & Integration](./EXAMPLES.md)** - Code examples for common tasks and real-world scenarios

## 🎯 Quick Navigation

### For New Developers
Start with these documents in order:
1. Read [Architecture Overview](./ARCHITECTURE.md) - Understand the big picture (15 min)
2. Read [Core Modules Reference](./CORE_MODULES.md) - Learn the key modules (20 min)
3. Explore [Examples & Integration](./EXAMPLES.md) - See code in action (15 min)
4. Reference [Development Patterns](./DEVELOPMENT_PATTERNS.md) as needed (ongoing)

**Estimated time to get started: ~50 minutes**

### For Architects & Tech Leads
- [Architecture Overview](./ARCHITECTURE.md) - Design decisions and layer organization
- [Advanced Topics](./ADVANCED_TOPICS.md) - Extensibility, modularity, and enterprise patterns
- [Extensions Guide](./EXTENSIONS.md) - Built-in modules and integration points

### For Extension Developers
- [Extensions Guide](./EXTENSIONS.md) - Understand existing extensions
- [Development Patterns](./DEVELOPMENT_PATTERNS.md) - ModuleProvider pattern, custom extensions
- [Advanced Topics](./ADVANCED_TOPICS.md) - Spring integration and bean lifecycle
- [Examples & Integration](./EXAMPLES.md) - Code examples for extensions

## 🏗️ Project Structure

```
DynamiaTools/
├── framework/                     # This repository
│   ├── platform/
│   │   ├── app/                    # Application bootstrap & metadata
│   │   ├── core/                   # Core modules
│   │   │   ├── commons/            # Utilities and common classes
│   │   │   ├── domain/             # Domain model abstractions
│   │   │   ├── domain-jpa/         # JPA implementations
│   │   │   ├── crud/               # CRUD framework
│   │   │   ├── navigation/         # Navigation & modules system
│   │   │   ├── actions/            # Actions framework
│   │   │   ├── integration/        # Spring integration
│   │   │   ├── web/                # Web utilities
│   │   │   ├── reports/            # Reporting framework
│   │   │   ├── templates/          # Template system
│   │   │   └── viewers/            # View rendering system
│   │   ├── ui/                     # UI components & themes
│   │   │   ├── zk/                 # ZK framework integration
│   │   │   └── ui-shared/          # Shared UI components
│   │   ├── packages/               # TypeScript/JavaScript packages
│   │   └── starters/               # Spring Boot starters
│   ├── extensions/                 # Enterprise extensions
│   │   ├── saas/                   # Multi-tenancy module
│   │   ├── entity-files/           # File attachment system
│   │   ├── email-sms/              # Communication module
│   │   ├── dashboard/              # Dashboard widgets
│   │   ├── reports/                # Advanced reporting
│   │   ├── finances/               # Financial calculations
│   │   ├── file-importer/          # Data import
│   │   ├── security/               # Auth & authorization
│   │   └── http-functions/         # HTTP-based functions
│   └── themes/                     # UI themes
└── website/                        # https://github.com/dynamiatools/website/
```

## 🔑 Key Concepts

### Modules
Modular building blocks that encapsulate features and functionality. Each module can define pages, navigation, and actions.

### Entities
Domain model classes mapped to database tables using JPA. Automatically get CRUD operations.

### CRUD Operations
Create, Read, Update, Delete operations performed through the `CrudService` abstraction.

### View Descriptors
YAML files that define how entities are displayed in the UI. Support for forms, tables, trees, and more.

### Actions
Reusable components that encapsulate behaviors triggered by user interactions (button clicks, menu selections, etc.).

### View Renderers
Components responsible for rendering views. Default implementation uses ZK, but you can create custom renderers.

## 📊 Technology Stack

- **Java 25** - Latest Java features and performance improvements
- **Spring Boot 4** - Modern Spring ecosystem
- **Spring Data JPA** - Database abstraction and ORM
- **ZK 10** - Enterprise-grade UI framework (default)
- **Maven 3.9+** - Build and dependency management
- **H2/PostgreSQL/MySQL** - Database support

## 🚀 Getting Started with Your First Application

### 1. Create a Spring Boot Project

Visit [start.spring.io](https://start.spring.io) and select:
- Java 25
- Spring Boot 4.x
- Spring Web
- Spring Data JPA
- Your preferred database (H2, PostgreSQL, MySQL, etc.)

### 2. Add DynamiaTools Dependencies

```xml
<!-- Platform Core -->
<dependency>
    <groupId>tools.dynamia</groupId>
    <artifactId>tools.dynamia.app</artifactId>
    <version>26.3.2</version>
</dependency>

<!-- ZK UI Framework -->
<dependency>
    <groupId>tools.dynamia</groupId>
    <artifactId>tools.dynamia.zk</artifactId>
    <version>26.3.2</version>
</dependency>

<!-- JPA Support -->
<dependency>
    <groupId>tools.dynamia</groupId>
    <artifactId>tools.dynamia.domain.jpa</artifactId>
    <version>26.3.2</version>
</dependency>
```

### 3. Enable DynamiaTools

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tools.dynamia.app.EnableDynamiaTools;

@SpringBootApplication
@EnableDynamiaTools
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### 4. Create Your First Entity

```java
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Contact {
    @Id
    @GeneratedValue
    private Long id;
    
    private String name;
    private String email;
    private String phone;
    
    // Getters and setters
}
```

### 5. Create a Module Provider

```java
import org.springframework.stereotype.Component;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.ModuleProvider;
import tools.dynamia.crud.CrudPage;

@Component
public class ContactModuleProvider implements ModuleProvider {
    @Override
    public Module getModule() {
        Module module = new Module("crm", "CRM");
        module.addPage(new CrudPage("contacts", "Contacts", Contact.class));
        return module;
    }
}
```

### 6. Run Your Application

```bash
mvn spring-boot:run
```

Visit `http://localhost:8080` and you'll see a fully functional CRUD interface!

## 📖 Recommended Reading Order

1. **[ARCHITECTURE.md](./ARCHITECTURE.md)** - Understand the foundational design
2. **[CORE_MODULES.md](./CORE_MODULES.md)** - Learn each module's responsibility
3. **[DEVELOPMENT_PATTERNS.md](./DEVELOPMENT_PATTERNS.md)** - Common patterns and best practices
4. **[EXTENSIONS.md](./EXTENSIONS.md)** - Explore pre-built enterprise features
5. **[EXAMPLES.md](./EXAMPLES.md)** - Complete code examples and real-world scenarios
6. **[ADVANCED_TOPICS.md](./ADVANCED_TOPICS.md)** - Spring integration, security, caching, and microservices


## 🔗 Additional Resources

- **Main Documentation**: https://dynamia.tools
- **GitHub Repository**: https://github.com/dynamiatools/framework
- **Maven Central**: https://search.maven.org/search?q=tools.dynamia
- **Issue Tracker**: https://github.com/dynamiatools/framework/issues
- **Discussions**: https://github.com/dynamiatools/framework/discussions

## ❓ Common Questions

**Q: What Java version is required?**
A: Java 25 or higher. The framework uses latest Java features for performance and modern syntax.

**Q: Can I use DynamiaTools with other frameworks?**
A: Yes! DynamiaTools is built on Spring Boot, so it integrates seamlessly with any Spring-compatible library.

**Q: Do I have to use ZK for the UI?**
A: No. ZK is the default renderer, but you can create custom view renderers for React, Vue, Angular, or any framework.

**Q: How do I add custom validation?**
A: Use the `Validator` interface and `@InstallValidator` annotation. See [Development Patterns](./DEVELOPMENT_PATTERNS.md).

**Q: Can I use DynamiaTools for microservices?**
A: Yes! Each module can be packaged as a JAR and deployed independently. See [Advanced Topics](./ADVANCED_TOPICS.md).

**Q: Where can I get help?**
A: Check the documentation, browse GitHub discussions, or file an issue on GitHub.

## 📝 CalVer Versioning

DynamiaTools uses Calendar Versioning (CalVer) with format `YY.MM.MINOR`:
- **26.3.2** = Year 26, Month 03 (March), Release 02
- All platform components share the same version
- No more dependency version mismatches!
