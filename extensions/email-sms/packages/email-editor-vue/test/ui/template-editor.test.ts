// @vitest-environment happy-dom
import { flushPromises, mount } from '@vue/test-utils';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  createBlock, EmailApiError, EmailTemplateEditor, EmailTemplatesClient, emptyDesign, insert, serializeDesign, setMjmlLoader,
  type EmailTemplatesApi, type TemplateData,
} from '../../src/index.js';

beforeEach(() => {
  (document as unknown as { execCommand: () => boolean }).execCommand = () => true;
  // the real MJML is exercised in compiler.test.ts; here a stand-in keeps these tests fast
  setMjmlLoader(async () => (mjml: string) => ({ html: `<!doctype html><html><body>COMPILED ${mjml.includes('${name}') ? 'Hi ${name} {{#if paid}}PAID{{/if}}' : ''}</body></html>`, errors: [] }));
});

const design = insert(emptyDesign(), { parentId: null, slot: 'children', index: 0 }, createBlock('text', { html: '<p>${name}</p>' }));

function template(overrides: Partial<TemplateData> = {}): TemplateData {
  return { id: 7, name: 'welcome', subject: 'Hi ${name}', engine: 'DYNAMIA', escapeValues: true, design: serializeDesign(design), content: '<p/>', hasBackup: false, parentId: null, ...overrides };
}

function api(overrides: Partial<EmailTemplatesApi> = {}): EmailTemplatesApi & { saveDesign: ReturnType<typeof vi.fn> } {
  return {
    get: vi.fn(async () => template()),
    saveDesign: vi.fn(async (_id: number, body: { subject?: string }) => template({ subject: body.subject ?? '' })),
    variables: vi.fn(async () => [{ name: 'name', label: 'Customer', sample: 'Ada' }, { name: 'paid', sample: true }]),
    testSend: vi.fn(async () => ({ sent: true, message: null })),
    migrate: vi.fn(async () => ({ alreadyMigrated: false, portable: true, applied: false, converted: {}, issues: [], warnings: ['careful'] })),
    revert: vi.fn(async () => ({ reverted: true, designEdited: false, reason: null })),
    ...overrides,
  } as never;
}

async function mountEditor(a: EmailTemplatesApi, props: Record<string, unknown> = {}) {
  const wrapper = mount(EmailTemplateEditor, { props: { api: a, templateId: 7, ...props }, attachTo: document.body });
  await flushPromises();
  return wrapper;
}

