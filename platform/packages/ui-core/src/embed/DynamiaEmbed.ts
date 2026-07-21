// DynamiaEmbed.ts — <dynamia-embed>: sandboxed-iframe or dynamic-Web-Component embed, zero frameworks, zero build.
//
// Browser-only module: it extends the global `HTMLElement`, so importing it in a non-DOM
// environment (Node/SSR) throws immediately. Keep it out of the package's main `.` entry —
// consume it only via the `@dynamia-tools/ui-core/embed` subpath.

import { detectEmbedType } from './detectType.js';

const DEFAULT_TIMEOUT_MS = 8000;
const DEFAULT_SANDBOX = 'allow-scripts';
const DEFAULT_FALLBACK_HEIGHT = '150px';
const CUSTOM_ELEMENT_NAME_RE = /^[a-z][a-z0-9._-]*-[a-z0-9._-]*$/;

/**
 * `<dynamia-embed src="...">` — embeds a remote resource without knowing its type ahead of time.
 *
 * It probes the real `Content-Type` via `HEAD` (falling back to a URL-extension heuristic when
 * the probe is blocked, unsupported or times out) and then either:
 * - renders HTML in a sandboxed `<iframe>`, or
 * - dynamically `import()`s a JS module and mounts its default export as a Web Component.
 *
 * Attributes:
 * - `src` (required) — URL of the resource to embed.
 * - `type` — `"html" | "js"`, forces the embed kind and skips the `HEAD` probe entirely.
 * - `tag` — custom element tag name to use for a JS embed (must contain a hyphen); defaults to a
 *   deterministic `dyn-<hash>` derived from `src`.
 * - `sandbox` — iframe `sandbox` attribute; defaults to `"allow-scripts"`. Pass `sandbox=""` for
 *   the strictest possible sandbox (no scripts at all).
 * - `height` — fallback height (number → px, or any CSS length) used until auto-resize kicks in,
 *   or permanently for cross-origin content that can't be measured.
 * - `timeout` — milliseconds before the `HEAD` probe / JS import is abandoned. Defaults to 8000.
 * - `loading` — `"lazy" | "eager"`, passed through to the iframe. Defaults to `"lazy"`.
 * - `no-resize` — boolean attribute; disables the `ResizeObserver` + postMessage auto-resize path.
 *
 * Events (bubble, cross shadow boundary):
 * - `dynamia-embed:load` — `{ type, src }`
 * - `dynamia-embed:error` — `{ src, error }`
 *
 * Example:
 * <pre>{@code
 * <dynamia-embed src="https://cdn.example.com/widgets/chart.html"></dynamia-embed>
 * <dynamia-embed src="https://cdn.example.com/widgets/chart.js" tag="acme-chart"></dynamia-embed>
 * }</pre>
 */
export class DynamiaEmbed extends HTMLElement {
  static readonly tagName = 'dynamia-embed';

  static get observedAttributes(): string[] {
    return ['src'];
  }

  private readonly _shadow: ShadowRoot;
  private _loadToken = 0;
  private _resizeObserver: ResizeObserver | null = null;
  private _messageListener: ((event: MessageEvent) => void) | null = null;

  constructor() {
    super();
    this._shadow = this.attachShadow({ mode: 'open' });
  }

  connectedCallback(): void {
    if (this.getAttribute('src')) this.reload();
  }

  disconnectedCallback(): void {
    this._cancelPending();
  }

  attributeChangedCallback(name: string, oldValue: string | null, newValue: string | null): void {
    if (name === 'src' && newValue !== oldValue && this.isConnected) {
      this.reload();
    }
  }

  get src(): string | null {
    return this.getAttribute('src');
  }

  set src(value: string | null) {
    if (value == null) this.removeAttribute('src');
    else this.setAttribute('src', value);
  }

  /** Cancels any in-flight load and re-runs detection + rendering for the current `src`. */
  reload(): void {
    const src = this.getAttribute('src');
    if (!src) {
      this._renderError('Missing "src" attribute');
      return;
    }
    void this._load(src);
  }

  private async _load(src: string): Promise<void> {
    this._cancelPending();
    const token = ++this._loadToken;
    this._renderLoading();

    try {
      const forced = this.getAttribute('type');
      const type = forced === 'html' || forced === 'js' ? forced : await detectEmbedType(src, { timeoutMs: this._timeoutMs });

      if (token !== this._loadToken) return; // superseded by a newer load

      if (type === 'html') {
        this._loadIframe(src);
      } else {
        await this._loadJsComponent(src, token);
      }

      if (token === this._loadToken) {
        this._dispatch('dynamia-embed:load', { type, src });
      }
    } catch (error) {
      if (token !== this._loadToken) return;
      this._renderError('Failed to load embed');
      this._dispatch('dynamia-embed:error', { src, error });
    }
  }

  // ── HTML → sandboxed iframe ──────────────────────────────────────────────

