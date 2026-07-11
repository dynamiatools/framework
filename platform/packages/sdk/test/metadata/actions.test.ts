import { describe, it, expect } from 'vitest';
import { mockFetch, makeClient } from '../helpers.js';

describe('ActionsApi', () => {
  it('executeGlobal() calls POST /api/app/metadata/actions/{action}', async () => {
    const fetchMock = mockFetch(200, { data: null, status: 'SUCCESS', statusCode: 200 });
    const client = makeClient(fetchMock);
    await client.actions.executeGlobal('sendEmail', { params: { to: 'a@b.com' } });
    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(url).toContain('/api/app/metadata/actions/sendEmail');
    expect(init.method).toBe('POST');
  });

  it('execute() uses the global endpoint for plain Action metadata', async () => {
    const fetchMock = mockFetch(200, { data: null, status: 'SUCCESS', statusCode: 200 });
    const client = makeClient(fetchMock);

    await client.actions.execute({
      id: 'ExportDataAction',
      name: 'Export',
      type: 'Action',
    });

    const [url] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(url).toContain('/api/app/metadata/actions/ExportDataAction');
  });

  it('execute() uses the entity endpoint for CrudAction metadata', async () => {
    const fetchMock = mockFetch(200, { data: null, status: 'SUCCESS', statusCode: 200 });
    const client = makeClient(fetchMock);

    await client.actions.execute(
      {
        id: 'SaveAction',
        name: 'Save',
        type: 'CrudAction',
        applicableClasses: ['mybookstore.domain.Book'],
        applicableStates: ['CREATE', 'UPDATE'],
      },
      { dataType: 'mybookstore.domain.Book', data: { id: 1, name: 'Clean Code' } },
    );

    const [url] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(url).toContain('/api/app/metadata/entities/mybookstore.domain.Book/action/SaveAction');
  });

  it('execute() throws when entity-scoped metadata has no resolvable class name', async () => {
    const fetchMock = mockFetch(200, { data: null, status: 'SUCCESS', statusCode: 200 });
    const client = makeClient(fetchMock);

    await expect(async () => {
      await client.actions.execute({
        id: 'DeleteAction',
        name: 'Delete',
        type: 'CrudAction',
        applicableClasses: ['mybookstore.domain.Book', 'mybookstore.domain.Author'],
        applicableStates: ['READ'],
      });
    }).rejects.toThrow(/requires an entity class name/i);
  });
});
