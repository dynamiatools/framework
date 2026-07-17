import { vi, type Mock } from 'vitest';
import { DynamiaClient } from '../src/index.js';

export function rawPageResponse<T>(items: T[], page = 1, pageSize = 50) {
  return {
    data: items,
    pageable: { firstResult: 0, page, pageSize, pagesNumber: 1, totalSize: items.length },
    response: 'OK',
  };
}

export function mockFetch(status: number, body: unknown, contentType = 'application/json'): Mock<typeof fetch> {
  return vi.fn().mockResolvedValue({
    ok: status >= 200 && status < 300,
    status,
    statusText: status === 200 ? 'OK' : 'Error',
    headers: { get: (key: string) => (key === 'content-type' ? contentType : null) },
    json: () => Promise.resolve(body),
    text: () => Promise.resolve(String(body)),
    blob: () => Promise.resolve(new Blob()),
  } as unknown as Response) as unknown as Mock<typeof fetch>;
}

export function makeClient(fetchMock: Mock<typeof fetch>) {
  return new DynamiaClient({ baseUrl: 'https://app.example.com', token: 'test-token', fetch: fetchMock });
}
