# @dynamia-tools/cli

> Scaffold new [Dynamia Platform](https://dynamia.tools) projects from the command line.

---

## Installation

### One-line bootstrap (Linux & macOS)

Installs JDK 25, Node.js LTS, and the CLI:

```bash
curl -fsSL https://get.dynamia.tools | bash
```

### npm (global)

```bash
npm install -g @dynamia-tools/cli
```

### Requirements

| Tool | Minimum version |
|---|---|
| Node.js | 22 |
| git | any recent version (required) |
| Java | 25 (recommended) |

---

## Usage

```bash
dynamia new
```

The wizard guides you through:

1. **Project name** — lowercase, letters/numbers/hyphens only
2. **Scaffold choice** — Backend + Frontend / Backend only / Frontend only
3. **Backend language** — Java, Kotlin, or Groovy
4. **Maven coordinates** — Group ID, Artifact ID, version, description
5. **Frontend framework** — Vue 3 or React
6. **Package manager** — pnpm, npm, or yarn
7. **Confirm** — shows a summary table before generating

---

## What gets generated

```
my-erp-app/
├── backend/     Spring Boot + Dynamia Tools (Java/Kotlin/Groovy)
└── frontend/    Vue 3 or React + Vite + @dynamia-tools/vue|sdk
```

### Backend

- Cloned from a GitHub template repo (e.g. `dynamiatools/template-backend-java`)
- Placeholder package `com.example.demo` renamed to your `groupId.artifactId`
- All tokens replaced in `.java`, `.kt`, `.groovy`, `.xml`, `.yml`, `.properties`, `.md`
- `DemoApplication.java` renamed to `<YourArtifactId>Application.java`

### Frontend

- Cloned from a GitHub template repo (e.g. `dynamiatools/template-frontend-vue`)
- Falls back to `npm create vite@latest` if clone fails
- `@dynamia-tools/sdk` and `@dynamia-tools/ui-core` installed automatically

---

## Configuration

All versions, URLs, and template repositories live in `cli.properties` — the single source of truth. TypeScript code never hardcodes versions or URLs.

Key sections:

| Section | Description |
|---|---|
| `dynamia.*` | Framework version and docs URL |
| `java.*` | JDK version and SDKMAN candidate |
| `template.backend.<id>.*` | Backend template repos (java, kotlin, groovy) |
| `template.frontend.<id>.*` | Frontend template repos (vue, react) |
| `token.*` | Placeholder tokens used inside template repos |
| `vite.*` | Vite fallback config |

---

## Template author conventions

When creating template repos (e.g. `dynamiatools/template-backend-java`):

- Default Java source package: `com.example.demo`
- `pom.xml` groupId: `com.example`, artifactId: `demo`
- Main class: `src/main/java/com/example/demo/DemoApplication.java`
- Use these placeholders in files:
  - `{{GROUP_ID}}` — user's group ID
  - `{{ARTIFACT_ID}}` — user's artifact ID
  - `{{BASE_PACKAGE}}` — computed base package
  - `{{PROJECT_NAME}}` — project name
  - `{{PROJECT_VERSION}}` — project version
  - `{{DYNAMIA_VERSION}}` — Dynamia Tools version
  - `{{SPRING_BOOT_VERSION}}` — Spring Boot version

---

## Links

- [Dynamia Tools docs](https://dynamia.tools/docs)
- [GitHub org](https://github.com/dynamiatools)
- [Framework repo](https://github.com/dynamiatools/framework)

---

## License

Apache-2.0 — © Dynamia Soluciones IT SAS
