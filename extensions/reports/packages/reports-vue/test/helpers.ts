import { vi, type Mock } from 'vitest';
import { DynamiaClient } from '@dynamia-tools/sdk';
import type {
  ReportColumn,
  ReportDefinition,
  ReportFilterDefinition,
  ReportRunResult,
} from '@dynamia-tools/reports-sdk';

type Reply = { status?: number; body?: unknown; blob?: Blob; contentType?: string };
type Route = unknown | ((url: string, init: RequestInit) => Reply);

/**
 * A DynamiaClient whose fetch answers by URL fragment. The first key contained in the URL wins, so list the most
 * specific fragments first. A route is a JSON body or a function returning `{ status, body, blob }`.
 */
export function makeClient(routes: Record<string, Route>): { client: DynamiaClient; fetchMock: Mock<typeof fetch> } {
  const fetchMock = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = String(input);
    const key = Object.keys(routes).find((k) => url.includes(k));
    const route: Route = key ? routes[key] : (): Reply => ({ status: 404, body: { message: `no route for ${url}` } });
    const reply: Reply = typeof route === 'function' ? (route as (u: string, i: RequestInit) => Reply)(url, init ?? {}) : { body: route };
    const status = reply.status ?? 200;
    const isBlob = reply.blob !== undefined;
    return {
      ok: status >= 200 && status < 300,
      status,
      statusText: status === 200 ? 'OK' : 'Error',
      headers: { get: (name: string) => (name.toLowerCase() === 'content-type' ? (isBlob ? reply.contentType ?? 'application/octet-stream' : 'application/json') : null) },
      json: () => Promise.resolve(reply.body),
      text: () => Promise.resolve(JSON.stringify(reply.body)),
      blob: () => Promise.resolve(reply.blob ?? new Blob()),
    } as unknown as Response;
  }) as unknown as Mock<typeof fetch>;
  return { client: new DynamiaClient({ baseUrl: 'https://app.example.com', fetch: fetchMock }), fetchMock };
}

/** The body sent in the n-th call that matches a URL fragment. */
export function bodyOf(fetchMock: Mock<typeof fetch>, fragment: string, index = 0): Record<string, unknown> {
  const calls = fetchMock.mock.calls.filter(([url]) => String(url).includes(fragment));
  const init = calls[index]?.[1] as RequestInit | undefined;
  return init?.body ? JSON.parse(init.body as string) : {};
}

export function callsTo(fetchMock: Mock<typeof fetch>, fragment: string): string[] {
  return fetchMock.mock.calls.map(([url]) => String(url)).filter((url) => url.includes(fragment));
}

export function column(name: string, extra: Partial<ReportColumn> = {}): ReportColumn {
  return { name, label: name, dataType: 'TEXT', align: 'LEFT', upperCase: false, ...extra };
}

export function filter(name: string, extra: Partial<ReportFilterDefinition> = {}): ReportFilterDefinition {
  return { name, label: name, dataType: 'TEXT', required: false, hideLabel: false, order: 0, optionsSource: 'NONE', ...extra };
}

export function definition(filters: ReportFilterDefinition[] = [], extra: Partial<ReportDefinition> = {}): ReportDefinition {
  return {
    report: { id: 7, name: 'Sales by region', title: 'Sales by region', chartable: false, hasFilters: filters.length > 0 },
    autofields: true,
    columns: [],
    filters,
    charts: [],
    exportFormats: ['xlsx', 'csv', 'pdf'],
    ...extra,
  };
}

export function runResult(extra: Partial<ReportRunResult> = {}): ReportRunResult {
  return {
    columns: [column('region'), column('total', { dataType: 'NUMBER', align: 'RIGHT' })],
    rows: [
      { region: 'north', total: 1200.5 },
      { region: 'south', total: 80 },
    ],
    total: 2,
    page: 0,
    size: 25,
    truncated: false,
    durationMs: 4,
    charts: [],
    ...extra,
  };
}
