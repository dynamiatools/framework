import { describe, it, expect } from 'vitest';
import { FilesApi } from '../src/index.js';
import { mockFetch, makeHttpClient } from './helpers.js';

describe('FilesApi', () => {
  it('getUrl() returns a URL with uuid query param', () => {
    const http = makeHttpClient(mockFetch(200, {}));
    const api = new FilesApi(http);
    const url = api.getUrl('photo.png', 'uuid-123');
    expect(url).toContain('/storage/photo.png');
    expect(url).toContain('uuid=uuid-123');
  });
});

