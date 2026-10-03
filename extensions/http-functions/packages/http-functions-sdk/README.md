# @dynamia-tools/http-functions-sdk

TypeScript / JavaScript client for the Dynamia **HTTP Functions** extension: functions exposed as HTTP endpoints under
`/api/dynamia/fx/{functionName}`. It delegates HTTP, auth and errors to the core `@dynamia-tools/sdk` client.

## Installation

```bash
pnpm add @dynamia-tools/http-functions-sdk @dynamia-tools/sdk
```

## Usage

```ts
import { DynamiaClient } from '@dynamia-tools/sdk';
import { FunctionsApi } from '@dynamia-tools/http-functions-sdk';

const client = new DynamiaClient({ baseUrl: 'https://app.example.com', token: '...' });
const fx = new FunctionsApi(client.http);

// Structured result: resolves with the function's `data`
const sent = await fx.call<{ messageId: string }>('WhatsApp.sendMessage', { to: '+57300...', text: 'Hi' });

// Pin a version (default: the highest active one)
await fx.call('Math.double', { n: 21 }, { version: 2 });

// Binary result (a PDF, an image...): resolves with a Blob
const pdf = await fx.callBinary('Invoice.pdf', { id: 42 });
```

A function answers either JSON or a binary payload; the function decides. `call()` throws if it receives a
binary response and `callBinary()` throws if it receives JSON.

## Errors

Failures reject with `DynamiaApiError` (from `@dynamia-tools/sdk`) carrying the HTTP `status` and the server's
message: `400` invalid parameters, `404` unknown or inactive function or version, `500` the function failed.

```ts
try {
  await fx.call('Math.double', {});
} catch (e) {
  if (e instanceof DynamiaApiError && e.status === 400) console.warn(e.message);
}
```
