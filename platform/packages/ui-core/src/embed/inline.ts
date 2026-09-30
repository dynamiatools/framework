// inline.ts — same-origin, iframe-less embedding of server-rendered ZK pages/views.
//
// A ZK view such as `GET /books` answers with a *full* HTML document: `<head>` lists every
// script/stylesheet the ZK client and the page need, `<body>` holds the widget markup plus one
// inline `<script>` (`zk.afterLoad(function(){zkmx([...])})`) that builds the ZK Desktop.
// `mountInline` fetches that document, injects the (deduplicated) head assets into the host page,
// appends the body into `container` re-creating its scripts so they execute, and returns a handle
// whose `destroy()` releases the Desktop both client- and server-side.
//
// Same-origin only, by design: cookies (JSESSIONID) and the global ZK client engine are shared
// with the host page. Depends on private ZK client API (`zAu._rmDesktop`, `zk.Desktop.all`);
// every access to it is guarded so an unexpected ZK version degrades to "no server-side cleanup"
// instead of throwing.

const DEFAULT_TIMEOUT_MS = 15000;
const POLL_INTERVAL_MS = 25;

/** Options for {@link mountInline}. */
export interface InlineMountOptions {
  /** Milliseconds before fetching, loading assets or waiting for the Desktop is abandoned. Defaults to 15000. */
  timeoutMs?: number;
  /** Aborts an in-flight mount; the returned promise rejects and nothing is left mounted. */
  signal?: AbortSignal;
  /** Injectable `fetch` (tests). Defaults to the global one. */
  fetchImpl?: typeof fetch;
}

/** Handle of a mounted inline embed. */
export interface InlineHandle {
  /** ZK desktop ids created by this mount (empty when the response was not a ZK page). */
  readonly desktopIds: readonly string[];
  /** Detaches widgets, releases the Desktop(s) on the server and empties the container. Idempotent. */
  destroy(): void;
}

// Minimal view of the ZK client globals we touch.
interface ZkDesktop {
  id: string;
  firstChild?: ZkPage | null;
}
interface ZkPage {
  nextSibling?: ZkPage | null;
  detach?: () => void;
}
interface ZkGlobals {
  zk?: { Desktop?: { all?: Record<string, ZkDesktop> }; mounting?: boolean };
  zAu?: { _rmDesktop?: (desktop: ZkDesktop, keep: boolean) => void };
}

// One registry per document: url -> in-flight/settled load, so concurrent embeds on the same
// page share a single injection of each asset.
const assetRegistry = new WeakMap<Document, Map<string, Promise<void>>>();

/**
 * Fetches `src` (same-origin ZK view/page) and mounts it inline inside `container`.
 *
 * @throws if `src` is cross-origin, the request fails / is not OK, an asset fails to load, or the
 *         mount is aborted or times out.
 */
