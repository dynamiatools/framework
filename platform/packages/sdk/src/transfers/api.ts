import type { HttpClient } from '../http.js';
import type { FlowFileRef } from '../metadata/types.js';

/** Options of {@link TransfersApi.upload}. */
export interface UploadOptions {
  /** Called with the fraction (0..1) already sent. Needs `XMLHttpRequest`; without it the upload still works, no progress. */
  onProgress?: (fraction: number) => void;
  /** Cancels the upload. */
  signal?: AbortSignal;
}

/**
 * Moves files between a client and the server without Base64: a file is sent as the raw body of one request to
 * `/api/app/transfers` and travels afterwards as a {@link FlowFileRef}, which is what an `UPLOAD` flow step is answered with.
 * Downloads are fetched from the `url` of `params.downloads`.
 */
export class TransfersApi {
  private static readonly PATH = '/api/app/transfers';

  constructor(private readonly http: HttpClient) {
  }

  /**
   * Uploads one file. The `Blob` is handed to the browser as is, so a big file is streamed from disk, not read into memory.
   *
   * @returns the reference to answer an `UPLOAD` step with
   */
  async upload(file: File | Blob, options: UploadOptions = {}): Promise<FlowFileRef> {
    const name = (file as File).name ?? 'file';
    const type = file.type || 'application/octet-stream';
    const headers = {
      'Content-Type': 'application/octet-stream',
      'X-File-Name': encodeURIComponent(name),
      'X-File-Type': type,
    };
    if (options.onProgress && typeof XMLHttpRequest !== 'undefined') {
      return this.uploadWithProgress(file, headers, options);
    }
    return this.http.sendRaw<FlowFileRef>('POST', TransfersApi.PATH, file, headers, options.signal);
  }

  /** Cancels an upload the action will not use. Safe to call for an unknown reference. */
  async cancel(ref: string): Promise<void> {
    await this.http.delete(`${TransfersApi.PATH}/${encodeURIComponent(ref)}`);
  }

  /** Fetches a file the action gave to the user (an entry of `params.downloads`). */
  download(url: string, signal?: AbortSignal): Promise<Blob> {
    return this.http.getBlob(url, signal);
  }

  /** Absolute URL of a download, for a plain link when requests are authenticated with cookies. */
  absoluteUrl(url: string): string {
    return this.http.url(url);
  }

  /** Whether a plain link cannot be used for downloads because authentication goes in a header. */
  needsAuthorizedFetch(): boolean {
    return this.http.usesHeaderAuth();
  }

  private uploadWithProgress(file: File | Blob, headers: Record<string, string>, options: UploadOptions): Promise<FlowFileRef> {
    return new Promise((resolve, reject) => {
      const xhr = new XMLHttpRequest();
      xhr.open('POST', this.http.url(TransfersApi.PATH));
      Object.entries(this.http.authorizationHeaders()).forEach(([k, v]) => xhr.setRequestHeader(k, v));
      Object.entries(headers).forEach(([k, v]) => xhr.setRequestHeader(k, v));
      xhr.setRequestHeader('Accept', 'application/json');
      xhr.upload.onprogress = event => {
        if (event.lengthComputable) options.onProgress?.(event.loaded / event.total);
      };
      xhr.onload = () => {
        if (xhr.status >= 200 && xhr.status < 300) {
          resolve(JSON.parse(xhr.responseText) as FlowFileRef);
        } else {
          reject(new Error(`Upload failed: ${xhr.status} ${xhr.responseText}`));
        }
      };
      xhr.onerror = () => reject(new Error('Upload failed: network error'));
      options.signal?.addEventListener('abort', () => xhr.abort());
      xhr.send(file);
    });
  }
}
