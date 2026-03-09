import type { HttpClient } from '../http.js';
import type { AccountDTO } from '../types.js';

/**
 * Manage multi-tenant accounts via the SaaS extension.
 * Base path: /api/saas
 */
export class SaasApi {
  private readonly http: HttpClient;

  constructor(http: HttpClient) {
    this.http = http;
  }

  /** GET /api/saas/account/{uuid} — Get account information by UUID */
  getAccount(uuid: string): Promise<AccountDTO> {
    return this.http.get<AccountDTO>(`/api/saas/account/${encodeURIComponent(uuid)}`);
  }
}