export async function mountInline(container: HTMLElement, src: string, options: InlineMountOptions = {}): Promise<InlineHandle> {
  const doc = container.ownerDocument;
  const win = doc.defaultView;
  if (!win) throw new Error('mountInline requires a container attached to a window');

  const url = new URL(src, doc.baseURI);
  if (url.origin !== win.location.origin) {
    throw new Error(`Inline embed is same-origin only: ${url.origin} != ${win.location.origin}`);
  }

  const timeoutMs = options.timeoutMs && options.timeoutMs > 0 ? options.timeoutMs : DEFAULT_TIMEOUT_MS;
  const deadline = Date.now() + timeoutMs;
  const signal = options.signal;
  const throwIfAborted = () => {
    if (signal?.aborted) throw new DOMException('Inline embed aborted', 'AbortError');
  };
  throwIfAborted();

  const doFetch = options.fetchImpl ?? win.fetch.bind(win);
  const timeoutCtl = new AbortController();
  const timer = setTimeout(() => timeoutCtl.abort(), timeoutMs);
  signal?.addEventListener('abort', () => timeoutCtl.abort(), { once: true });

  let html: string;
  try {
    const response = await doFetch(url.href, {
      credentials: 'same-origin',
      headers: { Accept: 'text/html' },
      signal: timeoutCtl.signal,
    });
    if (!response.ok) throw new Error(`Inline embed request failed: ${response.status} ${url.pathname}`);
    html = await response.text();
  } finally {
    clearTimeout(timer);
  }
  throwIfAborted();

  const parsed = new win.DOMParser().parseFromString(html, 'text/html');

  // 1. Head assets: styles in parallel, scripts strictly in order (ZK scripts depend on each other).
  const styles = Array.from(parsed.head.querySelectorAll('link[rel~="stylesheet"][href]'))
    .map(link => loadStyle(doc, resolve(doc, link.getAttribute('href')!), remaining(deadline)));
  for (const script of Array.from(parsed.head.querySelectorAll('script[src]'))) {
    await loadScript(doc, resolve(doc, script.getAttribute('src')!), remaining(deadline));
    throwIfAborted();
  }
  await Promise.all(styles);
  throwIfAborted();

  // 2. Body: append, recreating <script> nodes (innerHTML/importNode-d scripts never execute).
  // The desktop id(s) come from this response's own bootstrap script (zkmx([0,'uuid',{dt:'...'}...])),
  // NOT from diffing zk.Desktop.all: concurrent mounts would steal each other's desktops.
  const expectedIds = extractDesktopIds(parsed.body);
  for (const node of Array.from(parsed.body.childNodes)) {
    if (node.nodeName === 'NOSCRIPT') continue;
    container.appendChild(node.nodeName === 'SCRIPT' ? cloneScript(doc, node as HTMLScriptElement) : doc.importNode(node, true));
  }

  // 3. ZK builds the Desktop asynchronously (zk.afterLoad -> zkmx); wait for it to register.
  let desktopIds: string[];
  try {
    desktopIds = await waitForDesktops(win, expectedIds, deadline, signal);
  } catch (error) {
    releaseDesktops(win, expectedIds);
    container.replaceChildren();
    throw error;
  }

  let destroyed = false;
  const handle: InlineHandle = {
    desktopIds,
    destroy() {
      if (destroyed) return;
      destroyed = true;
      releaseDesktops(win, desktopIds);
      container.replaceChildren();
    },
  };

  if (signal?.aborted) {
    handle.destroy();
    throwIfAborted();
  }
  return handle;
}

// ── Assets ──────────────────────────────────────────────────────────────────

function resolve(doc: Document, href: string): string {
  return new URL(href, doc.baseURI).href;
}

function remaining(deadline: number): number {
  return Math.max(1, deadline - Date.now());
}

function registry(doc: Document): Map<string, Promise<void>> {
  let map = assetRegistry.get(doc);
  if (!map) {
    map = new Map();
    assetRegistry.set(doc, map);
  }
  return map;
}

function alreadyInDom(doc: Document, tag: 'script' | 'link', attr: 'src' | 'href', url: string): boolean {
  return Array.from(doc.querySelectorAll<HTMLElement>(`${tag}[${attr}]`))
    .some(el => resolve(doc, el.getAttribute(attr)!) === url);
}

function loadOnce(doc: Document, url: string, create: () => Promise<void>): Promise<void> {
  const map = registry(doc);
  let pending = map.get(url);
  if (!pending) {
    pending = create();
    map.set(url, pending);
    // A failed load must not poison the registry: allow a later mount to retry.
    pending.catch(() => map.delete(url));
  }
  return pending;
}

function loadScript(doc: Document, url: string, timeoutMs: number): Promise<void> {
  // Present in the DOM but not loaded by us (e.g. the host page already includes zk.wpd): trust it.
  if (!registry(doc).has(url) && alreadyInDom(doc, 'script', 'src', url)) return Promise.resolve();
  return loadOnce(doc, url, () => injectAsset(doc, 'script', url, timeoutMs));
}