describe('EmailTemplateEditor', () => {
  it('loads the template and its variables into the editor', async () => {
    const a = api();
    const wrapper = await mountEditor(a);
    expect(a.get).toHaveBeenCalledWith(7);
    expect(wrapper.find('[data-type="text"]').exists()).toBe(true);
    expect(wrapper.findAll('[data-variable]')).toHaveLength(2);
    expect((wrapper.get('[data-subject]').element as HTMLInputElement).value).toBe('Hi ${name}');
    expect(wrapper.get('[data-action="save"]').attributes('disabled')).toBeDefined();
    wrapper.unmount();
  });

  it('saves the design with its compiled html and the subject', async () => {
    const a = api();
    const wrapper = await mountEditor(a);
    await wrapper.get('[data-palette="spacer"]').trigger('click');
    expect(wrapper.find('[data-dirty]').exists()).toBe(true);
    const subject = wrapper.get('[data-subject]');
    await subject.setValue('New subject');
    await wrapper.get('[data-action="save"]').trigger('click');
    await flushPromises();
    expect(a.saveDesign).toHaveBeenCalledTimes(1);
    const [id, body] = a.saveDesign.mock.calls[0] as [number, { design: string; content: string; subject: string }];
    expect(id).toBe(7);
    expect(JSON.parse(body.design).blocks.map((b: { type: string }) => b.type)).toEqual(['text', 'spacer']);
    expect(body.content).toContain('Hi ${name} {{#if paid}}PAID{{/if}}');
    expect(body.subject).toBe('New subject');
    expect(wrapper.find('[data-dirty]').exists()).toBe(false);
    expect(wrapper.get('[data-message]').text()).toContain('Saved');
    expect(wrapper.emitted('saved')).toHaveLength(1);
    wrapper.unmount();
  });

  it('shows the error when saving fails and stays dirty', async () => {
    const a = api({ saveDesign: vi.fn(async () => { throw new EmailApiError(413, 'TOO_LARGE', 'The template is too large'); }) } as never);
    const wrapper = await mountEditor(a);
    await wrapper.get('[data-palette="spacer"]').trigger('click');
    await wrapper.get('[data-action="save"]').trigger('click');
    await flushPromises();
    expect(wrapper.get('[data-message]').text()).toBe('The template is too large');
    expect(wrapper.get('[data-message]').classes()).toContain('error');
    expect(wrapper.find('[data-dirty]').exists()).toBe(true);
    wrapper.unmount();
  });

  it('sends a test after saving pending changes', async () => {
    const a = api();
    const wrapper = await mountEditor(a);
    await wrapper.get('[data-palette="spacer"]').trigger('click');
    await wrapper.get('[data-action="send-test"]').trigger('click');
    await flushPromises();
    expect(a.saveDesign).toHaveBeenCalledTimes(1);
    expect(a.testSend).toHaveBeenCalledWith(7, {});
    expect(wrapper.get('[data-message]').text()).toContain('Test email sent');
    wrapper.unmount();
  });

  it('reports a test that was not sent', async () => {
    const a = api({ testSend: vi.fn(async () => ({ sent: false, message: 'no account' })) } as never);
    const wrapper = await mountEditor(a);
    await wrapper.get('[data-action="send-test"]').trigger('click');
    await flushPromises();
    expect(a.saveDesign).not.toHaveBeenCalled();
    expect(wrapper.get('[data-message]').text()).toBe('no account');
    wrapper.unmount();
  });

  it('previews with the sample data in a frame that cannot run scripts, on desktop and mobile', async () => {
    const wrapper = await mountEditor(api());
    await wrapper.get('[data-action="preview"]').trigger('click');
    await flushPromises();
    const frame = wrapper.get('[data-frame]');
    expect(frame.attributes('sandbox')).toBe('');
    expect(frame.attributes('srcdoc')).toContain('Hi Ada PAID');
    expect(frame.attributes('srcdoc')).not.toContain('{{');
    expect(wrapper.get('[data-preview] [data-subject]').text()).toBe('Hi Ada');
    expect(frame.attributes('style')).toContain('width: 640px');
    await wrapper.get('[data-device="mobile"]').trigger('click');
    expect(wrapper.get('[data-frame]').attributes('style')).toContain('width: 375px');
    await wrapper.get('[data-action="close-preview"]').trigger('click');
    expect(wrapper.find('[data-preview]').exists()).toBe(false);
    wrapper.unmount();
  });

  it('lets the sample data be overridden with JSON and tells when it is not valid', async () => {
    const wrapper = await mountEditor(api());
    await wrapper.get('[data-action="preview"]').trigger('click');
    await flushPromises();
    const box = wrapper.get('[data-preview] textarea');
    await box.setValue('{ "name": "Grace", "paid": false }');
    await flushPromises();
    expect(wrapper.get('[data-frame]').attributes('srcdoc')).toContain('Hi Grace');
    expect(wrapper.get('[data-frame]').attributes('srcdoc')).not.toContain('PAID');
    await box.setValue('{ nope');
    await flushPromises();
    expect(wrapper.get('[data-preview] [role="alert"]').text()).toContain('not valid JSON');
    wrapper.unmount();
  });

  it('escapes the sample data in the preview when the template escapes values', async () => {
    const a = api({ variables: vi.fn(async () => [{ name: 'name', sample: '<b>Ada</b>' }]) } as never);
    const wrapper = await mountEditor(a);
    await wrapper.get('[data-action="preview"]').trigger('click');
    await flushPromises();
    expect(wrapper.get('[data-frame]').attributes('srcdoc')).toContain('Hi &lt;b&gt;Ada&lt;/b&gt;');
    wrapper.unmount();
  });

  it('offers the migration for a Velocity template, with the report', async () => {
    const a = api({ get: vi.fn(async () => template({ engine: 'VELOCITY', design: null })) } as never);
    const wrapper = await mountEditor(a);
    expect(wrapper.find('[data-velocity]').exists()).toBe(true);
    expect(wrapper.find('[data-palette]').exists()).toBe(false);
    await wrapper.get('[data-action="check-migration"]').trigger('click');
    await flushPromises();
    expect(a.migrate).toHaveBeenCalledWith(7, true);
    expect(wrapper.get('[data-report]').text()).toContain('can be migrated');
    expect(wrapper.get('[data-report]').text()).toContain('careful');
  });

  it('migrates and then shows the editor', async () => {
    let migrated = false;
    const a = api({
      get: vi.fn(async () => (migrated ? template({ hasBackup: true }) : template({ engine: 'VELOCITY', design: null }))),
      migrate: vi.fn(async () => { migrated = true; return { alreadyMigrated: false, portable: true, applied: true, converted: {}, issues: [], warnings: [] }; }),
    } as never);
    const wrapper = await mountEditor(a);
    await wrapper.get('[data-action="migrate"]').trigger('click');
    await flushPromises();
    expect(a.migrate).toHaveBeenCalledWith(7, false);
    expect(wrapper.find('[data-palette]').exists()).toBe(true);
    expect(wrapper.find('[data-action="revert"]').exists()).toBe(true);
    wrapper.unmount();
  });

  it('lists what blocks a migration and does not allow it', async () => {
    const a = api({
      get: vi.fn(async () => template({ engine: 'VELOCITY', design: null })),
      migrate: vi.fn(async () => ({ alreadyMigrated: false, portable: false, applied: false, converted: {}, issues: [{ field: 'content', kind: 'SET', line: 3, snippet: '#set($x = 1)', reason: '#set has no equivalent' }], warnings: [] })),
    } as never);
    const wrapper = await mountEditor(a);
    await wrapper.get('[data-action="check-migration"]').trigger('click');
    await flushPromises();
    expect(wrapper.get('[data-report]').text()).toContain('#set($x = 1)');
    expect(wrapper.get('[data-action="migrate"]').attributes('disabled')).toBeDefined();
  });

  it('asks before reverting a migration whose design was edited', async () => {
    const revert = vi.fn(async (_id: number, confirm: boolean) => (confirm ? { reverted: true, designEdited: true, reason: null } : { reverted: false, designEdited: true, reason: 'edited' }));
    let reverted = false;
    const a = api({ get: vi.fn(async () => (reverted ? template({ engine: 'VELOCITY', design: null }) : template({ hasBackup: true }))), revert: vi.fn(async (id: number, c: boolean) => { const r = await revert(id, c); reverted = r.reverted; return r; }) } as never);
    const wrapper = await mountEditor(a);
    await wrapper.get('[data-action="revert"]').trigger('click');
    await flushPromises();
    expect(wrapper.find('[data-revert-warning]').exists()).toBe(true);
    expect(a.revert).toHaveBeenLastCalledWith(7, false);
    await wrapper.get('[data-action="confirm-revert"]').trigger('click');
    await flushPromises();
    expect(a.revert).toHaveBeenLastCalledWith(7, true);
    expect(wrapper.find('[data-velocity]').exists()).toBe(true);
  });

  it('shows why the template could not be loaded', async () => {
    const a = api({ get: vi.fn(async () => { throw new EmailApiError(404, 'NOT_FOUND', 'Template not found'); }) } as never);
    const wrapper = await mountEditor(a);
    expect(wrapper.get('[data-load-error]').text()).toBe('Template not found');
  });

  it('a corrupt design is reported instead of breaking the page', async () => {
    const a = api({ get: vi.fn(async () => template({ design: '{"version":9,"blocks":[]}' })) } as never);
    const wrapper = await mountEditor(a);
    expect(wrapper.get('[data-load-error]').text()).toContain('version 9');
  });

  it('starts from an empty design when the template has none', async () => {
    const a = api({ get: vi.fn(async () => template({ design: null })) } as never);
    const wrapper = await mountEditor(a);
    expect(wrapper.text()).toContain('Drag a block here');
    wrapper.unmount();
  });
});

