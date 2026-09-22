import type { NavigationNode } from '@dynamia-tools/sdk';

/** `http(s)://…` or root-relative (`/reports/monthly.html`). Anything else in `file` (a `classpath:` ZUL, a ConfigPage's bean name like `discountsCfg`...) is not something a browser can load. */
const BROWSABLE_URL = /^(https?:\/\/|\/)/i;

/**
 * Resolves a non-CrudPage {@link NavigationNode} into a URL for `<dynamia-embed src="...">`.
 *
 * - `ExternalPage`, or any `Page` whose `file` is a real URL (absolute `http(s)://` or a
 *   root-relative path, e.g. `/reports/monthly.html`) → that URL, used as-is.
 * - Anything else (a ZUL-backed `Page`, a `ConfigPage`, or a node with no `file` at all) falls
 *   back to `/page-embed/{node.path}` — the framework's `PageEmbedController`
 *   (`tools.dynamia.web.navigation`), which renders just that page's ZK content with no
 *   header/sidebar/footer around it (a minimal `embed.zul` workspace, resolved independently of
 *   whichever `ApplicationTemplate` is active), instead of the full app shell `/page/{path}`
 *   would double up. Requires ZK to actually be on the running app's classpath — if it isn't
 *   (this theme itself has no ZK dependency), that route 404s and the page has no way to render.
 */
export function resolveEmbedSrc(node: NavigationNode): string | null {
  const file = node.file;
  if (file && BROWSABLE_URL.test(file)) {
    return file;
  }
  return node.path ? `/page-embed/${node.path}` : null;
}
