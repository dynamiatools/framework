# `<dynamia-embed>`

A zero-dependency Web Component for embedding a remote resource **without knowing its type ahead of time**. It probes the real `Content-Type` of `src` via `HEAD` and renders it as:

- **HTML** → a sandboxed `<iframe>`, or
- **JS** → a dynamically `import()`-ed module, mounted as a real Web Component.

No frameworks, no build step to consume it, no vendor lock-in. It is a general-purpose **integration utility** — useful for embedding third-party widgets, internal micro-frontends, legacy pages, or server-rendered apps (ZK, JSP, etc.) inside any host page, plain HTML or framework-based.

> **Browser-only.** This module extends `HTMLElement`, so it must be imported from the `@dynamia-tools/ui-core/embed` subpath — never from the package's main `.` entry, which stays framework-agnostic and DOM-free (safe for Node/SSR).

---

## Why "auto-detect" instead of just an `<iframe>`?

Because the caller often doesn't control (or know) what the URL will serve: a CMS field, a plugin registry, a partner-supplied widget URL, a ZK page behind a reverse proxy… `<dynamia-embed>` inspects the response instead of trusting the file extension, and falls back safely (to `html`, the sandboxed option) whenever detection is inconclusive.

---

## Usage

### 1. As an npm package (tree-shakeable, explicit registration)

```bash
pnpm add @dynamia-tools/ui-core
```

```ts
import { registerDynamiaEmbed } from '@dynamia-tools/ui-core/embed';

registerDynamiaEmbed(); // registers <dynamia-embed> once, safe to call multiple times
```

```html
<dynamia-embed src="https://cdn.example.com/widgets/chart.html"></dynamia-embed>
```

### 2. As a plain `<script>` tag (zero build, self-registering)

```html
<script src="https://unpkg.com/@dynamia-tools/ui-core/dist/dynamia-embed.global.js"></script>

<dynamia-embed src="/widgets/chart.js" tag="acme-chart"></dynamia-embed>
```

`dist/dynamia-embed.global.js` is a self-contained IIFE bundle (~2 kB gzipped) — no `type="module"`, no bundler, nothing else to load.

### 3. From source (workspace / monorepo development condition)

```ts
import { DynamiaEmbed, detectEmbedType } from '../../packages/ui-core/src/embed/index.js';
```

---

## Attributes

| Attribute   | Description                                                                                              | Default          |
|-------------|------------------------------------------------------------------------------------------------------------|-------------------|
| `src`       | **Required.** URL of the resource to embed. Reactive — changing it reloads the embed.                     | —                 |
| `type`      | Forces `"html"` or `"js"` and skips the `HEAD` probe entirely. Any other value re-enables auto-detection. | *(auto-detect)*  |
| `tag`       | Custom element tag name to mount for a JS embed. Must contain a hyphen.                                    | `dyn-<hash(src)>` |
| `sandbox`   | Value of the iframe's `sandbox` attribute. Pass `sandbox=""` for the strictest possible sandbox.          | `allow-scripts`   |
| `height`    | Fallback height (number → px, or any CSS length) used until auto-resize kicks in, or permanently for cross-origin content that can't be measured. | `150px` |
| `timeout`   | Milliseconds before the `HEAD` probe or the JS `import()` is abandoned.                                    | `8000`            |
| `loading`   | `"lazy" \| "eager"`, passed through to the iframe.                                                          | `lazy`            |
| `no-resize` | Boolean attribute — disables the `ResizeObserver` + `postMessage` auto-resize entirely.                    | *(enabled)*       |

Only `src` is reactive; the others are read once per load. Call `.reload()` on the element after changing them to apply.

## Events

Both bubble and cross the shadow boundary (`composed: true`):

- `dynamia-embed:load` — `detail: { type: 'html' | 'js', src: string }`
- `dynamia-embed:error` — `detail: { src: string, error: unknown }`

```ts
document.querySelector('dynamia-embed')
  ?.addEventListener('dynamia-embed:error', (e: Event) => {
    console.error('embed failed', (e as CustomEvent).detail);
  });
```

## Security notes

- Unknown/ambiguous content defaults to `html` (sandboxed iframe), **never** `js` — an unrecognised resource is never `import()`-ed and executed in the host page.
- The default `sandbox="allow-scripts"` does **not** include `allow-same-origin`. That's deliberate: `allow-scripts` + `allow-same-origin` together let the framed document script its way out of the sandbox if it's same-origin with the host. Only widen `sandbox` when you trust the embedded content (see the ZK example below).
- A JS embed (`type="js"` or auto-detected) always runs in the **main world**, with no isolation — only point it at code you trust.

---

## Example: embedding a ZK (`.zul`) page

ZK never sends `.zul` source to the browser — its servlet renders each `.zul` into a real HTML document server-side (`Content-Type: text/html`), so `<dynamia-embed>` already treats it as `html` with zero extra code. Two things are worth doing explicitly for a ZK target:

1. **Force `type="html"`** — you already know it's ZK, so skip the `HEAD` round-trip (and sidestep any proxy/filter in front of your ZK app that doesn't implement `HEAD` cleanly).
2. **Widen `sandbox`** for same-origin, trusted ZK apps — the default `allow-scripts` puts the iframe in an opaque origin, which breaks `sessionStorage`/`localStorage`/`document.domain` that ZK's client engine may rely on. Session cookies (`JSESSIONID`) are unaffected (cookie matching is per-URL, not per sandbox origin), so AU polling / server push over the same origin keeps working either way — but same-origin storage access needs `allow-same-origin`.

```html
<dynamia-embed
  src="/zkau/mypage.zul"
  type="html"
  sandbox="allow-scripts allow-same-origin allow-forms allow-popups"
  height="400"
></dynamia-embed>
```

Because `sandbox` includes `allow-same-origin` here (same-origin ZUL page), `<dynamia-embed>` can read `iframe.contentDocument` and auto-resizes as the page's content grows — including ZK dialogs/comboboxes, which are appended inside the iframe's own `<body>`, not the host page's.

If the ZK app is **cross-origin** instead, `contentDocument` is not reachable regardless of `sandbox`, so auto-resize falls back to the fixed `height` — unless the ZUL page opts in to the `postMessage` resize protocol itself, e.g. from an `onSize`/`afterRender` handler:

```java
Clients.evalJavaScript(
    "parent.postMessage({type:'dynamia-embed:resize', height:document.body.scrollHeight}, '*')"
);
```

## Example: a JS-embedded widget

```js
// widget.js — served with a Content-Type containing "javascript"
export default class AcmeChart extends HTMLElement {
  connectedCallback() {
    this.attachShadow({ mode: 'open' }).innerHTML = `<div>chart goes here</div>`;
  }
}
```

```html
<dynamia-embed src="/widgets/acme-chart.js" tag="acme-chart"></dynamia-embed>
```

`<dynamia-embed>` imports the module, reads its `default` (or named `Component`) export, registers it under `tag` (or a deterministic `dyn-<hash>` name if omitted), and mounts one instance in its shadow root.
