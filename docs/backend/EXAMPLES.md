# Examples & Integration

This document provides complete, real-world code examples for the most common tasks in DynamiaTools applications. All examples are based on a **Book Store** domain and can be adapted to any enterprise application.

## Table of Contents

1. [Complete Application Setup](#complete-application-setup)
2. [Domain Entities](#domain-entities)
3. [View Descriptors](#view-descriptors)
4. [Module Provider](#module-provider)
5. [Custom Actions](#custom-actions)
6. [Validators](#validators)
7. [CRUD Listeners](#crud-listeners)
8. [Services](#services)
9. [Form Customizers](#form-customizers)
10. [REST Integration](#rest-integration)
11. [Extensions Usage](#extensions-usage)

---

## Complete Application Setup

### 1. `pom.xml` — Core Dependencies

```xml
<project>
    <groupId>com.example</groupId>
    <artifactId>mybookstore</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.0</version>
    </parent>

    <dependencies>
        <!-- DynamiaTools core -->
        <dependency>
            <groupId>tools.dynamia</groupId>
            <artifactId>tools.dynamia.app</artifactId>
            <version>26.3.2</version>
        </dependency>

        <!-- ZK UI framework -->
        <dependency>
            <groupId>tools.dynamia</groupId>
            <artifactId>tools.dynamia.zk</artifactId>
            <version>26.3.2</version>
        </dependency>

        <!-- JPA support -->
        <dependency>
            <groupId>tools.dynamia</groupId>
            <artifactId>tools.dynamia.domain.jpa</artifactId>
            <version>26.3.2</version>
        </dependency>

        <!-- H2 in-memory database (development) -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>
</project>
```

### 2. Main Application Class

```java
package mybookstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import tools.dynamia.app.Ehcache3CacheManager;
import tools.dynamia.domain.DefaultEntityReferenceRepository;
import tools.dynamia.domain.EntityReferenceRepository;
import tools.dynamia.navigation.DefaultPageProvider;
import tools.dynamia.ui.icons.IconsProvider;
import tools.dynamia.zk.ui.ZIconsProvider;

@SpringBootApplication
@EntityScan({"mybookstore", "tools.dynamia"})
@EnableCaching
@EnableScheduling
public class MyBookStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBookStoreApplication.class, args);
    }

    @Bean
    public CacheManager cacheManager() {
        return new Ehcache3CacheManager();
    }

    /** Exposes Category as a named reference list (used in combo boxes) */
    @Bean
    public EntityReferenceRepository<Long> categoryReferenceRepository() {
        return new DefaultEntityReferenceRepository<>(Category.class, "name");
    }

    /** Navigates to this page on first load */
    @Bean
    public DefaultPageProvider defaultPageProvider() {
        return () -> "library/books";
    }

    /** Use ZK icon set */
    @Bean
    public IconsProvider iconsProvider() {
        return new ZIconsProvider();
    }
}
```

### 3. `application.yml`

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false
    show-sql: false

server:
  port: 8080
  servlet:
    session:
      tracking-modes: cookie

dynamia:
  app:
    name: My Book Store
    short-name: Books
    version: 1.0.0
    description: Book inventory and sales management
    template: Dynamical
    default-skin: Green
    default-logo: /static/logo.png
    base-package: mybookstore
    web-cache-enabled: true
```

---

## Domain Entities

### Category (Parent-Child Tree)

```java
package mybookstore.domain;

import jakarta.persistence.*;
import tools.dynamia.domain.jpa.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> children = new ArrayList<>();

    public Category() {}

    public Category(String name) {
        this.name = name;
    }

    // Getters / Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Category getParent() { return parent; }
    public void setParent(Category parent) { this.parent = parent; }
    public List<Category> getChildren() { return children; }

    @Override
    public String toString() { return name; }
}
```

### Book (Rich Domain Entity)

```java
package mybookstore.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import tools.dynamia.domain.OrderBy;
import tools.dynamia.domain.jpa.BaseEntity;
import tools.dynamia.modules.entityfile.domain.EntityFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
@OrderBy("title")
public class Book extends BaseEntity {

    @NotNull
    private String title;

    @NotEmpty
    private String isbn;

    @Column(length = 2000)
    private String synopsis;

    private int year;

    private LocalDate publishDate;
    private LocalDate buyDate;

    private BigDecimal price;

    @ManyToOne
    private Category category;

    @Enumerated(EnumType.STRING)
    private StockStatus stockStatus = StockStatus.IN_STOCK;

    private boolean onSale;
    private BigDecimal salePrice;
    private double discount; // percent

    @OneToOne
    private EntityFile bookCover;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookReview> reviews = new ArrayList<>();

    // Getters / Setters (abbreviated)
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public boolean isOnSale() { return onSale; }
    public void setOnSale(boolean onSale) { this.onSale = onSale; }
    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
    public EntityFile getBookCover() { return bookCover; }
    public void setBookCover(EntityFile bookCover) { this.bookCover = bookCover; }
    public List<BookReview> getReviews() { return reviews; }
    public StockStatus getStockStatus() { return stockStatus; }
    public void setStockStatus(StockStatus stockStatus) { this.stockStatus = stockStatus; }

    @Override
    public String toString() { return title; }
}
```

### Invoice with Line Details

```java
package mybookstore.domain;

import jakarta.persistence.*;
import tools.dynamia.domain.jpa.BaseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
public class Invoice extends BaseEntity {

    private String number;

    @ManyToOne
    private Customer customer;

    private String email;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceDetail> details = new ArrayList<>();

    private BigDecimal total;

    public void calcTotal() {
        this.total = details.stream()
            .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Getters / Setters
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public List<InvoiceDetail> getDetails() { return details; }
    public BigDecimal getTotal() { return total; }
}
```

---

## View Descriptors

View descriptors (YAML files in `src/main/resources/META-INF/descriptors/`) control what users see in forms, tables, and CRUD pages without writing any UI code.

### Book Form (`BookForm.yml`)

```yaml
view: form
beanClass: mybookstore.domain.Book
autofields: false
customizer: mybookstore.customizers.BookFormCustomizer

fields:
  title:
    params:
      span: 2            # Spans 2 columns
  category:
  isbn:
  year:
  publishDate:
    component: dateselector
  onSale:
  salePrice:
    params:
      format: $###,###
  buyDate:
  price:
    params:
      format: $###,###
  synopsis:
    params:
      span: 4
      multiline: true
      height: 80px
  discount:
    params:
      constraint: "min 0 max 100"
  bookCover:
    params:
      imageOnly: true
  preview:
    component: entityfileImage
    params:
      thumbnail: true
      bindings:
        value: bookCover
  reviews:
    component: crudview

groups:
  details:
    label: Book Details
    fields: [buyDate, price, reviews, salePrice]

layout:
  columns: 4
```

### Book Table (`BookTable.yml`)

```yaml
view: table
beanClass: mybookstore.domain.Book
autofields: false

fields:
  bookCover:
    label: Cover
    component: entityfileImage
    params:
      thumbnail: true
      renderWhenNull: true
      header:
        width: 70px
        align: center

  title:
  category:

  stockStatus:
    label: Stock
    component: enumlabel
    params:
      defaultSclass: stockStatus
      sclassPrefix: status
      header:
        width: 90px

  isbn:
  year:

  publishDate:
    params:
      converter: converters.Date

  price:
    params:
      converter: converters.Currency
      header:
        align: right
        sclass: orange color-white
      cell:
        sclass: orange lighten-5
```

### Book CRUD (`BookCrud.yml`)

```yaml
view: crud
beanClass: mybookstore.domain.Book
autofields: false

params:
  queryProjection: true
```

### Book Filters (`BookFilters.yml`)

```yaml
view: entityfilters
beanClass: mybookstore.domain.Book
autofields: false

fields:
  title:
  category:
  isbn:
  stockStatus:
  publishDate:
    params:
      converter: converters.Date
  price:
    params:
      converter: converters.Currency
  creationTimestamp:
    label: Created
    params:
      converter: converters.LocalDateTime
      header:
        width: 120px
        align: center
```

### Category Tree (`CategoryTree.yml`)

```yaml
view: tree
beanClass: mybookstore.domain.Category
parentName: parent
```

### Category CRUD with Tree (`CategoryCrud.yml`)

```yaml
view: crud
beanClass: mybookstore.domain.Category
dataSetView: tree
parentName: parent
```

### Invoice Form with Inline Details (`InvoiceForm.yml`)

```yaml
view: form
beanClass: mybookstore.domain.Invoice
autofields: false

fields:
  number:
  customer:
  email:
  details:
    component: crudview
    params:
      inplace: true
      height: 400px
      span: 3

layout:
  columns: 3
```

---

## Module Provider

The module provider defines the navigation structure of your application.

```java
package mybookstore.providers;

import mybookstore.domain.*;
import org.springframework.stereotype.Component;
import tools.dynamia.crud.CrudPage;
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.navigation.*;

@Provider
public class MyBookStoreModuleProvider implements ModuleProvider {

    @Override
    public Module getModule() {
        return new Module("library", "Library")
            .icon("book")
            .description("Book inventory and sales")
            .position(0)
            .addPage(
                new CrudPage("books", "Books", Book.class),
                new CrudPage("categories", "Categories", Category.class).icon("tree"),
                new CrudPage("customers", "Customers", Customer.class).icon("people"),
                new CrudPage("invoices", "Invoices", Invoice.class)
            )
            .addPageGroup(new PageGroup("reports", "Reports")
                .addPage(
                    new Page("sales-report", "Sales Report", "classpath:/pages/sales-report.zul"),
                    new ExternalPage("docs", "Documentation", "https://dynamia.tools")
                )
            );
    }
}
```

### Settings Module

```java
@Provider
public class SettingsModuleProvider implements ModuleProvider {

    @Override
    public Module getModule() {
        return new Module("settings", "Settings")
            .icon("cog")
            .position(100)
            .addPage(new CrudPage("categories", "Book Categories", Category.class));
    }
}
```

---

## Custom Actions

### Global Action (always visible in toolbar)

```java
package mybookstore.actions;

import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.actions.ApplicationGlobalAction;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;

@InstallAction
public class HelpAction extends ApplicationGlobalAction {

    public HelpAction() {
        setName("Help");
        setImage("help");
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        UIMessages.showMessage("Visit https://dynamia.tools for documentation.", MessageType.INFO);
    }
}
```

### CRUD Action (entity-specific button)

```java
package mybookstore.actions;

import mybookstore.domain.Book;
import mybookstore.domain.StockStatus;
import tools.dynamia.actions.InstallAction;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.crud.CrudActionEvent;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;

@InstallAction
public class MarkOutOfStockAction extends AbstractCrudAction {

    public MarkOutOfStockAction() {
        setName("Mark Out of Stock");
        setApplicableClass(Book.class);
        setImage("warning");
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        Book book = (Book) evt.getEntity();
        if (book == null) {
            UIMessages.showMessage("Please select a book first.", MessageType.WARNING);
            return;
        }
        book.setStockStatus(StockStatus.OUT_OF_STOCK);
        crudService().save(book);
        UIMessages.showMessage("Book marked as out of stock.", MessageType.SUCCESS);
    }
}
```

### Bulk Action (operates on selected rows)

```java
@InstallAction
public class BulkDiscountAction extends AbstractCrudAction {

    public BulkDiscountAction() {
        setName("Apply 10% Discount");
        setApplicableClass(Book.class);
        setImage("percent");
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        @SuppressWarnings("unchecked")
        List<Book> selected = (List<Book>) evt.getSelectedEntities();
        if (selected == null || selected.isEmpty()) {
            UIMessages.showMessage("Select at least one book.", MessageType.WARNING);
            return;
        }
        selected.forEach(book -> {
            book.setDiscount(10.0);
            crudService().save(book);
        });
        UIMessages.showMessage(selected.size() + " book(s) updated.", MessageType.SUCCESS);
    }
}
```

### Long-Running Action with Progress Monitor

```java
@InstallAction
public class ReindexBooksAction extends AbstractCrudAction {

    public ReindexBooksAction() {
        setName("Reindex Books");
        setApplicableClass(Book.class);
        setImage("refresh");
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        LongOperationMonitorWindow.start("Reindexing books...", "Done", monitor -> {
            List<Book> books = crudService().findAll(Book.class);
            monitor.setMax(books.size());

            for (Book book : books) {
                monitor.setMessage("Processing: " + book.getTitle());
                reindex(book);
                monitor.increment();

                if (monitor.isStopped()) {
                    throw new ValidationError("Reindex cancelled by user.");
                }
            }
        });
    }

    private void reindex(Book book) {
        // Simulate reindexing
    }
}
```

---

## Validators

### Single-Field Validator

```java
package mybookstore.validators;

import mybookstore.domain.Book;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.Validator;
import tools.dynamia.integration.sterotypes.InstallValidator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@InstallValidator
public class BookValidator implements Validator<Book> {

    @Override
    public List<ValidationError> validate(Book book) {
        List<ValidationError> errors = new ArrayList<>();

        if (book.getTitle() == null || book.getTitle().isBlank()) {
            errors.add(new ValidationError("title", "Title is required"));
        }

        if (book.getIsbn() == null || book.getIsbn().isBlank()) {
            errors.add(new ValidationError("isbn", "ISBN is required"));
        }

        if (book.getPrice() != null && book.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            errors.add(new ValidationError("price", "Price must be zero or positive"));
        }

        if (book.isOnSale() && book.getSalePrice() == null) {
            errors.add(new ValidationError("salePrice", "Sale price is required when book is on sale"));
        }

        return errors;
    }

    @Override
    public Class<Book> getValidatedClass() {
        return Book.class;
    }
}
```

### Cross-Entity Validator

```java
@InstallValidator
public class InvoiceValidator implements Validator<Invoice> {

    private final CrudService crudService;

    public InvoiceValidator(CrudService crudService) {
        this.crudService = crudService;
    }

    @Override
    public List<ValidationError> validate(Invoice invoice) {
        List<ValidationError> errors = new ArrayList<>();

        if (invoice.getDetails() == null || invoice.getDetails().isEmpty()) {
            errors.add(new ValidationError("details", "Invoice must have at least one line"));
        }

        if (invoice.getNumber() != null) {
            long count = crudService.count(Invoice.class, "number", invoice.getNumber());
            if (count > 0 && invoice.isNew()) {
                errors.add(new ValidationError("number", "Invoice number already exists"));
            }
        }

        return errors;
    }

    @Override
    public Class<Invoice> getValidatedClass() {
        return Invoice.class;
    }
}
```

---

## CRUD Listeners

### Invoice Lifecycle Listener

```java
package mybookstore.listeners;

import mybookstore.domain.Invoice;
import tools.dynamia.domain.util.CrudServiceListenerAdapter;
import tools.dynamia.integration.sterotypes.Listener;

@Listener
public class InvoiceCrudListener extends CrudServiceListenerAdapter<Invoice> {

    @Override
    public void beforeCreate(Invoice entity) {
        entity.calcTotal();
    }

    @Override
    public void beforeUpdate(Invoice entity) {
        entity.calcTotal();
    }
}
```

### Audit Listener

```java
@Listener
public class BookAuditListener extends CrudServiceListenerAdapter<Book> {

    private final ApplicationEventPublisher eventPublisher;

    public BookAuditListener(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void afterCreate(Book book) {
        eventPublisher.publishEvent(new BookCreatedEvent(book));
    }

    @Override
    public void beforeDelete(Book book) {
        // Prevent deletion of books with invoices
        if (hasActiveInvoices(book)) {
            throw new ValidationError("Cannot delete a book that appears in invoices.");
        }
    }

    private boolean hasActiveInvoices(Book book) {
        // check in DB
        return false;
    }
}
```

---

## Services

### Book Service with Business Logic

```java
package mybookstore.services;

import mybookstore.domain.Book;
import mybookstore.domain.StockStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.dynamia.domain.services.CrudService;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class BookService {

    private final CrudService crudService;

    public BookService(CrudService crudService) {
        this.crudService = crudService;
    }

    public Book create(Book book) {
        return crudService.save(book);
    }

    public List<Book> findByCategory(Long categoryId) {
        return crudService.findAll(Book.class, "category.id", categoryId);
    }

    public List<Book> findOnSale() {
        return crudService.findAll(Book.class, "onSale", true);
    }

    public Book applyDiscount(Long bookId, double discountPercent) {
        Book book = crudService.find(Book.class, bookId);
        if (book == null) throw new IllegalArgumentException("Book not found: " + bookId);

        book.setDiscount(discountPercent);
        BigDecimal discountAmount = book.getPrice()
            .multiply(BigDecimal.valueOf(discountPercent / 100.0));
        book.setSalePrice(book.getPrice().subtract(discountAmount));
        book.setOnSale(true);

        return crudService.save(book);
    }

    public void restock(Long bookId) {
        Book book = crudService.find(Book.class, bookId);
        if (book != null) {
            book.setStockStatus(StockStatus.IN_STOCK);
            crudService.save(book);
        }
    }
}
```

---

## Form Customizers

### Conditionally Show/Hide Fields

```java
package mybookstore.customizers;

import mybookstore.domain.Book;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Checkbox;
import tools.dynamia.viewers.ViewCustomizer;
import tools.dynamia.zk.viewers.form.FormFieldComponent;
import tools.dynamia.zk.viewers.form.FormView;

public class BookFormCustomizer implements ViewCustomizer<FormView<Book>> {

    @Override
    public void customize(FormView<Book> view) {
        FormFieldComponent onSale = view.getFieldComponent("onSale");
        FormFieldComponent salePrice = view.getFieldComponent("salePrice");

        // Hide sale price by default
        salePrice.hide();

        // Show sale price when book is already on sale (edit mode)
        view.addEventListener(FormView.ON_VALUE_CHANGED, event -> {
            if (view.getValue() != null && view.getValue().isOnSale()) {
                salePrice.show();
            }
        });

        // Toggle sale price on checkbox change
        if (onSale != null && onSale.getInputComponent() instanceof Checkbox checkbox) {
            checkbox.addEventListener(Events.ON_CHECK, event -> {
                if (checkbox.isChecked()) {
                    salePrice.show();
                } else {
                    salePrice.hide();
                }
            });
        }
    }
}
```

---

## REST Integration

### Book REST Controller

```java
package mybookstore.controllers;

import mybookstore.domain.Book;
import mybookstore.services.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.dynamia.domain.services.CrudService;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookRestController {

    private final CrudService crudService;
    private final BookService bookService;

    public BookRestController(CrudService crudService, BookService bookService) {
        this.crudService = crudService;
        this.bookService = bookService;
    }

    @GetMapping
    public List<Book> getAll() {
        return crudService.findAll(Book.class);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getById(@PathVariable Long id) {
        Book book = crudService.find(Book.class, id);
        return book != null ? ResponseEntity.ok(book) : ResponseEntity.notFound().build();
    }

    @GetMapping("/on-sale")
    public List<Book> getOnSale() {
        return bookService.findOnSale();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book create(@RequestBody Book book) {
        return bookService.create(book);
    }

    @PutMapping("/{id}/discount")
    public ResponseEntity<Book> applyDiscount(
            @PathVariable Long id,
            @RequestParam double percent) {
        return ResponseEntity.ok(bookService.applyDiscount(id, percent));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Book book = crudService.find(Book.class, id);
        if (book != null) crudService.delete(book);
    }
}
```

### Sample Data Initializer

```java
package mybookstore;

import mybookstore.domain.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tools.dynamia.domain.services.CrudService;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@Order(1)
public class InitSampleDataCommandLinerRunner implements CommandLineRunner {

    private final CrudService crudService;

    public InitSampleDataCommandLinerRunner(CrudService crudService) {
        this.crudService = crudService;
    }

    @Override
    public void run(String... args) {
        if (crudService.count(Category.class) == 0) {
            Category fiction = crudService.save(new Category("Fiction"));
            Category tech = crudService.save(new Category("Technology"));

            Book b1 = new Book();
            b1.setTitle("Clean Code");
            b1.setIsbn("978-0132350884");
            b1.setPrice(new BigDecimal("39.99"));
            b1.setCategory(tech);
            b1.setPublishDate(LocalDate.of(2008, 8, 1));
            crudService.save(b1);

            Book b2 = new Book();
            b2.setTitle("The Great Gatsby");
            b2.setIsbn("978-0743273565");
            b2.setPrice(new BigDecimal("12.99"));
            b2.setCategory(fiction);
            b2.setPublishDate(LocalDate.of(1925, 4, 10));
            crudService.save(b2);
        }
    }
}
```

---

## Extensions Usage

### Using Entity Files (File Attachments)

**Dependency**:
```xml
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.entityfiles</artifactId>
    <version>26.3.2</version>
</dependency>
```

**Entity**:
```java
@Entity
public class Book extends BaseEntity {

    @OneToOne
    private EntityFile bookCover;   // Single file reference

    // ...
}
```

**In view descriptor** (`BookForm.yml`):
```yaml
fields:
  bookCover:
    params:
      imageOnly: true
  preview:
    component: entityfileImage
    params:
      thumbnail: true
      bindings:
        value: bookCover
```

---

### Using Email Extension

**Dependency**:
```xml
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.email</artifactId>
    <version>26.3.2</version>
</dependency>
```

**Send Welcome Email on Customer Creation**:
```java
@Listener
public class CustomerCrudListener extends CrudServiceListenerAdapter<Customer> {

    private final EmailService emailService;

    public CustomerCrudListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void afterCreate(Customer customer) {
        Message message = new Message();
        message.setTo(customer.getEmail());
        message.setSubject("Welcome to My Book Store!");
        message.setBody("Hello " + customer.getName() + ", thanks for registering.");
        emailService.send(message);
    }
}
```

---

### Using SaaS Multi-Tenancy

**Dependency**:
```xml
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.saas</artifactId>
    <version>26.3.2</version>
</dependency>
```

**Tenant-Aware Entity**:
```java
@Entity
public class Book extends BaseEntity {

    @ManyToOne
    private Account account;   // Links this record to a tenant account

    private String title;
    // ...
}
```

**Query current tenant's books**:
```java
@Service
public class BookService {

    private final TenantProvider tenantProvider;
    private final CrudService crudService;

    public List<Book> getCurrentTenantBooks() {
        Account account = tenantProvider.getCurrentAccount();
        return crudService.findAll(Book.class, "account", account);
    }
}
```

---

### Using Finance Framework

**Dependency**:
```xml
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.finances</artifactId>
    <version>26.3.2</version>
</dependency>
```

**Calculate Invoice Total with Tax and Discount**:
```java
public FinancialSummary calculateInvoice(Invoice invoice) {
    FinancialCalculator calculator = new FinancialCalculator();

    for (InvoiceDetail detail : invoice.getDetails()) {
        calculator.addCharge(new Charge()
            .setType(ChargeType.LINE_ITEM)
            .setAmount(detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
            .setTaxable(true));
    }

    // 5% discount
    calculator.addCharge(new Charge()
        .setType(ChargeType.DISCOUNT)
        .setPercentage(5));

    // 19% VAT
    calculator.addCharge(new Charge()
        .setType(ChargeType.TAX)
        .setPercentage(19));

    return calculator.calculate();
}
```

---

## Summary

These examples demonstrate:

| Topic | Pattern |
|-------|---------|
| **Application Setup** | `@SpringBootApplication` + `@EntityScan` + `@EnableCaching` |
| **Domain Entities** | `extends BaseEntity` + JPA annotations |
| **View Descriptors** | YAML files in `META-INF/descriptors/` |
| **Module Provider** | `implements ModuleProvider` + `@Provider` |
| **Actions** | `extends AbstractCrudAction` + `@InstallAction` |
| **Validators** | `implements Validator<T>` + `@InstallValidator` |
| **CRUD Listeners** | `extends CrudServiceListenerAdapter<T>` + `@Listener` |
| **Services** | `@Service` + constructor-injected `CrudService` |
| **Form Customizers** | `implements ViewCustomizer<FormView<T>>` |
| **REST Controllers** | `@RestController` + `@RequestMapping` |
| **Extensions** | Add dependency + implement/use provided interfaces |

All patterns follow DynamiaTools conventions: minimal boilerplate, Spring-managed beans, and metadata-driven UI generation.

---

Next: Explore [Advanced Topics](./ADVANCED_TOPICS.md) for Spring integration, security, caching, and microservice patterns.

