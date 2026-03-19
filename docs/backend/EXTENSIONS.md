# Enterprise Extensions Guide

DynamiaTools includes powerful, production-ready extensions that solve common enterprise challenges. This guide provides an overview of each extension, its purpose, key features, and integration patterns.

## Table of Contents

1. [Extensions Overview](#extensions-overview)
2. [SaaS (Multi-Tenancy) Extension](#saas-multi-tenancy-extension)
3. [Entity Files Extension](#entity-files-extension)
4. [Email & SMS Extension](#email--sms-extension)
5. [Dashboard Extension](#dashboard-extension)
6. [Reports Extension](#reports-extension)
7. [Finance Framework](#finance-framework)
8. [File Importer Extension](#file-importer-extension)
9. [Security Extension](#security-extension)
10. [HTTP Functions Extension](#http-functions-extension)
11. [Extension Integration Patterns](#extension-integration-patterns)

---

## Extensions Overview

All extensions are located in `/extensions/` and follow consistent patterns:

```
extensions/
├── saas/                    # Multi-tenancy
├── entity-files/            # File attachments
├── email-sms/               # Communication
├── dashboard/               # Dashboards
├── reports/                 # Advanced reporting
├── finances/                # Financial calculations
├── file-importer/           # Data import
├── security/                # Auth & authorization
└── http-functions/          # HTTP functions
```

### Extension Dependency Format

All extensions use CalVer versioning (same as core platform):

```xml
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.EXTENSION_NAME</artifactId>
    <version>26.3.2</version>
</dependency>
```

### Common Extension Pattern

Most extensions follow this structure:

```
extension-name/sources/
├── api/                     # Public interfaces
├── core/ or jpa/            # Implementation
├── ui/                      # UI components
└── pom.xml
```

---

## SaaS (Multi-Tenancy) Extension

**Artifact ID**: `tools.dynamia.modules.saas`

**Purpose**: Build multi-tenant applications where each customer has isolated data and configuration.

### Key Features

- **Account Management**: Create and manage customer accounts
- **Data Isolation**: Per-tenant data segregation
- **Subscription Handling**: Subscription and payment tracking
- **Tenant Context**: Automatic tenant filtering
- **Shared Infrastructure**: Single database for multiple tenants

### What the Extension Provides

- Account entity and management
- Subscription and billing support
- Automatic tenant filtering in queries
- Multi-tenant context management
- Per-tenant configuration

### Adding the Dependency

```xml
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.saas</artifactId>
    <version>26.3.2</version>
</dependency>
```

### Using SaaS Features

#### 1. Making Entities Tenant-Aware

```java
@Entity
public class Contact extends BaseEntity {
    
    @ManyToOne
    private Account account;  // Add this to make tenant-aware
    
    private String name;
    private String email;
    
    // Getters and setters
}
```

#### 2. Automatic Tenant Filtering

```java
@Service
public class ContactService {
    
    private final CrudService crudService;
    private final TenantProvider tenantProvider;
    
    public List<Contact> getContactsForCurrentAccount() {
        // TenantProvider automatically filters by current tenant
        Account currentAccount = tenantProvider.getCurrentAccount();
        return crudService.findAll(Contact.class, "account", currentAccount);
    }
}
```

#### 3. Account Management

```java
@Service
public class AccountService {
    
    private final CrudService crudService;
    
    public Account createAccount(String name, String plan) {
        Account account = new Account();
        account.setName(name);
        account.setPlan(plan);
        account.setStatus("ACTIVE");
        return crudService.save(account);
    }
    
    public Subscription addSubscription(Account account, String planType) {
        Subscription subscription = new Subscription();
        subscription.setAccount(account);
        subscription.setPlanType(planType);
        subscription.setStartDate(LocalDate.now());
        return crudService.save(subscription);
    }
}
```

### Configuration

```properties
# Multi-tenant mode
dynamia.saas.enabled=true
dynamia.saas.mode=ACCOUNT_ISOLATION

# Database strategy
dynamia.saas.database-strategy=SHARED_DATABASE

# Automatic tenant context
dynamia.saas.auto-tenant-context=true
```

### When to Use SaaS Extension

- Building SaaS products
- Multi-customer applications
- Account-based data isolation
- Subscription management
- Per-account configuration

---

## Entity Files Extension

**Artifact ID**: `tools.dynamia.modules.entityfiles` and `tools.dynamia.modules.entityfiles.s3`

**Purpose**: Attach files to any entity with storage options (local disk or AWS S3).

### Key Features

- **File Attachment**: Attach files to entities
- **Multiple Files**: Multiple files per entity
- **Storage Options**: Local disk or AWS S3
- **File Metadata**: Track file information
- **Automatic Cleanup**: Remove files when entities deleted
- **Direct Download**: Secure file access

### Adding the Dependency

```xml
<!-- Local Storage -->
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.entityfiles</artifactId>
    <version>26.3.2</version>
</dependency>

<!-- AWS S3 Storage (optional) -->
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.entityfiles.s3</artifactId>
    <version>26.3.2</version>
</dependency>
```

### Using Entity Files

#### 1. Mark Entity for File Attachment

```java
@Entity
public class Contact extends BaseEntity implements HasFiles {
    
    private String name;
    private String email;
    
    // From HasFiles interface - file management
    @OneToMany(mappedBy = "entity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EntityFile> files = new ArrayList<>();
    
    @Override
    public List<EntityFile> getFiles() {
        return files;
    }
    
    @Override
    public void addFile(EntityFile file) {
        files.add(file);
    }
}
```

#### 2. File Upload

```java
@Service
public class ContactFileService {
    
    private final EntityFileManager entityFileManager;
    
    public EntityFile uploadProfilePicture(Contact contact, MultipartFile file) throws IOException {
        EntityFile entityFile = new EntityFile();
        entityFile.setEntity(contact);
        entityFile.setName(file.getOriginalFilename());
        entityFile.setContentType(file.getContentType());
        entityFile.setSize(file.getSize());
        
        return entityFileManager.save(entityFile, file.getInputStream());
    }
}
```

#### 3. File Download

```java
@RestController
@RequestMapping("/api/files")
public class FileDownloadController {
    
    private final EntityFileManager entityFileManager;
    
    @GetMapping("/{fileId}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable Long fileId) {
        EntityFile file = entityFileManager.find(fileId);
        InputStream inputStream = entityFileManager.getInputStream(file);
        
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
            .body(new InputStreamResource(inputStream));
    }
}
```

### Storage Configuration

#### Local Storage

```properties
# Local disk storage (default)
dynamia.entityfiles.storage=local
dynamia.entityfiles.local.base-directory=/app/files
```

#### AWS S3 Storage

```properties
# AWS S3 storage
dynamia.entityfiles.storage=s3
dynamia.entityfiles.s3.bucket-name=my-bucket
dynamia.entityfiles.s3.region=us-east-1
dynamia.entityfiles.s3.access-key=${AWS_ACCESS_KEY}
dynamia.entityfiles.s3.secret-key=${AWS_SECRET_KEY}
```

### When to Use Entity Files Extension

- Attach documents to records
- Store user profiles or avatars
- Manage entity-related files
- Build document management features
- Track file metadata

---

## Email & SMS Extension

**Artifact ID**: `tools.dynamia.modules.email`

**Purpose**: Send professional emails and SMS messages with templates and tracking.

### Key Features

- **Email Sending**: JavaMail-based email
- **SMS Delivery**: AWS SNS integration
- **Templates**: Reusable email/SMS templates
- **Accounts**: Multiple email/SMS accounts
- **Logging**: Delivery tracking
- **Async Sending**: Non-blocking message delivery

### Adding the Dependency

```xml
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.email</artifactId>
    <version>26.3.2</version>
</dependency>
```

### Using Email Features

#### 1. Send Simple Email

```java
@Service
public class ContactNotificationService {
    
    private final EmailService emailService;
    
    public void sendWelcomeEmail(Contact contact) {
        Message message = new Message();
        message.setTo(contact.getEmail());
        message.setSubject("Welcome to Our Platform");
        message.setBody("Thank you for registering, " + contact.getName());
        
        emailService.send(message);
    }
}
```

#### 2. Send Email with Template

```java
@Service
public class TemplatedEmailService {
    
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    
    public void sendPersonalizedEmail(Contact contact) {
        Map<String, Object> data = new HashMap<>();
        data.put("firstName", contact.getFirstName());
        data.put("company", contact.getCompany().getName());
        
        String body = templateEngine.render("welcome-email", data);
        
        Message message = new Message();
        message.setTo(contact.getEmail());
        message.setSubject("Welcome, " + contact.getFirstName());
        message.setBody(body);
        message.setHtml(true);
        
        emailService.send(message);
    }
}
```

#### 3. Send SMS

```java
@Service
public class SmsNotificationService {
    
    private final SmsService smsService;
    
    public void sendActivationCode(Contact contact, String code) {
        SmsMessage sms = new SmsMessage();
        sms.setPhoneNumber(contact.getPhone());
        sms.setContent("Your activation code is: " + code);
        
        smsService.send(sms);
    }
}
```

#### 4. Email Account Configuration

```java
@Configuration
public class EmailConfig {
    
    @Bean
    public EmailAccount defaultEmailAccount() {
        EmailAccount account = new EmailAccount();
        account.setName("Default");
        account.setEmail("noreply@example.com");
        account.setSmtpHost("smtp.gmail.com");
        account.setSmtpPort(587);
        account.setUsername("your-email@gmail.com");
        account.setPassword("your-app-password");
        account.setUsesTls(true);
        return account;
    }
}
```

### Configuration

```properties
# Email settings
dynamia.email.enabled=true
dynamia.email.default-account=Default
dynamia.email.async=true

# SMTP settings
dynamia.email.smtp.host=smtp.gmail.com
dynamia.email.smtp.port=587
dynamia.email.smtp.username=${EMAIL_USERNAME}
dynamia.email.smtp.password=${EMAIL_PASSWORD}

# SMS settings (AWS SNS)
dynamia.sms.enabled=true
dynamia.sms.provider=aws-sns
dynamia.sms.aws.region=us-east-1
dynamia.sms.aws.access-key=${AWS_ACCESS_KEY}
dynamia.sms.aws.secret-key=${AWS_SECRET_KEY}
```

### When to Use Email & SMS Extension

- Send transactional emails
- Deliver SMS notifications
- Marketing communications
- Delivery confirmations
- Account verification
- Alerts and reminders

---

## Dashboard Extension

**Artifact ID**: `tools.dynamia.modules.dashboard`

**Purpose**: Create beautiful, responsive dashboards with customizable widgets.

### Key Features

- **Widgets**: Pre-built dashboard widgets
- **Charts**: Chart.js integration
- **Responsive Layout**: Mobile-friendly design
- **Customizable**: Widget drag-and-drop, theme support
- **Real-time Updates**: WebSocket support for live data
- **Permissions**: Widget-level access control

### Adding the Dependency

```xml
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.dashboard</artifactId>
    <version>26.3.2</version>
</dependency>
```

### Creating Dashboard Widgets

#### 1. Custom Widget

```java
@Component
public class ContactCountWidget extends AbstractDashboardWidget {
    
    private final CrudService crudService;
    
    public ContactCountWidget(CrudService crudService) {
        super("contactCount", "Contact Count", "chart-bar");
        this.crudService = crudService;
    }
    
    @Override
    public Map<String, Object> getData() {
        long totalContacts = crudService.count(Contact.class);
        long activeContacts = crudService.count(Contact.class, "active", true);
        
        return Map.of(
            "total", totalContacts,
            "active", activeContacts,
            "percentage", (activeContacts * 100) / totalContacts
        );
    }
    
    @Override
    public String getTemplateId() {
        return "contact-count-widget";
    }
}
```

#### 2. Chart Widget

```java
@Component
public class ContactsByCityWidget extends AbstractDashboardWidget {
    
    private final CrudService crudService;
    
    public ContactsByCityWidget(CrudService crudService) {
        super("contactsByCity", "Contacts by City", "chart-pie");
        this.crudService = crudService;
    }
    
    @Override
    public Map<String, Object> getData() {
        // Query and group by city
        List<Object[]> results = crudService.find(Contact.class)
            .stream()
            .collect(Collectors.groupingByConcurrent(Contact::getCity, Collectors.counting()))
            .entrySet()
            .stream()
            .map(e -> new Object[]{e.getKey(), e.getValue()})
            .toList();
        
        return Map.of("data", results);
    }
}
```

#### 3. Dashboard Page

```java
@Component
public class DashboardModuleProvider implements ModuleProvider {
    
    @Override
    public Module getModule() {
        Module module = new Module("dashboard", "Dashboard");
        
        DashboardPage dashboardPage = new DashboardPage("main", "Executive Dashboard");
        dashboardPage.addWidget("contactCount");
        dashboardPage.addWidget("contactsByCity");
        dashboardPage.addWidget("revenueChart");
        
        module.addPage(dashboardPage);
        return module;
    }
}
```

### When to Use Dashboard Extension

- Executive dashboards
- Real-time monitoring
- KPI tracking
- Business intelligence
- Analytics and reporting

---

## Reports Extension

**Artifact ID**: `tools.dynamia.reports.core`

**Purpose**: Advanced reporting framework with multiple export formats and visualization.

### Key Features

- **Query Builders**: JPQL and native SQL
- **Export Formats**: CSV, Excel, PDF
- **Charts**: Built-in chart support
- **Templates**: Reusable report templates
- **Scheduling**: Scheduled report generation
- **Email Delivery**: Send reports via email
- **Caching**: Performance optimization

### Adding the Dependency

```xml
<dependency>
    <groupId>tools.dynamia.reports</groupId>
    <artifactId>tools.dynamia.reports.core</artifactId>
    <version>26.3.2</version>
</dependency>
```

### Creating Reports

#### 1. Simple Report

```java
@Service
public class ContactListReportService {
    
    private final ReportService reportService;
    
    public Report generateContactReport() {
        Report report = new Report();
        report.setName("contact-list");
        report.setTitle("All Contacts");
        report.setQuery("SELECT c FROM Contact c ORDER BY c.name");
        
        report.addColumn("id", "ID");
        report.addColumn("name", "Name");
        report.addColumn("email", "Email");
        report.addColumn("company.name", "Company");
        
        return report;
    }
}
```

#### 2. Report with Filtering

```java
@Service
public class FilteredReportService {
    
    private final ReportService reportService;
    
    public Report generateActiveContactsReport(String city) {
        Report report = new Report();
        report.setName("active-contacts-by-city");
        report.setTitle("Active Contacts in " + city);
        
        String query = "SELECT c FROM Contact c WHERE c.active = true AND c.city = :city";
        report.setQuery(query);
        report.addParameter("city", city);
        
        report.addColumn("name", "Name");
        report.addColumn("email", "Email");
        report.addColumn("phone", "Phone");
        
        return report;
    }
}
```

#### 3. Report Export

```java
@RestController
@RequestMapping("/api/reports")
public class ReportExportController {
    
    private final ReportService reportService;
    
    @GetMapping("/{reportId}/export")
    public ResponseEntity<InputStreamResource> exportReport(
            @PathVariable String reportId,
            @RequestParam(defaultValue = "EXCEL") String format) {
        
        Report report = reportService.getReport(reportId);
        byte[] data = reportService.export(report, ExportFormat.valueOf(format));
        
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"" + report.getName() + "." + format.toLowerCase() + "\"")
            .body(new InputStreamResource(new ByteArrayInputStream(data)));
    }
}
```

### When to Use Reports Extension

- Business intelligence
- Data export
- Compliance reporting
- Analytics dashboards
- Scheduled report generation

---

## Finance Framework

**Artifact ID**: `tools.dynamia.modules.finances`

**Purpose**: Pure Java financial calculation framework for invoices, quotes, POs with taxes, discounts, and withholdings.

### Key Features

- **Immutable Value Objects**: Money, exchange rates, totals
- **Strategy Pattern**: Pluggable calculation strategies
- **No Persistence**: Pure domain logic, not tied to database
- **No Framework Dependencies**: Works with any Java project
- **Deterministic Results**: Consistent calculations
- **Fully Tested**: Comprehensive test coverage

### What the Extension Does NOT Include

❌ User Interface (no forms or views)
❌ Persistence (no database logic)
❌ Accounting (no double-entry bookkeeping)
❌ Tax legislation (country-specific rules)
❌ Electronic invoicing (XML, signatures)
❌ Workflow (approvals, status transitions)
❌ Numbering (document sequences)

### Adding the Dependency

```xml
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.finances</artifactId>
    <version>26.3.2</version>
</dependency>
```

### Using Finance Framework

#### 1. Simple Invoice Calculation

```java
public class InvoiceCalculator {
    
    public FinancialSummary calculateInvoice(Invoice invoice) {
        FinancialCalculator calculator = new FinancialCalculator();
        
        // Add line items
        for (InvoiceLine line : invoice.getLines()) {
            calculator.addCharge(
                new Charge()
                    .setType(ChargeType.LINE_ITEM)
                    .setAmount(line.getUnitPrice().multiply(line.getQuantity()))
                    .setTaxable(true)
            );
        }
        
        // Add discount
        if (invoice.getDiscount() != null) {
            calculator.addCharge(
                new Charge()
                    .setType(ChargeType.DISCOUNT)
                    .setAmount(invoice.getDiscount())
            );
        }
        
        // Add tax
        calculator.addCharge(
            new Charge()
                .setType(ChargeType.TAX)
                .setPercentage(invoice.getTaxRate())
        );
        
        return calculator.calculate();
    }
}
```

#### 2. Multi-Currency Support

```java
public class MultiCurrencyCalculator {
    
    public FinancialSummary calculate(Invoice invoice, ExchangeRate rate) {
        FinancialCalculator calculator = new FinancialCalculator();
        
        // Add charges in original currency
        // Then apply exchange rate
        
        for (InvoiceLine line : invoice.getLines()) {
            Money amount = line.getUnitPrice()
                .multiply(line.getQuantity())
                .convertTo(rate);
            
            calculator.addCharge(
                new Charge()
                    .setAmount(amount)
            );
        }
        
        return calculator.calculate();
    }
}
```

#### 3. Complex Pricing

```java
public class ComplexPricingCalculator {
    
    public FinancialSummary calculate(PurchaseOrder po) {
        FinancialCalculator calculator = new FinancialCalculator();
        
        // Line items
        for (POLine line : po.getLines()) {
            calculator.addCharge(new Charge()
                .setType(ChargeType.LINE_ITEM)
                .setAmount(Money.of(line.getPrice(), Currency.USD))
            );
        }
        
        // Volume discount
        if (po.getLines().size() > 10) {
            calculator.addCharge(new Charge()
                .setType(ChargeType.DISCOUNT)
                .setPercentage(5)
            );
        }
        
        // Tax
        calculator.addCharge(new Charge()
            .setType(ChargeType.TAX)
            .setPercentage(po.getTaxRate())
        );
        
        // Withholding
        calculator.addCharge(new Charge()
            .setType(ChargeType.WITHHOLDING)
            .setPercentage(3)
        );
        
        return calculator.calculate();
    }
}
```

### When to Use Finance Framework

- E-commerce pricing
- Invoice generation
- Purchase order calculations
- Subscription billing
- Tax calculations
- Complex pricing logic

---

## File Importer Extension

**Artifact ID**: `tools.dynamia.modules.fileimporter`

**Purpose**: Import bulk data from Excel/CSV files with validation and error handling.

### Key Features

- **Format Support**: Excel (.xlsx) and CSV
- **Field Mapping**: Map file columns to entity properties
- **Validation**: Pre-import and row-level validation
- **Error Handling**: Detailed error reporting
- **Batch Processing**: Efficient bulk import
- **Dry Run**: Preview before import

### Adding the Dependency

```xml
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.fileimporter</artifactId>
    <version>26.3.2</version>
</dependency>
```

### Using File Importer

#### 1. Configure Import

```java
@Service
public class ContactImportService {
    
    private final FileImporterService importerService;
    private final CrudService crudService;
    
    public ImportResult importContactsFromExcel(MultipartFile file) {
        ImportConfiguration config = new ImportConfiguration();
        config.setTargetClass(Contact.class);
        config.setBatchSize(100);
        config.setValidateBeforeImport(true);
        
        // Map file columns to properties
        config.addColumnMapping(0, "name");           // Column A → name
        config.addColumnMapping(1, "email");          // Column B → email
        config.addColumnMapping(2, "phone");          // Column C → phone
        config.addColumnMapping(3, "company.id");     // Column D → company.id
        
        return importerService.importFile(file, config);
    }
}
```

#### 2. Custom Import Handler

```java
@Service
public class CustomContactImporter {
    
    private final CrudService crudService;
    
    public ImportResult importContacts(File file) {
        List<Contact> contacts = new ArrayList<>();
        List<ImportError> errors = new ArrayList<>();
        
        // Read Excel file
        ExcelReader reader = new ExcelReader(file);
        int rowNumber = 0;
        
        for (Map<String, String> row : reader.readRows()) {
            rowNumber++;
            
            try {
                Contact contact = new Contact();
                contact.setName(row.get("name"));
                contact.setEmail(row.get("email"));
                contact.setPhone(row.get("phone"));
                
                // Validation
                if (contact.getEmail() == null) {
                    errors.add(new ImportError(rowNumber, "email", "Email is required"));
                    continue;
                }
                
                contacts.add(contact);
                
            } catch (Exception e) {
                errors.add(new ImportError(rowNumber, "general", e.getMessage()));
            }
        }
        
        // Save if no errors
        if (errors.isEmpty()) {
            crudService.save(contacts);
        }
        
        return new ImportResult(contacts.size(), errors);
    }
}
```

### When to Use File Importer Extension

- Bulk data import
- Customer migration
- Inventory updates
- Periodic data synchronization
- Legacy system migration

---

## Security Extension

**Artifact ID**: `tools.dynamia.modules.security`

**Purpose**: Enterprise authentication and authorization with role-based access control.

### Key Features

- **User Management**: User and role management
- **RBAC**: Role-based access control
- **Password Security**: Bcrypt hashing, password policies
- **Audit Trail**: Track user actions
- **OAuth2/OIDC**: External identity provider support
- **Multi-factor Auth**: Optional 2FA support

### Adding the Dependency

```xml
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.security</artifactId>
    <version>26.3.2</version>
</dependency>
```

### Using Security Features

#### 1. Define Roles and Permissions

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityProfile adminProfile() {
        SecurityProfile admin = new SecurityProfile();
        admin.setName("ADMIN");
        admin.addPermission("contacts", "READ");
        admin.addPermission("contacts", "CREATE");
        admin.addPermission("contacts", "UPDATE");
        admin.addPermission("contacts", "DELETE");
        admin.addPermission("system", "MANAGE");
        return admin;
    }
    
    @Bean
    public SecurityProfile userProfile() {
        SecurityProfile user = new SecurityProfile();
        user.setName("USER");
        user.addPermission("contacts", "READ");
        user.addPermission("contacts", "CREATE");
        user.addPermission("contacts", "UPDATE");
        return user;
    }
}
```

#### 2. Secure Methods

```java
@Service
public class ContactService {
    
    @Secured("ROLE_ADMIN")
    public void deleteContact(Long id) {
        // Only admins can delete
    }
    
    @PreAuthorize("hasPermission('contacts', 'READ')")
    public List<Contact> getAllContacts() {
        // Requires read permission
    }
    
    @PreAuthorize("hasPermission(#contact, 'UPDATE')")
    public Contact updateContact(Contact contact) {
        // Requires update permission on this contact
    }
}
```

#### 3. User Authentication

```java
@Service
public class AuthService {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    public AuthResponse login(String email, String password) {
        User user = userService.findByEmail(email);
        
        if (user != null && passwordEncoder.matches(password, user.getPasswordHash())) {
            // Generate JWT token
            String token = generateJWT(user);
            return new AuthResponse(token, user);
        }
        
        throw new AuthenticationException("Invalid credentials");
    }
}
```

### When to Use Security Extension

- User authentication
- Role-based access control
- Permission management
- Audit trail
- Single sign-on (OAuth2/OIDC)

---

## HTTP Functions Extension

**Artifact ID**: `tools.dynamia.modules.http` (if available)

**Purpose**: Create serverless-style HTTP functions that can be triggered by HTTP requests.

### Key Features

- **Function Registration**: Define HTTP endpoints as functions
- **Automatic Routing**: Route HTTP requests to functions
- **Request/Response Handling**: Automatic serialization/deserialization
- **Error Handling**: Global error handling for functions
- **Authentication**: Optional authentication per function

### When to Use HTTP Functions Extension

- Create custom REST endpoints
- Integrate external webhooks
- Build event handlers
- Microservice endpoints

---

## Extension Integration Patterns

### Pattern 1: Stacking Extensions

```java
@SpringBootApplication
@EnableDynamiaTools
public class MyApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}

// Dependencies include:
// - Core modules (automatic)
// - SaaS extension (automatic tenant filtering)
// - Entity Files extension (file attachment)
// - Email extension (notifications)
// - Reports extension (analytics)
```

### Pattern 2: Conditional Extension Usage

```java
@Configuration
@ConditionalOnProperty(name = "dynamia.saas.enabled", havingValue = "true")
public class SaasConfig {
    
    @Bean
    public TenantProvider tenantProvider() {
        return new AccountBasedTenantProvider();
    }
}
```

### Pattern 3: Custom Extension

```java
@Component
public class CustomFeatureProvider implements ModuleProvider {
    
    @Override
    public Module getModule() {
        Module module = new Module("custom", "Custom Features");
        
        // Integrate with SaaS
        // Integrate with Files
        // Integrate with Email
        
        return module;
    }
}
```

### Pattern 4: Extension Interoperability

```java
@Service
public class NotificationService {
    
    // Uses Email extension
    private final EmailService emailService;
    
    // Uses File attachment
    private final EntityFileManager fileManager;
    
    // Uses Security for permissions
    @PreAuthorize("hasPermission('notifications', 'MANAGE')")
    public void sendNotificationWithAttachment(User user, String message, File attachment) {
        // Implementation combining multiple extensions
    }
}
```

---

## Extension Development Best Practices

### 1. **Follow Module Structure**
```
extension-name/
├── sources/api/          # Public interfaces
├── sources/core/         # Implementation
├── sources/ui/           # UI components
└── sources/pom.xml
```

### 2. **Use Dependency Injection**
```java
@Component
public class ExtensionService {
    
    private final CrudService crudService;
    private final EventPublisher eventPublisher;
    
    public ExtensionService(CrudService crudService, EventPublisher eventPublisher) {
        this.crudService = crudService;
        this.eventPublisher = eventPublisher;
    }
}
```

### 3. **Publish Extension Events**
```java
@Service
public class FileUploadService {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public void uploadFile(EntityFile file) {
        // ... upload logic
        eventPublisher.publishEvent(new FileUploadedEvent(file));
    }
}
```

### 4. **Provide Extension Points**
```java
public interface FileStorageProvider {
    void save(EntityFile file, InputStream inputStream);
    InputStream load(EntityFile file);
}

@Component
public class S3FileStorageProvider implements FileStorageProvider {
    // AWS S3 implementation
}
```

---

## Summary

DynamiaTools extensions provide:

- **SaaS**: Multi-tenancy and account management
- **Entity Files**: File attachment with S3 support
- **Email & SMS**: Professional messaging
- **Dashboard**: Executive dashboards
- **Reports**: Advanced reporting and analytics
- **Finance**: Financial calculations
- **File Importer**: Bulk data import
- **Security**: Authentication and authorization
- **HTTP Functions**: Serverless-style functions

Each extension follows consistent patterns and integrates seamlessly with core modules.

---

Next: Read [Development Patterns](./DEVELOPMENT_PATTERNS.md) to learn best practices for working with extensions and core modules.

