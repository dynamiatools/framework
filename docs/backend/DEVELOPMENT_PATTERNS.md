# Development Patterns & Best Practices

This document provides guidance on common patterns, best practices, and anti-patterns when developing with DynamiaTools.

## Table of Contents

1. [Entity Design Patterns](#entity-design-patterns)
2. [Service Layer Patterns](#service-layer-patterns)
3. [Module Provider Pattern](#module-provider-pattern)
4. [Action Patterns](#action-patterns)
5. [Validator Pattern](#validator-pattern)
6. [Customizer Pattern](#customizer-pattern)
7. [View Descriptor Patterns](#view-descriptor-patterns)
8. [Error Handling](#error-handling)
9. [Performance Patterns](#performance-patterns)
10. [Anti-Patterns to Avoid](#anti-patterns-to-avoid)

---

## Entity Design Patterns

### Pattern 1: Proper Entity Inheritance

**✅ CORRECT**: Extend BaseEntity with JPA annotations

```java
@Entity
@Table(name = "contacts")
public class Contact extends BaseEntity {
    
    private String name;
    private String email;
    
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
    
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;
    
    // Business methods
    public boolean isValid() {
        return name != null && email != null;
    }
    
    // Getters and setters
}
```

**❌ WRONG**: Manual id management, no inheritance

```java
@Entity
public class Contact {
    
    @Id
    @GeneratedValue
    private Long id;
    
    private String name;
    // Missing audit fields
    // No business logic
}
```

### Pattern 2: Relationships

**✅ CORRECT**: Proper JPA relationships with cascade settings

```java
@Entity
public class Company extends BaseEntity {
    
    private String name;
    
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contact> contacts = new ArrayList<>();
    
    // Add/remove helpers
    public void addContact(Contact contact) {
        contacts.add(contact);
        contact.setCompany(this);
    }
    
    public void removeContact(Contact contact) {
        contacts.remove(contact);
        contact.setCompany(null);
    }
}
```

**❌ WRONG**: No cascade settings, missing collection initialization

```java
@Entity
public class Company extends BaseEntity {
    
    private String name;
    
    @OneToMany
    private List<Contact> contacts;  // Not initialized, no cascade
}
```

### Pattern 3: Enums

**✅ CORRECT**: Using @Enumerated for proper persistence

```java
@Entity
public class Contact extends BaseEntity {
    
    @Enumerated(EnumType.STRING)
    private ContactStatus status;
    
    @Enumerated(EnumType.STRING)
    private ContactType type;
}

public enum ContactStatus {
    ACTIVE,
    INACTIVE,
    ARCHIVED
}

public enum ContactType {
    PERSON,
    COMPANY,
    GOVERNMENT
}
```

**❌ WRONG**: Storing enums as integers or strings without @Enumerated

```java
@Entity
public class Contact extends BaseEntity {
    
    private String status;  // Fragile to typos
    private Integer type;   // Unclear what numbers mean
}
```

### Pattern 4: Embedded Objects

**✅ CORRECT**: Using @Embeddable for value objects

```java
@Embeddable
public class Address {
    
    private String street;
    private String city;
    private String state;
    private String zipCode;
    
    public String getFormattedAddress() {
        return street + ", " + city + ", " + state + " " + zipCode;
    }
}

@Entity
public class Contact extends BaseEntity {
    
    private String name;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "billing_street")),
        @AttributeOverride(name = "city", column = @Column(name = "billing_city"))
    })
    private Address billingAddress;
    
    @Embedded
    private Address shippingAddress;
}
```

**❌ WRONG**: Repeated fields instead of embedded objects

```java
@Entity
public class Contact extends BaseEntity {
    
    private String billingStreet;
    private String billingCity;
    private String shippingStreet;
    private String shippingCity;
    // Duplicate logic
}
```

---

## Service Layer Patterns

### Pattern 1: Dependency Injection

**✅ CORRECT**: Constructor injection for immutability

```java
@Service
public class ContactService {
    
    private final CrudService crudService;
    private final EmailService emailService;
    private final ContactValidator validator;
    
    public ContactService(CrudService crudService, 
                         EmailService emailService,
                         ContactValidator validator) {
        this.crudService = crudService;
        this.emailService = emailService;
        this.validator = validator;
    }
    
    public Contact createContact(String name, String email) {
        Contact contact = new Contact();
        contact.setName(name);
        contact.setEmail(email);
        return crudService.save(contact);
    }
}
```

**❌ WRONG**: Field injection or service locator pattern

```java
@Service
public class ContactService {
    
    @Autowired  // Fragile, hard to test
    private CrudService crudService;
    
    // Or even worse:
    private static ContactService instance;
    
    public static ContactService getInstance() {
        return instance;
    }
}
```

### Pattern 2: Transaction Management

**✅ CORRECT**: Using @Transactional annotation

```java
@Service
public class OrderService {
    
    private final CrudService crudService;
    private final EmailService emailService;
    
    @Transactional
    public Order createOrder(Order order, List<OrderLine> lines) {
        // All or nothing - if email fails, order is rolled back
        Order saved = crudService.save(order);
        
        for (OrderLine line : lines) {
            line.setOrder(saved);
            crudService.save(line);
        }
        
        emailService.sendOrderConfirmation(saved);
        
        return saved;
    }
    
    @Transactional(readOnly = true)
    public Order getOrderWithLines(Long orderId) {
        return crudService.find(Order.class, orderId);
    }
}
```

**❌ WRONG**: No transaction management, manual transaction handling

```java
@Service
public class OrderService {
    
    public Order createOrder(Order order, List<OrderLine> lines) {
        // If email fails, order is saved but transaction inconsistent
        Order saved = crudService.save(order);
        // ... more logic
        emailService.sendOrderConfirmation(saved);  // Could fail
        return saved;
    }
}
```

### Pattern 3: Business Logic in Services

**✅ CORRECT**: Services contain business logic, not entities

```java
@Service
public class ContactApprovalService {
    
    private final CrudService crudService;
    private final NotificationService notificationService;
    
    @Transactional
    public Contact approveContact(Long contactId) {
        Contact contact = crudService.find(Contact.class, contactId);
        
        if (!contact.isPending()) {
            throw new IllegalStateException("Contact is not in pending status");
        }
        
        contact.setStatus(ContactStatus.APPROVED);
        contact.setApprovedDate(LocalDateTime.now());
        
        Contact saved = crudService.save(contact);
        
        // Notify after approval
        notificationService.sendApprovalNotification(saved);
        
        return saved;
    }
}
```

**❌ WRONG**: Business logic in entity or in action

```java
@Entity
public class Contact extends BaseEntity {
    
    public void approve(NotificationService notificationService) {
        // Bad: Entity should not call services
        this.status = ContactStatus.APPROVED;
        notificationService.send(...);
    }
}
```

---

## Module Provider Pattern

### Pattern 1: Basic Module Provider

**✅ CORRECT**: Implement ModuleProvider as Spring component

```java
@Component
public class CrmModuleProvider implements ModuleProvider {
    
    @Override
    public Module getModule() {
        Module module = new Module("crm", "CRM");
        module.setIcon("business");
        module.setDescription("Customer Relationship Management");
        module.setPosition(10);
        
        // Contacts page group
        PageGroup contactGroup = new PageGroup("contacts-group", "Contacts");
        contactGroup.addPage(new CrudPage("contacts", "All Contacts", Contact.class));
        module.addPageGroup(contactGroup);
        
        // Companies page group
        PageGroup companyGroup = new PageGroup("companies-group", "Companies");
        companyGroup.addPage(new CrudPage("companies", "All Companies", Company.class));
        module.addPageGroup(companyGroup);
        
        return module;
    }
}
```

**❌ WRONG**: Module provider not registered as component

```java
public class CrmModuleProvider implements ModuleProvider {
    // Not a Spring component - won't be discovered!
    
    @Override
    public Module getModule() {
        // ...
    }
}
```

### Pattern 2: Advanced Module Configuration

**✅ CORRECT**: Complex module setup with dependencies

```java
@Component
public class AdvancedModuleProvider implements ModuleProvider {
    
    private final CrudService crudService;
    private final SecurityService securityService;
    
    public AdvancedModuleProvider(CrudService crudService, 
                                 SecurityService securityService) {
        this.crudService = crudService;
        this.securityService = securityService;
    }
    
    @Override
    public Module getModule() {
        Module module = new Module("advanced", "Advanced");
        
        // Only add pages if user has permission
        if (securityService.hasPermission("advanced:read")) {
            
            // Get data to calculate position
            long contactCount = crudService.count(Contact.class);
            
            PageGroup group = new PageGroup("main", "Main");
            
            // Add dashboard
            if (contactCount > 0) {
                group.addPage(new Page("dashboard", "Dashboard"));
            }
            
            module.addPageGroup(group);
        }
        
        return module;
    }
}
```

---

## Action Patterns

### Pattern 1: Simple Action

**✅ CORRECT**: Basic action implementation

```java
@InstallAction
public class ExportContactsAction extends AbstractAction {
    
    private final CrudService crudService;
    private final ExportService exportService;
    
    public ExportContactsAction(CrudService crudService, ExportService exportService) {
        super("Export Contacts", "exportContacts");
        this.crudService = crudService;
        this.exportService = exportService;
        setIcon("download");
        setDescription("Export all contacts to Excel");
    }
    
    @Override
    public void actionPerformed(ActionEvent evt) {
        List<Contact> contacts = crudService.findAll(Contact.class);
        exportService.exportToExcel(contacts);
    }
}
```

**❌ WRONG**: Action with hardcoded logic, not reusable

```java
@InstallAction
public class ContactAction extends AbstractAction {
    
    public ContactAction() {
        super("Contact Action", "contactAction");
    }
    
    @Override
    public void actionPerformed(ActionEvent evt) {
        // Hardcoded queries
        List results = entityManager.createQuery("SELECT c FROM Contact c").getResultList();
        // Direct file I/O
        // No separation of concerns
    }
}
```

### Pattern 2: CRUD Action

**✅ CORRECT**: Entity-specific action

```java
@InstallAction
public class ActivateContactAction extends AbstractCrudAction {
    
    private final ContactService contactService;
    
    public ActivateContactAction(ContactService contactService) {
        super("Activate", "activate");
        this.contactService = contactService;
        setIcon("check");
    }
    
    @Override
    public void actionPerformed(CrudActionEvent evt) {
        Contact contact = (Contact) evt.getEntity();
        
        if (contact.getStatus() != ContactStatus.INACTIVE) {
            showMessage("Contact is not inactive");
            return;
        }
        
        contactService.activateContact(contact);
        showMessage("Contact activated successfully");
    }
}
```

**❌ WRONG**: Action without entity validation

```java
@InstallAction
public class ActivateAction extends AbstractCrudAction {
    
    @Override
    public void actionPerformed(CrudActionEvent evt) {
        Contact contact = (Contact) evt.getEntity();
        contact.setActive(true);  // No validation, no service call
    }
}
```

### Pattern 3: Conditional Actions

**✅ CORRECT**: Actions that check conditions

```java
@InstallAction
public class ApproveContactAction extends AbstractCrudAction {
    
    private final ContactService contactService;
    private final SecurityService securityService;
    
    @Override
    public void actionPerformed(CrudActionEvent evt) {
        Contact contact = (Contact) evt.getEntity();
        contactService.approveContact(contact);
    }
    
    @Override
    public boolean isEnabled(Object entity) {
        if (!(entity instanceof Contact)) {
            return false;
        }
        
        Contact contact = (Contact) entity;
        
        // Only enabled for pending contacts
        return contact.getStatus() == ContactStatus.PENDING;
    }
    
    @Override
    public boolean isVisible() {
        // Only show to managers
        return securityService.hasRole("MANAGER");
    }
}
```

---

## Validator Pattern

### Pattern 1: Field Validator

**✅ CORRECT**: Validate individual fields

```java
@InstallValidator
public class ContactEmailValidator implements Validator<Contact> {
    
    private final CrudService crudService;
    
    public ContactEmailValidator(CrudService crudService) {
        this.crudService = crudService;
    }
    
    @Override
    public List<ValidationError> validate(Contact contact) {
        List<ValidationError> errors = new ArrayList<>();
        
        // Email format validation
        if (contact.getEmail() != null && !isValidEmail(contact.getEmail())) {
            errors.add(new ValidationError("email", "Invalid email format"));
        }
        
        // Uniqueness validation (database check)
        if (contact.getEmail() != null && contact.getId() == null) {
            Contact existing = crudService.findFirst(Contact.class, 
                "email", contact.getEmail());
            if (existing != null) {
                errors.add(new ValidationError("email", "Email already registered"));
            }
        }
        
        return errors;
    }
    
    @Override
    public Class<Contact> getValidatedClass() {
        return Contact.class;
    }
    
    private boolean isValidEmail(String email) {
        return email.matches("[A-Za-z0-9+_.-]+@(.+)$");
    }
}
```

**❌ WRONG**: Validation in entity

```java
@Entity
public class Contact extends BaseEntity {
    
    private String email;
    
    @PrePersist
    public void validate() {
        if (email == null) {
            throw new RuntimeException("Email is required");  // Bad: exceptions in lifecycle
        }
    }
}
```

### Pattern 2: Complex Validation

**✅ CORRECT**: Multi-field cross validation

```java
@InstallValidator
public class OrderValidator implements Validator<Order> {
    
    @Override
    public List<ValidationError> validate(Order order) {
        List<ValidationError> errors = new ArrayList<>();
        
        // Check dates
        if (order.getShipDate() != null && order.getOrderDate() != null) {
            if (order.getShipDate().isBefore(order.getOrderDate())) {
                errors.add(new ValidationError("shipDate", 
                    "Ship date must be after order date"));
            }
        }
        
        // Check total amount
        if (order.getTotal() != null && order.getTotal().signum() <= 0) {
            errors.add(new ValidationError("total", "Total must be positive"));
        }
        
        // Check lines
        if (order.getLines() == null || order.getLines().isEmpty()) {
            errors.add(new ValidationError("lines", "Order must have at least one line"));
        }
        
        return errors;
    }
    
    @Override
    public Class<Order> getValidatedClass() {
        return Order.class;
    }
}
```

---

## Customizer Pattern

### Pattern 1: View Customizer

**✅ CORRECT**: Modify view descriptors before rendering

```java
@Component
public class ContactFormCustomizer implements ViewCustomizer {
    
    private final SecurityService securityService;
    
    public ContactFormCustomizer(SecurityService securityService) {
        this.securityService = securityService;
    }
    
    @Override
    public void customize(ViewDescriptor view, Map<String, Object> metadata) {
        if (view.getBeanClass() != Contact.class) {
            return;
        }
        
        // Hide fields from non-admins
        if (!securityService.hasRole("ADMIN")) {
            view.getFields().get("internalNotes").setVisible(false);
            view.getFields().get("creditLimit").setVisible(false);
        }
        
        // Make email read-only for existing records
        if (metadata.get("isNew") == false) {
            view.getFields().get("email").setReadOnly(true);
        }
        
        // Add computed field
        FieldDescriptor statusField = new FieldDescriptor("statusText");
        statusField.setLabel("Status");
        statusField.setReadOnly(true);
        view.addField(statusField);
    }
    
    @Override
    public Class<?> getCustomizedClass() {
        return Contact.class;
    }
}
```

### Pattern 2: Multiple Customizers

**✅ CORRECT**: Chain multiple customizers

```java
@Component
public class AdminViewCustomizer implements ViewCustomizer {
    
    @Override
    public void customize(ViewDescriptor view, Map<String, Object> metadata) {
        // Admin-specific customization
    }
}

@Component
public class AuditViewCustomizer implements ViewCustomizer {
    
    @Override
    public void customize(ViewDescriptor view, Map<String, Object> metadata) {
        // Add audit fields
        view.addField(createReadOnlyField("createdBy"));
        view.addField(createReadOnlyField("createdDate"));
        view.addField(createReadOnlyField("modifiedBy"));
        view.addField(createReadOnlyField("modifiedDate"));
    }
}
```

---

## View Descriptor Patterns

### Pattern 1: CRUD View Descriptor

**✅ CORRECT**: Well-structured view descriptor

```yaml
# ContactCrud.yml
view: crud
entity: com.example.Contact
title: Contact Management
description: Manage customer contacts

pages:
  # List page
  list:
    view: table
    fields:
      - name
      - email
      - company.name
      - status
    actions:
      - edit
      - delete
      - export
  
  # Form page (create/edit)
  form:
    view: form
    title: Contact Details
    fields:
      name:
        label: Contact Name
        required: true
        minLength: 2
        maxLength: 100
      email:
        label: Email Address
        required: true
        pattern: '^[A-Za-z0-9+_.-]+@(.+)$'
      phone:
        label: Phone Number
      company:
        reference: true
        referencedEntity: com.example.Company
      status:
        label: Status
        type: enum
        enumClass: com.example.ContactStatus
```

### Pattern 2: Customized Field Descriptors

**✅ CORRECT**: Field-level configuration

```yaml
# DetailedContact.yml
view: form
entity: com.example.Contact

fields:
  name:
    label: Full Name
    required: true
    placeholder: Enter contact name
    help: Full legal name of the contact
    className: field-required
  
  email:
    label: Email
    type: email
    required: true
    validator: EmailValidator
  
  phone:
    label: Phone Number
    mask: '(999) 999-9999'
    placeholder: (123) 456-7890
  
  status:
    label: Current Status
    type: select
    readOnly: true
    options:
      - { value: ACTIVE, label: Active }
      - { value: INACTIVE, label: Inactive }
      - { value: ARCHIVED, label: Archived }
  
  notes:
    label: Internal Notes
    type: textarea
    rows: 5
    visible: ${user.hasRole('ADMIN')}
```

---

## Error Handling

### Pattern 1: Service Exception Handling

**✅ CORRECT**: Custom exceptions for domain errors

```java
@Service
public class ContactService {
    
    @Transactional
    public Contact approveContact(Long contactId) throws ContactNotFoundException, 
                                                         InvalidContactStatusException {
        Contact contact = crudService.find(Contact.class, contactId);
        
        if (contact == null) {
            throw new ContactNotFoundException("Contact not found: " + contactId);
        }
        
        if (contact.getStatus() != ContactStatus.PENDING) {
            throw new InvalidContactStatusException(
                "Cannot approve contact with status: " + contact.getStatus());
        }
        
        contact.setStatus(ContactStatus.APPROVED);
        return crudService.save(contact);
    }
}

// Custom exceptions
public class ContactNotFoundException extends RuntimeException {
    public ContactNotFoundException(String message) {
        super(message);
    }
}

public class InvalidContactStatusException extends RuntimeException {
    public InvalidContactStatusException(String message) {
        super(message);
    }
}
```

**❌ WRONG**: Generic exceptions or silent failures

```java
public Contact approveContact(Long contactId) {
    Contact contact = crudService.find(Contact.class, contactId);
    
    if (contact == null) {
        return null;  // Silent failure
    }
    
    if (contact.getStatus() != ContactStatus.PENDING) {
        throw new RuntimeException("Invalid status");  // Too generic
    }
    
    // ...
}
```

### Pattern 2: Controller Exception Handling

**✅ CORRECT**: Global exception handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ContactNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleContactNotFound(
            ContactNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("CONTACT_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(404).body(error);
    }
    
    @ExceptionHandler(InvalidContactStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatus(
            InvalidContactStatusException ex) {
        ErrorResponse error = new ErrorResponse("INVALID_STATUS", ex.getMessage());
        return ResponseEntity.status(400).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", 
            "An unexpected error occurred");
        return ResponseEntity.status(500).body(error);
    }
}

public record ErrorResponse(String code, String message) {}
```

---

## Performance Patterns

### Pattern 1: Lazy Loading

**✅ CORRECT**: Proper lazy loading configuration

```java
@Entity
public class Contact extends BaseEntity {
    
    private String name;
    
    // Many-to-One is EAGER by default (ok)
    @ManyToOne
    private Company company;
    
    // One-to-Many should be LAZY (default)
    @OneToMany(mappedBy = "contact", fetch = FetchType.LAZY)
    private List<ContactNote> notes = new ArrayList<>();
    
    // Large content field should be lazy loaded
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] attachmentContent;
}
```

### Pattern 2: Eager Loading When Needed

**✅ CORRECT**: Join fetch for single queries

```java
@Service
public class ContactDetailService {
    
    private final EntityManager entityManager;
    
    public Contact getContactWithNotes(Long contactId) {
        String jpql = "SELECT DISTINCT c FROM Contact c " +
                      "LEFT JOIN FETCH c.notes " +
                      "WHERE c.id = :id";
        
        return entityManager.createQuery(jpql, Contact.class)
            .setParameter("id", contactId)
            .getSingleResult();
    }
}
```

**❌ WRONG**: N+1 query problem

```java
@Service
public class BadContactService {
    
    public List<Contact> getContactsWithNotes() {
        // This executes 1 query
        List<Contact> contacts = crudService.findAll(Contact.class);
        
        // This executes N queries (one per contact)
        for (Contact c : contacts) {
            c.getNotes().size();  // Triggers lazy loading
        }
        
        return contacts;
    }
}
```

### Pattern 3: Pagination

**✅ CORRECT**: Paginate large result sets

```java
@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    
    private final CrudService crudService;
    
    @GetMapping
    public Page<Contact> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return crudService.findAll(Contact.class, pageable);
    }
}
```

---

## Anti-Patterns to Avoid

### ❌ Anti-Pattern 1: God Object

**Problem**: Single class doing too much

```java
@Service
public class ContactManagementService {
    
    // Validates, saves, sends emails, generates reports, exports, etc.
    // Too many responsibilities!
}
```

**Solution**: Break into focused services

```java
@Service
public class ContactService {
    // Only CRUD and basic business logic
}

@Service
public class ContactNotificationService {
    // Email and SMS notifications
}

@Service
public class ContactReportService {
    // Report generation
}

@Service
public class ContactExportService {
    // Data export
}
```

### ❌ Anti-Pattern 2: Anemic Entities

**Problem**: Entities with no business logic

```java
@Entity
public class Contact {
    
    private String name;
    private String email;
    
    // Only getters and setters, no logic
}
```

**Solution**: Add business methods to entities

```java
@Entity
public class Contact {
    
    private String name;
    private String email;
    private ContactStatus status;
    
    // Business methods
    public boolean isActive() {
        return status == ContactStatus.ACTIVE;
    }
    
    public void activate() {
        this.status = ContactStatus.ACTIVE;
    }
}
```

### ❌ Anti-Pattern 3: Leaky Abstraction

**Problem**: Exposing database details in service layer

```java
@Service
public class ContactService {
    
    public List<Contact> getContacts() {
        // Leaking JPA implementation detail
        return entityManager.createQuery("SELECT c FROM Contact c").getResultList();
    }
}
```

**Solution**: Use abstraction (CrudService)

```java
@Service
public class ContactService {
    
    private final CrudService crudService;
    
    public List<Contact> getContacts() {
        return crudService.findAll(Contact.class);
    }
}
```

### ❌ Anti-Pattern 4: Tight Coupling

**Problem**: Direct dependencies on concrete classes

```java
@Service
public class OrderService {
    
    private final MySqlContactRepository contactRepository;  // Too specific
    
    public void placeOrder(Order order) {
        contactRepository.save(order.getContact());
    }
}
```

**Solution**: Depend on abstractions

```java
@Service
public class OrderService {
    
    private final CrudService crudService;  // Generic abstraction
    
    public void placeOrder(Order order) {
        crudService.save(order.getContact());
    }
}
```

### ❌ Anti-Pattern 5: Silent Failures

**Problem**: Errors not properly reported

```java
@Service
public class ContactService {
    
    public void importContacts(List<String> names) {
        for (String name : names) {
            try {
                Contact contact = new Contact();
                contact.setName(name);
                crudService.save(contact);
            } catch (Exception e) {
                // Silently ignoring errors
                e.printStackTrace();
            }
        }
    }
}
```

**Solution**: Proper error handling and logging

```java
@Service
public class ContactService {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    public ImportResult importContacts(List<String> names) {
        List<String> successful = new ArrayList<>();
        List<ImportError> errors = new ArrayList<>();
        
        for (String name : names) {
            try {
                Contact contact = new Contact();
                contact.setName(name);
                crudService.save(contact);
                successful.add(name);
            } catch (ValidationException e) {
                logger.warn("Validation failed for contact: {}", name, e);
                errors.add(new ImportError(name, e.getMessage()));
            } catch (Exception e) {
                logger.error("Failed to import contact: {}", name, e);
                errors.add(new ImportError(name, "Unexpected error: " + e.getMessage()));
            }
        }
        
        return new ImportResult(successful, errors);
    }
}
```

### ❌ Anti-Pattern 6: Mixing Concerns in Views

**Problem**: Business logic in view layer

```java
public class ContactForm extends Window {
    
    private final Contact contact;
    
    public void onSaveClick() {
        // Validation logic in view
        if (contact.getName() == null) {
            showError("Name required");
            return;
        }
        
        // Save logic in view
        Session session = sessionFactory.openSession();
        session.save(contact);
        session.close();
    }
}
```

**Solution**: Keep business logic in services

```java
public class ContactForm extends Window {
    
    private final ContactService contactService;
    private Contact contact;
    
    public ContactForm(ContactService contactService) {
        this.contactService = contactService;
    }
    
    public void onSaveClick() {
        try {
            contactService.saveContact(contact);
            showSuccess("Contact saved");
        } catch (ValidationException e) {
            showError("Validation failed: " + e.getMessage());
        }
    }
}
```

---

## Summary of Best Practices

1. **Use Constructor Injection** for immutability and testability
2. **Apply @Transactional** at service method level
3. **Implement ModuleProvider** for module registration
4. **Create focused services** with single responsibility
5. **Use custom validators** for complex validation
6. **Implement view customizers** for display logic
7. **Handle exceptions properly** with custom exception types
8. **Use lazy loading** wisely to avoid N+1 queries
9. **Paginate large result sets** for performance
10. **Avoid tight coupling** - depend on abstractions

---

Next: Read [Advanced Topics](./ADVANCED_TOPICS.md) for deep dives into complex scenarios.

