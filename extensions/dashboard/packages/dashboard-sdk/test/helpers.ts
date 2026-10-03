import { vi, type Mock } from 'vitest';
import { DynamiaClient, HttpClient } from '@dynamia-tools/sdk';

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

export function makeHttpClient(fetchMock: Mock<typeof fetch>): HttpClient {
  const client = new DynamiaClient({ baseUrl: 'https://app.example.com', token: 'test-token', fetch: fetchMock });
  return client.http as HttpClient;
}

