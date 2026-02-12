
[![Maven Central](https://img.shields.io/maven-central/v/tools.dynamia/tools.dynamia.app)](https://search.maven.org/search?q=tools.dynamia)
![Java Version Required](https://img.shields.io/badge/java-25-blue)
![Version](https://img.shields.io/badge/version-26.2.0-brightgreen)
![CalVer](https://img.shields.io/badge/versioning-CalVer%20YY.MM.MINOR-blue)
[![Java CI with Maven](https://github.com/dynamiatools/framework/actions/workflows/maven.yml/badge.svg)](https://github.com/dynamiatools/framework/actions/workflows/maven.yml)
[![Release and Deploy](https://github.com/dynamiatools/framework/actions/workflows/release.yml/badge.svg)](https://github.com/dynamiatools/framework/actions/workflows/release.yml)
[![DynamiaTools Documentation](https://img.shields.io/badge/DynamiaTools-Documentation-orange)](https://dynamia.tools/getting-started/)


<h1 align="center">Dynamia Platform</h1>

> **The Next Generation of DynamiaTools**

**Dynamia Platform** is a cutting-edge full-stack Java 25 framework engineered for building robust, scalable enterprise web applications. Powered by **Spring Boot 4** and **ZK 10**, this new generation consolidates the core framework with essential enterprise extensions in a unified, batteries-included platform.

### 📅 CalVer Versioning

Starting with version **26.2.0**, Dynamia Platform adopts **Calendar Versioning (CalVer)** with the format `YY.MM.MINOR`. This means:
- **All modules share the same version**: Core, extensions, starters, themes—everything is released together
- **26.2.0** = First release of February 2026 (Year 26, Month 02, Release 0)
- **26.2.1** = Second release of February 2026
- **26.3.0** = First release of March 2026
- **Unified releases** ensure compatibility and simplify dependency management
- No more version mismatches between platform components!

**Examples**:
- `26.2.0` → February 2026, first release
- `26.2.1` → February 2026, second release (hotfix or minor update)
- `26.12.3` → December 2026, fourth release

## 🚀 What Makes Dynamia Platform Special

Dynamia Platform represents a major evolution from DynamiaTools, bringing together:

- **Unified Repository**: Core framework + enterprise extensions in one place
- **Production-Ready Extensions**: Pre-integrated modules for common enterprise needs
- **Zero-Config Philosophy**: Sensible defaults with easy customization
- **Enterprise-Grade**: Built for scalability, multi-tenancy, and security
- **Developer Experience**: Focus on productivity and code simplicity

### 🎨 Automatic Frontend Generation

By default, **Dynamia Platform automatically generates full-featured web interfaces** using **ZK Framework**, a powerful server-side component framework that eliminates the need to write JavaScript, HTML, or CSS manually. Simply define your entities and view descriptors, and ZK renders responsive, interactive UIs with zero frontend code.

**Coming Soon**: In upcoming releases, Dynamia Platform will support **modern frontend frameworks** like **React**, **Vue**, and **Angular**, giving you the flexibility to choose your preferred technology stack while maintaining the same automatic generation capabilities. You'll be able to leverage our **TypeScript SDK** and pre-built components to build modern, decoupled SPAs that consume Dynamia's RESTful APIs.

## ✨ Core Capabilities

- **Automatic CRUD Generation**: Define entities, get full-featured interfaces instantly
- **Automatic REST Endpoints**: RESTful APIs generated from your domain models
- **Modular Architecture**: Build and reuse modules (JARs) across projects
- **DRY Principle**: Write once, use everywhere approach
- **View Descriptors**: Declare UIs with YAML instead of verbose code
- **Modern Web Stack**: HTML5, CSS3, Bootstrap via ZK Framework
- **Flexible Patterns**: Use MVC or MVVM as per your preference
- **Theme Support**: Customize look and feel with pluggable themes
- **Seamless Integration**: Works with any Java framework or library

## 🗺️ Roadmap

### 📋 Short-Term Goals (2026 Q1-Q2)

We're actively working on making Dynamia Platform more accessible, flexible, and developer-friendly. Here's what's coming:

#### 📚 **Enhanced Documentation**
- Comprehensive guides and tutorials
- Real-world use cases and examples
- API reference improvements
- Video tutorials and screencasts
- Interactive documentation with live code samples

#### 🌐 **Website Improvements**
- Modernized dynamia.tools website
- Interactive playground for testing features
- Community showcase and success stories
- Regular blog posts with tips and best practices
- Better search and navigation

#### 📦 **TypeScript SDK**
A complete TypeScript/JavaScript package ecosystem for frontend development:
- **`@dynamia/client`** - Core client library for REST API interaction
- **`@dynamia/types`** - TypeScript type definitions for all API responses
- **`@dynamia/hooks`** - React hooks for common operations
- **`@dynamia/composables`** - Vue composables for reactive integration
- Full type safety and IntelliSense support
- Automatic API client generation from backend schema
- WebSocket support for real-time features

#### 🎨 **Vue Template**
Modern frontend template with:
- Vue 3 + TypeScript + Vite
- Pre-configured integration with Dynamia Platform APIs
- Responsive design with Tailwind CSS
- Authentication and authorization out of the box
- CRUD components library
- Form validation and data binding
- Real-time updates with WebSocket
- Dashboard and reporting components

#### 🔗 **Frontend Framework Integration**
Seamless integration with popular frameworks:
- **React** - Components and hooks for React apps
- **Vue** - Full Vue 3 template and composables
- **Angular** - Services and components for Angular projects
- **Svelte** - Stores and components for Svelte
- **Next.js / Nuxt** - SSR support and optimizations
- Zero-config setup with sensible defaults
- Automatic API discovery and client generation
- Built-in authentication flows

#### ⚡ **Automation & Developer Experience**
- CLI tool for scaffolding projects and modules
- Code generators for entities, views, and endpoints
- Hot reload for view descriptors
- Development dashboard for monitoring
- Docker Compose templates for quick setup
- GitHub Actions templates for CI/CD
- Automated testing utilities

### 🔮 Long-Term Vision

- **GraphQL Support** - Alternative to REST with automatic schema generation
- **Microservices Toolkit** - Tools for building distributed systems
- **Cloud-Native Features** - Enhanced Kubernetes support, service mesh integration
- **AI-Powered Code Generation** - Generate CRUD, forms, and reports from natural language
- **Mobile SDK** - Native iOS and Android libraries
- **Low-Code Builder** - Visual editor for building apps without coding

---

### 🤝 Help Shape the Future

We value community feedback! If you have ideas, suggestions, or want to contribute to any of these initiatives:
- 💬 Join discussions on [GitHub Discussions](https://github.com/dynamiatools/framework/discussions)
- 🐛 Report issues or request features on [GitHub Issues](https://github.com/dynamiatools/framework/issues)
- 🌟 Star the project and spread the word!

---

## 📦 Built-in Enterprise Extensions

Dynamia Platform includes powerful extensions in the `/extensions` folder:

### **Multi-Tenancy (SaaS)**
Full-featured SaaS support with:
- Account management and isolation
- Payment and subscription handling
- Per-tenant data segregation
- Status monitoring and control

### **Entity File Management**
Attach files to any entity with:
- Local disk storage
- **AWS S3 integration** for cloud storage
- Metadata persistence
- Automatic file handling

### **Email & SMS**
Professional communication capabilities:
- Email sending via JavaMail
- SMS delivery via AWS
- Template management
- Account configuration
- Delivery logging and tracking

### **Dashboard**
Beautiful, responsive dashboards featuring:
- Custom widget system
- Chart integration
- Responsive layouts
- Real-time data display

### **Reporting**
Comprehensive reporting framework:
- JPQL and native SQL queries
- Datasource management
- Export to CSV, Excel, PDF
- Email delivery
- Chart visualization
- Embeddable reports

### **Finance Framework**
Domain-driven financial calculations for:
- Invoices, credit/debit notes
- Purchase orders and quotes
- Multi-currency support
- Tax, discount, and withholding calculations
- Auditable, deterministic results

### **File Importer**
Streamlined data import workflows:
- Excel/CSV file processing
- Field mapping
- Validation and error handling
- Batch operations

### **Security**
Enterprise authentication and authorization:
- Role-based access control (RBAC)
- User and permission management
- Security profiles
- Integration with Spring Security

## 🚀 Quick Start

### Prerequisites
- **Java 25** or higher
- Maven 3.9+ or Gradle 8+
- Your favorite IDE (IntelliJ IDEA, Eclipse, VS Code)

### Installation

1. **Create a Spring Boot Project**
   
   Visit [start.spring.io](https://start.spring.io) or [click here for a preconfigured template](https://start.spring.io/#!type=maven-project&language=java&packaging=jar&jvmVersion=25&groupId=com.example&artifactId=my-dynamia-app&name=MyApp&description=Dynamia%20Platform%20Application&packageName=com.example.myapp&dependencies=web,data-jpa,h2)
   
   Select:
   - **Java 25**
   - **Spring Boot 4.x**
   - **Spring Web**
   - **Spring Data JPA**
   - **H2 Database** (or your preferred database)
   - Your preferred language (Java, Kotlin, or Groovy)

2. **Add Dynamia Platform Dependencies**

   **Maven** (`pom.xml`)
   ```xml
   <dependencies>
       <dependency>
           <groupId>tools.dynamia</groupId>
           <artifactId>tools.dynamia.app</artifactId>
           <version>26.2.0</version>
       </dependency>
   
       <dependency>
           <groupId>tools.dynamia</groupId>
           <artifactId>tools.dynamia.zk</artifactId>
           <version>26.2.0</version>
       </dependency>
   
       <dependency>
           <groupId>tools.dynamia</groupId>
           <artifactId>tools.dynamia.domain.jpa</artifactId>
           <version>26.2.0</version>
       </dependency>
   </dependencies>
   ```

   **Gradle** (`build.gradle`)
   ```groovy
   dependencies {
       implementation 'tools.dynamia:tools.dynamia.app:26.2.0'
       implementation 'tools.dynamia:tools.dynamia.zk:26.2.0'
       implementation 'tools.dynamia:tools.dynamia.domain.jpa:26.2.0'
   }
   ```

3. **Enable Dynamia Platform**

   Add the `@EnableDynamiaTools` annotation to your main application class:

   ```java
   import org.springframework.boot.SpringApplication;
   import org.springframework.boot.autoconfigure.SpringBootApplication;
   import tools.dynamia.app.EnableDynamiaTools;
   
   @SpringBootApplication
   @EnableDynamiaTools  // ← This is all you need!
   public class MyApplication {
   
       public static void main(String[] args) {
           SpringApplication.run(MyApplication.class, args);
       }
   }
   ```

4. **Run Your Application**
   
   ```bash
   mvn spring-boot:run
   # or
   gradle bootRun
   ```

5. **Access Your Application**
   
   Open your browser and navigate to: `http://localhost:8080`
   
   You'll see a fully functional web application with the Dynamical theme!

### Adding Extensions

To use any of the built-in extensions, simply add their dependencies. **All extensions now share the same version (26.2.0)** thanks to unified CalVer:

```xml
<!-- Multi-tenancy / SaaS -->
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.saas</artifactId>
    <version>26.2.0</version>
</dependency>

<!-- Email & SMS -->
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.email</artifactId>
    <version>26.2.0</version>
</dependency>

<!-- Entity Files with S3 Support -->
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.entityfiles</artifactId>
    <version>26.2.0</version>
</dependency>
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.entityfiles.s3</artifactId>
    <version>26.2.0</version>
</dependency>

<!-- Dashboard -->
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.dashboard</artifactId>
    <version>26.2.0</version>
</dependency>

<!-- Reporting -->
<dependency>
    <groupId>tools.dynamia.reports</groupId>
    <artifactId>tools.dynamia.reports.core</artifactId>
    <version>26.2.0</version>
</dependency>

<!-- File Importer -->
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.fileimporter</artifactId>
    <version>26.2.0</version>
</dependency>

<!-- Security -->
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.security</artifactId>
    <version>26.2.0</version>
</dependency>
```

> **💡 Pro Tip**: With CalVer, all Dynamia Platform components use the same version. Just use `26.2.0` for everything!

> **Note**: All artifacts are available on [Maven Central](https://search.maven.org/search?q=tools.dynamia)

## 📚 Documentation

Comprehensive guides, tutorials, and API documentation are available at:

**🌐 [https://dynamia.tools](https://dynamia.tools)**

### Key Resources

- **[Getting Started Guide](https://dynamia.tools/getting-started/)** - Your first Dynamia app in 5 minutes
- **[Core Concepts](https://dynamia.tools/docs/)** - Understanding the framework architecture
- **[View Descriptors](https://dynamia.tools/docs/view-descriptors/)** - Declarative UI with YAML
- **[API Reference](https://dynamia.tools/api/)** - Complete API documentation
- **[Extensions Guide](https://dynamia.tools/extensions/)** - Working with built-in modules
- **[Examples](https://github.com/dynamiatools/framework/tree/master/examples)** - Sample applications

## 🏗️ Building from Source

### Prerequisites
- OpenJDK 25
- Maven 3.8+
- Git

### Build Instructions

```bash
# Clone the repository
git clone https://github.com/dynamiatools/framework.git
cd framework

# Build all modules
mvn clean install

# Skip tests for faster builds
mvn clean install -DskipTests

# Build only the core platform
cd platform
mvn clean install

# Build only extensions
cd extensions
mvn clean install
```

### Project Structure

```
DynamiaPlatform/
├── platform/           # Core framework modules
│   ├── app/           # Application bootstrap
│   ├── core/          # Core utilities and APIs
│   ├── starters/      # Spring Boot starters
│   └── ui/            # UI components and themes
├── extensions/        # Enterprise extensions
│   ├── saas/          # Multi-tenancy support
│   ├── entity-files/  # File attachment system
│   ├── email-sms/     # Communication module
│   ├── dashboard/     # Dashboard widgets
│   ├── reports/       # Reporting engine
│   ├── finances/      # Financial calculations
│   ├── file-importer/ # Data import utilities
│   └── security/      # Authentication & authorization
├── themes/            # UI themes
└── examples/          # Sample applications
```

## 📜 Version History & Evolution

Dynamia Platform has evolved over **13+ years** of continuous development at [Dynamia Soluciones IT](https://www.dynamiasoluciones.com), powering **10+ production projects** and **40+ modules** in real-world enterprise environments.

### 🎯 Evolution Journey

#### **v1.x - Foundation** (2011-2013)
The beginning: Internal utility library focused on:
- Hiding Spring and ZK complexity
- Database abstraction and configuration
- Service layer patterns
- Basic modular architecture

#### **v2.x - Automation** (2014-2016)
Revolutionary leap with:
- **View Descriptors**: YAML-based UI declaration
- **Automatic CRUD generation**: Zero-code interfaces
- **Navigation framework**: Modular page system
- Focus on maintainability over code generation

#### **v3.x - Open Source** (2017-2019)
Major refactoring and public release:
- Complete code reorganization
- Performance optimizations
- First public open-source release
- Community-driven development begins

#### **v4.x - Modernization** (2020-2022)
Java 11+ and ecosystem update:
- Java 11+ support
- Spring Boot integration
- Modern framework compatibility
- Enhanced documentation and demos
- Growing community adoption

#### **v5.x - Platform Era** (2023-2025)
**The Dynamia Platform Era** - Enterprise-ready unified platform:
- ☕ **Java 21** - Latest LTS features
- 🚀 **Spring Boot 3** - Modern Spring ecosystem
- 🎨 **ZK 10** - Advanced UI framework
- 🏢 **Unified Repository** - Core + Extensions together
- 📦 **Production Extensions** - Battle-tested enterprise modules
- 🔐 **Security First** - Built-in authentication & authorization
- ☁️ **Cloud Ready** - AWS integration, containerization support
- 📊 **Enterprise Features** - Multi-tenancy, reporting, dashboards

#### **v26.x - CalVer Generation** (2026+)
**The Modern Era** - Simplified versioning and cutting-edge technology:
- 📅 **CalVer Versioning** (YY.MM.MINOR) - All modules unified under same version (Year.Month.Release)
- ☕ **Java 25** - Latest Java innovations and performance
- 🚀 **Spring Boot 4** - Next-gen Spring ecosystem
- 🎨 **ZK 10+** - Modern web UI capabilities
- 🔄 **Synchronized Releases** - Core, extensions, starters, and themes share the same version
- 🎯 **Simplified Dependencies** - One version to rule them all (e.g., 26.2.0 for February 2026)
- ⚡ **Enhanced Performance** - Optimized for modern JVM and cloud environments
- 🛡️ **Production Hardened** - Battle-tested in enterprise environments

### 🏆 Production Proven

- **13+ years** of continuous development
- **10+ enterprise applications** in production
- **40+ reusable modules** built with the framework
- Used by companies across **Latin America** and **Europe**
- Powers critical business systems in various industries

This is not a new framework—it's a **mature, battle-tested platform** that's been serving real businesses for over a decade. Now open-source and ready for the global developer community.

## 🤝 Contributing

We welcome contributions from the community! Whether it's:

- 🐛 Bug reports and fixes
- ✨ New feature suggestions
- 📚 Documentation improvements
- 💡 Example applications
- 🌐 Translations

Please read our **[Contributing Guidelines](CONTRIBUTING.md)** before submitting pull requests.

## 📄 License

Dynamia Platform is open-source software licensed under the **Apache License 2.0**.

See [LICENSE](LICENSE) for details.

## ☕ Support Dynamia Platform

Dynamia Platform is built with dedication to help developers speed up their projects and make enterprise development more enjoyable. If this platform has been useful to you or your organization, consider supporting its development. Your support helps keep the project alive and evolving with new features and improvements. Every coffee counts! 😊

<a href="https://www.buymeacoffee.com/marioserrano" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" style="height: 60px !important;width: 217px !important;" ></a>

---

<div align="center">

**Made with ❤️ by [Dynamia Soluciones IT](https://www.dynamiasoluciones.com)**

[Website](https://dynamia.tools) • [Documentation](https://dynamia.tools/getting-started/) • [GitHub](https://github.com/dynamiatools/framework) • [Maven Central](https://search.maven.org/search?q=tools.dynamia)

</div>
