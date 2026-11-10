// @vitest-environment happy-dom
import { flushPromises } from '@vue/test-utils';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { createDemoApi, mountEmailEditor, registerEmailEditorElement, sampleDesign, setMjmlLoader } from '../../src/index.js';

beforeEach(() => {
  (document as unknown as { execCommand: () => boolean }).execCommand = () => true;
  setMjmlLoader(async () => () => ({ html: '<html><body>X</body></html>', errors: [] }));
});
afterEach(() => {
  document.body.innerHTML = '';
});

describe('mountEmailEditor', () => {
  it('mounts the editor in an element, saves through the handle and unmounts', async () => {
    const target = document.createElement('div');
    document.body.append(target);
    const saved = vi.fn();
    const mounted = mountEmailEditor(target, { api: createDemoApi(), templateId: 1, locale: 'es', onSaved: saved });
    await vi.waitFor(() => expect(target.querySelector('[data-palette]')).not.toBeNull());
    expect(target.textContent).toContain('Bloques');
    (target.querySelector('[data-palette="spacer"]') as HTMLElement).click();
    await flushPromises();
    expect(await mounted.save()).toBe(true);
    expect(saved).toHaveBeenCalledTimes(1);
    mounted.unmount();
    expect(target.innerHTML).toBe('');
  });

  it('shows the migration for a Velocity demo template', async () => {
    const target = document.createElement('div');
    document.body.append(target);
    const mounted = mountEmailEditor(target, { api: createDemoApi({ velocity: true }), templateId: 1 });
    await vi.waitFor(() => expect(target.querySelector('[data-velocity]')).not.toBeNull());
    mounted.unmount();
  });
});

describe('<dynamia-email-editor>', () => {
  it('is defined once and mounts when it has a template id', async () => {
    registerEmailEditorElement();
    registerEmailEditorElement();
    expect(customElements.get('dynamia-email-editor')).toBeDefined();
    const element = document.createElement('dynamia-email-editor');
    document.body.append(element);
    expect(element.children.length).toBe(0); // no template-id: nothing to edit
    element.remove();
  });

  it('mounts and unmounts with the tag and reacts to its attributes', async () => {
    registerEmailEditorElement();
    const template = { id: 7, name: 'x', subject: 's', engine: 'DYNAMIA', escapeValues: true, design: null, content: '', hasBackup: false, parentId: null };
    const fetchMock = vi.fn(async (url: string) => new Response(JSON.stringify(url.endsWith('/variables') ? [] : template), { status: 200 }));
    vi.stubGlobal('fetch', fetchMock);
    const element = document.createElement('dynamia-email-editor');
    element.setAttribute('template-id', '7');
    element.setAttribute('base-url', 'https://erp.example.com/');
    document.body.append(element);
    await vi.waitFor(() => expect(element.querySelector('[data-palette]')).not.toBeNull());
    expect(fetchMock.mock.calls.map((c) => String(c[0]))).toContain('https://erp.example.com/api/email/templates/7');
    element.setAttribute('template-id', '8');
    await vi.waitFor(() => expect(fetchMock.mock.calls.map((c) => String(c[0]))).toContain('https://erp.example.com/api/email/templates/8'));
    element.remove();
    expect(element.innerHTML).toBe('');
    vi.unstubAllGlobals();
  });

  it('the sample design has every kind of block', () => {
    const types = new Set<string>();
    const walk = (blocks: { type: string; children?: unknown[]; elseChildren?: unknown[] }[]): void => blocks.forEach((b) => { types.add(b.type); walk((b.children ?? []) as never); walk((b.elseChildren ?? []) as never); });
    walk(sampleDesign().blocks);
    expect([...types].sort()).toEqual(['button', 'column', 'columns', 'condition', 'divider', 'footer', 'header', 'image', 'repeat', 'text']);
  });
});
