import { vi, type Mock } from 'vitest';
import { DynamiaClient } from '@dynamia-tools/sdk';
import type { ViewDescriptor } from '@dynamia-tools/sdk';

export function dashboardDescriptor(fields: Array<[string, Record<string, unknown>]>): ViewDescriptor {
  return {
    id: 'main',
    beanClass: '',
    view: 'dashboard',
    params: {},
    layout: { params: { columns: 2 } },
    fields: fields.map(([name, params]) => ({ name, params })),
  };
}

type Route = unknown | ((url: string) => { status?: number; body: unknown });

/** A DynamiaClient whose fetch answers by URL suffix; `routes` maps a URL fragment to a body or a function. */
export function makeClient(routes: Record<string, Route>): { client: DynamiaClient; fetchMock: Mock<typeof fetch> } {
  const fetchMock = vi.fn(async (input: RequestInfo | URL) => {
    const url = String(input);
    const key = Object.keys(routes).find((k) => url.includes(k));
    const route = key ? routes[key] : { status: 404, body: { message: `no route for ${url}` } };
    const result =
      typeof route === 'function' ? (route as (u: string) => { status?: number; body: unknown })(url) : { body: route };
    const status = result.status ?? 200;
    return {
      ok: status >= 200 && status < 300,
      status,
      statusText: status === 200 ? 'OK' : 'Error',
      headers: { get: () => 'application/json' },
      json: () => Promise.resolve(result.body),
      text: () => Promise.resolve(JSON.stringify(result.body)),
    } as unknown as Response;
  }) as unknown as Mock<typeof fetch>;
  return { client: new DynamiaClient({ baseUrl: 'https://app.example.com', fetch: fetchMock }), fetchMock };
}

export function widgetResponse(field: string, type: string, data: unknown, extra: Record<string, unknown> = {}) {
  return {
    field,
    widget: `${field}-widget`,
    type,
    title: field,
    titleVisible: false,
    editable: false,
    closable: false,
    maximizable: false,
    data,
    ...extra,
  };
}
