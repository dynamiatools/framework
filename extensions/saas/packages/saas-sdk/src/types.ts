// ─── SaaS types mirroring the Dynamia Platform Java model ───────────────────

/**
 * Account data transfer object returned by the SaaS API.
 *
 * Mirrors `tools.dynamia.saas.AccountDTO`.
 * Fields annotated `@JsonInclude(NON_NULL)` in Java will be absent from the
 * JSON payload when they have not been set on the account.
 */
export interface AccountDTO {
  /** Internal numeric account ID */
  id: number;
  /** Stable UUID identifying the account */
  uuid: string;
  /** Account / company display name */
  name: string;
  /** Current lifecycle status key (e.g. `"ACTIVE"`, `"SUSPENDED"`, `"TRIAL"`) */
  status: string;
  /** Human-readable description of the current status */
  statusDescription?: string;
  /** Subdomain assigned to this account (e.g. `"acme"` → `acme.example.com`) */
  subdomain?: string;
  /** Contact e-mail address for the account */
  email?: string;
  /** ISO date-time string when the current status was set */
  statusDate?: string;
  /** ISO date-time string when the account subscription expires */
  expirationDate?: string;
  /** BCP 47 locale code for the account (e.g. `"en_US"`, `"es_CO"`) */
  locale?: string;
  /** IANA time-zone ID (e.g. `"America/Bogota"`) */
  timeZone?: string;
  /** External customer ID from a billing / CRM system */
  customerId?: string;
  /** Identifier of the subscription plan assigned to this account */
  planId?: string;
  /** Display name of the subscription plan */
  planName?: string;
}
