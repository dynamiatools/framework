# @dynamia-tools/files-sdk

> TypeScript / JavaScript client SDK for the Dynamia Entity Files extension REST API.

`@dynamia-tools/files-sdk` provides a small, focused client to download files managed by the Entity Files extension of a Dynamia Platform backend. The package exposes a single API class, `FilesApi`, which delegates HTTP, authentication and error handling to the core `@dynamia-tools/sdk` `HttpClient`.

This README explains how to install the package, how to use `FilesApi` (recommended via `DynamiaClient`) and how to handle binary downloads in browser and Node.js environments.

---

## Table of Contents

- [Installation](#installation)
- [Quick Start (recommended)](#quick-start-recommended)
- [API methods](#api-methods)
- [Browser example (download and show)](#browser-example-download-and-show)
- [Node.js example (save to disk)](#nodejs-example-save-to-disk)
- [Authentication & Errors](#authentication--errors)
- [Contributing](#contributing)
- [License](#license)

---

## Installation

Install the package using your preferred package manager:

```bash
# pnpm (recommended)
pnpm add @dynamia-tools/files-sdk

# npm
npm install @dynamia-tools/files-sdk

# yarn
yarn add @dynamia-tools/files-sdk
```

This package declares a peer dependency on `@dynamia-tools/sdk` (see `package.json`). The recommended pattern is to use the core SDK's `DynamiaClient` so you reuse the same HTTP client, auth configuration and fetch implementation.

---

## Quick Start (recommended)

Construct `FilesApi` from an existing `DynamiaClient` so authentication, base URL and fetch are consistent:

```text
import { DynamiaClient } from '@dynamia-tools/sdk';
import { FilesApi } from '@dynamia-tools/files-sdk';

const client = new DynamiaClient({ baseUrl: 'https://app.example.com', token: '...' });
const files = new FilesApi(client.http);

// Download a file as a Blob (browser)
const blob = await files.download('myfile.pdf', 'f9a3e8c2-...');

// Get a direct URL (no network call performed)
const url = files.getUrl('images/logo.png', 'f9a3e8c2-...');
console.log(url);
```

Notes:
- `FilesApi` methods are thin wrappers over the core `HttpClient` (`get`, `url`) implemented by `DynamiaClient`.
- The `download()` method calls `GET /storage/{file}?uuid={uuid}` and returns a `Blob` in browser environments; when running in Node.js the underlying fetch polyfill may provide an `ArrayBuffer`/`Buffer` which you should convert to a file.

---

## API methods

The implementation in `src/api.ts` exposes two methods on `FilesApi`:

- `download(file: string, uuid: string): Promise<Blob>`
  - GET `/storage/{file}?uuid={uuid}` — Downloads the file. The SDK returns parsed JSON for `application/json` responses and a `Blob` for other content types (binary).
- `getUrl(file: string, uuid: string): string`
  - Returns a fully-qualified URL that points to `/storage/{file}` with the `uuid` query parameter. No HTTP request is made.

Use `download()` when you need the file content programmatically (e.g., for preview or file save). Use `getUrl()` when you want to put a direct link in an `img` `src`, anchor `href`, or let the browser perform the download.

---

## Browser example (download and show)

```text
// show a downloaded PDF in a new tab
const blob = await files.download('reports/monthly.pdf', 'f9a3e8c2-...');
const url = URL.createObjectURL(blob);
window.open(url, '_blank');
// Remember to revoke the object URL when done
URL.revokeObjectURL(url);
```

Use `files.getUrl('images/logo.png', uuid)` directly in `<img src={...} />` if you just need to render an image.

---

## Node.js example (save to disk)

When running in Node, the fetch implementation may not return a `Blob`. Use `arrayBuffer()` or convert to a `Buffer` before writing the file to disk:

```text
import fs from 'fs';
import { DynamiaClient } from '@dynamia-tools/sdk';
import { FilesApi } from '@dynamia-tools/files-sdk';

const client = new DynamiaClient({ baseUrl: 'https://app.example.com', token: process.env.TOKEN, fetch: fetch });
const files = new FilesApi(client.http);

const res = await files.download('reports/monthly.pdf', 'f9a3e8c2-...');

// If the returned value has arrayBuffer(), use it
if (res && typeof (res as any).arrayBuffer === 'function') {
  const ab = await (res as any).arrayBuffer();
  const buffer = Buffer.from(ab);
  await fs.promises.writeFile('./monthly.pdf', buffer);
} else if (Buffer.isBuffer(res)) {
  await fs.promises.writeFile('./monthly.pdf', res);
} else {
  // Fallback: inspect the object
  console.error('Unexpected download result:', res);
}
```

Note: if you are using Node.js < 18 you must provide a `fetch` implementation (e.g. `node-fetch`) and pass it to `DynamiaClient` via the `fetch` config option.

---

## Authentication & errors

Use the same authentication methods supported by `DynamiaClient`:

- Bearer token: pass `token` to `DynamiaClient`.
- HTTP Basic: pass `username` and `password`.
- Session / form-login: use `withCredentials: true` and perform the login flow before calling the SDK.

Non-2xx responses are translated by the core `HttpClient` into `DynamiaApiError` (from `@dynamia-tools/sdk`). Catch this error to examine `status`, `url` and `body` for logging or user-friendly messages.

---

## Contributing

See the repository-level `CONTRIBUTING.md` for contribution guidelines.

Quick local steps:

1. Clone the repo and install dependencies: `pnpm install`
2. Work inside `framework/extensions/entity-files/packages/files-sdk/`
3. Build and test: `pnpm --filter @dynamia-tools/files-sdk build` / `pnpm --filter @dynamia-tools/files-sdk test`

---

## License

[Apache License 2.0](../../../../LICENSE) — © Dynamia Soluciones IT SAS


