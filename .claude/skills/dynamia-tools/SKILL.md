---
name: dynamia-tools
description: DynamiaTools framework (backend) best practices — creating JPA entities, using CrudService/QueryParameters, ModuleProvider/CrudPage, Actions, Validators, ViewCustomizers, CrudServiceListener, and YAML view descriptors (form/table/tree/json/crud/entityfilters). Use when writing code inside this framework/ repo or in apps consuming tools.dynamia.*.
---

# DynamiaTools — Backend Development Skill

Full references: `framework/docs/backend/*.md` (ARCHITECTURE, CORE_MODULES, DEVELOPMENT_PATTERNS,
EXTENSIONS, ADVANCED_TOPICS, EXAMPLES) and a real working example in
`framework/examples/demo-zk-books/`. This skill summarizes what has been verified against the actual
source code — prefer these patterns over generic Spring assumptions.

## 1. Framework-specific stereotypes — do NOT use Spring's `@Component`/`@Service` directly

The framework defines meta-annotations in `tools.dynamia.integration.sterotypes.*` that are aliases of
the Spring ones but signal semantic intent within the framework:

| Annotation | Use |
|---|---|
| `@Provider` | `ModuleProvider` (registers modules/navigation pages) |
| `@Listener` | `CrudServiceListener` / `CrudServiceListenerAdapter` |
| `@Service` | Business services |
| `@Repository` | Repositories |
| `@Controller` | Classic MVC/REST controllers |
| `@Component` | Generic beans (customizers, converters, validators) |

```java
@Provider
public class MyModuleProvider implements ModuleProvider {
    @Override
    public Module getModule() {
        return new Module("library", "Library")
                .icon("book")
                .description("my books library")
                .position(0)
                .addPage(
                        new CrudPage("books", "Books", Book.class),
                        new CrudPage("categories", "Categories", Category.class).icon("tree")
                );
    }
}
```

`Module`/`Page`/`CrudPage` use a fluent chainable API (`.icon()`, `.description()`, `.position()`).

## 2. Entities — extend `BaseEntity`

```java
@Entity
@Table(name = "books")
@OrderBy("title")          // default ordering in listings
public class Book extends BaseEntity {

    @NotNull
    private String title;

    @Column(name = "publication_year")
    private int year;

    @ManyToOne
    private Category category;

    @Enumerated
    private StockStatus stockStatus = StockStatus.IN_STOCK;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookReview> reviews = new ArrayList<>();

    @Transient
    @JsonIgnore
    private boolean selected;   // UI-only field, not persistent

    @Override
    public String toString() {
        return title;           // used by combos/references in the UI
    }
    // standard getters/setters
}
```

Rules:
- `BaseEntity` provides `id`, `creationDate`/`creationTimestamp`, `createdBy`, `modificationDate`, `modifiedBy` automatically.
- Always override `toString()` — the framework uses it to display the entity in pickers/references.
- `@Enumerated` without `EnumType.STRING` is valid in this framework (defaults to ORDINAL unless the project requires otherwise); check consistency with the rest of the domain before deciding.
- `@OneToMany` relationships with `cascade = CascadeType.ALL, orphanRemoval = true` when the child is dependent (e.g. `BookReview` of `Book`).
- File attachments: use `EntityFile` (from the `entity-files` extension) via `@OneToOne`.

## 3. CrudService — persistence abstraction

Interface `tools.dynamia.domain.services.CrudService` (inject it, never use `EntityManager` directly in services):

```java
@Service
public class BookService {
    private final CrudService crudService;

    public BookService(CrudService crudService) {
        this.crudService = crudService;
    }

    public Book create(Book book) {
        return crudService.create(book);         // forces insert
    }

    public Book save(Book book) {
        return crudService.save(book);           // create or update based on id
    }

    public Book findById(Long id) {
        return crudService.find(Book.class, id);
    }

    public List<Book> findByCategory(Category category) {
        return crudService.find(Book.class,
                QueryParameters.with("category", category)
                        .orderBy("title", true));
    }

    public long countInStock() {
        return crudService.count(Book.class,
                QueryParameters.with("stockStatus", StockStatus.IN_STOCK));
    }
}
```

