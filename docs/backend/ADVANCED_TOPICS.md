# Advanced Topics

This document covers advanced concepts for building enterprise-grade applications with DynamiaTools, including deep Spring integration, custom extension development, security, caching, and deployment patterns.

## Table of Contents

1. [Advanced Spring Integration](#advanced-spring-integration)
2. [Custom Extension Development](#custom-extension-development)
3. [Custom View Renderers](#custom-view-renderers)
4. [Security Integration](#security-integration)
5. [Caching & Performance](#caching--performance)
6. [Event System](#event-system)
7. [Scheduled Tasks](#scheduled-tasks)
8. [REST API Development](#rest-api-development)
9. [Progressive Web Apps (PWA)](#progressive-web-apps-pwa)
10. [Modularity & Microservices](#modularity--microservices)

---

## Advanced Spring Integration

### Bean Lifecycle Hooks

Use standard Spring lifecycle annotations to integrate with the DynamiaTools startup:

```java
@Component
public class AppInitializer {

    @Autowired
    private CrudService crudService;

    @PostConstruct
    public void init() {
        // Runs once after the bean is fully initialized
        // Good place to register defaults, seed data checks, etc.
    }

    @PreDestroy
    public void teardown() {
        // Cleanup resources before context shutdown
    }
}
```

### CommandLineRunner for Startup Logic

Use `CommandLineRunner` (or `ApplicationRunner`) to run logic after the application starts:

```java
@Component
@Order(1)
public class InitSampleDataCLR implements CommandLineRunner {

    private final CrudService crudService;

    public InitSampleDataCLR(CrudService crudService) {
        this.crudService = crudService;
    }

    @Override
    public void run(String... args) {
        if (crudService.count(Category.class) == 0) {
            crudService.save(new Category("Fiction"));
            crudService.save(new Category("Science"));
            crudService.save(new Category("Technology"));
        }
    }
}
```

### Conditional Beans

Use `@ConditionalOnProperty` to enable or disable beans based on configuration:

```java
@Configuration
@ConditionalOnProperty(name = "myapp.feature.advanced-search", havingValue = "true")
public class AdvancedSearchConfig {

    @Bean
    public AdvancedSearchService advancedSearchService() {
        return new AdvancedSearchServiceImpl();
    }
}
```

### Multiple Bean Implementations

When multiple implementations of the same interface exist, use `@Qualifier` or `@Primary`:

```java
public interface ReportGenerator {
    byte[] generate(Report report);
}

@Service
@Primary
public class PdfReportGenerator implements ReportGenerator {
    @Override
    public byte[] generate(Report report) { /* PDF generation */ }
}

@Service
public class ExcelReportGenerator implements ReportGenerator {
    @Override
    public byte[] generate(Report report) { /* Excel generation */ }
}

// Injection with qualifier
@Service
public class ReportService {

    private final ReportGenerator defaultGenerator;

    @Qualifier("excelReportGenerator")
    private final ReportGenerator excelGenerator;

    public ReportService(ReportGenerator defaultGenerator,
                         @Qualifier("excelReportGenerator") ReportGenerator excelGenerator) {
        this.defaultGenerator = defaultGenerator;
        this.excelGenerator = excelGenerator;
    }
}
```

### Application Properties with Custom Prefix

Define strongly-typed configuration using `@ConfigurationProperties`:

```java
@ConfigurationProperties(prefix = "myapp")
public class MyAppConfigProperties {

    private String name;
    private boolean featureEnabled;
    private int maxConnections = 10;
    private Map<String, String> settings = new HashMap<>();

    // Getters and setters
}

// Enable in configuration class
@Configuration
@EnableConfigurationProperties(MyAppConfigProperties.class)
public class MyAppConfiguration { }
```

**`application.yml`**:
```yaml
myapp:
  name: My Enterprise App
  feature-enabled: true
  max-connections: 20
  settings:
    theme: dark
    locale: es
```

### DynamiaTools Application Properties

The `dynamia.app.*` prefix is the main configuration namespace:

```yaml
dynamia:
  app:
    name: My Application
    short-name: MyApp
    version: 1.0.0
    description: Enterprise application built with DynamiaTools
    company: Acme Corp
    author: Dev Team
    url: https://myapp.example.com
    template: Dynamical
    default-skin: Green
    default-logo: /static/images/logo.png
    default-icon: /static/images/icon.png
    base-package: com.example.myapp
    api-base-path: /api/v1
    web-cache-enabled: true
    build: ${timestamp}
```

---

## Custom Extension Development

Extensions allow you to add reusable features that can be activated across multiple applications.

### Extension Structure

```
my-extension/
├── sources/
│   ├── api/                   # Public interfaces and DTOs
│   │   └── pom.xml
│   ├── core/                  # Business logic implementation
│   │   └── pom.xml
│   └── ui/                    # ZK or web UI components
│       └── pom.xml
└── pom.xml
```

### 1. Define the API (interfaces)

```java
// my-extension/sources/api/
public interface NotificationService {
    void send(Notification notification);
    List<Notification> findPending(String recipient);
}

public class Notification {
    private String recipient;
    private String subject;
    private String body;
    private NotificationType type;
    // Getters/Setters
}

public enum NotificationType { EMAIL, SMS, PUSH }
```

### 2. Implement the Core

```java
// my-extension/sources/core/
@Service
public class DefaultNotificationService implements NotificationService {

    private final CrudService crudService;

    public DefaultNotificationService(CrudService crudService) {
        this.crudService = crudService;
    }

    @Override
    public void send(Notification notification) {
        // Persist and send the notification
        crudService.save(notification);
    }

    @Override
    public List<Notification> findPending(String recipient) {
        return crudService.findAll(Notification.class, "recipient", recipient);
    }
}
```

### 3. Register a Module Provider (optional)

```java
@Provider
public class NotificationModuleProvider implements ModuleProvider {

    @Override
    public Module getModule() {
        return new Module("notifications", "Notifications")
                .icon("bell")
                .addPage(new CrudPage("notifications", "Notifications", Notification.class));
    }
}
```

### 4. Use the Extension in Your Application

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.example</groupId>
    <artifactId>my-extension-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

```java
@Service
public class OrderService {

    private final NotificationService notificationService;

    public OrderService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void placeOrder(Order order) {
        // ... save order
        notificationService.send(new Notification(
            order.getCustomer().getEmail(),
            "Order Confirmed",
            "Your order #" + order.getNumber() + " has been received."
        ));
    }
}
```

### Extension Points Pattern

Provide interfaces that users of your extension can implement:

```java
// In the extension API
public interface NotificationFilter {
    boolean shouldSend(Notification notification);
}

// In the extension core – discover all filters automatically
@Service
public class FilteredNotificationService implements NotificationService {

    private final List<NotificationFilter> filters;

    public FilteredNotificationService(List<NotificationFilter> filters) {
        this.filters = filters;
    }

    @Override
    public void send(Notification notification) {
        boolean allowed = filters.stream().allMatch(f -> f.shouldSend(notification));
        if (allowed) {
            // send
        }
    }
}

// Application code provides its own filter
@Component
public class BusinessHoursFilter implements NotificationFilter {
    @Override
    public boolean shouldSend(Notification n) {
        int hour = LocalTime.now().getHour();
        return hour >= 8 && hour < 18;
    }
}
```

---

## Custom View Renderers

The default view renderer uses ZK, but you can create renderers for any frontend technology.

### Implementing ViewRenderer

```java
@Component
public class ReactViewRenderer implements ViewRenderer {

    @Override
    public String getViewType() {
        return "react-form";  // Maps to view: react-form in YAML
    }

    @Override
    public Object render(ViewDescriptor descriptor, Object value) {
        // Build the component tree for React
        return buildReactComponent(descriptor, value);
    }
}
```

### View Descriptor for Custom Renderer

```yaml
# ContactForm.yml
view: react-form
beanClass: com.example.Contact
fields:
  name:
    label: Full Name
  email:
    label: Email Address
```

### ZK ViewCustomizer (Advanced)

Override the runtime rendering of any view by implementing `ViewCustomizer<FormView<T>>`:

```java
public class InvoiceFormCustomizer implements ViewCustomizer<FormView<Invoice>> {

    @Override
    public void customize(FormView<Invoice> view) {
        FormFieldComponent detailsField = view.getFieldComponent("details");

        // React to value changes
        view.addEventListener(FormView.ON_VALUE_CHANGED, event -> {
            Invoice invoice = view.getValue();
            if (invoice != null) {
                detailsField.setVisible(invoice.getCustomer() != null);
            }
        });
    }
}
```

Register the customizer in your YAML descriptor:

```yaml
view: form
beanClass: com.example.Invoice
customizer: com.example.customizers.InvoiceFormCustomizer
fields:
  customer:
  details:
    component: crudview
    params:
      inplace: true
```

---

## Security Integration

DynamiaTools integrates with Spring Security for authentication and authorization.

### Basic Spring Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### Role-Based Access in Actions

```java
@InstallAction
public class DeleteUserAction extends AbstractCrudAction {

    public DeleteUserAction() {
        setName("Delete User");
        setApplicableClass(User.class);
    }

    @Override
    public boolean isVisible(ActionEvent event) {
        // Only show for admins
        return SecurityContextHolder.getContext()
            .getAuthentication()
            .getAuthorities()
            .stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @Override
    @Secured("ROLE_ADMIN")
    public void actionPerformed(CrudActionEvent evt) {
        User user = (User) evt.getEntity();
        crudService().delete(user);
    }
}
```

### Getting Current User

```java
@Service
public class AuditService {

    public UserInfo getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return new UserInfo(auth.getName(), auth.getAuthorities());
        }
        return UserInfo.anonymous();
    }
}
```

### OAuth2 / OIDC Integration

```java
@Configuration
@EnableWebSecurity
public class OAuth2SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/")
                .failureUrl("/login?error")
            )
            .oauth2ResourceServer(rs -> rs.jwt());
        return http.build();
    }
}
```

**`application.yml`**:
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid, profile, email
```

---

## Caching & Performance

### Enabling Ehcache 3

DynamiaTools provides `Ehcache3CacheManager` for out-of-the-box caching:

```java
@SpringBootApplication
@EnableCaching
public class MyApplication {

    @Bean
    public CacheManager cacheManager() {
        return new Ehcache3CacheManager();
    }
}
```

### Caching Service Results

```java
@Service
public class CategoryService {

    private final CrudService crudService;

    public CategoryService(CrudService crudService) {
        this.crudService = crudService;
    }

    @Cacheable("categories")
    public List<Category> getAllCategories() {
        return crudService.findAll(Category.class);
    }

    @CacheEvict(value = "categories", allEntries = true)
    public Category save(Category category) {
        return crudService.save(category);
    }
}
```

### Entity Reference Repository with Caching

DynamiaTools provides `DefaultEntityReferenceRepository` to expose named lookup lists (used in dropdowns):

```java
@Bean
public EntityReferenceRepository<Long> categoryReferenceRepository() {
    return new DefaultEntityReferenceRepository<>(Category.class, "name");
}
```

### Query Optimization

```java
@Service
public class BookService {

    private final CrudService crudService;

    public BookService(CrudService crudService) {
        this.crudService = crudService;
    }

    // Use projections to avoid loading full entities
    public List<Map<String, Object>> getBookTitlesAndISBNs() {
        return crudService.executeQuery(
            "SELECT b.id, b.title, b.isbn FROM Book b ORDER BY b.title"
        );
    }

    // Use pagination for large datasets
    public Page<Book> getBooksPage(int page, int size) {
        return crudService.findAll(Book.class, PageRequest.of(page, size));
    }
}
```

---

## Event System

DynamiaTools and Spring provide a rich event system for decoupled communication.

### Publishing Events

```java
@Service
public class OrderService {

    private final ApplicationEventPublisher eventPublisher;
    private final CrudService crudService;

    public OrderService(ApplicationEventPublisher eventPublisher, CrudService crudService) {
        this.eventPublisher = eventPublisher;
        this.crudService = crudService;
    }

    public Order placeOrder(Order order) {
        Order saved = crudService.save(order);
        eventPublisher.publishEvent(new OrderPlacedEvent(saved));
        return saved;
    }
}

// Custom event
public class OrderPlacedEvent extends ApplicationEvent {

    private final Order order;

    public OrderPlacedEvent(Order order) {
        super(order);
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }
}
```

### Listening to Events

```java
@Component
public class OrderNotificationListener {

    private final EmailService emailService;

    public OrderNotificationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        Order order = event.getOrder();
        emailService.sendOrderConfirmation(order.getCustomer().getEmail(), order);
    }

    // Async listener
    @Async
    @EventListener
    public void onOrderPlacedAsync(OrderPlacedEvent event) {
        // Long-running notification task
    }
}
```

### CRUD Lifecycle Events

Use `CrudServiceListenerAdapter` to hook into entity lifecycle:

```java
@Listener
public class BookCrudListener extends CrudServiceListenerAdapter<Book> {

    @Override
    public void beforeCreate(Book entity) {
        // Called before INSERT
        if (entity.getPublishDate() == null) {
            entity.setPublishDate(LocalDate.now());
        }
    }

    @Override
    public void afterCreate(Book entity) {
        // Called after INSERT
    }

    @Override
    public void beforeUpdate(Book entity) {
        // Called before UPDATE
        entity.setLastModified(LocalDateTime.now());
    }

    @Override
    public void beforeDelete(Book entity) {
        // Called before DELETE
        if (entity.hasActiveOrders()) {
            throw new ValidationError("Cannot delete a book with active orders");
        }
    }
}
```

---

## Scheduled Tasks

Use Spring's `@Scheduled` annotation with `@EnableScheduling` on the main class:

```java
@SpringBootApplication
@EnableScheduling
public class MyApplication { }
```

```java
@Component
public class DailyReportJob {

    private final ReportService reportService;
    private final EmailService emailService;

    public DailyReportJob(ReportService reportService, EmailService emailService) {
        this.reportService = reportService;
        this.emailService = emailService;
    }

    // Run every day at 8:00 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyReport() {
        byte[] report = reportService.generateDailyReport();
        emailService.sendReportToManagers(report);
    }

    // Run every 5 minutes
    @Scheduled(fixedDelay = 300_000)
    public void processQueuedNotifications() {
        // Process pending notifications
    }
}
```

### Long-Running Operations with Progress Monitoring

For operations that may take a long time, use `LongOperationMonitorWindow`:

```java
@InstallAction
public class ProcessAllBooksAction extends AbstractCrudAction {

    public ProcessAllBooksAction() {
        setName("Process All Books");
        setApplicableClass(Book.class);
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        LongOperationMonitorWindow.start("Processing Books", "Done", monitor -> {
            List<Book> books = crudService().findAll(Book.class);
            monitor.setMax(books.size());

            for (Book book : books) {
                monitor.setMessage("Processing: " + book.getTitle());
                processBook(book);
                monitor.increment();

                if (monitor.isStopped()) {
                    throw new ValidationError("Operation cancelled.");
                }
            }
        });
    }

    private void processBook(Book book) {
        // Long processing logic
    }
}
```

---

## REST API Development

DynamiaTools provides the `@EnableDynamiaToolsApi` annotation and `CrudServiceRestClient` to build and consume REST APIs.

### Exposing Entity REST Endpoints

```java
@SpringBootApplication
@EnableDynamiaTools
@EnableDynamiaToolsApi
public class MyApplication { }
```

### Custom REST Controllers

```java
@RestController
@RequestMapping("/api/books")
public class BookRestController {

    private final CrudService crudService;

    public BookRestController(CrudService crudService) {
        this.crudService = crudService;
    }

    @GetMapping
    public List<Book> getAll() {
        return crudService.findAll(Book.class);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getById(@PathVariable Long id) {
        Book book = crudService.find(Book.class, id);
        return book != null
            ? ResponseEntity.ok(book)
            : ResponseEntity.notFound().build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book create(@RequestBody @Valid Book book) {
        return crudService.save(book);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> update(@PathVariable Long id, @RequestBody Book updated) {
        Book existing = crudService.find(Book.class, id);
        if (existing == null) return ResponseEntity.notFound().build();
        updated.setId(id);
        return ResponseEntity.ok(crudService.save(updated));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Book book = crudService.find(Book.class, id);
        if (book != null) crudService.delete(book);
    }
}
```

### API Configuration

```yaml
dynamia:
  app:
    api-base-path: /api/v1

# Spring MVC
spring:
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher
```

### DTO Pattern

Avoid exposing JPA entities directly in REST APIs:

```java
public record BookDTO(Long id, String title, String isbn, BigDecimal price, String categoryName) {
    public static BookDTO from(Book book) {
        return new BookDTO(
            book.getId(),
            book.getTitle(),
            book.getIsbn(),
            book.getPrice(),
            book.getCategory() != null ? book.getCategory().getName() : null
        );
    }
}

@GetMapping
public List<BookDTO> getAll() {
    return crudService.findAll(Book.class).stream()
        .map(BookDTO::from)
        .toList();
}
```

---

## Progressive Web Apps (PWA)

DynamiaTools has built-in PWA support via `PWAManifest`.

### Configuring PWA Manifest

```java
@Bean
public PWAManifest manifest() {
    return PWAManifest.builder()
        .name("My Book Store")
        .shortName("Books")
        .startUrl("/")
        .backgroundColor("#ffffff")
        .themeColor("#3f51b5")
        .display("standalone")
        .categories(List.of("books", "education"))
        .addIcon(PWAIcon.builder()
            .src("android-chrome-192x192.png")
            .sizes("192x192")
            .type("image/png")
            .build())
        .addIcon(PWAIcon.builder()
            .src("android-chrome-512x512.png")
            .sizes("512x512")
            .type("image/png")
            .build())
        .addShortcut(PWAShortcut.builder()
            .name("Books")
            .shortName("Books")
            .description("Go to Books")
            .url("/library/books")
            .build())
        .build();
}
```

### Service Worker Integration

The framework auto-registers a service worker when `PWAManifest` bean is present. Static assets are cached automatically.

### Making the App Installable

Once `PWAManifest` is configured and served over HTTPS, the browser will prompt users to install the app. No additional code is required.

---

## Modularity & Microservices

### Packaging Modules as Independent JARs

Each module in your application should be a separate Maven module:

```
my-enterprise-app/
├── app/                  # Spring Boot main module
├── crm/                  # CRM module JAR
├── inventory/            # Inventory module JAR
├── billing/              # Billing module JAR
└── pom.xml
```

Each module JAR contributes `ModuleProvider`, entities, services, and descriptors. When included as a dependency in `app/`, it auto-registers.

### Module Discovery

DynamiaTools discovers modules via Spring's `@Provider` / `@Component` scanning. Just add the module JAR to the classpath and the module appears automatically:

```java
// In crm.jar
@Provider
public class CrmModuleProvider implements ModuleProvider {
    @Override
    public Module getModule() {
        return new Module("crm", "CRM")
            .addPage(new CrudPage("contacts", "Contacts", Contact.class));
    }
}
```

### Cross-Module Communication

Prefer events over direct service calls to keep modules decoupled:

```java
// CRM module publishes an event
eventPublisher.publishEvent(new CustomerCreatedEvent(customer));

// Billing module listens
@EventListener
public void onCustomerCreated(CustomerCreatedEvent event) {
    billingService.createBillingProfile(event.getCustomer());
}
```

### Conditional Modules

Load modules conditionally based on configuration:

```java
@Provider
@ConditionalOnProperty(name = "myapp.modules.crm", havingValue = "true", matchIfMissing = true)
public class CrmModuleProvider implements ModuleProvider {
    // ...
}
```

**`application.yml`**:
```yaml
myapp:
  modules:
    crm: true
    billing: false
```

---

## Summary

Advanced DynamiaTools development leverages:

- **Spring Boot** capabilities: lifecycle hooks, conditional beans, typed properties
- **Custom Extensions**: independent JARs with API/core/UI layers and extension points
- **Custom Renderers**: plug in any frontend technology via `ViewRenderer`
- **Spring Security**: role-based access, OAuth2/OIDC, method security
- **Caching**: `Ehcache3CacheManager` and `@Cacheable` for performance
- **Events**: decoupled communication via `ApplicationEventPublisher` and `@EventListener`
- **REST APIs**: standard Spring MVC controllers + DTOs + `@EnableDynamiaToolsApi`
- **PWA**: zero-config installable apps via `PWAManifest` bean
- **Modularity**: independent JARs for each domain module, auto-discovered at runtime

---

Next: Read [Examples & Integration](./EXAMPLES.md) for complete real-world code examples.

