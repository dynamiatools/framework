# Dynamia Tools Vue Backoffice Demo

This demo shows a typical backoffice web app experience using Dynamia Tools with vue

It connects to a local backend at `http://localhost:8484` and displays a full app shell with:

- a left navigation menu,
- a top title/breadcrumb area,
- a main content area in the center.

When a menu item is a **CRUD page**, the demo opens it in the content area.
If a page type is different, the demo shows a simple message.

## What This Demo Is For

Use this demo to quickly see how a Dynamia-based backoffice feels for end users:

- browse modules from the left menu,
- see where you are from the top breadcrumb,
- work with data screens in the main area.

## Quick Start

1. Start your Dynamia backend locally on port `8484`.
2. Open and run this Vue demo app.
3. Open the app in your browser.

If your project already has npm scripts configured, a common flow is:

```bash
npm install
npm run dev
```

## What You Should See

- **Left side:** navigation menu with available modules/pages.
- **Top area:** current location (module/group/page).
- **Center:**
  - CRUD pages render as full data screens.
  - Other page types show an informational message.

## Notes

- This README is user-focused and intentionally keeps technical details minimal.
- If your environment requires login or a token, complete authentication before using the demo.