Key methods to know (don't reinvent with manual JPQL):
- `save/create/update/delete/find/findAll/count`
- `QueryParameters.with(prop, value).orderBy(...)` for declarative filters
- `findByExample`, `findByFields` for dynamic search/autocomplete
- `executeQuery(QueryBuilder, params)` for reusable complex JPQL queries
- `executeProjection(Class, query, params)` for `count/sum/max/avg`
- `batchUpdate`, `updateField`, `increaseCounter/deacreaseCounter` for bulk updates without loading the full entity
- `saveWithinTransaction`/`executeWithinTransaction` for operations outside the current transaction

Don't use `EntityManager`/`Session` directly in application services — this is a documented anti-pattern (leaky abstraction).

## 4. Navigation — Module / PageGroup / CrudPage

```java
@Provider
public class CrmModuleProvider implements ModuleProvider {
    @Override
    public Module getModule() {
        Module module = new Module("crm", "CRM").icon("business").position(10);

        PageGroup contacts = new PageGroup("contacts-group", "Contacts")
                .addPage(new CrudPage("contacts", "All Contacts", Contact.class));
        module.addPageGroup(contacts);

        return module;
    }
}
```

- `CrudPage(id, name, EntityClass)` automatically generates list+form+delete.
- `CrudPage(id, name, EntityClass, "customCrudServiceName")` to use an alternate `CrudService`.
- External pages: `new Page(id, name, "classpath:/pages/x.zul")`, `new ExternalPage(id, name, url)`.

## 5. Actions — `AbstractAction` / `AbstractCrudAction`

```java
@InstallAction
public class FilterBookByBuyDateAction extends AbstractCrudAction {

    public FilterBookByBuyDateAction() {
        setName("Filter By Buy Date");
        setApplicableClass(Book.class);
        setImage("calendar");
        setType("primary");
        setPosition(1);
    }

    @Override
    public void actionPerformed(CrudActionEvent evt) {
        UIMessages.showInput("Select Buy Date", LocalDate.class, date -> {
            if (date != null) {
                evt.getController().setParemeter("buyDate", date);
                evt.getController().doQuery();
            }
        });
    }
}
```

- `@InstallAction` registers the action automatically (no `@Provider` needed).
- `setApplicableClass(Entity.class)` restricts the action to that entity in CRUD tables/toolbars.
- Override `isEnabled(Object entity)` / `isVisible()` for conditional rules (role, status).
- Access the controller via `CrudActionEvent.getController()` to refresh queries, get the selected entity, etc.

## 6. Validators

```java
@InstallValidator
public class ContactEmailValidator implements Validator<Contact> {
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

Never validate in the entity's `@PrePersist`/lifecycle — validation belongs in `Validator<T>`, not the domain.

## 7. CrudServiceListener — lifecycle hooks

```java
@Listener
public class InvoiceCrudListener extends CrudServiceListenerAdapter<Invoice> {
    @Override
    public void beforeUpdate(Invoice entity) {
        entity.calcTotal();
    }

    @Override
    public void beforeCreate(Invoice entity) {
        entity.calcTotal();
    }
}
```

Use for derived calculations/side effects that must apply across create/update/delete (don't put them in the business `Service` if they must always apply, regardless of entry point).

## 8. ViewCustomizer — modify forms/tables at runtime

```java
public class BookFormViewCustomizer implements ViewCustomizer<FormView<Book>> {
    @Override
    public void customize(FormView<Book> view) {
        FormFieldComponent salePrice = view.getFieldComponent("salePrice");
        salePrice.hide();

        view.addEventListener(FormView.ON_VALUE_CHANGED, event -> {
            if (view.getValue() != null && view.getValue().isOnSale()) {
                salePrice.show();
            }
        });
    }
}
```

Referenced from the YAML descriptor with `customizer: fully.qualified.ClassName` (see section 9). No registration annotation required — it's instantiated by class name from the YAML.

## 9. View descriptors (YAML) — `META-INF/descriptors/*.yml`

Naming convention: `{Entity}Crud.yml`, `{Entity}Form.yml`, `{Entity}Table.yml`, `{Entity}Tree.yml`,
`{Entity}Json.yml`, `{Entity}Filters.yml`. `beanClass` is always the FQCN. `autofields: false` is
recommended for explicit control over which fields are shown (`autofields: true` exposes all bean
fields, including ones you don't want to show).

### `crud` — bundles form+table+actions for the entity
```yaml
view: crud
beanClass: mybookstore.domain.Book
autofields: false
params:
  queryProjection: true
```

### `form`
```yaml
view: form
beanClass: mybookstore.domain.Book
autofields: false
customizer: mybookstore.customizers.BookFormViewCustomizer

fields:
  title:
    params:
      span: 2                      # columns occupied in the grid
  category:                        # @ManyToOne relation -> automatic combo
  publishDate:
    component: dateselector
  price:
    params:
      format: $###,###
  sinopsys:
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
  preview:                         # virtual field reusing another field's binding
    component: entityfileImage
    params:
      thumbnail: true
      bindings:
        value: bookCover
  reviews:
    component: crudview            # embedded sub-CRUD for @OneToMany

groups:
  details:
    label: Book Details
    fields: [ buyDate, price, reviews, salePrice ]

layout:
  columns: 4
```

### `table`
```yaml
view: table
beanClass: mybookstore.domain.Book
autofields: false

fields:
  bookCover:
    component: entityfileImage
    params:
      thumbnail: true
      header: { width: 70px, align: center }
  title:
  stockStatus:
    component: enumlabel
    params:
      defaultSclass: stockStatus
      sclassPrefix: status
      header: { width: 90px }
  price:
    params:
      converter: converters.Currency
      header: { align: right }

params:
  enumColors:
    name: stockStatus
    colors:
      OUT_STOCK: "#ffe8e8"
  orderBy: title
```

Useful built-in converters: `converters.Date`, `converters.LocalDateTime`, `converters.Currency`.

### `entityfilters` — filter panel for table/crud
```yaml
view: entityfilters
beanClass: mybookstore.domain.Book
autofields: false
fields:
  title:
  category:
  stockStatus:
    component: enumlabel
```

### `tree` — hierarchies (e.g. categories with parent/child)
```yaml
view: tree
beanClass: mybookstore.domain.Category
autofields: false
fields:
  name:
    params:
      header: { width: 300px }
  description:
```

### `json` — projection for APIs/autocomplete
```yaml
view: json
beanClass: mybookstore.domain.Category
autofields: false
fields:
  name:
  description:
  subcategories:
  parent.id:              # dot-path to nested property
params:
  conditions:
    onlyParents: 'parent is null'
```

### Embedded sub-CRUD in a form (master-detail)
```yaml
# InvoiceForm.yml
fields:
  details:
    component: crudview
    params:
      inplace: true
      height: 400px
      span: 3
```

### Detail table with totals
```yaml
# InvoiceDetailTable.yml
fields:
  book:
    component: coollabel
    params:
      showImage: false
      bindings:
        title: book.title
        subtitle: book.category
  quantity:
    params:
      footer:
        function: sum
  subtotal:
    params:
      converter: converters.Currency
      footer:
        function: sum
```

## 10. Quick checklist for a new full CRUD entity

1. Entity `extends BaseEntity`, `toString()` override, correct JPA annotations.
2. `Validator<T>` with `@InstallValidator` only if there are rules beyond bean validation (`@NotNull`, etc).
3. `CrudServiceListener` with `@Listener` only if there are lifecycle side effects.
4. `CrudPage` in a `ModuleProvider` (`@Provider`) to expose it in navigation.
5. YAML descriptors in `src/main/resources/META-INF/descriptors/`: at least `{Entity}Form.yml` and `{Entity}Table.yml`; use `{Entity}Crud.yml` if the default form/table (autofields) is enough and you only need global params.
6. Specific Actions (`@InstallAction` + `AbstractCrudAction`, `setApplicableClass(Entity.class)`) only for operations that aren't standard CRUD.
7. `ViewCustomizer` only when you need reactive UI logic the YAML can't express declaratively (show/hide a field based on another field, etc).

## 11. Anti-patterns to avoid (see DEVELOPMENT_PATTERNS.md for full detail)

- Anemic entities with no domain business logic (methods like `isActive()`, `calcTotal()` do belong on the entity).
- Business logic in the entity that depends on injected services (services belong in `@Service`, not the entity).
- Loose `EntityManager`/JPQL queries in services instead of `CrudService`/`QueryBuilder`.
- Field injection (`@Autowired` on a field) — use constructor injection.
- Silently swallowing exceptions (`catch (Exception e) { e.printStackTrace(); }`) — log and propagate/report.
- `autofields: true` in production descriptors without reviewing which fields it exposes (may leak internal/sensitive fields).