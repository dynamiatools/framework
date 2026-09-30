# Inline (no-iframe) embedding of ZK views

Embed a server-rendered ZK view/page into a Vue (or any) host **without an iframe**, same origin only.
Issue: #113. Code: `platform/packages/ui-core/src/embed/inline.ts`, `<dynamia-embed mode="inline">`,
`<DynamiaZkEmbed>` in `@dynamia-tools/vue`.

## Usage

```vue
<!-- Vue -->
<DynamiaZkEmbed src="/page-embed/library/books" @load="onLoad" @error="onError" />
```

```html
<!-- Any page: <script src=".../dynamia-embed.global.js"> or registerDynamiaEmbed() -->
<dynamia-embed mode="inline" src="/books"></dynamia-embed>
```

```ts
// Programmatic
import { mountInline } from '@dynamia-tools/ui-core/embed';
const handle = await mountInline(container, '/books');
// ...
handle.destroy(); // releases the ZK desktop client- and server-side
```

`src` can be any same-origin URL that answers with a ZK-rendered document: a plain `.zul` view (`/books`) or a
navigation page through `PageEmbedController` (`/page-embed/<module>/<group>/<page>`). No server change is needed.

## How it works (non-obvious parts)

- A `.zul` without `<html>` root is answered as a **full HTML document**. `<head>` lists every script/CSS the ZK client
  and the page need (URLs are versioned, e.g. `/zkau/web/57aacf5b/js/zk.wpd`); `<body>` has the widgets plus one inline
  `<script class="z-runonce">zk.afterLoad(function(){zkmx([0,'..',{dt:'<desktopId>',...}, ...])})</script>`.
- `mountInline` parses that document, injects the `<head>` assets into the host (scripts strictly in order, deduplicated
  per document by absolute URL, shared between concurrent embeds), appends the body **re-creating its `<script>` nodes**
  (scripts created by `innerHTML`/`importNode` never run), then waits until the desktop announced by the response
  appears in `zk.Desktop.all`.
- The desktop id is read from the response's own `dt:'...'` (JS-unescaped: ZK writes `-` as `\-` in the source). Do not
  attribute desktops by diffing `zk.Desktop.all`: concurrent mounts steal each other's desktops.
- `destroy()` detaches the desktop's root pages (so floating popups go away), calls `zAu._rmDesktop(desktop, false)`
  (same `rmDesktop` beacon ZK sends on page unload; the server then treats that desktop as unknown) and deletes it from
  `zk.Desktop.all`. Each `hx-get`/embed is its own request, hence its own `NavigationManagerSession` scope and its own
  `ZKNavigationManager` (see `NAVIGATION_SESSION.md` in `docs/backend/`).
- The custom element renders the content in its **light DOM** (slotted), not in the shadow root: ZK needs the host
  document's global scope (ids, CSS, popups appended to `<body>`).

## Constraints

- **Same origin only.** Cross-origin URLs are rejected (`mountInline` throws before any request). Use the iframe mode of
  `<dynamia-embed>` or the official `zEmbedded` (zkmax, PE/EE) for those.
- Depends on **private ZK client API** (`zAu._rmDesktop`, `zk.Desktop.all`). Every access is guarded: an unexpected ZK
  version degrades to "no server-side cleanup" (desktop lingers until the session expires), not to an exception. Verified
  with ZK 10.3.0.1.
- Scope it to **view fragments** (viewers, CRUD, components). Page navigation (`setPageLater`) from inside an inline
  fragment has no workspace to open pages in.
- ZK's global CSS (`zk.wcs`, `zk-bootstrap.css`) is injected into the host and can affect its styles; host styles can
  affect ZK widgets. Not isolated (ZK-5061 documents the same for `zEmbedded`).
- One WebSocket per ZK page (`dynamia-tools-ws.js`): behavior with several simultaneous desktops in one window is not
  validated.
- Inline `<script>` inside the response `<head>` is ignored (only `<script src>` and stylesheets are injected).

## Verified

Unit tests (`ui-core/test/embed/inline.test.ts`, happy-dom) and a headless-Chrome run against `examples/demo-zk-books`:
two concurrent inline embeds (`/books` and `/page-embed/library/books`) each get their own desktop; removing one leaves
the other working; changing `src` replaces the desktop; cross-origin is rejected. The Vue component is type-checked and
built, not exercised in a browser yet.
