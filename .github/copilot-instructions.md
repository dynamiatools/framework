# Copilot Instructions for Dynamia Tools (Framework Internal)

These guidelines are for contributing to the **Dynamia Tools framework itself**, not for applications that use the framework.  
The focus is on keeping the codebase consistent, maintainable, and well-documented.

---

## Project Structure

The framework is organized into modules. Each module has a specific responsibility:

- **actions**  
  Handles platform actions, implementing operations users can perform (create, update, delete entities).

- **app**  
  Main application module, orchestrating the integration of all other modules and providing the entry point.

- **commons**  
  Contains shared utilities and common code used across multiple modules to avoid duplication.

- **crud**  
  Provides generic Create, Read, Update, Delete functionalities for entities, simplifying data management.

- **domain**  
  Defines core business entities and domain logic, serving as the foundation for other modules.

- **domain-jpa**  
  Adds JPA (Java Persistence API) support for domain entities, enabling ORM and database integration.

- **integration**  
  Manages integration with external systems and services, handling communication and data exchange.

- **io**  
  Responsible for input/output operations, such as file handling and data streams.

- **navigation**  
  Implements navigation logic and structures for the application's user interface.

- **reports**  
  Generates and manages reports, providing tools for data analysis and export.

- **starter**  
  Offers starter templates and configurations to bootstrap new projects or modules.

- **templates**  
  Contains reusable templates for UI, emails, or documents.

- **ui**  
  Manages user interface components and visual elements.

- **viewers**  
  Provides components for viewing and presenting data in various formats.

- **web**  
  Exposes web functionalities, including REST endpoints and web resources.

- **zk**  
  Integrates ZK framework components for building rich web interfaces.

---

## Coding Guidelines

- Code must be **clean, modular, and reusable**.
- Avoid duplication by placing shared logic in **commons**.
- Respect module boundaries — do not introduce tight coupling across unrelated modules.
- Follow **Java best practices** with Spring Boot, JPA, and ZK integrations.

---

## Documentation Guidelines (Javadoc)

- Every class and public method must have **Javadoc in English**.
- Keep comments **descriptive**: explain what the class or method does and why it exists.
- Do **not** alter original code logic when adding documentation.
- Include:
    - **Purpose of the class/method**
    - **Parameters** with `@param`
    - **Return values** with `@return`
    - **Exceptions** with `@throws` if applicable
- Add **examples** using `<pre>{@code ... }</pre>` when usage is clear.

### Example for a Class
```java
/**
 * Provides generic CRUD operations for domain entities.
 * This class acts as a helper to reduce boilerplate code
 * when managing persistent objects.
 *
 * Example:
 * <pre>{@code
 * CrudService service = new CrudService();
 * service.save(new Customer("John Doe"));
 * }</pre>
 */
public class CrudService {
    ...
}
```

### Example for a Method
```java
/**
 * Finds a domain entity by its unique identifier.
 *
 * @param id the unique identifier of the entity
 * @return the entity if found, otherwise null
 *
 * Example:
 * <pre>{@code
 * Customer customer = service.findById(123L);
 * }</pre>
 */
public Customer findById(Long id) {
    ...
}
```

---

## Extra Notes

- Use **descriptive class names** aligned with their module purpose.
- Place reusable constants and helpers in **commons**.
- Use **domain** only for business entities and logic.
- Keep **zk** and **ui** focused on presentation concerns, separate from business rules.
- Ensure all Javadocs compile with automated tools (`mvn javadoc:javadoc`).



# Getting Started
Start fast with DynamiaTools

## Installation

