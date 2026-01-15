# Security Module

[![Maven Central](https://img.shields.io/maven-central/v/tools.dynamia.modules/tools.dynamia.modules.security.core)](https://search.maven.org/search?q=tools.dynamia.modules.security)
![Java Version Required](https://img.shields.io/badge/java-17-blue)
[![Maven Build](https://github.com/dynamiatools/module-security/actions/workflows/maven.yml/badge.svg)](https://github.com/dynamiatools/module-security/actions/workflows/maven.yml)

Welcome to Module-Security, a powerful integration with DynamiaTools designed to provide user management, profile management, access tokens, restrictions, and much more for any web application developed with DynamiaTools. Internally, it leverages Spring Security and Spring Boot to ensure robust security features.

## 🔒 Features
- User Management: Easily manage users within your application.
- Profile Management: Define and assign profiles to users.
- Access Tokens: Manage access tokens for secure authentication.
- Restrictions: Set up access restrictions based on user roles and permissions.
- And much more!

## 🚀 **Getting Started**
To integrate Security into your DynamiaTools project, follow these steps:

1. Add the following Maven dependency to your project's `pom.xml`:
```xml
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.security.core</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. If you're using ZK for the frontend, include the UI module dependency as well:
```xml
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.security.ui</artifactId>
    <version>1.0.0</version>
</dependency>
```

3. Ensure your project is configured to use Spring Security and Spring Boot.

4. Start integrating user management, profile management, and other security features into your application!

## 📝 **License**
Security module is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for more details.

## 📖 **Documentation**
For detailed documentation and usage instructions, please refer to the [official documentation](https://yourdocumentationlinkhere.com).

## 🤝 **Contributing**
Contributions are welcome! Feel free to open issues or submit pull requests.

