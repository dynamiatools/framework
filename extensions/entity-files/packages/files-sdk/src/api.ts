import { DynamiaApiError, type DynamiaClientConfig, type HttpClient } from '@dynamia-tools/sdk';

export interface EntityFileExportResponse {
  id: number;
  uuid: string;
  name: string;
  description?: string | null;
  size?: number | null;
  version?: number | null;
  url: string;
}

export interface FilesUploadOptions {
  className?: string;
  entityId?: string | number;
  description?: string;
  shared?: boolean;
  subfolder?: string;
  storedFileName?: string;
  parentUuid?: string;
}

export interface MultipartUploadOptions extends FilesUploadOptions {
  fileName?: string;
}

export interface Base64UploadRequest extends FilesUploadOptions {
  fileName?: string;
  name?: string;
  contentType?: string;
  base64?: string;
  data?: string;
}

export interface EntityFileUploadResponse {
  id: number;
  uuid: string;
  name: string;
  description?: string | null;
  contentType?: string | null;
  size?: number | null;
  shared: boolean;
  subfolder?: string | null;
  storedFileName?: string | null;
  targetEntity?: string | null;
  targetEntityId?: number | null;
  targetEntitySId?: string | null;
  parentId?: number | null;
  version?: number | null;
  url: string;
  valid: boolean;
}

interface InternalHttpClientState {
  config?: DynamiaClientConfig;
  _fetch?: typeof fetch;
}

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
   * GET /api/storage/{uuid}/export
   * Returns the server-side metadata and direct URL for a stored entity file.
   */
  export(uuid: string): Promise<EntityFileExportResponse> {
    return this.http.get<EntityFileExportResponse>(`/api/storage/${encodeURIComponent(uuid)}/export`);
  }

  /**
   * GET /storage/{uuid}/{file}
   * Download a file as a Blob.
   */
  download(file: string, uuid: string): Promise<Blob> {
    return this.http.get<Blob>(`/storage/${encodeURIComponent(uuid)}/${encodeURIComponent(file)}`);
  }

  /**
   * Returns a direct URL for the file (useful for <img src="...">, anchor href, etc.)
   * No HTTP request is made.
   */
  getUrl(file: string, uuid: string): string {
    return this.http.url(`/storage/${encodeURIComponent(uuid)}/${encodeURIComponent(file)}`);
  }

  /**
   * POST /api/storage/upload
   * Uploads a file using multipart/form-data.
   */
  uploadMultipart(file: Blob | File, options: MultipartUploadOptions = {}): Promise<EntityFileUploadResponse> {
    const fileName = this.resolveFileName(file, options.fileName);
    const formData = new FormData();
    formData.append('file', file, fileName);
    this.appendOptionalFormValue(formData, 'className', options.className);
    this.appendOptionalFormValue(formData, 'entityId', this.toOptionalString(options.entityId));
    this.appendOptionalFormValue(formData, 'description', options.description);
    this.appendOptionalFormValue(formData, 'shared', options.shared);
    this.appendOptionalFormValue(formData, 'subfolder', options.subfolder);
    this.appendOptionalFormValue(formData, 'storedFileName', options.storedFileName);
    this.appendOptionalFormValue(formData, 'parentUuid', options.parentUuid);

    return this.request<EntityFileUploadResponse>('/api/storage/upload', {
      method: 'POST',
      body: formData,
      headers: this.buildHeaders(),
    });
  }

  /**
   * POST /api/storage/upload-base64
   * Uploads a file using a JSON payload containing base64 or data URI content.
   */
  uploadBase64(request: Base64UploadRequest): Promise<EntityFileUploadResponse> {
    return this.request<EntityFileUploadResponse>('/api/storage/upload-base64', {
      method: 'POST',
      body: JSON.stringify(request),
      headers: this.buildHeaders('application/json'),
    });
  }

  private resolveFileName(file: Blob | File, fileName?: string): string {
    if (fileName && fileName.trim()) {
      return fileName.trim();
    }

    if ('name' in file && typeof file.name === 'string' && file.name.trim()) {
      return file.name.trim();
    }

    throw new Error('FilesApi.uploadMultipart requires options.fileName when the provided Blob has no name');
  }

  private appendOptionalFormValue(formData: FormData, name: string, value: string | boolean | undefined): void {
    if (value === undefined) {
      return;
    }

    formData.append(name, String(value));
  }

  private toOptionalString(value: string | number | undefined): string | undefined {
    return value === undefined ? undefined : String(value);
  }

  private buildHeaders(contentType?: string): Headers {
    const headers = new Headers({ Accept: 'application/json' });
    const config = this.getConfig();

    if (contentType) {
      headers.set('Content-Type', contentType);
    }

    if (config?.token) {
      headers.set('Authorization', `Bearer ${config.token}`);
    } else if (config?.username && config?.password) {
      headers.set('Authorization', `Basic ${this.encodeBasicAuth(config.username, config.password)}`);
    }

    return headers;
  }

  private encodeBasicAuth(username: string, password: string): string {
    const plain = `${username}:${password}`;
    if (typeof btoa === 'function') {
      return btoa(plain);
    }
    return Buffer.from(plain, 'utf-8').toString('base64');
  }

  private getConfig(): DynamiaClientConfig | undefined {
    return (this.http as unknown as InternalHttpClientState).config;
  }

  private getFetch(): typeof fetch {
    const fetchImpl = (this.http as unknown as InternalHttpClientState)._fetch ?? globalThis.fetch;
    if (!fetchImpl) {
      throw new Error('FilesApi requires a fetch implementation. Pass one through DynamiaClient config.');
    }
    return fetchImpl.bind(globalThis);
  }

  private async request<T>(path: string, init: RequestInit): Promise<T> {
    const url = this.http.url(path);
    const config = this.getConfig();
    const requestInit: RequestInit = {
      ...init,
    };

    if (config?.corsMode) {
      requestInit.mode = config.corsMode;
    }

    if (config?.withCredentials) {
      requestInit.credentials = 'include';
    }

    const response = await this.getFetch()(url, requestInit);

    if (!response.ok) {
      let errorBody: unknown;
      try {
        errorBody = await response.json();
      } catch {
        errorBody = await response.text().catch(() => undefined);
      }

      const message =
        (errorBody as Record<string, string> | undefined)?.message ??
        (errorBody as Record<string, string> | undefined)?.error ??
        response.statusText;

      throw new DynamiaApiError(message, response.status, url, errorBody);
    }

    if (response.status === 204) {
      return undefined as T;
    }

    return response.json() as Promise<T>;
  }
}