  private _loadIframe(src: string): void {
    const iframe = document.createElement('iframe');
    iframe.src = src;
    iframe.style.cssText = 'border:0;display:block;width:100%;';
    iframe.style.height = this._fallbackHeight;
    iframe.loading = this.getAttribute('loading') === 'eager' ? 'eager' : 'lazy';
    iframe.referrerPolicy = 'no-referrer-when-downgrade';
    iframe.setAttribute('sandbox', this.hasAttribute('sandbox') ? (this.getAttribute('sandbox') ?? '') : DEFAULT_SANDBOX);

    if (!this._resizeDisabled) {
      iframe.addEventListener('load', () => this._observeIframeHeight(iframe));
      this._messageListener = (event: MessageEvent) => this._onResizeMessage(event, iframe);
      window.addEventListener('message', this._messageListener);
    }

    this._shadow.replaceChildren(iframe);
  }

  private _observeIframeHeight(iframe: HTMLIFrameElement): void {
    try {
      const body = iframe.contentDocument?.body;
      if (!body) return; // cross-origin — stays at fallback height unless the page posts its own

      const applyHeight = () => {
        iframe.style.height = `${body.scrollHeight}px`;
      };
      applyHeight();

      this._resizeObserver = new ResizeObserver(applyHeight);
      this._resizeObserver.observe(body);
    } catch {
      // Cross-origin access blocked by the browser — same fallback as above.
    }
  }

  private _onResizeMessage(event: MessageEvent, iframe: HTMLIFrameElement): void {
    if (event.source !== iframe.contentWindow) return;
    const data = event.data as { type?: unknown; height?: unknown } | null;
    if (data && data.type === 'dynamia-embed:resize' && typeof data.height === 'number') {
      iframe.style.height = `${data.height}px`;
    }
  }

  // ── JS → dynamic Web Component ───────────────────────────────────────────

  private async _loadJsComponent(src: string, token: number): Promise<void> {
    const requestedTag = this.getAttribute('tag');
    if (requestedTag && !CUSTOM_ELEMENT_NAME_RE.test(requestedTag)) {
      throw new Error(`Invalid tag "${requestedTag}": custom element names must contain a hyphen`);
    }

    const module = await withTimeout(
      import(/* @vite-ignore */ src),
      this._timeoutMs,
      `Timed out loading module: ${src}`,
    );
    if (token !== this._loadToken) return; // superseded while the import was in flight

    const moduleExports = module as Record<string, unknown>;
    const ComponentCtor = moduleExports.default ?? moduleExports.Component;
    if (typeof ComponentCtor !== 'function') {
      throw new Error('JS module has no default export or "Component" export');
    }

    const tag = requestedTag || `dyn-${hashString(src)}`;
    if (!customElements.get(tag)) {
      customElements.define(tag, ComponentCtor as CustomElementConstructor);
    }

    this._shadow.replaceChildren(document.createElement(tag));
  }

  // ── Rendering ─────────────────────────────────────────────────────────────

  private _renderLoading(): void {
    this._shadow.innerHTML = `
      <style>.dynamia-embed-loading{font-family:system-ui,sans-serif;opacity:.6;padding:1rem}</style>
      <div class="dynamia-embed-loading" role="status" aria-live="polite">Loading…</div>
    `;
  }

  private _renderError(message: string): void {
    this._shadow.innerHTML = `
      <style>.dynamia-embed-error{font-family:system-ui,sans-serif;color:#b00020;padding:1rem}</style>
      <div class="dynamia-embed-error" role="alert">${escapeHtml(message)}</div>
    `;
  }

  private _dispatch(name: string, detail: Record<string, unknown>): void {
    this.dispatchEvent(new CustomEvent(name, { detail, bubbles: true, composed: true }));
  }

  // ── Attribute accessors ───────────────────────────────────────────────────

  private get _timeoutMs(): number {
    const parsed = Number(this.getAttribute('timeout'));
    return Number.isFinite(parsed) && parsed > 0 ? parsed : DEFAULT_TIMEOUT_MS;
  }

  private get _resizeDisabled(): boolean {
    return this.hasAttribute('no-resize');
  }

  private get _fallbackHeight(): string {
    const raw = this.getAttribute('height');
    if (!raw) return DEFAULT_FALLBACK_HEIGHT;
    return /^\d+$/.test(raw) ? `${raw}px` : raw;
  }

  private _cancelPending(): void {
    this._resizeObserver?.disconnect();
    this._resizeObserver = null;
    if (this._messageListener) {
      window.removeEventListener('message', this._messageListener);
      this._messageListener = null;
    }
  }
}

function withTimeout<T>(promise: Promise<T>, timeoutMs: number, message: string): Promise<T> {
  return new Promise<T>((resolve, reject) => {
    const timer = setTimeout(() => reject(new Error(message)), timeoutMs);
    promise.then(
      value => {
        clearTimeout(timer);
        resolve(value);
      },
      error => {
        clearTimeout(timer);
        reject(error);
      },
    );
  });
}

/** Deterministic short hash used to derive a default custom element tag from a URL. */
function hashString(value: string): string {
  let hash = 0;
  for (let i = 0; i < value.length; i++) {
    hash = (Math.imul(31, hash) + value.charCodeAt(i)) | 0;
  }
  return Math.abs(hash).toString(36);
}

function escapeHtml(value: string): string {
  return value.replace(/[&<>"']/g, char => {
    switch (char) {
      case '&': return '&amp;';
      case '<': return '&lt;';
      case '>': return '&gt;';
      case '"': return '&quot;';
      default: return '&#39;';
    }
  });
}
