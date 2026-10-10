import { describe, expect, it, vi } from 'vitest';
import { DynamiaClient } from '../../src/index.js';

function clientWith(fetchMock: ReturnType<typeof vi.fn>, config: Record<string, unknown> = {}) {
  return new DynamiaClient({ baseUrl: 'https://app.example.com', fetch: fetchMock as unknown as typeof fetch, ...config });
}

const json = (body: unknown, status = 200) =>
  new Response(JSON.stringify(body), { status, headers: { 'content-type': 'application/json' } });

describe('client.transfers', () => {
  it('uploads a file as the raw body of one request, with no Base64 and no multipart', async () => {
    const fetchMock = vi.fn(async () => json({ ref: 'r1', name: 'my report.json', contentType: 'application/json', size: 2 }));
    const client = clientWith(fetchMock, { token: 'secret' });
    const file = new File(['{}'], 'my report.json', { type: 'application/json' });

    const ref = await client.transfers.upload(file);

    expect(ref).toEqual({ ref: 'r1', name: 'my report.json', contentType: 'application/json', size: 2 });
    const [url, init] = fetchMock.mock.calls[0] as unknown as [string, RequestInit];
    expect(url).toBe('https://app.example.com/api/app/transfers');
    expect(init.method).toBe('POST');
    expect(init.body).toBe(file);
    const headers = init.headers as Record<string, string>;
    expect(headers['Content-Type']).toBe('application/octet-stream');
    expect(headers['X-File-Name']).toBe('my%20report.json');
    expect(headers['X-File-Type']).toBe('application/json');
    expect(headers['Authorization']).toBe('Bearer secret');
  });

  it('turns a refusal of the server into an error that carries the status', async () => {
    const fetchMock = vi.fn(async () => json({ error: 'TOO_LARGE', message: 'The file is bigger than the limit' }, 413));

    await expect(clientWith(fetchMock).transfers.upload(new File(['x'], 'a.bin')))
      .rejects.toMatchObject({ status: 413, message: 'The file is bigger than the limit' });
  });

  it('downloads with the credentials of the client', async () => {
    const fetchMock = vi.fn(async () => new Response('hello', { status: 200, headers: { 'content-type': 'text/plain' } }));
    const client = clientWith(fetchMock, { token: 't' });

    const blob = await client.transfers.download('/api/app/transfers/abc');

    expect(await blob.text()).toBe('hello');
    const [url, init] = fetchMock.mock.calls[0] as unknown as [string, RequestInit];
    expect(url).toBe('https://app.example.com/api/app/transfers/abc');
    expect((init.headers as Record<string, string>)['Authorization']).toBe('Bearer t');
  });

  it('knows when a plain link cannot be used', () => {
    expect(clientWith(vi.fn(), { token: 't' }).transfers.needsAuthorizedFetch()).toBe(true);
    expect(clientWith(vi.fn(), { withCredentials: true }).transfers.needsAuthorizedFetch()).toBe(false);
    expect(clientWith(vi.fn()).transfers.absoluteUrl('/api/app/transfers/abc')).toBe('https://app.example.com/api/app/transfers/abc');
  });
});
