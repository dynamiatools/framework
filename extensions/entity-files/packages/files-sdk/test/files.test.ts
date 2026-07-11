import { describe, it, expect } from 'vitest';
import { FilesApi } from '../src/index.js';
import { makeHttpClient, mockBlobResponse, mockJsonResponse } from './helpers.js';

describe('FilesApi', () => {
  it('getUrl() returns a URL using the new /storage/{uuid}/{file} pattern', () => {
    const http = makeHttpClient(mockJsonResponse({}));
    const api = new FilesApi(http);
    const url = api.getUrl('photo.png', 'uuid-123');
    expect(url).toBe('https://app.example.com/storage/uuid-123/photo.png');
  });

  it('download() requests the new storage path', async () => {
    const fetchMock = mockBlobResponse();
    const http = makeHttpClient(fetchMock);
    const api = new FilesApi(http);

    await api.download('photo.png', 'uuid-123');

    expect(fetchMock).toHaveBeenCalledWith(
      'https://app.example.com/storage/uuid-123/photo.png',
      expect.objectContaining({ method: 'GET' }),
    );
  });

  it('export() loads file metadata from the export endpoint', async () => {
    const fetchMock = mockJsonResponse({ uuid: 'uuid-123', name: 'photo.png', url: 'https://cdn.example.com/photo.png', id: 1 });
    const http = makeHttpClient(fetchMock);
    const api = new FilesApi(http);

    const result = await api.export('uuid-123');

    expect(result.uuid).toBe('uuid-123');
    expect(fetchMock).toHaveBeenCalledWith(
      'https://app.example.com/api/storage/uuid-123/export',
      expect.objectContaining({ method: 'GET' }),
    );
  });

  it('uploadMultipart() sends multipart form data to the new upload endpoint', async () => {
    const fetchMock = mockJsonResponse({ uuid: 'uuid-123', valid: true, name: 'photo.png', shared: false, url: '/storage/uuid-123/photo.png', id: 1 });
    const http = makeHttpClient(fetchMock);
    const api = new FilesApi(http);
    const file = new File(['content'], 'photo.png', { type: 'image/png' });

    const result = await api.uploadMultipart(file, {
      className: 'com.example.Customer',
      entityId: 15,
      description: 'Profile picture',
      shared: true,
      parentUuid: 'parent-1',
    });

    expect(result.valid).toBe(true);
    expect(fetchMock).toHaveBeenCalledTimes(1);

    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(url).toBe('https://app.example.com/api/storage/upload');
    expect(init.method).toBe('POST');
    expect(init.headers).toBeInstanceOf(Headers);
    expect((init.headers as Headers).get('Authorization')).toBe('Bearer test-token');
    expect((init.headers as Headers).get('Content-Type')).toBeNull();
    expect(init.body).toBeInstanceOf(FormData);

    const formData = init.body as FormData;
    expect(formData.get('file')).toBeInstanceOf(File);
    expect(formData.get('className')).toBe('com.example.Customer');
    expect(formData.get('entityId')).toBe('15');
    expect(formData.get('description')).toBe('Profile picture');
    expect(formData.get('shared')).toBe('true');
    expect(formData.get('parentUuid')).toBe('parent-1');
  });

  it('uploadBase64() posts JSON payload to the base64 endpoint', async () => {
    const fetchMock = mockJsonResponse({ uuid: 'uuid-123', valid: true, name: 'photo.png', shared: false, url: '/storage/uuid-123/photo.png', id: 1 });
    const http = makeHttpClient(fetchMock);
    const api = new FilesApi(http);

    await api.uploadBase64({
      fileName: 'photo.png',
      contentType: 'image/png',
      base64: 'ZmFrZQ==',
      className: 'com.example.Customer',
      entityId: '15',
    });

    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(url).toBe('https://app.example.com/api/storage/upload-base64');
    expect(init.method).toBe('POST');
    expect((init.headers as Headers).get('Content-Type')).toBe('application/json');
    expect((init.headers as Headers).get('Authorization')).toBe('Bearer test-token');
    expect(init.body).toBe(JSON.stringify({
      fileName: 'photo.png',
      contentType: 'image/png',
      base64: 'ZmFrZQ==',
      className: 'com.example.Customer',
      entityId: '15',
    }));
  });
});
