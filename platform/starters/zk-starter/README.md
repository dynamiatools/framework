# DynamiaTools Starter

## Overview

**DynamiaTools Starter** is a modern Spring Boot auto-configuration module for the [DynamiaTools](https://dynamia.tools) framework. It provides a seamless, annotation-free alternative to the legacy `@EnableDynamiaTools` approach, enabling automatic setup and integration of DynamiaTools features in your Spring Boot applications. This starter is designed for rapid development, modularity, and best practices in enterprise Java applications, including support for ZK UI, metadata, viewers, logging, and more.

---

## Key Features

- **Automatic Configuration**: Instantly enables DynamiaTools core modules and services without manual annotations or configuration.
- **Spring Boot Native**: Integrates with Spring Boot's auto-configuration mechanism for zero-effort setup.
- **ZK UI Integration**: Out-of-the-box support for ZK (ZKoss) rich web UI components.
- **Metadata & Viewers**: Auto-initializes view descriptors and metadata for dynamic UI generation.
- **Logging**: Configures SLF4J-based logging for all DynamiaTools modules.
- **Modular Architecture**: Supports DynamiaTools modules (domain, reports, navigation, etc.) for scalable enterprise solutions.
- **Best Practices**: Follows Spring Boot conventions for configuration, extensibility, and testing.

---

## Why Use DynamiaTools Starter?

- **No More `@EnableDynamiaTools`**: Just add the starter dependency; no need to annotate your main class.
- **Modern Spring Boot Experience**: Leverages auto-configuration, conditional beans, and best practices.
- **Plug & Play**: Works with existing DynamiaTools modules and your own extensions.
- **Rapid Development**: Focus on your business logic, not on framework setup.
- **Future-Proof**: Designed for compatibility with Spring Boot 2.x/3.x and future DynamiaTools releases.

---

## Getting Started

### 1. Add the Dependency

Add the following to your Maven `pom.xml`:

```xml
<dependency>
    <groupId>tools.dynamia</groupId>
    <artifactId>starter</artifactId>
    <version>YOUR_VERSION_HERE</version>
</dependency>
```

### 2. Create Your Spring Boot Application

```java
@SpringBootApplication
public class MyApp {
    public static void main(String[] args) {
        SpringApplication.run(MyApp.class, args);
    }
}
```

> **Note:** No need for `@EnableDynamiaTools`! The starter will auto-configure everything.

### 3. Configuration & Customization

- Use `application.properties` or `application.yml` for custom settings.
- Extend or override beans as needed using standard Spring mechanisms.
- DynamiaTools modules (domain, reports, navigation, etc.) are auto-configured if present in the classpath.

---

## How It Works

- **Auto-Configuration**: The class `DynamiaToolsAutoConfiguration` is detected by Spring Boot and sets up all required beans, including logging and DynamiaTools core services.
- **View Descriptor Initialization**: The `AutoInitViewDescriptors` runner loads all view descriptors at startup, enabling dynamic UI and metadata features.
- **Logging**: SLF4J logging is configured for all DynamiaTools components.
- **Component Scanning**: DynamiaTools modules and your own beans are discovered automatically.

---

## Advanced Usage

- **Module Integration**: Add any DynamiaTools module (e.g., `domain`, `reports`, `navigation`) to your project; the starter will auto-configure them.
- **ZK UI**: If ZK libraries are present, UI components are enabled and ready to use.
- **Custom Logging**: Override the default logger by providing your own `LoggingService` bean.
- **Metadata & Viewers**: Extend or customize view descriptors by implementing your own `ViewDescriptorFactory`.

---

## Migration from `@EnableDynamiaTools`

If you previously used `@EnableDynamiaTools`, simply:

1. Remove the annotation from your main class.
2. Add the starter dependency.
3. Verify that your modules and configuration files are present.
4. Start your application—DynamiaTools will be auto-configured!

---

## Example Project Structure

```
my-app/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/myapp/MyApp.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/
│           └── ...
└── ...
```

---

## Extensibility & Customization

- **Override Beans**: Use `@Primary` or custom configuration classes to override default beans.
- **Add Modules**: Drop in any DynamiaTools module; the starter will auto-configure it.
- **Custom Metadata**: Implement your own metadata providers or viewers for advanced UI scenarios.

---

## Troubleshooting

- **Missing Features?** Ensure the required DynamiaTools modules are present in your dependencies.
- **ZK UI Issues?** Verify ZK libraries are included and compatible with your Spring Boot version.
- **Logging Problems?** Check your SLF4J configuration and override the logger if needed.

---
