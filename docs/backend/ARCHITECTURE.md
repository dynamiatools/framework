# Backend Architecture Overview

This document explains the architectural design of DynamiaTools, the layered approach, core design principles, and how components interact.

## Table of Contents

1. [Architectural Philosophy](#architectural-philosophy)
2. [Layered Architecture](#layered-architecture)
3. [Core Layers Explained](#core-layers-explained)
4. [Module System](#module-system)
5. [Request Processing Flow](#request-processing-flow)
6. [Dependency Injection & Spring Integration](#dependency-injection--spring-integration)
7. [Design Patterns Used](#design-patterns-used)

---

## Architectural Philosophy

DynamiaTools follows several core architectural principles:

### 1. **DRY (Don't Repeat Yourself)**
The framework minimizes boilerplate through automation and conventions. Define an entity once, get CRUD operations automatically.

### 2. **Zero-Config with Sensible Defaults**
Conventions over configuration. The framework uses reasonable defaults that work for 80% of use cases.

### 3. **Modularity**
Applications are built as collections of modules. Each module is independently deployable and testable.

### 4. **Separation of Concerns**
Clear distinction between domain logic (entities), business logic (services), data access (repositories), and presentation (views).

### 5. **Abstraction Over Implementation**
Core concepts use interfaces and abstractions, allowing implementations to be swapped (CrudService, ViewRenderer, etc.).

### 6. **Spring Boot Foundation**
Built on Spring Boot 4, leveraging Spring's dependency injection, AOP, and ecosystem.

---

## Layered Architecture

DynamiaTools follows a **multi-layer architecture** with clear responsibilities:

```
┌─────────────────────────────────────────────────────────┐
│           Presentation Layer (UI)                       │
│    (ZK Components, REST Controllers, View Renderers)    │
├─────────────────────────────────────────────────────────┤
│           Application Layer                             │
│  (Actions, CRUD Controllers, Navigation, Descriptors)  │
├─────────────────────────────────────────────────────────┤
│           Business Logic Layer                          │
│      (Services, Validators, Domain Events)              │
├─────────────────────────────────────────────────────────┤
│           Domain Layer                                  │
│         (Entities, Value Objects, Domain Logic)         │
├─────────────────────────────────────────────────────────┤
│           Data Access Layer                             │
│    (CrudService, Repositories, JPA, Query Engine)       │
├─────────────────────────────────────────────────────────┤
│           Infrastructure Layer                          │
│     (Database, File System, External Services)          │
└─────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

| Layer | Responsibility | Examples |
|-------|---|---|
| **Presentation** | Render UI, handle user interactions | ZK components, REST endpoints, view descriptors |
| **Application** | Orchestrate operations, route requests | CRUD controllers, actions, navigation |
| **Business Logic** | Implement business rules | Services, validators, calculations |
| **Domain** | Model the business domain | Entities, value objects, domain logic |
| **Data Access** | Persist and retrieve data | CrudService, repositories, queries |
| **Infrastructure** | External systems integration | Database drivers, file storage, APIs |

---

## Core Layers Explained

### 1. Presentation Layer

**Responsibility**: Render user interfaces and handle user interactions.

**Components**:
- **View Renderers**: Convert view descriptors to UI components (ZK by default)
- **REST Controllers**: Handle HTTP requests and API interactions
- **ZK Components**: Rich server-side UI components
- **View Descriptors**: YAML files defining UI structure

**Example**:
```java
// REST Controller
@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    @GetMapping
    public List<ContactDTO> getContacts() {
        // Returns data to be rendered
    }
}
```

-- View Descriptor (YAML)
```yaml
view: form
beanClass: com.example.Contact
fields:
    name:
    email:
```

### 2. Application Layer

**Responsibility**: Orchestrate application operations, coordinate between layers, handle navigation and CRUD operations.

**Components**:
- **CRUD Controllers**: Handle entity CRUD operations
- **Actions**: Reusable components for common operations
- **Navigation System**: Module and page management
- **Metadata Engine**: Exposes metadata to frontend

**Example**:
```java
// Action: Reusable operation
@InstallAction
public class ExportContactsAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent evt) {
        // Export logic
    }
}

// Module Provider: Define module structure
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

### 3. Business Logic Layer

**Responsibility**: Implement business rules, validations, and domain-specific operations.

**Components**:
- **Services**: Spring services with @Service annotation
- **Validators**: Custom validation logic
- **Domain Events**: Events triggered by domain operations
- **Calculators**: Complex business calculations

**Example**:
```java
@Service
public class ContactService {
    
    private final CrudService crudService;
    
    public Contact activateContact(Long id) {
        Contact contact = crudService.find(Contact.class, id);
        // Business logic here
        contact.setActive(true);
        crudService.save(contact);
        return contact;
    }
}

// Custom Validator
@InstallValidator
public class EmailValidator implements Validator<Contact> {
    @Override
    public List<ValidationError> validate(Contact contact) {
        // Validation logic
        return validationErrors;
    }
}
```

### 4. Domain Layer

**Responsibility**: Model the business domain with entities and value objects.

**Components**:
- **Entities**: @Entity classes with business logic
- **Value Objects**: Immutable objects representing concepts
- **Domain Interfaces**: Contracts for domain behavior

**Example**:
```java
@Entity
@Table(name = "contacts")
public class Contact {
    @Id
    @GeneratedValue
    private Long id;
    
    private String name;
    private String email;
    
    @ManyToOne
    private Company company;
    
    // Business logic methods
    public boolean isValid() {
        return name != null && email != null;
    }
    
    // Getters and setters
}
```

### 5. Data Access Layer

**Responsibility**: Handle data persistence and retrieval from the database.

**Components**:
- **CrudService**: Main abstraction for CRUD operations
- **JpaRepository**: Spring Data repositories
- **Query Engine**: Complex query construction
- **Entity Managers**: JPA entity lifecycle management

**Example**:
```java
// Using CrudService
@Service
public class ContactAppService {
    
    private final CrudService crudService;
    
    public List<Contact> findActiveContacts() {
        // CrudService handles JPA internally
        return crudService.findAll(Contact.class, "active", true);
    }
    
    public Contact createContact(Contact contact) {
        return crudService.save(contact);
    }
}

// CrudService is implemented by JpaCrudService
// Which uses Spring Data JPA repositories internally
```

### 6. Infrastructure Layer

**Responsibility**: Manage external systems and resources.

**Components**:
- **Database**: PostgreSQL, MySQL, H2, etc.
- **File Storage**: Local disk, AWS S3, etc.
- **Cache**: Redis, Memcached, etc.
- **Message Brokers**: RabbitMQ, Kafka, etc.
- **External APIs**: Third-party service integrations

---

## Module System

DynamiaTools applications are organized as **modules**. Each module is a cohesive unit containing related functionality.

### Module Structure

```
mymodule/
├── src/main/java/com/example/
│   ├── domain/              # Domain entities
│   │   └── Contact.java
│   ├── services/            # Business logic
│   │   └── ContactService.java
│   ├── ui/                  # UI components (optional)
│   ├── providers/           # Spring components
│   │   └── ContactModuleProvider.java
│   └── actions/             # Actions
│       └── ExportContactsAction.java
└── src/main/resources/
    └── META-INF/descriptors/  # View descriptors
        ├── ContactForm.yml
        ├── ContactTable.yml
        └── ContactCrud.yml
```

### Module Provider Pattern

```java
@Component
public class ContactModuleProvider implements ModuleProvider {
    
    @Override
    public Module getModule() {
        Module module = new Module("crm", "CRM");
        module.setIcon("contacts");
        
        // Add pages
        PageGroup customerGroup = new PageGroup("customers", "Customers");
        customerGroup.addPage(new CrudPage("contacts", "Contacts", Contact.class));
        customerGroup.addPage(new CrudPage("companies", "Companies", Company.class));
        module.addPageGroup(customerGroup);
        
        return module;
    }
}
```

### Module Dependencies

Modules can depend on other modules:

```java
// Module A depends on Module B
@Component
public class ModuleA implements ModuleProvider {
    
    @Override
    public Module getModule() {
        Module module = new Module("moduleA", "Module A");
        
        // Reference another module
        Module moduleB = new Module("moduleB", "Module B");
        moduleB.setReference(true);  // Mark as reference
        module.addSubModule(moduleB);
        
        return module;
    }
}
```

---

## Request Processing Flow

Here's how a typical request flows through the layers:

### CRUD Read Request Flow

```
1. User clicks "Edit Contact" button
   ↓
2. ZK Component triggers event
   ↓
3. CrudController receives request
   ↓
4. Controller calls CrudService.find(Contact.class, id)
   ↓
5. CrudService queries database via repository
   ↓
6. Repository returns entity from database
   ↓
7. Controller prepares response
   ↓
8. View Descriptor defines form layout
   ↓
9. View Renderer converts descriptor to ZK components
   ↓
10. ZK renders HTML/JavaScript to browser
```

### CRUD Create Request Flow

```
1. User submits form
   ↓
2. Form data reaches CrudController
   ↓
3. Controller instantiates Contact entity from form data
   ↓
4. Validators (ValidationService) validate entity
   ↓
5. If validation passes → CrudService.save(contact)
   ↓
6. Save triggers business logic services (optional listeners)
   ↓
7. JPA persists entity to database
   ↓
8. Transaction completes
   ↓
9. Navigation returns to list view
```

---

## Dependency Injection & Spring Integration

DynamiaTools is built on Spring's dependency injection. All components are managed by the Spring container.

### Registering Components

```java
// Automatic registration via annotations
@Component
public class MyComponent {
    // Spring manages lifecycle
}

@Service
public class MyService {
    // Singleton service
}

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    // Repository for queries
}

// Custom annotation for DynamiaTools-specific registration
@InstallAction
public class MyAction extends AbstractAction {
    // Installed as action
}

@InstallValidator
public class MyValidator implements Validator<Contact> {
    // Installed as validator
}
```

### Dependency Injection

```java
@Service
public class ContactService {
    
    // Constructor injection (recommended)
    private final CrudService crudService;
    private final EmailService emailService;
    
    public ContactService(CrudService crudService, EmailService emailService) {
        this.crudService = crudService;
        this.emailService = emailService;
    }
    
    // Field injection (not recommended but supported)
    @Autowired
    private NotificationService notificationService;
    
    // Method injection
    @Autowired
    public void setAuditService(AuditService auditService) {
        this.auditService = auditService;
    }
}
```

### Spring Integration Points

| Feature | Usage |
|---------|-------|
| **@Service** | Create business logic services |
| **@Component** | Create generic Spring beans |
| **@Repository** | Create data access objects |
| **@Configuration** | Define Spring configurations |
| **@Autowired** | Inject dependencies |
| **@Qualifier** | Select specific bean implementation |
| **@Scope** | Control bean lifecycle (singleton, prototype, etc.) |
| **@Transactional** | Manage transactions |
| **@Async** | Run methods asynchronously |
| **@Scheduled** | Schedule periodic tasks |
| **@EventListener** | Listen to Spring events |

---

## Design Patterns Used

DynamiaTools extensively uses proven design patterns:

### 1. **Provider Pattern**
Modules are registered via `ModuleProvider` interface, allowing dynamic module discovery.

```java
@Component
public class ContactModuleProvider implements ModuleProvider {
    @Override
    public Module getModule() {
        return new Module("crm", "CRM");
    }
}
```

### 2. **Strategy Pattern**
Different implementations for the same concept (CrudService, ViewRenderer).

```java
public interface CrudService {
    <T> T find(Class<T> entityClass, Object id);
    <T> T save(T entity);
}

// Different implementations
@Service
public class JpaCrudService implements CrudService {
    // JPA implementation
}

@Service
public class MongodbCrudService implements CrudService {
    // MongoDB implementation
}
```

### 3. **Template Method Pattern**
Base classes provide template implementation, subclasses override specific methods.

```java
public abstract class AbstractCrudAction extends AbstractAction {
    
    protected abstract void actionPerformed(CrudActionEvent evt);
    
    // Template method
    final void execute(ActionEvent evt) {
        if (validateAction()) {
            actionPerformed((CrudActionEvent) evt);
        }
    }
}
```

### 4. **Decorator Pattern**
ViewCustomizers decorate view descriptors before rendering.

```java
@Component
public class ContactFormCustomizer implements ViewCustomizer {
    
    @Override
    public void customize(ViewDescriptor view, Map metadata) {
        // Modify view descriptor
        view.getFields().get("email").setRequired(true);
    }
}
```

### 5. **Observer Pattern**
Actions and validators observe CRUD operations via events.

```java
@Component
public class AuditListener {
    
    @EventListener
    public void onEntityCreated(EntityCreatedEvent event) {
        // React to entity creation
    }
}
```

### 6. **Factory Pattern**
ViewRendererFactory creates appropriate renderer based on view type.

```java
public class ViewRendererFactory {
    
    public ViewRenderer createRenderer(ViewDescriptor view) {
        if ("form".equals(view.getType())) {
            return new FormViewRenderer();
        } else if ("table".equals(view.getType())) {
            return new TableViewRenderer();
        }
        // ...
    }
}
```

### 7. **Builder Pattern**
Fluent API for constructing complex objects.

```java
Module module = new Module("crm", "CRM")
    .addPageGroup(new PageGroup("sales", "Sales")
        .addPage(new CrudPage("orders", "Orders", Order.class))
        .addPage(new CrudPage("invoices", "Invoices", Invoice.class))
    )
    .addPageGroup(new PageGroup("customers", "Customers")
        .addPage(new CrudPage("contacts", "Contacts", Contact.class))
    );
```

### 8. **Command Pattern**
Actions encapsulate requests as objects.

```java
@InstallAction
public class EmailContactAction extends AbstractCrudAction {
    
    @Override
    public void actionPerformed(CrudActionEvent evt) {
        Contact contact = (Contact) evt.getEntity();
        // Send email
    }
}
```

---

## Key Architectural Principles

### 1. **Convention Over Configuration**
The framework uses naming conventions to reduce configuration. For example, view descriptors are automatically discovered from standard locations.

### 2. **Metadata-Driven**
UI generation is driven by metadata (view descriptors), not hardcoded in views.

### 3. **Extensibility**
Multiple extension points allow customization without modifying core code:
- View Customizers
- Actions
- Validators
- Custom Services

### 4. **Lazy Initialization**
Components are lazily initialized and cached for performance.

### 5. **Transaction Management**
Automatic transaction handling via Spring @Transactional.

### 6. **Security Integration**
Built-in Spring Security integration for authentication and authorization.

---

## Performance Considerations

### Caching Strategy
- Application metadata is cached at startup
- Entity metadata is cached
- View descriptors are loaded and cached on first access

### Lazy Loading
- Relationships are lazy-loaded by default
- Use @Fetch(FetchMode.JOIN) for eager loading when needed

### Query Optimization
- Use CrudService with appropriate filters
- Leverage Spring Data repository custom queries for complex queries

### Connection Pooling
- HikariCP connection pool for database connections
- Configurable pool size and timeout

---

## Integration with Spring Boot

DynamiaTools seamlessly integrates with Spring Boot:

```java
@SpringBootApplication
@EnableDynamiaTools  // Enables DynamiaTools auto-configuration
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

The `@EnableDynamiaTools` annotation triggers:
1. Component scanning for ModuleProviders
2. Initialization of metadata engine
3. Registration of ViewRenderers
4. Configuration of default beans (CrudService, NavigationManager, etc.)

---

## Summary

DynamiaTools architecture is built on:
- **Layered design** for separation of concerns
- **Module system** for modularity and reusability
- **Spring Boot foundation** for robust dependency injection
- **Design patterns** for proven solutions
- **Metadata-driven approach** for zero-code UI generation
- **Abstraction over implementation** for flexibility

This architecture enables rapid development of scalable enterprise applications while maintaining clean, maintainable code.

---

Next: Read [Core Modules Reference](./CORE_MODULES.md) to learn about each platform module.

