import type { NavigationNode } from '@dynamia-tools/sdk';

/** `http(s)://…` or root-relative (`/reports/monthly.html`). The server only sends `url` for those; a `classpath:` ZUL or a ConfigPage's bean name like `discountsCfg` is never sent. */
const BROWSABLE_URL = /^(https?:\/\/|\/)/i;

/**
 * Resolves a non-CrudPage {@link NavigationNode} into a URL for `<dynamia-embed src="...">`.
 *
 * - `ExternalPage`, or any `Page` with a `url` (absolute `http(s)://` or a
 *   root-relative path, e.g. `/reports/monthly.html`) → that URL, used as-is.
 * - Anything else (a ZUL-backed `Page`, a `ConfigPage`, or a node with no `url`) falls
 *   back to `/page-embed/{node.path}` — the framework's `PageEmbedController`
 *   (`tools.dynamia.web.navigation`), which renders just that page's ZK content with no
 *   header/sidebar/footer around it (a minimal `embed.zul` workspace, resolved independently of
 *   whichever `ApplicationTemplate` is active), instead of the full app shell `/page/{path}`
 *   would double up. Requires ZK to actually be on the running app's classpath — if it isn't
 *   (this theme itself has no ZK dependency), that route 404s and the page has no way to render.
 */
export function resolveEmbedSrc(node: NavigationNode): string | null {
  const url = node.url;
  if (url && BROWSABLE_URL.test(url)) {
    return url;
  }
  return node.path ? `/page-embed/${node.path}` : null;
}

/**
 * `sandbox` for the iframe of an HTML embed. The default of `<dynamia-embed>` (`allow-scripts`) puts the frame in an
 * opaque origin: no cookies reach the server from it, so an embedded page of this same application could not call
 * `/api`. A page served by the same origin is declared by the application itself, so it gets `allow-same-origin`.
 * Pages of other origins keep the strict default.
 */
export function embedSandbox(src: string | null): string | undefined {
  if (!src) return undefined;
  try {
    const sameOrigin = new URL(src, window.location.origin).origin === window.location.origin;
    return sameOrigin ? 'allow-scripts allow-same-origin allow-forms allow-popups allow-downloads' : undefined;
  } catch {
    return undefined;
  }
}
