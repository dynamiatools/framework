# demo-client-books

> Public storefront demo built with **Astro** + **@dynamia-tools/sdk**, consuming the [`demo-zk-books`](../demo-zk-books) backend.

---

## What it shows

| Page | URL | Description |
|------|-----|-------------|
| Home | `/` | Featured books grid with category filter and pagination |
| Books | `/books` | Full catalog with search, category filter and pagination |
| Book detail | `/books/:id` | Cover, price, stock status, synopsis and reader reviews |
| Categories | `/categories` | All categories with links to filtered book list |

Everything is **server-rendered** (SSR via `@astrojs/node`), so each request fetches live data from the Dynamia Platform REST API via `@dynamia-tools/sdk`.

---

## Prerequisites

- Node.js ≥ 20
- [`demo-zk-books`](../demo-zk-books) backend running (defaults to `http://localhost:8080`)

---

## Quick start

```bash
# 1 – Install dependencies
npm install

# 2 – Point to your backend (edit .env or set the env var)
echo "PUBLIC_API_URL=http://localhost:8484" > .env

# 3 – Dev server with hot-reload
npm run dev          # http://localhost:4321

# 4 – Production build
npm run build
node dist/server/entry.mjs
```

---

## Project structure

```
demo-client-books/
├── src/
│   ├── layouts/
│   │   └── Layout.astro        # Navbar, footer, global CSS
│   ├── lib/
│   │   ├── client.ts           # DynamiaClient singleton (reads PUBLIC_API_URL)
│   │   ├── types.ts            # TypeScript types mirroring Java domain
│   │   └── api.ts              # Thin helpers: getBooks(), getCoverUrl(), ...
│   └── pages/
│       ├── index.astro         # Home / featured books
│       ├── books/
│       │   ├── index.astro     # /books — full catalog
│       │   └── [id].astro      # /books/:id — book detail
│       └── categories/
│           └── index.astro     # /categories
├── .env                        # PUBLIC_API_URL (git-ignored)
├── .env.example                # Template
├── astro.config.mjs
├── package.json
└── tsconfig.json
```

---

## SDK usage

```typescript
import { DynamiaClient } from '@dynamia-tools/sdk';

const client = new DynamiaClient({ baseUrl: 'http://localhost:8080' });

// List books (CrudPage virtual path: "library/books")
const { content, total } = await client.crud('library/books').findAll({ page: 1, size: 12 });

// Single book
const book = await client.crud('library/books').findById(1);

// File cover URL
const url = client.files.getUrl(book.bookCover.filename, book.bookCover.uuid);
```

---

## License

Apache 2.0 — © Dynamia Soluciones IT SAS

