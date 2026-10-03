import { describe, it, expect } from 'vitest';
import { FunctionsApi } from '../src/index.js';
import { makeHttpClient, mockFetch } from './helpers.js';

describe('FunctionsApi', () => {
  it('call() POSTs { params } to /api/dynamia/fx/{name} and resolves with data', async () => {
    const fetchMock = mockFetch(200, { success: true, data: { messageId: 'm1' } });
    const fx = new FunctionsApi(makeHttpClient(fetchMock));

    const result = await fx.call<{ messageId: string }>('WhatsApp.sendMessage', { to: '+57300' });

    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(url).toBe('https://app.example.com/api/dynamia/fx/WhatsApp.sendMessage');
    expect(init.method).toBe('POST');
    expect(JSON.parse(init.body as string)).toEqual({ params: { to: '+57300' } });
    expect(result).toEqual({ messageId: 'm1' });
  });

  it('sends an empty params object when none is given and encodes the name', async () => {
    const fetchMock = mockFetch(200, { success: true, data: {} });
    const fx = new FunctionsApi(makeHttpClient(fetchMock));

    await fx.call('my fn/x');

    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(url).toBe('https://app.example.com/api/dynamia/fx/my%20fn%2Fx');
    expect(JSON.parse(init.body as string)).toEqual({ params: {} });
  });

  it('selects a version with the v query parameter', async () => {
    const fetchMock = mockFetch(200, { success: true, data: 1 });
    const fx = new FunctionsApi(makeHttpClient(fetchMock));

    await fx.call('Math.double', { n: 1 }, { version: 2 });

    expect((fetchMock.mock.calls[0] as [string])[0]).toBe('https://app.example.com/api/dynamia/fx/Math.double?v=2');
  });

  it('callBinary() resolves with the Blob of a binary response', async () => {
    const fetchMock = mockFetch(200, null, 'application/pdf');
    const fx = new FunctionsApi(makeHttpClient(fetchMock));

    const blob = await fx.callBinary('Invoice.pdf', { id: 1 });

    expect(blob).toBeInstanceOf(Blob);
  });

  it('call() refuses a binary response and callBinary() refuses JSON', async () => {
    const binary = new FunctionsApi(makeHttpClient(mockFetch(200, null, 'application/pdf')));
    const json = new FunctionsApi(makeHttpClient(mockFetch(200, { success: true, data: 1 })));

    await expect(binary.call('Invoice.pdf')).rejects.toThrow(/callBinary/);
    await expect(json.callBinary('Math.double')).rejects.toThrow(/call\(\)/);
  });

  it.each([
    [400, 'Missing parameter: to'],
    [404, 'Function not found: Nope'],
    [500, 'Function failed'],
  ])('rejects with DynamiaApiError on %i using the server message', async (status, message) => {
    const fx = new FunctionsApi(makeHttpClient(mockFetch(status, { success: false, error: message })));

    await expect(fx.call('Any.fn')).rejects.toMatchObject({ status, message });
  });
});
