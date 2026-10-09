import { describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import type { NavigationNode } from '@dynamia-tools/sdk';
import { DynamiaReportDesigner, DataSourceTest, DefinitionTransfer, QueryPreview, useReportDesigner } from '../src/index.js';
import { bodyOf, callsTo, makeClient } from './helpers.js';

const allowed = { allowed: true, previewLimit: 50 };
const preview = { columns: ['id', 'name'], rows: [{ id: 1, name: 'Ana' }], truncated: true, durationMs: 3 };

function routes(extra: Record<string, unknown> = {}) {
  return makeClient({
    '/api/reports/v2/design/info': allowed,
    '/api/reports/v2/design/preview': preview,
    '/api/reports/v2/design/import': { id: 42 },
    '/api/reports/v2/design/5/definition': { name: 'Sales', queryScript: 'select 1' },
    '/api/reports/v2/design/datasources/3/test': { ok: true, message: 'Connection ok' },
    ...extra,
  });
}

const crudNode: NavigationNode = { id: 'design', name: 'Reports Design', type: 'CrudPage', internalPath: 'reports/design' };

describe('useReportDesigner', () => {
  it('loads what the user can do', async () => {
    const { client } = routes();
    const designer = useReportDesigner(client);
    await flushPromises();
    expect(designer.info.value).toEqual(allowed);
  });

  it('treats a failing info call as not allowed and keeps the error', async () => {
    const { client } = routes({ '/api/reports/v2/design/info': () => ({ status: 403, body: { message: 'Access denied to this report' } }) });
    const designer = useReportDesigner(client);
    await flushPromises();
    expect(designer.info.value).toEqual({ allowed: false, previewLimit: 0 });
    expect(designer.error.value).toBe('Access denied to this report');
  });

  it('previews a query and keeps the result', async () => {
    const { client, fetchMock } = routes();
    const designer = useReportDesigner(client);
    const result = await designer.preview({ queryLang: 'sql', queryScript: 'select * from t', dataSourceId: 2 });

    expect(result).toEqual(preview);
    expect(designer.previewResult.value).toEqual(preview);
    expect(bodyOf(fetchMock, '/preview')).toEqual({ queryLang: 'sql', queryScript: 'select * from t', dataSourceId: 2 });
  });

  it('keeps the error of a rejected query and clears the previous preview', async () => {
    const { client } = routes();
    const designer = useReportDesigner(client);
    await designer.preview({ queryLang: 'sql', queryScript: 'select 1' });

    const failing = routes({ '/api/reports/v2/design/preview': () => ({ status: 400, body: { message: 'Keyword not allowed in reports: DROP' } }) });
    const second = useReportDesigner(failing.client);
    expect(await second.preview({ queryLang: 'sql', queryScript: 'drop table t' })).toBeUndefined();
    expect(second.error.value).toContain('DROP');
    expect(second.previewResult.value).toBeNull();
    expect(second.busy.value).toBe(false);
  });

  it('downloads a definition as a JSON file', async () => {
    const { client } = routes();
    const download = vi.fn();
    const designer = useReportDesigner(client, { download });
    await designer.exportDefinition(5, 'Sales report');

    const [blob, name] = download.mock.calls[0] as [Blob, string];
    expect(name).toMatch(/^sales-report-\d{4}-\d{2}-\d{2}\.json$/);
    expect(JSON.parse(await blob.text())).toEqual({ name: 'Sales', queryScript: 'select 1' });
  });

  it('imports a definition file and returns the new id', async () => {
    const { client, fetchMock } = routes();
    const designer = useReportDesigner(client);
    const id = await designer.importDefinition(new Blob(['{"name":"x"}']));

    expect(id).toBe(42);
    expect(bodyOf(fetchMock, '/design/import')).toEqual({ name: 'x' });
  });

  it('rejects a file that is not JSON without calling the server', async () => {
    const { client, fetchMock } = routes();
    const designer = useReportDesigner(client);
    expect(await designer.importDefinition(new Blob(['not json']))).toBeUndefined();

    expect(designer.error.value).toContain('not a valid JSON');
    expect(callsTo(fetchMock, '/design/import')).toHaveLength(0);
  });

  it('tests a datasource', async () => {
    const { client } = routes();
    const designer = useReportDesigner(client);
    expect(await designer.testDataSource(3)).toEqual({ ok: true, message: 'Connection ok' });
  });
});

describe('designer components', () => {
  function designer() {
    const { client, fetchMock } = routes();
    return { instance: useReportDesigner(client, { download: vi.fn(), immediate: false }), fetchMock, client };
  }

  it('QueryPreview sends the query, datasource and parameters and shows the rows', async () => {
    const { instance, fetchMock } = designer();
    const wrapper = mount(QueryPreview, { props: { designer: instance } });

    await wrapper.get('select').setValue('jpql');
    const [query, params] = wrapper.findAll('textarea');
    await query!.setValue('select c from Customer c where c.age > :min');
    await wrapper.get('input[type="number"]').setValue('2');
    await params!.setValue('{"min": 18}');
    await wrapper.get('button').trigger('click');
    await flushPromises();

    expect(bodyOf(fetchMock, '/preview')).toEqual({
      queryLang: 'jpql',
      queryScript: 'select c from Customer c where c.age > :min',
      dataSourceId: 2,
      parameters: { min: 18 },
    });
    expect(wrapper.findAll('tbody tr')).toHaveLength(1);
    expect(wrapper.text()).toContain('Ana');
    expect(wrapper.text()).toContain('more rows than the preview shows');
  });

  it('QueryPreview does not call the server with invalid parameters or an empty query', async () => {
    const { instance, fetchMock } = designer();
    const wrapper = mount(QueryPreview, { props: { designer: instance } });
    expect(wrapper.get('button').attributes('disabled')).toBeDefined();

    const [query, params] = wrapper.findAll('textarea');
    await query!.setValue('select 1');
    await params!.setValue('{oops');
    await wrapper.get('button').trigger('click');

    expect(wrapper.get('[role="alert"]').text()).toContain('invalid JSON');
    expect(callsTo(fetchMock, '/preview')).toHaveLength(0);
  });

  it('DefinitionTransfer exports by id and imports a file, emitting the new id', async () => {
    const { instance, fetchMock } = designer();
    const wrapper = mount(DefinitionTransfer, { props: { designer: instance } });

    await wrapper.get('input[type="number"]').setValue('5');
    await wrapper.findAll('button')[0]!.trigger('click');
    await flushPromises();
    expect(callsTo(fetchMock, '/design/5/definition')).toHaveLength(1);

    const input = wrapper.get('input[type="file"]');
    const file = new File(['{"name":"x"}'], 'report.json', { type: 'application/json' });
    Object.defineProperty(input.element, 'files', { value: [file], configurable: true });
    await input.trigger('change');
    await flushPromises();

    expect(wrapper.emitted('imported')![0]).toEqual([42]);
    expect(wrapper.get('.dynamia-report-imported').text()).toContain('42');
  });

  it('DataSourceTest shows the result of the connection test', async () => {
    const { instance } = designer();
    const wrapper = mount(DataSourceTest, { props: { designer: instance } });

    await wrapper.get('input').setValue('3');
    await wrapper.get('button').trigger('click');
    await flushPromises();

    expect(wrapper.get('.dynamia-report-ok').text()).toBe('Connection ok');
  });

  it('ReportDesigner shows the CRUD page and the tools in tabs', async () => {
    const { client } = routes();
    const crud = { props: ['node', 'client'], template: '<div class="crud-stub">{{ node.internalPath }}</div>' };
    const wrapper = mount(DynamiaReportDesigner, {
      props: { client, node: crudNode },
      global: { stubs: { CrudPage: crud } },
    });
    await flushPromises();

    expect(wrapper.get('.crud-stub').text()).toBe('reports/design');
    const tabs = wrapper.findAll('[role="tab"]');
    expect(tabs.map((t) => t.text())).toEqual(['Reports', 'Tools']);

    await tabs[1]!.trigger('click');
    expect(wrapper.find('.crud-stub').exists()).toBe(false);
    expect(wrapper.find('.dynamia-report-preview').exists()).toBe(true);
    expect(wrapper.find('.dynamia-report-transfer').exists()).toBe(true);
    expect(wrapper.find('.dynamia-report-datasource-test').exists()).toBe(true);
  });

  it('ReportDesigner shows only the tools without a node and hides them from non designers', async () => {
    const { client } = routes({ '/api/reports/v2/design/info': { allowed: false, previewLimit: 0 } });
    const wrapper = mount(DynamiaReportDesigner, { props: { client } });
    await flushPromises();

    expect(wrapper.findAll('[role="tab"]')).toHaveLength(1);
    expect(wrapper.get('[role="alert"]').text()).toContain('not allowed');
    expect(wrapper.find('.dynamia-report-preview').exists()).toBe(false);
  });
});