1. Create a new SpringBoot project using [start.spring.io](https://start.spring.io) and select Web, JPA and a programming language for your Spring Boot app.  
   DynamiaTools is compatible with Java, Groovy and Kotlin.

   You can also [click here](https://start.spring.io/#!type=maven-project&language=java&packaging=jar&jvmVersion=21&groupId=com.example&artifactId=dynamia-tools-project&name=Dynaima&description=Demo%20project%20for%20Spring%20Boot&packageName=com.example.demo&dependencies=web,data-jpa,h2) to get a preconfigured Spring Boot project with Java, Maven and Web with JPA support.

2. Download and import it in your IDE.
3. Add DynamiaTools starter dependency.

**Maven**
```xml
<!--pom.xml-->
<dependencies>
    <dependency>
        <groupId>tools.dynamia</groupId>
        <artifactId>dynamia-tools-starter</artifactId>
        <version>LAST_VERSION</version>
    </dependency>
</dependencies>
```

**Gradle**
```groovy
//build.gradle
compile 'tools.dynamia:dynamia-tools-starter:LAST_VERSION'
```

Go to [Maven Central](https://central.sonatype.com/artifact/tools.dynamia/tools.dynamia.app) to check last version.

This starter enables DynamiaTools in your application and adds support for ZK, JPA, and custom views and routes.

4. Run your project:

```java
//MyApplication.java

package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
class MyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }

}
```

After running the application, open your browser and go to http://localhost:8080.  
You should see a blank page with a fully functional HTML template called Dynamical.

---

## First project

Let's create something useful, like a contact CRUD to store our contact list. To create a CRUD in Dynamia Tools, follow these 3 steps:

1. Create a JPA entity
2. Create a Dynamia module
3. Define a view descriptor for **form** and **table** views

### 1. Create a JPA entity

```java
//Contact.java
package demo;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

@Entity
@Table(name="contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotEmpty
    private String name;
    @Email
    private String email;

    private String phone;

    // Getters and setters omitted for brevity
    @Override
    public String toString() {
        return name;
    }
}
```

This is a simple POJO class annotated with standard JPA annotations. Note that you can also use validation annotations.

---

### 2. DynamiaTools modules

Modules are standard Spring component classes implementing the `tools.dynamia.navigation.ModuleProvider`
interface and returning a new `tools.dynamia.navigation.Module`.

```java
//ContactModuleProvider.java
package demo;

import org.springframework.stereotype.Component;
import tools.dynamia.crud.CrudPage;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.ModuleProvider;

@Component
public class ContactModuleProvider implements ModuleProvider {

    @Override
    public Module getModule() {
        Module myModule = new Module("my-module", "My Module");
        myModule.addPage(new CrudPage("contacts", "Contacts", Contact.class));
        return myModule;
    }
}
```

Modules include ID, name, pages and page groups. Pages include ID, name and path, which in this case is represented by an entity class.  
For example, the `Contact` crud page has the path `/pages/my-module/contacts`.

---

### 3. View descriptors

Descriptors are YAML files defining how views for entities are rendered at runtime.  
Create a folder `/resources/META-INF/descriptors` and a file `ContactForm.yml`:

```yaml
# /resources/META-INF/descriptors/ContactForm.yml
view: form
beanClass: demo.Contact

fields:
  name:
    params:
      span: 3
  email:
    params:
      type: email
  phone:
    component: textbox

layout:
  columns: 3
```

Now create a table descriptor `ContactTable.yml`:

```yaml
# /resources/META-INF/descriptors/ContactTable.yml
view: table
beanClass: demo.Contact

fields:
  name:
  email:
  phone:
```

---

### 4. Run and enjoy

Your app now has a new menu called *My Module* and a submenu called *Contacts*.  
This is a fully functional CRUD with create, read, update, delete, and many more ready-to-use actions.

---

### Customize your project

Use Spring Boot `application.properties` or `application.yml` to configure DynamiaTools:

```yaml
#application.yml
dynamia:
  app:
    name: My First Project
    short-name: MFP
    default-skin: Green
    default-logo: /static/logo.png
    default-icon: /static/icon.png
    url: https://www.dynamia.tools
```

---

### Automatic REST

Every `CrudPage` automatically generates a REST endpoint.  
Example: `http://localhost:8080/api/my-module/contacts`

```json
{
  "data" : [ {
    "id" : 1,
    "name" : "Peter Parker",
    "email" : "spidy@gmail.com",
    "phone" : "5556565656"
  } ],
  "pageable" : {
    "totalSize" : 1,
    "pageSize" : 50,
    "firstResult" : 0,
    "page" : 1,
    "pagesNumber" : 1
  },
  "response" : "OK"
}
```

---

## Done

With this guide, you’ve just built a web application with:

- Automatic CRUD support
- Automatic RESTful endpoints
- A responsive template

Continue with the advanced guides to explore more features of DynamiaTools.
