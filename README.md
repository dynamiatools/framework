[![Maven Central](https://img.shields.io/maven-central/v/tools.dynamia/tools.dynamia.app?label=Maven%20Central)](https://central.sonatype.com/search?q=g%3Atools.dynamia)
[![GitHub Packages](https://img.shields.io/github/v/release/dynamiatools/framework?include_prereleases&label=GitHub%20Packages&logo=github)](https://github.com/orgs/dynamiatools/packages?repo_name=framework)
[![npm](https://img.shields.io/npm/v/@dynamia-tools/sdk?label=npm&logo=npm)](https://www.npmjs.com/search?q=%40dynamia-tools)
![Java Version Required](https://img.shields.io/badge/java-25-blue)
[![License](https://img.shields.io/github/license/dynamiatools/framework)](LICENSE)

[![Java CI with Maven](https://github.com/dynamiatools/framework/actions/workflows/maven.yml/badge.svg)](https://github.com/dynamiatools/framework/actions/workflows/maven.yml)
[![Release and Deploy](https://github.com/dynamiatools/framework/actions/workflows/release.yml/badge.svg)](https://github.com/dynamiatools/framework/actions/workflows/release.yml)
[![Publish NPM Packages](https://github.com/dynamiatools/framework/actions/workflows/publish-npm.yml/badge.svg)](https://github.com/dynamiatools/framework/actions/workflows/publish-npm.yml)
[![DynamiaTools Documentation](https://img.shields.io/badge/DynamiaTools-Documentation-orange)](https://dynamia.tools/getting-started/)

<h1 align="center">
<img src="https://raw.githubusercontent.com/dynamiatools/website/main/public/icon.png" alt="Dynamia Logo" width="150"/>

Dynamia Platform
</h1>

<p align="center"><b>Describe your business. Get a working enterprise app.</b></p>

**Dynamia Platform** is a full-stack framework for building enterprise web applications fast, without giving up quality. Define your entities, describe your screens in a few lines of YAML, and the platform gives you the REST API, the metadata and the components to render complete CRUD screens in a modern **Vue 3** app, with navigation, security and multi-tenancy included. The backend runs on the JVM (**Java 25**, **Kotlin** or **Groovy**) with **Spring Boot 4**, and comes with the enterprise modules most business apps end up needing, all in one place.

Not a Vue fan? Dynamia can also render the whole UI on the server with **ZK**, with no JavaScript at all, or mix both in the same application.

It is not a new experiment: it has been running real businesses for **13+ years**, across **10+ production applications** and **40+ reusable modules**.

## 🚀 Why teams choose Dynamia

- **Ship in days, not months.** An entity and a descriptor give you a full CRUD screen with search, filters, validation and REST endpoints.
- **A modern frontend, ready to go.** Build with **Vue 3** and TypeScript on the official SDK and components: `DynamiaCrudPage`, forms, tables, navigation and more render straight from your backend's metadata. Prefer no JavaScript? ZK renders the same screens on the server.
- **Your JVM language, your choice.** Write the backend in **Java**, **Kotlin** or **Groovy**: the CLI has a template for each, and any other JVM language that works with Spring Boot works here too.
- **Write less code, keep more control.** Screens are declared in YAML; when you need something custom, you drop down to plain code, Vue components or ZK.
- **Batteries included.** Multi-tenant SaaS, file storage, reporting, dashboards, email and SMS, finance calculations, data import and security are ready to plug in.
- **Modular by design.** Package your domain as reusable modules (plain JARs) and share them across projects.
- **AI-ready.** An official Claude Code plugin teaches your AI assistant the platform's conventions.

## ⚡ Getting Started

The fastest way to start is the **Dynamia CLI**. In a couple of minutes you will have a backend and a frontend running.

### 1. Install the CLI

**One line (Linux and macOS).** Installs JDK 25, Node.js and the CLI for you:

```bash
curl -fsSL https://get.dynamia.tools | bash
```

**Or with npm**, if you already have Node.js 24+, Git and JDK 25:

```bash
npm install -g @dynamia-tools/cli
```

### 2. Create your project

```bash
dynamia new
```

The wizard asks for a project name, what to generate (backend, frontend or both), the backend language (**Java**, **Kotlin** or **Groovy**), your Maven coordinates, the frontend (**Vue 3**) and your package manager. It shows a summary before generating anything.

Prefer scripts or CI? Pass flags and skip every prompt:

```bash
dynamia new --name my-erp --scaffold both --backend-lang java \
  --group-id com.acme --artifact-id erp --frontend vue --pm pnpm --yes
```

| Flag | Values |
|---|---|
| `--name` | Project folder name (lowercase, numbers and hyphens) |
| `--scaffold` | `both`, `backend` or `frontend` |
| `--backend-lang` | `java`, `kotlin` or `groovy` |
| `--group-id`, `--artifact-id`, `--version`, `--description` | Maven coordinates |
| `--frontend` | `vue` |
| `--pm` | `pnpm`, `npm` or `yarn` |
| `--yes` | Accept the summary without asking |
| `--no-git` | Do not initialize a Git repository |

### 3. Run it

```
my-erp/
├── backend/     Spring Boot + Dynamia Platform
└── frontend/    Vue 3 + Vite + @dynamia-tools/vue
```

```bash
cd my-erp/backend && ./mvnw spring-boot:run      # backend on http://localhost:8080
cd my-erp/frontend && pnpm dev                   # Vue app (use npm or yarn if you chose them)
```

The generated backend already includes Spring Data JPA with an in-memory database, so it runs out of the box. See the [CLI documentation](platform/packages/cli/README.md) for every option.

## 🧭 The Dynamia Way

Dynamia has one idea at its center: **your domain model drives everything else.** You work in four small steps.

**1. Model your domain.** A plain JPA entity, in Java, Kotlin or Groovy. Extend `BaseEntity` to get the id, audit fields and CRUD support.

```java
@Entity
public class Book extends BaseEntity {

    @NotNull
    private String title;
    private BigDecimal price;
    @ManyToOne
    private Category category;
    // getters and setters
}
```

**2. Describe the screens.** A YAML view descriptor in `META-INF/descriptors/` declares the form, table, tree or filters. No templates, no boilerplate.

```yaml
# BookForm.yml
view: form
beanClass: mybookstore.domain.Book
fields:
  title:
  category:
  price:
    params:
      format: $###,###
```

**3. Register it in a module.** A `ModuleProvider` places the entity in your app's navigation with one line.

```java
@Provider
public class LibraryModuleProvider implements ModuleProvider {

    @Override
    public Module getModule() {
        return new Module("library", "Library")
                .addPage(new CrudPage("books", "Books", Book.class));
    }
}
```

**4. Add behavior where it matters.** Validators, `CrudServiceListener`s, view customizers and **actions** (toolbar buttons and flows) hold your business rules. Actions written against the UI-neutral ports (`UIMessages`, `CrudControllerAPI`) run unchanged in ZK and for REST clients.

Run the app and you get a complete screen with list, search, create, edit and delete, validation, and the matching REST endpoints, ready for a Vue or a ZK frontend. Customize only what is different about your business.

> **See it working:** [`examples/demo-zk-books`](examples) is a full application following exactly these steps, and `examples/demo-vue-books` is a Vue frontend for it.

### Choose your frontend

| You want | Use |
|---|---|
| A modern, decoupled SPA | **Vue 3** with [`@dynamia-tools/vue`](https://www.npmjs.com/package/@dynamia-tools/vue) on top of the REST API and the TypeScript SDK. This is what the CLI generates |
| A complete UI with no JavaScript | **ZK**: screens are rendered on the server from the same descriptors |
| The best of both | **Microfrontends**: drop a Vue, React or Svelte bundle into any ZK page with `<microfrontend>` |

## 🤖 Build with AI: the Claude Code plugin

Working with [Claude Code](https://claude.com/claude-code)? The **`dynamia-platform`** plugin teaches Claude the platform's patterns, so the code it writes follows them: entities and `CrudService`, `ModuleProvider` and `CrudPage`, actions, YAML descriptors, ZK pages, the `@dynamia-tools` Vue packages, the extensions, and live docs read from GitHub at your project's version. It can also install the `dynamia` CLI and scaffold new projects for you.

Install it from the [`dynamia-tools` plugin marketplace](https://github.com/dynamiatools/claude-plugins). Inside Claude Code:

```
/plugin marketplace add dynamiatools/claude-plugins
/plugin install dynamia-platform@dynamia-tools
```

Or from a terminal:

```bash
claude plugin marketplace add dynamiatools/claude-plugins
claude plugin install dynamia-platform@dynamia-tools
```

Then open Claude Code in your project and ask for what you need, or call a skill directly: `dynamia-platform:backend`, `dynamia-platform:zk-ui`, `dynamia-platform:vue-frontend`, `dynamia-platform:extensions`, `dynamia-platform:docs` or `dynamia-platform:cli`. See the [plugin README](https://github.com/dynamiatools/claude-plugins/tree/master/plugins/dynamia-platform) for the full list.

## ✨ Core Capabilities

- **Automatic CRUD and REST**: define entities, get full interfaces and RESTful APIs instantly
- **Vue 3 and TypeScript SDK**: typed REST client, composables and ready-made CRUD components
- **View descriptors**: declare forms, tables, trees and filters in YAML
- **Any JVM language**: Java, Kotlin or Groovy, with a CLI template for each
- **Modular architecture**: build and reuse modules (JARs) across projects
- **Server-driven actions**: write an action once and run it in Vue, ZK or any REST client
- **Flexible patterns**: MVC or MVVM, as you prefer
- **Pluggable themes**: change the look and feel without touching business code
- **Microfrontends**: embed Vue, React or Svelte bundles in ZK pages, with MVVM binding included
- **Works with your stack**: integrates with any Java framework or library

## 📦 Built-in Enterprise Extensions

Add only what you need. Each extension is a dependency away, and the ones with a REST API ship a TypeScript SDK.

| Extension | What you get | npm SDK |
|---|---|---|
| **Multi-tenancy (SaaS)** | Account isolation, per-tenant data, subscriptions and payments, status control | [`saas-sdk`](https://www.npmjs.com/package/@dynamia-tools/saas-sdk) |
| **Entity Files** | Attach files to any entity: local disk, AWS S3 or [Buckie](https://dynamia.tools/buckie/), the lightweight file server built by Dynamia | [`files-sdk`](https://www.npmjs.com/package/@dynamia-tools/files-sdk) |
| **Reports** | JPQL and SQL reports, CSV, Excel and PDF export, email delivery, charts and embeddable reports | [`reports-sdk`](https://www.npmjs.com/package/@dynamia-tools/reports-sdk), [`reports-vue`](https://www.npmjs.com/package/@dynamia-tools/reports-vue) |
| **Dashboard** | Responsive dashboards with custom widgets and charts | [`dashboard-sdk`](https://www.npmjs.com/package/@dynamia-tools/dashboard-sdk), [`dashboard-vue`](https://www.npmjs.com/package/@dynamia-tools/dashboard-vue) |
| **Email & SMS** | Email through JavaMail, SMS through AWS, templates and delivery logs | |
| **Finances** | Invoices, credit and debit notes, quotes, multi-currency, taxes, discounts and withholdings, with deterministic results | |
| **File Importer** | Excel and CSV imports with field mapping, validation and batch operations | |
| **HTTP Functions** | Declarative, versioned calls to third-party APIs | [`http-functions-sdk`](https://www.npmjs.com/package/@dynamia-tools/http-functions-sdk) |
| **Security** | Role-based access control, users, permissions and profiles, built on Spring Security | |

## 📦 Add Dynamia to an Existing Spring Boot Project

Already have a Spring Boot 4 application? Add the platform by hand.

**Maven**

```xml
<dependencies>
    <dependency>
        <groupId>tools.dynamia</groupId>
        <artifactId>tools.dynamia.app</artifactId>
        <version>26.11.1</version>
    </dependency>
    <dependency>
        <groupId>tools.dynamia</groupId>
        <artifactId>tools.dynamia.zk</artifactId>
        <version>26.11.1</version>
    </dependency>
    <dependency>
        <groupId>tools.dynamia</groupId>
        <artifactId>tools.dynamia.domain.jpa</artifactId>
        <version>26.11.1</version>
    </dependency>
</dependencies>
```

**Gradle**

```groovy
dependencies {
    implementation 'tools.dynamia:tools.dynamia.app:26.11.1'
    implementation 'tools.dynamia:tools.dynamia.zk:26.11.1'
    implementation 'tools.dynamia:tools.dynamia.domain.jpa:26.11.1'
}
```

Then enable the platform on your main class:

```java
@SpringBootApplication
@EnableDynamiaTools  // this is all you need
public class MyApplication {

    static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

Run it with `mvn spring-boot:run` (or `gradle bootRun`) and open `http://localhost:8080`.

Every Dynamia artifact, core and extensions alike, shares the same version, so you never have to match them. Extensions live under the `tools.dynamia.modules` group:

```xml
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.saas</artifactId>
    <version>26.11.1</version>
</dependency>
```

Other artifact ids: `email`, `entityfiles`, `entityfiles.s3`, `dashboard`, `reports.core`, `importer` and `security`.

### TypeScript packages

```bash
npm install @dynamia-tools/sdk @dynamia-tools/vue
```

| Package | Description |
|---|---|
| [`@dynamia-tools/sdk`](https://www.npmjs.com/package/@dynamia-tools/sdk) | Typed client for the REST API |
| [`@dynamia-tools/ui-core`](https://www.npmjs.com/package/@dynamia-tools/ui-core) | Framework-agnostic view, viewer and renderer core |
| [`@dynamia-tools/vue`](https://www.npmjs.com/package/@dynamia-tools/vue) | Vue 3 plugin, components and composables |
| [`@dynamia-tools/microfrontend-bridge`](https://www.npmjs.com/package/@dynamia-tools/microfrontend-bridge) | Typed helpers for bundles embedded with `<microfrontend>` |
| [`@dynamia-tools/cli`](https://www.npmjs.com/package/@dynamia-tools/cli) | Project scaffolding |

<details>
<summary>Where each version is published</summary>

| Version | Maven Central | GitHub Packages | npm |
|---|---|---|---|
| Monthly release (`YY.MM.0`) | yes | yes | yes |
| Maintenance release (`YY.MM.N`, N > 0) | no | yes | yes |
| `*-SNAPSHOT` (`next` branch) | no | yes | no |

Unless you need a maintenance fix, use the latest monthly release from Maven Central. Details are in [docs/RELEASING.md](docs/RELEASING.md).

To use a maintenance release, add the GitHub Packages repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>dynamia-github</id>
        <url>https://maven.pkg.github.com/dynamiatools/framework</url>
    </repository>
</repositories>
```

and a matching server in `~/.m2/settings.xml`, with a personal access token that has `read:packages`:

```xml
<servers>
    <server>
        <id>dynamia-github</id>
        <username>YOUR_GITHUB_USER</username>
        <password>YOUR_GITHUB_TOKEN</password>
    </server>
</servers>
```

npm packages of every release are on the public npm registry, with no token needed.

</details>

## 📚 Documentation

Guides, tutorials and API reference live at **[dynamia.tools](https://dynamia.tools)**.

- **[Getting Started Guide](https://dynamia.tools/getting-started/)**: your first Dynamia app in minutes
- **[Core Concepts](https://dynamia.tools/docs/)**: the framework architecture
- **[View Descriptors](https://dynamia.tools/docs/view-descriptors/)**: declarative UI with YAML
- **[API Reference](https://dynamia.tools/api/)**
- **[Extensions Guide](https://dynamia.tools/extensions/)**
- **[Backend developer docs](docs/backend/README.md)** and **[frontend docs](docs/frontend/README.md)** in this repository
- **[Examples](examples)**: sample applications

## 🗺️ Where We Are Heading

Dynamia keeps growing. Here is what we are working on:

- **Developer experience**: more generators, hot reload for view descriptors, Docker Compose and CI templates
- **More frontends**: React and other frameworks on top of the same SDK, and the React option in the CLI
- **Richer docs and website**: tutorials, an interactive playground and community showcases
- **Beyond REST**: GraphQL, a microservices toolkit and cloud-native features
- **AI-assisted development**: code generation for CRUDs, forms and reports from natural language

Have an idea? Join the [GitHub Discussions](https://github.com/dynamiatools/framework/discussions), open an [issue](https://github.com/dynamiatools/framework/issues), or just star the project.

## 🏗️ Building from Source

### Prerequisites
- OpenJDK 25
- Maven 3.9+
- Git

### Build Instructions

```bash
# Clone the repository
git clone https://github.com/dynamiatools/framework.git
cd framework

# Build all modules
mvn clean install

# Skip tests for faster builds
mvn clean install -DskipTests

# Build only the core platform
cd platform
mvn clean install

# Build only extensions
cd extensions
mvn clean install
```

### Project Structure

```
framework/
├── platform/           # Core framework modules
│   ├── app/            # Application bootstrap
│   ├── core/           # Core utilities and APIs
│   ├── contract/       # UI contract generator
│   ├── packages/       # TypeScript packages (sdk, ui-core, vue, cli, microfrontend-bridge)
│   ├── starters/       # Spring Boot starters
│   ├── testing/        # Test support (UI contract, UI testing, architecture rules)
│   └── ui/             # ZK UI and shared UI components
├── extensions/         # Enterprise extensions (each with its own TS SDK under packages/)
│   ├── saas/           # Multi-tenancy support
│   ├── entity-files/   # File attachment system
│   ├── email-sms/      # Communication module
│   ├── dashboard/      # Dashboard widgets
│   ├── reports/        # Reporting engine
│   ├── finances/       # Financial calculations
│   ├── file-importer/  # Data import utilities
│   ├── http-functions/ # Declarative, versioned HTTP functions
│   └── security/       # Authentication & authorization
├── themes/             # UI themes
├── examples/           # Sample applications
└── docs/               # Project docs (see RELEASING.md)
```

## 📜 A Mature Platform

Dynamia Platform grew over **13+ years** at [Dynamia Soluciones IT](https://www.dynamiasoluciones.com), used by companies across **Latin America**, the **USA** and **Europe** in many industries.

| Era | Years | Highlights |
|---|---|---|
| **v1.x Foundation** | 2011-2013 | An internal library that hid the complexity of Spring and ZK |
| **v2.x Automation** | 2014-2016 | View descriptors, automatic CRUD and the navigation framework |
| **v3.x Open Source** | 2017-2019 | A full reorganization and the first public release |
| **v4.x Modernization** | 2020-2022 | Java 11+, Spring Boot integration and a growing community |
| **v5.x Platform** | 2023-2025 | Core and extensions together, Java 21, Spring Boot 3, multi-tenancy, reporting and dashboards |
| **v26.x Today** | 2026+ | Java 25, Spring Boot 4, one version for every module, Vue and microfrontends, the CLI and the Claude Code plugin |

This is not a new framework: it is a battle-tested platform, now open source and ready for the global developer community.

## 🤝 Contributing

We welcome contributions from the community! Whether it's:

- 🐛 Bug reports and fixes
- ✨ New feature suggestions
- 📚 Documentation improvements
- 💡 Example applications
- 🌐 Translations

Please read our **[Contributing Guidelines](CONTRIBUTING.md)** before submitting pull requests.

## 📄 License

Dynamia Platform is open-source software licensed under the **Apache License 2.0**.

See [LICENSE](LICENSE) for details.

## ☕ Support Dynamia Platform

Dynamia Platform is built with dedication to help developers speed up their projects and make enterprise development more enjoyable. If this platform has been useful to you or your organization, consider supporting its development. Your support helps keep the project alive and evolving with new features and improvements. Every coffee counts! 😊

<a href="https://www.buymeacoffee.com/marioserrano" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" style="height: 60px !important;width: 217px !important;" ></a>

---

<div align="center">

**Made with ❤️ by [Dynamia Soluciones IT](https://www.dynamiasoluciones.com)**

[Website](https://dynamia.tools) • [Documentation](https://dynamia.tools/getting-started/) • [GitHub](https://github.com/dynamiatools/framework) • [Maven Central](https://central.sonatype.com/search?q=g%3Atools.dynamia)

</div>
