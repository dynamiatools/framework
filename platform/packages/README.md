# Dynamia Platform – Packages

This directory is a **pnpm monorepo** containing all official Node.js / TypeScript packages for [Dynamia Platform](https://www.dynamia.tools) — the full-stack Java 25 enterprise framework.

These packages allow frontend developers to interact with Dynamia Platform backends, build rich UIs, and integrate with the platform from the JavaScript/TypeScript ecosystem.

---

## 📦 Packages

| Package                  | Description |
|--------------------------|-------------|
| `@dynamia-tools/sdk`     | JavaScript/TypeScript client SDK for Dynamia Platform REST APIs |
| `@dynamia-tools/ui-core` | Headless UI components and utilities aligned with Dynamia Platform's view descriptors |
| `@dynamia-tools/vue`     | Vue 3 integration — composables, components, and plugins for Dynamia Platform |

> **Note:** Packages are added progressively. Check each sub-folder for its own `README.md` and `CHANGELOG.md`.

---

## 🗂️ Repository Structure

```
packages/
├── sdk/          # @dynamia-tools/sdk   – Core API client
├── ui-core/      # @dynamia-tools/ui-core    – UI components
├── vue/          # @dynamia-tools/vue   – Vue 3 integration
└── README.md     # This file
```

---

## 🚀 Getting Started

### Prerequisites

- [Node.js](https://nodejs.org) >= 20
- [pnpm](https://pnpm.io) >= 9

### Install dependencies

```bash
pnpm install
```

### Build all packages

```bash
pnpm build
```

### Run tests

```bash
pnpm test
```

### Run a specific package script

```bash
pnpm --filter @dynamia-tools/sdk build
pnpm --filter @dynamia-tools/vue dev
```

---

## ⚙️ Workspace Configuration

The monorepo is managed with **pnpm workspaces**. The workspace root `pnpm-workspace.yaml` declares all packages:

```yaml
packages:
  - 'packages/*'
```

Shared dev dependencies (TypeScript, Vite, Vitest, ESLint, etc.) are hoisted to the root `package.json` to avoid duplication.

---

## 🔗 Using the SDK

```typescript
import { DynamiaClient } from '@dynamia-tools/sdk';

const client = new DynamiaClient({
  baseUrl: 'https://your-dynamia-app.com/api',
  token: 'your-access-token',
});

// Fetch entities
const books = await client.crud('Book').findAll();

// Create entity
await client.crud('Book').create({ title: 'Clean Code', author: 'Robert C. Martin' });
```

---

---

## 🤝 Contributing

1. Fork the repository and create a feature branch.
2. Make your changes inside the relevant package.
3. Add or update tests in the same package.
4. Run `pnpm test` from the root to verify all packages pass.
5. Open a Pull Request targeting `main`.

Please read the root [CONTRIBUTING.md](../../../../CONTRIBUTING.md) for more details.

---

## 📄 License

[Apache License 2.0](../../../../LICENSE) — © Dynamia Soluciones IT SAS

