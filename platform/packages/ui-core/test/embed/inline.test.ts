// @vitest-environment happy-dom
import { afterEach, beforeEach, describe, expect, it, vi, type Mock } from 'vitest';
import type { mountInline as MountInline } from '../../src/embed/inline.js';

// Fresh module per test: the per-document asset registry is module state.
let mountInline: typeof MountInline;

type ZkWin = Window & {
  zk?: { Desktop?: { all?: Record<string, unknown> }; mounting?: boolean };
  zAu?: { _rmDesktop?: (d: unknown, keep: boolean) => void };
  __order?: string[];
};

const win = window as unknown as ZkWin;

/** Fake ZK page: head assets + body markup + the inline bootstrap script that registers a Desktop. */
function zkPage(desktopId: string, extraHead = ''): string {
  return `<!DOCTYPE html><html><head>
    <script src="/zkau/web/aaa/js/zk.wpd"></script>
    <script src="/zkau/web/aaa/js/zkbind.wpd"></script>
    <link rel="stylesheet" href="/zkau/web/aaa/zk.wcs"/>
    ${extraHead}
  </head><body>
    <div id="w-${desktopId}" class="z-div">hello</div>
    <script class="z-runonce">var boot = [0, 'w', { dt: '${desktopId}' }]; window.zk.Desktop.all['${desktopId}'] = { id: '${desktopId}' };</script>
    <noscript><p>no js</p></noscript>
  </body></html>`;
}

function fakeFetch(pages: Record<string, string | number>): typeof fetch {
  return vi.fn(async (input: RequestInfo | URL) => {
    const path = new URL(String(input)).pathname;
    const page = pages[path];
    if (typeof page === 'number') return new Response('nope', { status: page });
    return new Response(page ?? '', { status: page === undefined ? 404 : 200 });
  }) as unknown as typeof fetch;
}

/** Intercepts head injection: records the element and fires `load` (happy-dom doesn't fetch scripts). */
function autoLoadHead(): { injected: string[]; restore: () => void } {
  const injected: string[] = [];
  const original = document.head.appendChild.bind(document.head);
  document.head.appendChild = ((node: Node) => {
    const el = node as HTMLElement;
    const url = el.getAttribute?.('src') ?? el.getAttribute?.('href');
    if (url && (el.tagName === 'SCRIPT' || el.tagName === 'LINK')) {
      injected.push(`${el.tagName.toLowerCase()}:${new URL(url, document.baseURI).pathname}`);
      queueMicrotask(() => el.dispatchEvent(new Event('load')));
      return node; // not really attached: keeps the DOM free of un-fetchable assets
    }
    return original(node);
  }) as typeof document.head.appendChild;
  return { injected, restore: () => { document.head.appendChild = original; } };
}

/** happy-dom does not evaluate inline scripts: run them when they are appended, like a browser would. */
function scriptRunningContainer(): HTMLElement {
  const el = document.createElement('div');
  const append = el.appendChild.bind(el);
  el.appendChild = ((node: Node) => {
    const result = append(node);
    if (node.nodeName === 'SCRIPT') new Function((node as HTMLScriptElement).textContent ?? '')();
    return result;
  }) as typeof el.appendChild;
  document.body.appendChild(el);
  return el;
}

