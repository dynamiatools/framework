import type { HttpClient } from '@dynamia-tools/sdk';
import type { AccountDTO } from './types.js';

/**
 * Manage multi-tenant accounts via the SaaS extension REST API.
 * Base path: /api/saas
 *
 * Server-side use only: these endpoints are server-to-server and authorized by the system account or the request
 * subdomain, so do not bundle this API (or a token that can call it) into a browser application.
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

