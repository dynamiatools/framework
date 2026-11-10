/**
 * A small client for the email template REST API (`/api/email/templates`, see `EmailTemplatesApiController`). It uses
 * `fetch` and the session cookie of the page, and turns error answers into {@link EmailApiError}.
 */

export interface TemplateData {
  id: number;
  name: string;
  subject: string;
  engine: 'VELOCITY' | 'DYNAMIA';
  escapeValues: boolean;
  design: string | null;
  content: string;
  hasBackup: boolean;
  parentId: number | null;
}

export interface ApiVariable {
  name: string;
  label?: string | null;
  sample?: unknown;
}

export interface MigrationIssue {
  field: string;
  kind: string;
  line: number;
  snippet: string;
  reason: string;
}

export interface MigrationReport {
  alreadyMigrated: boolean;
  portable: boolean;
  applied: boolean;
  converted: Record<string, string>;
  issues: MigrationIssue[];
  warnings: string[];
}

export interface RevertResult {
  reverted: boolean;
  designEdited: boolean;
  reason: string | null;
}

export interface PreviewResult {
  html: string;
  subject: string;
}

export interface TestSendResult {
  sent: boolean;
  message: string | null;
}

export class EmailApiError extends Error {
  constructor(
    readonly status: number,
    readonly code: string,
    message: string,
  ) {
    super(message);
    this.name = 'EmailApiError';
  }
}

/** What the editor needs from the API; tests and hosts can provide their own. */
export interface EmailTemplatesApi {
  get(id: number): Promise<TemplateData>;
  saveDesign(id: number, body: { design: string; content: string; subject?: string }): Promise<TemplateData>;
  variables(source?: string): Promise<ApiVariable[]>;
  testSend(id: number, body?: { source?: string; data?: Record<string, unknown> }): Promise<TestSendResult>;
  migrate(id: number, dryRun: boolean): Promise<MigrationReport>;
  revert(id: number, confirm: boolean): Promise<RevertResult>;
}

export interface ClientOptions {
  /** Origin of the application, empty for the same origin. */
  baseUrl?: string;
  /** Extra headers, for example a CSRF token. */
  headers?: Record<string, string>;
  fetch?: typeof fetch;
}

export class EmailTemplatesClient implements EmailTemplatesApi {
  private readonly base: string;
  private readonly doFetch: typeof fetch;

  constructor(private readonly options: ClientOptions = {}) {
    this.base = `${(options.baseUrl ?? '').replace(/\/+$/, '')}/api/email/templates`;
    this.doFetch = options.fetch ?? ((input, init) => fetch(input, init));
  }

  private async request<T>(method: string, path: string, body?: unknown): Promise<T> {
    const response = await this.doFetch(this.base + path, {
      method,
      credentials: 'same-origin',
      headers: { Accept: 'application/json', 'X-Requested-With': 'dynamia-email-editor', ...(body === undefined ? {} : { 'Content-Type': 'application/json' }), ...(this.options.headers ?? {}) },
      ...(body === undefined ? {} : { body: JSON.stringify(body) }),
    });
    const text = await response.text();
    let data: unknown = null;
    try {
      data = text === '' ? null : JSON.parse(text);
    } catch {
      data = null;
    }
    if (!response.ok) {
      const error = (data ?? {}) as { code?: string; message?: string };
      throw new EmailApiError(response.status, error.code ?? 'ERROR', error.message ?? `Request failed (${response.status})`);
    }
    return data as T;
  }

  get(id: number): Promise<TemplateData> {
    return this.request('GET', `/${id}`);
  }

  create(name: string, subject?: string): Promise<TemplateData> {
    return this.request('POST', '', { name, subject: subject ?? null });
  }

  saveDesign(id: number, body: { design: string; content: string; subject?: string }): Promise<TemplateData> {
    return this.request('PUT', `/${id}/design`, body);
  }

  variables(source?: string): Promise<ApiVariable[]> {
    return this.request('GET', source ? `/variables?source=${encodeURIComponent(source)}` : '/variables');
  }

  preview(id: number, body: { content?: string; subject?: string; source?: string; data?: Record<string, unknown> }): Promise<PreviewResult> {
    return this.request('POST', `/${id}/preview`, body);
  }

  testSend(id: number, body: { source?: string; data?: Record<string, unknown> } = {}): Promise<TestSendResult> {
    return this.request('POST', `/${id}/test-send`, body);
  }

  migrate(id: number, dryRun: boolean): Promise<MigrationReport> {
    return this.request('POST', `/${id}/migrate?dryRun=${dryRun}`);
  }

  revert(id: number, confirm: boolean): Promise<RevertResult> {
    return this.request('POST', `/${id}/revert?confirm=${confirm}`);
  }
}
