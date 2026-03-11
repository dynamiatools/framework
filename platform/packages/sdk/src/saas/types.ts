// ─── SaaS types mirroring the Dynamia Platform Java model ───────────────────

export interface AccountDTO {
  id: number;
  uuid: string;
  name: string;
  status: string;
  statusDescription?: string;
  subdomain?: string;
}