function loadStyle(doc: Document, url: string, timeoutMs: number): Promise<void> {
  if (!registry(doc).has(url) && alreadyInDom(doc, 'link', 'href', url)) return Promise.resolve();
  return loadOnce(doc, url, () => injectAsset(doc, 'link', url, timeoutMs));
}

function injectAsset(doc: Document, kind: 'script' | 'link', url: string, timeoutMs: number): Promise<void> {
  return new Promise<void>((resolvePromise, reject) => {
    const el = doc.createElement(kind);
    const timer = setTimeout(() => reject(new Error(`Timed out loading ${url}`)), timeoutMs);
    el.addEventListener('load', () => { clearTimeout(timer); resolvePromise(); });
    el.addEventListener('error', () => { clearTimeout(timer); reject(new Error(`Failed to load ${url}`)); });
    if (kind === 'script') {
      const script = el as HTMLScriptElement;
      script.async = false; // keep execution order for dynamically injected scripts
      script.charset = 'UTF-8';
      script.src = url;
    } else {
      const link = el as HTMLLinkElement;
      link.rel = 'stylesheet';
      link.href = url;
    }
    doc.head.appendChild(el);
  });
}

function cloneScript(doc: Document, original: HTMLScriptElement): HTMLScriptElement {
  const script = doc.createElement('script');
  for (const attr of Array.from(original.attributes)) script.setAttribute(attr.name, attr.value);
  script.textContent = original.textContent;
  return script;
}

// ── ZK desktops ─────────────────────────────────────────────────────────────

function zkDesktops(win: Window): Record<string, ZkDesktop> {
  return (win as unknown as ZkGlobals).zk?.Desktop?.all ?? {};
}

const DESKTOP_ID_RE = /\bdt\s*:\s*['"]([^'"]+)['"]/g;

/** Desktop ids declared by the ZK bootstrap script(s) of a parsed response body. */
function extractDesktopIds(body: HTMLElement): string[] {
  const ids = new Set<string>();
  for (const script of Array.from(body.querySelectorAll('script'))) {
    if (script.hasAttribute('src')) continue;
    // ZK escapes '-' as '\-' inside its JS string literals; the real id has no backslash.
    for (const match of (script.textContent ?? '').matchAll(DESKTOP_ID_RE)) ids.add(match[1]!.replace(/\\(.)/g, '$1'));
  }
  return Array.from(ids);
}

/** Resolves once every expected desktop is registered and ZK is idle. No ids expected = not a ZK page. */
async function waitForDesktops(win: Window, expected: string[], deadline: number, signal?: AbortSignal): Promise<string[]> {
  if (expected.length === 0) return [];
  const mounting = () => (win as unknown as ZkGlobals).zk?.mounting === true;

  while (Date.now() < deadline) {
    if (signal?.aborted) return expected;
    const all = zkDesktops(win);
    if (expected.every(id => id in all) && !mounting()) return expected;
    await new Promise(r => setTimeout(r, POLL_INTERVAL_MS));
  }
  throw new Error(`Timed out waiting for ZK desktop(s): ${expected.join(', ')}`);
}

function releaseDesktops(win: Window, ids: readonly string[]): void {
  const globals = win as unknown as ZkGlobals;
  const all = zkDesktops(win);
  for (const id of ids) {
    const desktop = all[id];
    if (!desktop) continue;
    try {
      // Detach root widgets first so floating children (popups, dialogs) attached to <body> go away.
      let page = desktop.firstChild ?? null;
      while (page) {
        const next = page.nextSibling ?? null;
        page.detach?.();
        page = next;
      }
    } catch { /* best effort */ }
    try {
      globals.zAu?._rmDesktop?.(desktop, false); // tells the server to drop the Desktop (sendBeacon)
    } catch { /* best effort */ }
    delete all[id];
  }
}
