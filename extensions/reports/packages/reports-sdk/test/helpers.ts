import { vi } from 'vitest';
import { DynamiaClient, HttpClient } from '@dynamia-tools/sdk';

export function mockFetch(status: number, body: unknown, contentType = 'application/json') {
  return vi.fn().mockResolvedValue({
    ok: status >= 200 && status < 300,
    status,
    statusText: status === 200 ? 'OK' : 'Error',
    headers: { get: (key: string) => (key === 'content-type' ? contentType : null) },
    json: () => Promise.resolve(body),
    text: () => Promise.resolve(String(body)),
    blob: () => Promise.resolve(new Blob()),
  } as unknown as Response);
}

export function makeHttpClient(fetchMock: ReturnType<typeof vi.fn>): HttpClient {
  const client = new DynamiaClient({ baseUrl: 'https://app.example.com', token: 'test-token', fetch: fetchMock });
  return client.http as HttpClient;
}

