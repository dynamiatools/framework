// detectType.ts — real content-type detection for <dynamia-embed>, HEAD-first with defensive fallbacks

/** The two content shapes `<dynamia-embed>` knows how to render. */
export type EmbedContentType = 'html' | 'js';

export interface DetectTypeOptions {
  /** Aborts the HEAD probe after this many milliseconds. Defaults to 8000. */
  timeoutMs?: number;
  /** Injectable `fetch` for testing; defaults to the global `fetch`. */
  fetchImpl?: typeof fetch;
}

const DEFAULT_TIMEOUT_MS = 8000;

/**
 * Detects whether `src` should be embedded as sandboxed HTML (iframe) or as a
 * dynamic JS Web Component, without trusting the file extension.
 *
 * Resolution order:
 * 1. `Content-Type` header from a `HEAD` request (real, server-declared type).
 * 2. URL extension heuristic, if HEAD fails, is blocked by CORS, times out, or
 *    the server returns an unrecognised content type.
 * 3. `"html"` as the final, safest default — an unknown resource is sandboxed
 *    in an iframe rather than imported and executed as a JS module.
 *
 * Example:
 * <pre>{@code
 * const type = await detectEmbedType('https://cdn.example.com/widget.js');
 * // → 'js'
 * }</pre>
 */
export async function detectEmbedType(src: string, options: DetectTypeOptions = {}): Promise<EmbedContentType> {
  const timeoutMs = options.timeoutMs ?? DEFAULT_TIMEOUT_MS;
  const fetchImpl = options.fetchImpl ?? fetch;

  const fromHeader = await probeContentType(src, timeoutMs, fetchImpl);
  if (fromHeader) return fromHeader;

  return heuristicTypeFromUrl(src);
}

async function probeContentType(
  src: string,
  timeoutMs: number,
  fetchImpl: typeof fetch,
): Promise<EmbedContentType | null> {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);

  try {
    const response = await fetchImpl(src, { method: 'HEAD', signal: controller.signal });
    return contentTypeToEmbedType(response.headers.get('content-type') ?? '');
  } catch {
    // HEAD blocked (CORS/proxy), unsupported by the server, or timed out on a
    // slow link — none of these are fatal, we just fall back to heuristics.
    return null;
  } finally {
    clearTimeout(timer);
  }
}

function contentTypeToEmbedType(contentType: string): EmbedContentType | null {
  const ct = contentType.toLowerCase();
  if (ct.includes('html')) return 'html';
  if (ct.includes('javascript') || ct.includes('ecmascript')) return 'js';
  return null;
}

function heuristicTypeFromUrl(src: string): EmbedContentType {
  const path = (src.split(/[?#]/)[0] ?? '').toLowerCase();
  if (/\.m?js$/.test(path)) return 'js';
  // Includes the ".html"/".htm" case and every ambiguous/unknown extension —
  // sandboxing in an iframe is the safe default; importing unknown code as a
  // JS module would execute it in the host page with no isolation.
  return 'html';
}