describe('mountInline', () => {
  let container: HTMLElement;
  let head: ReturnType<typeof autoLoadHead>;
  let rmDesktop: Mock<(d: unknown, keep: boolean) => void>;

  beforeEach(async () => {
    vi.resetModules();
    ({ mountInline } = await import('../../src/embed/inline.js'));
    document.head.innerHTML = '';
    document.body.innerHTML = '';
    container = scriptRunningContainer();
    win.zk = { Desktop: { all: {} }, mounting: false };
    rmDesktop = vi.fn<(d: unknown, keep: boolean) => void>();
    win.zAu = { _rmDesktop: rmDesktop };
    head = autoLoadHead();
  });

  afterEach(() => {
    head.restore();
    delete win.zk;
    delete win.zAu;
  });

  it('injects head assets in order, appends the body and re-executes its script', async () => {
    const handle = await mountInline(container, '/books', { fetchImpl: fakeFetch({ '/books': zkPage('dt1') }) });

    expect(head.injected).toContain('link:/zkau/web/aaa/zk.wcs');
    expect(head.injected.filter(i => i.startsWith('script:'))).toEqual([
      'script:/zkau/web/aaa/js/zk.wpd',
      'script:/zkau/web/aaa/js/zkbind.wpd',
    ]);
    expect(container.querySelector('#w-dt1')).not.toBeNull();
    expect(container.textContent).not.toContain('no js'); // <noscript> skipped
    expect(handle.desktopIds).toEqual(['dt1']);
  });

  it('shares assets between mounts on the same document (no duplicate injection)', async () => {
    const fetchImpl = fakeFetch({ '/a': zkPage('dtA'), '/b': zkPage('dtB') });
    const other = scriptRunningContainer();

    await Promise.all([
      mountInline(container, '/a', { fetchImpl }),
      mountInline(other, '/b', { fetchImpl }),
    ]);

    expect(head.injected.filter(i => i === 'script:/zkau/web/aaa/js/zk.wpd')).toHaveLength(1);
    expect(head.injected.filter(i => i === 'link:/zkau/web/aaa/zk.wcs')).toHaveLength(1);
  });

  it("unescapes ZK's JS-escaped desktop id ('\\-' in the script source is '-' at runtime)", async () => {
    const html = zkPage('dt-x').replace("dt: 'dt-x'", "dt: 'dt\\-x'");
    const handle = await mountInline(container, '/esc', { fetchImpl: fakeFetch({ '/esc': html }) });
    expect(handle.desktopIds).toEqual(['dt-x']);
  });

  it('gives each mount its own desktop ids', async () => {
    const fetchImpl = fakeFetch({ '/a': zkPage('dtA'), '/b': zkPage('dtB') });
    const other = scriptRunningContainer();

    const a = await mountInline(container, '/a', { fetchImpl });
    const b = await mountInline(other, '/b', { fetchImpl });

    expect(a.desktopIds).toEqual(['dtA']);
    expect(b.desktopIds).toEqual(['dtB']);
  });

  it('attributes desktops correctly when mounts overlap (destroying one keeps the other alive)', async () => {
    const fetchImpl = fakeFetch({ '/a': zkPage('dtA'), '/b': zkPage('dtB') });
    const other = scriptRunningContainer();

    const [a, b] = await Promise.all([
      mountInline(container, '/a', { fetchImpl }),
      mountInline(other, '/b', { fetchImpl }),
    ]);
    a.destroy();

    expect(a.desktopIds).toEqual(['dtA']);
    expect(b.desktopIds).toEqual(['dtB']);
    expect(Object.keys(win.zk!.Desktop!.all!)).toEqual(['dtB']);
  });

  it('fails and cleans up when the announced desktop never registers', async () => {
    // Announces the desktop (dt: '...') but the bootstrap never registers it in zk.Desktop.all.
    const html = zkPage('dtNever').replace("window.zk.Desktop.all['dtNever'] = { id: 'dtNever' };", '');
    await expect(mountInline(container, '/n', { fetchImpl: fakeFetch({ '/n': html }), timeoutMs: 150 }))
      .rejects.toThrow(/Timed out waiting for ZK desktop/);
    expect(container.childNodes).toHaveLength(0);
  });

  it('destroy() releases only its own desktop and empties the container; second call is a no-op', async () => {
    const fetchImpl = fakeFetch({ '/a': zkPage('dtA'), '/b': zkPage('dtB') });
    const other = scriptRunningContainer();
    const a = await mountInline(container, '/a', { fetchImpl });
    await mountInline(other, '/b', { fetchImpl });

    a.destroy();
    a.destroy();

    expect(rmDesktop).toHaveBeenCalledTimes(1);
    expect(rmDesktop).toHaveBeenCalledWith({ id: 'dtA' }, false);
    expect(Object.keys(win.zk!.Desktop!.all!)).toEqual(['dtB']);
    expect(container.childNodes).toHaveLength(0);
    expect(other.childNodes.length).toBeGreaterThan(0);
  });

  it('detaches root pages before removing the desktop', async () => {
    const detach = vi.fn();
    const html = zkPage('dtP').replace(
      "{ id: 'dtP' }",
      `{ id: 'dtP', firstChild: { detach: window.__detach, nextSibling: { detach: window.__detach } } }`,
    );
    (win as unknown as { __detach: () => void }).__detach = detach;

    const handle = await mountInline(container, '/p', { fetchImpl: fakeFetch({ '/p': html }) });
    handle.destroy();

    expect(detach).toHaveBeenCalledTimes(2);
    expect(rmDesktop).toHaveBeenCalledTimes(1);
  });

  it('survives an unexpected ZK client (no zAu._rmDesktop)', async () => {
    win.zAu = {};
    const handle = await mountInline(container, '/a', { fetchImpl: fakeFetch({ '/a': zkPage('dtA') }) });
    expect(() => handle.destroy()).not.toThrow();
    expect(win.zk!.Desktop!.all).toEqual({});
  });

  it('treats a non-ZK response as a plain fragment with no desktops', async () => {
    const html = '<!DOCTYPE html><html><head></head><body><p id="plain">hi</p></body></html>';
    const handle = await mountInline(container, '/plain', { fetchImpl: fakeFetch({ '/plain': html }), timeoutMs: 100 });
    expect(handle.desktopIds).toEqual([]);
    expect(container.querySelector('#plain')).not.toBeNull();
  });

  it('rejects cross-origin URLs without fetching', async () => {
    const fetchImpl = fakeFetch({});
    await expect(mountInline(container, 'https://evil.example.com/x', { fetchImpl })).rejects.toThrow(/same-origin/);
    expect(fetchImpl).not.toHaveBeenCalled();
  });

  it('rejects when the response is not OK and leaves the container untouched', async () => {
    await expect(mountInline(container, '/missing', { fetchImpl: fakeFetch({ '/missing': 500 }) })).rejects.toThrow(/500/);
    expect(container.childNodes).toHaveLength(0);
  });

  it('rejects immediately when the signal is already aborted', async () => {
    const ctl = new AbortController();
    ctl.abort();
    await expect(mountInline(container, '/a', { fetchImpl: fakeFetch({ '/a': zkPage('dtA') }), signal: ctl.signal }))
      .rejects.toThrow(/abort/i);
    expect(container.childNodes).toHaveLength(0);
  });

  it('cleans up if aborted while waiting for the desktop', async () => {
    const ctl = new AbortController();
    const fetchImpl = fakeFetch({ '/a': zkPage('dtA') });
    win.zk!.mounting = true; // ZK still mounting: mountInline keeps polling
    const pending = mountInline(container, '/a', { fetchImpl, signal: ctl.signal });
    setTimeout(() => ctl.abort(), 50);
    await expect(pending).rejects.toThrow(/abort/i);
    expect(container.childNodes).toHaveLength(0);
  });
});
