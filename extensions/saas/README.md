[![Maven Central](https://img.shields.io/maven-central/v/tools.dynamia.modules/tools.dynamia.modules.saas)](https://search.maven.org/search?q=tools.dynamia.modules.saas)
![Java Version Required](https://img.shields.io/badge/java-21-blue)
[![Maven Build](https://github.com/dynamiatools/module-saas/actions/workflows/maven.yml/badge.svg)](https://github.com/dynamiatools/module-saas/actions/workflows/maven.yml)
[![Release and Deploy](https://github.com/dynamiatools/module-saas/actions/workflows/release.yml/badge.svg)](https://github.com/dynamiatools/module-saas/actions/workflows/release.yml)

# SaaS Module
This [DynamiaTools](https://www.dynamia.tools) extension allow you to create modules with multi tenant support por SaaS applications. Its manage accounts,
payments, account status, data isolation by account and more

## Modules

- Core: Entities, Services and API implementation
- API: Use this clases from your external modules
- UI: Actions and views for user interface integration.
- Remote: Allow you to check account status from external systems

## Installation

Add the following dependencies to project classpath

**Maven**

```xml

<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.saas</artifactId>
    <version>3.4.0</version>
</dependency>
```

```xml
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.saas.ui</artifactId>
    <version>3.4.0</version>
</dependency>

```

**Gradle**

```groovy
compile 'tools.dynamia.modules:tools.dynamia.modules.saas:3.4.0'
compile 'tools.dynamia.modules:tools.dynamia.modules.saas.ui:3.4.0'
```

## Usage

The above dependencies install to your project a new DynamiaTool module called **SaaS**. From this
module you can create customer accounts, account types (plans), accounts profile and payments.

### Subdomains account based

By default, all account required a subdomain like:

- https://customer1.myapp.com
- https://customer2.myapp.com
- https://customer3.myapp.com

In your hosting provider you need to create a wildcard domain, like `*.myapp.com`
pointing to server IP using an **A RECORD**. To work local edit your `hosts` file and create multiple subdomains for
testing.

**For example**

In linux edit `/etc/hosts` file and add:

```
customer1.localhost.com    127.0.01
customer2.localhost.com    127.0.01
customer3.localhost.com    127.0.01
```

## Integration

For your modules you only require the API dependency

```xml

<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.saas.api</artifactId>
    <version>3.4.0</version>
</dependency>
```

Then, edit JPA entities and implement the `tool.dynamia.modules.saas.api.AccountAware`
interface. Add the field `Long accountId` field. The value of this field will be automatic set up by the SaaS modules
using a `CrudListener`.

You can also make you entity to extend from the helper classes `tools.dynamia.modules.saas.jpa.SimpleEntitySaas` or from
`tools.dynamia.modules.saas.jpa.BaseEntitySaas`.

## Example:

```Java
import tool.dynamia.modules.saas.api.AccountAware;

@Entity
public class Person implements AccountAware {

    @NotNull
    private Long accountId;
    // other fields

    //getter and setters    
}

```

## License

DynamiaTools SaaS is available under Apache 2 License
