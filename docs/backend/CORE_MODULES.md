# Core Modules Reference

This document provides detailed information about each core module in the DynamiaTools platform. Understanding these modules is essential for building applications.

## Table of Contents

1. [Module Organization](#module-organization)
2. [Commons Module](#commons-module)
3. [Domain Module](#domain-module)
4. [Domain-JPA Module](#domain-jpa-module)
5. [CRUD Module](#crud-module)
6. [Navigation Module](#navigation-module)
7. [Actions Module](#actions-module)
8. [Integration Module](#integration-module)
9. [Web Module](#web-module)
10. [Reports Module](#reports-module)
11. [Templates Module](#templates-module)
12. [Viewers Module](#viewers-module)
13. [App Module](#app-module)
14. [Module Dependencies](#module-dependencies)

---

## Module Organization

Core modules are located in `/platform/core/`:

```
platform/core/
├── commons/           # Shared utilities and helpers
├── domain/            # Domain model abstractions
├── domain-jpa/        # JPA implementations
├── crud/              # CRUD operations framework
├── navigation/        # Navigation and module system
├── actions/           # Action framework
├── integration/       # Spring Boot integration
├── web/               # Web utilities
├── reports/           # Reporting system
├── templates/         # Template rendering
└── viewers/           # View rendering system
```

Each module is packaged as a separate JAR with its own dependencies and can be used independently or together.

---

## Commons Module

**Artifact ID**: `tools.dynamia.commons`

**Purpose**: Provides shared utilities, helpers, and common functionality used across the platform.

### Key Features

- **Utility Classes**: Common helpers for string, collection, and reflection operations
- **Lambdas & Functional Programming**: Functional utility classes
- **Serialization**: Custom serialization utilities
- **Converters**: Type conversion between common types
- **Validators**: Common validation utilities
- **Bean Utilities**: Reflection-based bean inspection and manipulation
- **Text Processing**: String formatting and template utilities

### Common Classes

```java
// String utilities
import tools.dynamia.commons.StringUtils;

StringUtils.toTitleCase("hello world");  // "Hello World"
StringUtils.toCamelCase("hello_world");  // "helloWorld"

// Collection utilities
import tools.dynamia.commons.CollectionUtils;

CollectionUtils.isEmpty(list);
CollectionUtils.isNotEmpty(list);

// Functional utilities
import tools.dynamia.commons.Lambdas;

Supplier<String> cached = Lambdas.memoize(() -> expensiveOperation());

// Bean utilities
import tools.dynamia.commons.BeanUtils;

BeanUtils.setProperties(bean, propertyMap);
Map<String, Object> props = BeanUtils.getProperties(bean);

// Reflections
import tools.dynamia.commons.ReflectionUtils;

Field field = ReflectionUtils.getField(MyClass.class, "myField");
```

### When to Use Commons

- Need common string operations
- Working with collections or maps
- Reflection-based operations
- Type conversions
- Bean introspection

---

## Domain Module

**Artifact ID**: `tools.dynamia.domain`

**Purpose**: Provides abstractions for domain modeling without persistence details.

### Key Concepts

#### 1. Entity Interface
Base interface for all entities:

```java
import tools.dynamia.domain.Entity;

public interface Entity {
    Object getId();
    void setId(Object id);
    boolean isNew();
    boolean isPersistent();
}
```

#### 2. Identifiable
Objects with unique identifiers:

```java
import tools.dynamia.domain.Identifiable;

public interface Identifiable {
    Serializable getId();
}
```

#### 3. Auditable
Entities that track creation and modification:

```java
import tools.dynamia.domain.Auditable;

public interface Auditable {
    User getCreator();
    LocalDateTime getCreationDate();
    User getModifier();
    LocalDateTime getModificationDate();
}
```

#### 4. Validator Interface
Custom validation logic:

```java
import tools.dynamia.domain.Validator;

public interface Validator<T> {
    List<ValidationError> validate(T entity);
    Class<T> getValidatedClass();
}

// Register with annotation
@InstallValidator
public class ContactValidator implements Validator<Contact> {
    @Override
    public List<ValidationError> validate(Contact contact) {
        List<ValidationError> errors = new ArrayList<>();
        if (contact.getEmail() == null) {
            errors.add(new ValidationError("email", "Email is required"));
        }
        return errors;
    }
    
    @Override
    public Class<Contact> getValidatedClass() {
        return Contact.class;
    }
}
```

#### 5. Converter Interface
Convert between types:

```java
import tools.dynamia.domain.Converter;

public interface Converter<S, T> {
    T convert(S source);
    Class<S> getSourceClass();
    Class<T> getTargetClass();
}

// Example: String to LocalDate converter
@InstallConverter
public class StringToLocalDateConverter implements Converter<String, LocalDate> {
    @Override
    public LocalDate convert(String source) {
        return LocalDate.parse(source, DateTimeFormatter.ISO_DATE);
    }
    
    @Override
    public Class<String> getSourceClass() {
        return String.class;
    }
    
    @Override
    public Class<LocalDate> getTargetClass() {
        return LocalDate.class;
    }
}
```

### Core Classes

- **Entity**: Base interface for domain entities
- **Validator**: Custom validation logic
- **Converter**: Type conversion between objects
- **DomainEvent**: Events triggered by domain operations
- **ValidationError**: Validation error representation

### When to Use Domain Module

- Building domain models
- Defining entities and value objects
- Creating custom validators
- Building custom converters
- Handling domain events

---

## Domain-JPA Module

**Artifact ID**: `tools.dynamia.domain.jpa`

**Purpose**: Provides JPA-based implementations of domain abstractions.

### Key Features

- **BaseEntity**: Abstract base class for JPA entities
- **Auditable Implementation**: Built-in audit fields
- **Entity Lifecycle**: Automatic handling of creation/modification dates
- **Generic Repositories**: Pre-built JPA repositories

### BaseEntity

All JPA entities should extend BaseEntity:

```java
import tools.dynamia.domain.jpa.BaseEntity;

@Entity
@Table(name = "contacts")
public class Contact extends BaseEntity {
    
    private String name;
    private String email;
    
    // Inherits from BaseEntity:
    // - id (auto-generated)
    // - creationDate (auto-set)
    // - createdBy (auto-set)
    // - modificationDate (auto-set)
    // - modifiedBy (auto-set)
}
```

### Auditable Implementation

Automatic audit trail:

```java
@Entity
public class Contact extends BaseEntity {
    
    @ManyToOne
    private User createdBy;
    
    private LocalDateTime creationDate;
    
    @ManyToOne
    private User modifiedBy;
    
    private LocalDateTime modificationDate;
    
    // Auto-populated before persist/update
}
```

### Entity Lifecycle

JPA automatically manages:

```java
Contact contact = new Contact();
contact.setName("John");

// Before save: createdBy and creationDate are set
crudService.save(contact);

// Before update: modifiedBy and modificationDate are updated
contact.setEmail("john@example.com");
crudService.save(contact);
```

### When to Use Domain-JPA Module

- Building JPA entities
- Using auditable entities
- Leveraging Spring Data repositories
- Need for automatic lifecycle management

---

## CRUD Module

**Artifact ID**: `tools.dynamia.crud`

**Purpose**: Provides CRUD (Create, Read, Update, Delete) framework.

### Core Components

#### 1. CrudService
Main abstraction for data persistence:

```java
public interface CrudService {
    
    // Create / Save
    <T> T save(T entity);
    <T> void save(Collection<T> entities);
    
    // Read
    <T> T find(Class<T> entityClass, Object id);
    <T> List<T> findAll(Class<T> entityClass);
    <T> List<T> findAll(Class<T> entityClass, String property, Object value);
    
    // Update
    <T> T update(T entity);
    
    // Delete
    <T> void delete(Class<T> entityClass, Object id);
    <T> void delete(T entity);
    <T> void deleteAll(Class<T> entityClass);
    
    // Count
    <T> long count(Class<T> entityClass);
    
    // Exists
    <T> boolean exists(Class<T> entityClass, Object id);
}
```

#### 2. Using CrudService

```java
@Service
public class ContactService {
    
    private final CrudService crudService;
    
    public ContactService(CrudService crudService) {
        this.crudService = crudService;
    }
    
    // Create
    public Contact createContact(String name, String email) {
        Contact contact = new Contact();
        contact.setName(name);
        contact.setEmail(email);
        return crudService.save(contact);
    }
    
    // Read
    public Contact getContact(Long id) {
        return crudService.find(Contact.class, id);
    }
    
    // Read all
    public List<Contact> getAllContacts() {
        return crudService.findAll(Contact.class);
    }
    
    // Read with filter
    public List<Contact> getActiveContacts() {
        return crudService.findAll(Contact.class, "active", true);
    }
    
    // Update
    public Contact updateContact(Contact contact) {
        return crudService.update(contact);
    }
    
    // Delete
    public void deleteContact(Long id) {
        crudService.delete(Contact.class, id);
    }
    
    // Count
    public long getTotalContacts() {
        return crudService.count(Contact.class);
    }
}
```

#### 3. CrudAction
Base class for CRUD-related actions:

```java
@InstallAction
public class ExportContactsAction extends AbstractCrudAction {
    
    private final CrudService crudService;
    
    public ExportContactsAction(CrudService crudService) {
        this.crudService = crudService;
    }
    
    @Override
    public void actionPerformed(CrudActionEvent evt) {
        List<Contact> contacts = crudService.findAll(Contact.class);
        // Export logic...
    }
}
```

#### 4. CrudPage
Declarative CRUD interface:

```java
// In ModuleProvider
Module module = new Module("crm", "CRM");

// Add CRUD page with all operations
CrudPage contactsPage = new CrudPage("contacts", "Contacts", Contact.class);
module.addPage(contactsPage);

// Customize display settings (inherited from NavigationElement)
contactsPage.setName("Contacts");
contactsPage.setIcon("contacts");
contactsPage.setPosition(1.0);

// Use a custom CrudService by name (optional)
// CrudPage contactsPage = new CrudPage("contacts", "Contacts", Contact.class, "myCustomCrudService");
```

### CrudPage Configuration

`CrudPage` extends `AbstractCrudPage` → `RendereablePage` → `Page` → `NavigationElement`.
The available configuration methods come from the inherited hierarchy:

```java
public class CrudPage extends AbstractCrudPage<Object> {

    // Constructors
    CrudPage(Class entityClass);
    CrudPage(String id, String name, Class entityClass);
    CrudPage(String id, String name, Class entityClass, String crudServiceName);

    // Custom CrudService (optional)
    void setCrudServiceName(String crudServiceName);
    String getCrudServiceName();

    // Display settings (from NavigationElement)
    void setName(String name);
    void setIcon(String icon);
    void setDescription(String description);
    void setPosition(double position);
    void setVisible(boolean visible);

    // Page actions (from Page — uses PageAction, not Action)
    Page addAction(PageAction action);
    void removeAction(PageAction action);
    List<PageAction> getActions();
}
```

### When to Use CRUD Module

- Create CRUD operations for entities
- Build CRUD pages quickly
- Handle entity persistence
- Create data access services

---

## Navigation Module

**Artifact ID**: `tools.dynamia.navigation`

**Purpose**: Manages application navigation, modules, and pages.

### Core Components

#### 1. Module
Represents a navigational module:

```java
public class Module extends NavigationElement<Module> {

    // Constructors
    public Module();
    public Module(String id, String name);
    public Module(String id, String name, String description);

    // Navigation structure
    Module addPageGroup(PageGroup group);
    Module addPageGroup(PageGroup... groups);
    Module addPage(Page page);           // adds to the default group
    Module addPage(Page... pages);

    // Retrieval
    List<PageGroup> getPageGroups();
    PageGroup getDefaultPageGroup();     // implicit group for top-level pages
    PageGroup getPageGroupById(String id);
    Page getFirstPage();
    Page getMainPage();
    void setMainPage(Page page);

    // Search
    Page findPage(String virtualPath);           // e.g. "crm/contacts"
    Page findPageByPrettyPath(String prettyPath);

    // Iteration
    void forEachPage(Consumer<Page> action);

    // Custom properties
    Object addProperty(String key, Object value);
    Object getProperty(String key);
    Map<String, Object> getProperties();
    boolean isEmpty();

    // Fluent builder (static factory)
    static JavaModuleBuilder builder(String name);
    static JavaModuleBuilder builder(String id, String name);
    static JavaModuleBuilder builder(String id, String name, String icon, double position);

    // Localization / i18n
    void setBaseClass(Class baseClass);
    void addBaseClass(Class baseClass);

    // Reference module
    static Module getRef(String id);
}
```

#### 2. ModuleProvider
Register modules:

```java
public interface ModuleProvider {
    Module getModule();
}

@Component
public class ContactModuleProvider implements ModuleProvider {
    
    @Override
    public Module getModule() {
        Module module = new Module("crm", "CRM");
        module.setIcon("business");
        
        PageGroup salesGroup = new PageGroup("sales", "Sales");
        salesGroup.addPage(new CrudPage("orders", "Orders", Order.class));
        module.addPageGroup(salesGroup);
        
        return module;
    }
}
```

#### 3. Page
Represents a navigable page:

```java
public class Page extends NavigationElement<Page> {

    // Constructors — path is required
    public Page();
    public Page(String id, String name, String path);
    public Page(String id, String name, String path, boolean closeable);

    // Path
    Page setPath(String path);
    String getPath();
    String getVirtualPath();       // e.g. "crm/sales/orders"
    String getPrettyVirtualPath(); // e.g. "crm/sales/orders" (human-readable)

    // Display (inherited from NavigationElement)
    void setIcon(String icon);
    void setDescription(String description);
    void setPosition(double position);
    void setVisible(boolean visible);

    // Behaviour
    Page setClosable(boolean closable);
    Page setShowAsPopup(boolean popup);

    // Featured / priority
    Page setFeatured(boolean featured);
    Page featured();
    Page featured(int priority);
    void setPriority(int priority);   // default 100

    // Main page marker
    Page setMain(boolean main);
    Page main();

    // Actions (uses PageAction, not Action)
    Page addAction(PageAction action);
    Page addActions(PageAction action, PageAction... others);
    void removeAction(PageAction action);
    List<PageAction> getActions();

    // Lifecycle callbacks
    void onOpen(Callback callback);
    void onClose(Callback callback);
    void onUnload(Callback callback);

    // Hierarchy
    String getFullName();
    PageGroup getPageGroup();
}
```

#### 4. ModuleContainer
Central registry for modules:

```java
@Autowired
private ModuleContainer moduleContainer;

public void workWithModules() {
    // Get all modules
    Collection<Module> modules = moduleContainer.getModules();
    
    // Find specific module
    Module crm = moduleContainer.getModuleById("crm");
    
    // Find page
    Page contactsPage = moduleContainer.findPage("/crm/contacts");
    
    // Get navigation tree
    List<Module> topModules = moduleContainer.getModules();
}
```

### Navigation Hierarchy

```
Module (e.g., "CRM")
├── PageGroup (e.g., "Sales")
│   ├── Page (e.g., "Orders")
│   └── Page (e.g., "Invoices")
├── PageGroup (e.g., "Customers")
│   ├── Page (e.g., "Contacts")
│   └── Page (e.g., "Companies")
└── Page (e.g., "Dashboard")
```

### When to Use Navigation Module

- Register modules and pages
- Build navigation menu
- Create module references
- Organize application features

---

## Actions Module

**Artifact ID**: `tools.dynamia.actions`

**Purpose**: Provides action framework for handling user interactions.

### Core Components

#### 1. Action Interface
Represents an action:

```java
public interface Action {
    
    String getName();
    String getDescription();
    String getIcon();
    
    boolean isVisible();
    boolean isEnabled();
    
    void actionPerformed(ActionEvent evt);
}
```

#### 2. AbstractAction
Base class for actions:

```java
@InstallAction
public class SendEmailAction extends AbstractAction {
    
    private final EmailService emailService;
    
    public SendEmailAction(EmailService emailService) {
        super("Send Email", "sendEmail");
        this.emailService = emailService;
        setIcon("email");
    }
    
    @Override
    public void actionPerformed(ActionEvent evt) {
        // Action implementation
        emailService.send(...);
    }
}
```

#### 3. CrudAction
Action for CRUD operations:

```java
@InstallAction
public class ApproveContactAction extends AbstractCrudAction {
    
    private final ContactService contactService;
    
    public ApproveContactAction(ContactService contactService) {
        super("Approve", "approve");
        this.contactService = contactService;
    }
    
    @Override
    public void actionPerformed(CrudActionEvent evt) {
        Contact contact = (Contact) evt.getEntity();
        contactService.approveContact(contact);
    }
}
```

#### 4. InstallAction Annotation
Register actions:

```java
@InstallAction
public class MyAction extends AbstractAction {
    // Automatically registered as Spring bean
    // Discovered and registered in action registry
}

// Usage in UI
@Autowired
private ActionRegistry actionRegistry;

public void useAction() {
    Action action = actionRegistry.findAction("myAction");
    action.actionPerformed(new ActionEvent(this));
}
```

### Action Types

| Type | Purpose | Example |
|------|---------|---------|
| **Generic Action** | General operations | Print, Export, Archive |
| **CRUD Action** | Entity operations | Approve, Activate, Deactivate |
| **Custom Action** | Domain-specific | Calculate, SendEmail, GenerateReport |

### When to Use Actions Module

- Implement reusable operations
- Create custom button actions
- Encapsulate business logic
- Build menu items and toolbar actions

---

## Integration Module

**Artifact ID**: `tools.dynamia.integration`

**Purpose**: Provides Spring Boot integration and auto-configuration.

### Key Features

- **EnableDynamiaTools Annotation**: Main annotation to enable platform
- **Auto-configuration**: Automatic bean registration and initialization
- **Property Configuration**: Configuration via application.properties
- **Customizers**: Extension points for customization

### EnableDynamiaTools

```java
@SpringBootApplication
@EnableDynamiaTools
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

What @EnableDynamiaTools does:
1. Scans for ModuleProviders
2. Initializes metadata engine
3. Registers CrudService beans
4. Sets up navigation system
5. Initializes validators and converters
6. Configures view renderers

### Configuration Properties

```properties
# Application name and title
dynamia.application.name=My Application
dynamia.application.title=My Enterprise App

# Navigation settings
dynamia.navigation.auto-discover-modules=true
dynamia.navigation.default-module=home

# CRUD settings
dynamia.crud.default-page-size=20
dynamia.crud.enable-cache=true

# View rendering
dynamia.views.theme=dynamical
dynamia.views.enable-descriptors=true

# Security
dynamia.security.enabled=true
```

### When to Use Integration Module

- Configure DynamiaTools startup
- Set application properties
- Enable/disable features
- Customize auto-configuration

---

## Web Module

**Artifact ID**: `tools.dynamia.web`

**Purpose**: Provides web utilities and HTTP support.

### Key Features

- **REST Support**: REST endpoint utilities
- **HTTP Utilities**: Request/response helpers
- **PWA Support**: Progressive Web App features
- **Web Configuration**: Web layer configuration

### REST Utilities

```java
@RestController
@RequestMapping("/api/contacts")
public class ContactRestController {
    
    private final CrudService crudService;
    
    @GetMapping
    public ResponseEntity<List<Contact>> getAll() {
        List<Contact> contacts = crudService.findAll(Contact.class);
        return ResponseEntity.ok(contacts);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Contact> getById(@PathVariable Long id) {
        Contact contact = crudService.find(Contact.class, id);
        return contact != null ? ResponseEntity.ok(contact) : ResponseEntity.notFound().build();
    }
    
    @PostMapping
    public ResponseEntity<Contact> create(@RequestBody Contact contact) {
        Contact created = crudService.save(contact);
        return ResponseEntity.status(201).body(created);
    }
}
```

### PWA Configuration

```java
@Configuration
public class PwaConfig {
    
    @Bean
    public PwaManifest pwaManifest() {
        PwaManifest manifest = new PwaManifest();
        manifest.setName("My Application");
        manifest.setShortName("MyApp");
        manifest.setDisplay(PwaManifest.Display.STANDALONE);
        return manifest;
    }
}
```

### When to Use Web Module

- Build REST APIs
- Configure web layer
- Implement PWA features
- Handle HTTP requests/responses

---

## Reports Module

**Artifact ID**: `tools.dynamia.reports`

**Purpose**: Provides reporting and data analysis framework.

### Key Features

- **Query Builders**: JPQL and native SQL queries
- **Report Templates**: Pre-built report types
- **Export Formats**: CSV, Excel, PDF export
- **Report Actions**: Built-in report actions
- **Chart Integration**: Visual data representation

### Report Definition

```java
@Service
public class ContactReportService {
    
    private final ReportBuilder reportBuilder;
    
    public Report generateContactReport() {
        Report report = reportBuilder.create("contacts-report")
            .setTitle("Contacts Report")
            .setQuery("SELECT c FROM Contact c WHERE c.active = true")
            .addColumn("name", "Name", String.class)
            .addColumn("email", "Email", String.class)
            .addColumn("company.name", "Company", String.class)
            .setExportFormat(ExportFormat.EXCEL)
            .build();
        
        return report;
    }
}
```

### Report Rendering

```java
@Autowired
private ReportRenderer reportRenderer;

public void renderReport(Report report) {
    reportRenderer.render(report, response.getOutputStream());
}
```

### When to Use Reports Module

- Generate reports from entities
- Export data to various formats
- Create business analytics
- Generate visualizations

---

## Templates Module

**Artifact ID**: `tools.dynamia.templates`

**Purpose**: Provides template rendering system.

### Template Features

- **Template Engine**: Freemarker-based templating
- **Template Variables**: Pass data to templates
- **Template Inheritance**: Template composition
- **Custom Directives**: Create custom template functions

### Template Usage

```java
@Service
public class EmailTemplateService {
    
    private final TemplateEngine templateEngine;
    
    public String renderEmailTemplate(String templateName, Map<String, Object> data) {
        return templateEngine.render(templateName, data);
    }
}

// Usage
Map<String, Object> data = new HashMap<>();
data.put("firstName", "John");
data.put("lastName", "Doe");

String emailBody = emailTemplateService.renderEmailTemplate("welcome-email", data);
```

### Template File Example

```freemarker
<!-- src/main/resources/templates/welcome-email.html -->
<h1>Welcome ${firstName} ${lastName}!</h1>
<p>Thank you for registering.</p>
```

### When to Use Templates Module

- Generate email content
- Create dynamic documents
- Build report templates
- Generate dynamic HTML

---

## Viewers Module

**Artifact ID**: `tools.dynamia.viewers`

**Purpose**: Provides view rendering system and view renderers.

### Core Components

#### 1. ViewRenderer
Interface for rendering views:

```java
public interface ViewRenderer {
    
    void render(ViewDescriptor view, Map<String, Object> data, HttpServletResponse response);
    
    String getViewType();
    
    boolean supports(ViewDescriptor view);
}
```

#### 2. View Descriptors
YAML-based view definitions:

```yaml
# ContactForm.yml
view: form
beanClass: com.example.Contact
fields:
  name:
    label: Contact Name
    required: true
  email:
    label: Email Address
    required: true
  phone:
    label: Phone Number
  company:
    reference: true
    referencedClass: com.example.Company
```

#### 3. ViewCustomizer
Customize views before rendering:

```java
@Component
public class ContactViewCustomizer implements ViewCustomizer {
    
    @Override
    public void customize(ViewDescriptor view, Map<String, Object> metadata) {
        if (view.getBeanClass() == Contact.class) {
            // Hide sensitive fields
            view.getFields().get("internalNotes").setVisible(false);
        }
    }
    
    @Override
    public Class<?> getCustomizedClass() {
        return Contact.class;
    }
}
```

#### 4. ZKViewRenderer
Default ZK-based renderer:

```java
@Component
public class ZKViewRenderer implements ViewRenderer {
    
    @Override
    public void render(ViewDescriptor view, Map<String, Object> data, HttpServletResponse response) {
        // Convert view descriptor to ZK components
        // Generate HTML/JavaScript
    }
    
    @Override
    public String getViewType() {
        return "zk";
    }
}
```

### Supported View Types

| Type | Purpose | Example |
|------|---------|---------|
| **form** | Data entry forms | Edit contact form |
| **table** | Data display tables | Contact list table |
| **tree** | Hierarchical data | Category tree |
| **report** | Data reports | Sales report |
| **dashboard** | Multiple widgets | Executive dashboard |

### Creating Custom Renderer

```java
@Component
public class CustomViewRenderer implements ViewRenderer {
    
    @Override
    public void render(ViewDescriptor view, Map<String, Object> data, HttpServletResponse response) {
        // Custom rendering logic
        // Could render React, Vue, Angular, etc.
    }
    
    @Override
    public String getViewType() {
        return "custom";
    }
    
    @Override
    public boolean supports(ViewDescriptor view) {
        return "custom".equals(view.getType());
    }
}
```

### When to Use Viewers Module

- Render views from descriptors
- Create custom view renderers
- Customize existing views
- Build alternative UI frameworks

---

## App Module

**Artifact ID**: `tools.dynamia.app`

**Purpose**: Provides application bootstrap and metadata exposure.

### Key Components

#### 1. ApplicationMetadata
Exposes application metadata:

```java
@RestController
@RequestMapping("/api/metadata")
public class ApplicationMetadataController {
    
    @GetMapping("/modules")
    public ResponseEntity<List<ModuleDTO>> getModules() {
        // Returns all registered modules
    }
    
    @GetMapping("/entities/{className}")
    public ResponseEntity<EntityMetadata> getEntityMetadata(@PathVariable String className) {
        // Returns entity metadata: fields, relationships, constraints
    }
    
    @GetMapping("/views/{beanClass}")
    public ResponseEntity<ViewDescriptor> getViewDescriptor(@PathVariable String beanClass) {
        // Returns view descriptor for entity
    }
}
```

#### 2. Application Bootstrap
Automatic application startup:

```java
@SpringBootApplication
@EnableDynamiaTools
public class MyApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
        // Automatically:
        // - Scans for @Component classes
        // - Registers ModuleProviders
        // - Initializes metadata engine
        // - Sets up navigation
        // - Configures security (if enabled)
    }
}
```

### When to Use App Module

- Bootstrap DynamiaTools applications
- Expose metadata to frontend
- Configure application startup
- Access application information

---

## Module Dependencies

Here's the dependency hierarchy of core modules:

```
Presentation Layer
    └── Viewers Module
        └── Templates Module
            └── Web Module

Application Layer
    ├── CRUD Module
    │   └── Domain Module
    ├── Actions Module
    │   └── Domain Module
    ├── Navigation Module
    │   └── Domain Module
    └── Integration Module
        ├── CRUD Module
        ├── Navigation Module
        └── App Module

Business Logic Layer
    └── (Your services)

Data Access Layer
    ├── Domain-JPA Module
    │   └── Domain Module
    └── CrudService Implementation

Infrastructure Layer
    ├── Commons Module
    ├── Integration Module
    └── (Database, etc.)
```

### Minimal Dependencies

For a minimal DynamiaTools application:

```xml
<!-- Core -->
<dependency>
    <groupId>tools.dynamia</groupId>
    <artifactId>tools.dynamia.app</artifactId>
    <version>26.3.2</version>
</dependency>

<!-- Data Access -->
<dependency>
    <groupId>tools.dynamia</groupId>
    <artifactId>tools.dynamia.domain.jpa</artifactId>
    <version>26.3.2</version>
</dependency>

<!-- UI -->
<dependency>
    <groupId>tools.dynamia</groupId>
    <artifactId>tools.dynamia.zk</artifactId>
    <version>26.3.2</version>
</dependency>
```

This brings in all core modules as transitive dependencies.

---

## Module Interdependencies

| Module | Depends On |
|--------|-----------|
| **commons** | (no dependencies) |
| **domain** | commons |
| **domain-jpa** | domain |
| **crud** | domain, domain-jpa |
| **navigation** | domain, commons |
| **actions** | domain, commons |
| **integration** | all core modules |
| **web** | commons, domain |
| **reports** | domain, crud, commons |
| **templates** | commons |
| **viewers** | domain, templates, commons |
| **app** | all core modules |

---

## Summary

Each core module provides specific functionality:

- **commons**: Utilities and helpers
- **domain**: Domain abstractions
- **domain-jpa**: JPA implementations
- **crud**: Data operations
- **navigation**: Application structure
- **actions**: User interactions
- **integration**: Spring Boot setup
- **web**: HTTP and REST
- **reports**: Reporting and analytics
- **templates**: Template rendering
- **viewers**: UI rendering
- **app**: Application bootstrap

Understanding these modules is essential for building effective DynamiaTools applications.

---

Next: Read [Development Patterns](./DEVELOPMENT_PATTERNS.md) to learn best practices and patterns.

