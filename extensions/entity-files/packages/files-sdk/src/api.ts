import type { HttpClient } from '@dynamia-tools/sdk';

/**
 * Download files managed by the Entity Files extension.
 * Base path: /storage
 */
export class FilesApi {
  private readonly http: HttpClient;

  constructor(http: HttpClient) {
    this.http = http;
  }

  /**
   * GET /storage/{file}?uuid={uuid}
   * Download a file as a Blob.
   */
  download(file: string, uuid: string): Promise<Blob> {
    return this.http.get<Blob>(`/storage/${encodeURIComponent(file)}`, { uuid });
  }

  /**
   * Returns a direct URL for the file (useful for <img src="...">, anchor href, etc.)
   * No HTTP request is made.
   */
  getUrl(file: string, uuid: string): string {
    return this.http.url(`/storage/${encodeURIComponent(file)}`, { uuid });
  }
}

