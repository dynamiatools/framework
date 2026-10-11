import { describe, expect, it, vi } from 'vitest';
import type { DynamiaClient, NavigationNode, ViewDescriptor } from '@dynamia-tools/sdk';
import { useConfigPage } from './useConfigPage.js';

const node: NavigationNode = { id: 'entityFile', name: 'Entity Files', type: 'ConfigPage', internalPath: 'system/config/entityFile' };

const descriptor = {
  id: 'DemoCFG',
  view: 'config',
  fields: [
    { name: 'location', label: 'Location', component: 'directorybox' },
    { name: 'useHttps', label: 'Use HTTPS', component: 'checkbox' },
  ],
} as unknown as ViewDescriptor;

function fakeClient() {
  const read = vi.fn().mockResolvedValue({ descriptor: 'DemoCFG', data: { location: '/data', useHttps: true } });
  const save = vi.fn().mockImplementation(async (values) => ({ descriptor: 'DemoCFG', data: values }));
  const config = vi.fn().mockReturnValue({ read, save });
  const getView = vi.fn().mockResolvedValue(descriptor);
  const client = { config, metadata: { getView } } as unknown as DynamiaClient;
  return { client, read, save, config, getView };
}

const settled = () => new Promise((resolve) => setTimeout(resolve, 0));

describe('useConfigPage', () => {
  it('loads the values and the descriptor named by the server into a form', async () => {
    const { client, config, getView } = fakeClient();
    const page = useConfigPage({ node, client });
    await settled();

    expect(config).toHaveBeenCalledWith('system/config/entityFile');
    expect(getView).toHaveBeenCalledWith('DemoCFG');
    expect(page.error.value).toBeNull();
    expect(page.view.value?.values.value).toEqual({ location: '/data', useHttps: true });
    // a field with a component the frontend does not know is still a field of the form (rendered as text)
    expect(page.view.value?.resolvedFields.value.map((f) => f.name)).toEqual(['location', 'useHttps']);
  });

  it('saves the edited values and keeps what the server answers', async () => {
    const { client, save } = fakeClient();
    const page = useConfigPage({ node, client });
    await settled();

    page.view.value!.setFieldValue('location', '/srv');
    expect(await page.save()).toBe(true);

    expect(save).toHaveBeenCalledWith({ location: '/srv', useHttps: true });
    expect(page.view.value?.values.value['location']).toBe('/srv');
  });

  it('reports a failed load as error', async () => {
    const { client, read } = fakeClient();
    read.mockRejectedValue(new Error('403 Forbidden'));
    const page = useConfigPage({ node, client });
    await settled();

    expect(page.error.value).toBe('403 Forbidden');
    expect(page.view.value).toBeNull();
  });
});
