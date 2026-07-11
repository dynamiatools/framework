# Dynamia Tools Vue Backoffice Demo

Backoffice demo built with **Vue 3** + **@dynamia-tools/vue**.

It connects to a Dynamia backend (default `http://localhost:8484`) and renders:

- a left navigation menu,
- a breadcrumb on top,
- a center panel that auto-renders CRUD pages.

When a selected node is not `CrudPage`, the demo shows a fallback message.

## Prerequisites

- Node.js >= 20
- A running Dynamia backend with navigation metadata and CRUD pages

## Configuration

Copy `.env.example` to `.env` and adjust values if needed.

```bash
cp .env.example .env
```

Available variables:

- `VITE_DYNAMIA_API_URL` (default: `http://localhost:8484`)
- `VITE_DYNAMIA_TOKEN` (optional bearer token)
- `VITE_DYNAMIA_USERNAME` / `VITE_DYNAMIA_PASSWORD` (optional basic auth)

## Quick Start

```bash
npm install
npm run dev
```

Then open the URL printed by Vite (usually `http://localhost:5173`).

## Scripts

```bash
npm run dev
npm run build
npm run preview
npm run typecheck
```

## What You Should See

- **Left:** module/page navigation (`<DynamiaNavMenu>`)
- **Top:** active location breadcrumb (`<DynamiaNavBreadcrumb>`)
- **Center:**
  - `CrudPage` nodes render with `<DynamiaCrudPage>`
  - other node types render an informational message
