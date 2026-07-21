import { describe, it, expect, vi } from 'vitest';
import { detectEmbedType } from '../../src/embed/detectType.js';

function fakeFetch(contentType: string | null, opts: { rejects?: boolean; hangs?: boolean } = {}): typeof fetch {
  return vi.fn((async (_url: string, init?: RequestInit) => {
    if (opts.hangs) {
      return new Promise<Response>((_resolve, reject) => {
        init?.signal?.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError')));
      });
    }
    if (opts.rejects) throw new TypeError('Failed to fetch');
    return {
      headers: { get: () => contentType },
    } as unknown as Response;
  }) as typeof fetch);
}

describe('detectEmbedType', () => {
  it('detects html from a Content-Type header', async () => {
    const type = await detectEmbedType('https://example.com/x', { fetchImpl: fakeFetch('text/html; charset=utf-8') });
    expect(type).toBe('html');
  });

  it('detects js from a javascript Content-Type header', async () => {
    const type = await detectEmbedType('https://example.com/x', { fetchImpl: fakeFetch('application/javascript') });
    expect(type).toBe('js');
  });

  it('detects js from an ecmascript Content-Type header', async () => {
    const type = await detectEmbedType('https://example.com/x', { fetchImpl: fakeFetch('text/ecmascript') });
    expect(type).toBe('js');
  });

  it('falls back to the .js extension heuristic when HEAD fails', async () => {
    const type = await detectEmbedType('https://example.com/widget.js', { fetchImpl: fakeFetch(null, { rejects: true }) });
    expect(type).toBe('js');
  });

  it('falls back to html for an unrecognised Content-Type', async () => {
    const type = await detectEmbedType('https://example.com/x', { fetchImpl: fakeFetch('application/octet-stream') });
    expect(type).toBe('html');
  });

  it('falls back to html for an ambiguous URL with no extension', async () => {
    const type = await detectEmbedType('https://example.com/widgets/42', { fetchImpl: fakeFetch(null, { rejects: true }) });
    expect(type).toBe('html');
  });

  it('ignores query/hash when applying the extension heuristic', async () => {
    const type = await detectEmbedType('https://example.com/widget.js?v=2#frag', {
      fetchImpl: fakeFetch(null, { rejects: true }),
    });
    expect(type).toBe('js');
  });

  it('aborts the HEAD probe after timeoutMs and falls back to heuristics', async () => {
    const type = await detectEmbedType('https://example.com/widget.js', {
      timeoutMs: 10,
      fetchImpl: fakeFetch(null, { hangs: true }),
    });
    expect(type).toBe('js');
  });
});
