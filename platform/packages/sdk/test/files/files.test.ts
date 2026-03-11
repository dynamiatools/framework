import { describe, it, expect } from 'vitest';
import { mockFetch, makeClient } from '../helpers.js';

describe('FilesApi', () => {
  it('getUrl() returns a URL with uuid query param', () => {
    const client = makeClient(mockFetch(200, {}));
    const url = client.files.getUrl('photo.png', 'uuid-123');
    expect(url).toContain('/storage/photo.png');
    expect(url).toContain('uuid=uuid-123');
  });
});