describe('EmailTemplatesClient', () => {
  function client(response: { status?: number; body?: unknown }) {
    const fetchMock = vi.fn(async (_url: string, _init?: RequestInit) => new Response(response.body === undefined ? '' : JSON.stringify(response.body), { status: response.status ?? 200 }));
    return { fetchMock, client: new EmailTemplatesClient({ baseUrl: 'https://erp.example.com/', headers: { 'X-CSRF': 'abc' }, fetch: fetchMock as never }) };
  }

  it('calls the endpoints with the right method, path and body', async () => {
    const { client: c, fetchMock } = client({ body: { id: 7 } });
    await c.get(7);
    await c.saveDesign(7, { design: '{}', content: '<p/>' });
    await c.variables('sales');
    await c.testSend(7, { source: 's' });
    await c.migrate(7, true);
    await c.revert(7, false);
    await c.create('x');
    const calls = fetchMock.mock.calls.map(([url, init]) => `${(init as RequestInit).method} ${url}`);
    expect(calls).toEqual([
      'GET https://erp.example.com/api/email/templates/7',
      'PUT https://erp.example.com/api/email/templates/7/design',
      'GET https://erp.example.com/api/email/templates/variables?source=sales',
      'POST https://erp.example.com/api/email/templates/7/test-send',
      'POST https://erp.example.com/api/email/templates/7/migrate?dryRun=true',
      'POST https://erp.example.com/api/email/templates/7/revert?confirm=false',
      'POST https://erp.example.com/api/email/templates',
    ]);
    const put = fetchMock.mock.calls[1]![1] as RequestInit;
    expect(JSON.parse(put.body as string)).toEqual({ design: '{}', content: '<p/>' });
    expect((put.headers as Record<string, string>)['X-CSRF']).toBe('abc');
    expect((put.headers as Record<string, string>)['X-Requested-With']).toBe('dynamia-email-editor');
    expect((put.headers as Record<string, string>)['Content-Type']).toBe('application/json');
    expect(put.credentials).toBe('same-origin');
  });

  it('turns an error answer into an EmailApiError', async () => {
    const { client: c } = client({ status: 403, body: { status: 403, code: 'ACCESS_DENIED', message: 'You cannot edit email templates' } });
    await expect(c.get(1)).rejects.toMatchObject({ name: 'EmailApiError', status: 403, code: 'ACCESS_DENIED', message: 'You cannot edit email templates' });
    const { client: bad } = client({ status: 500 });
    await expect(bad.get(1)).rejects.toMatchObject({ status: 500, code: 'ERROR' });
  });
});
