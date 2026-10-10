import type { HttpClient } from '../http.js';

/** State of a background job. */
export type JobState = 'RUNNING' | 'DONE' | 'FAILED';

/** Where a background job is: what `GET /api/app/jobs/{id}` answers. */
export interface JobStatus {
  id: string;
  title: string;
  state: JobState;
  /** How much work is done, in the unit of `max`. */
  current: number;
  /** How much work there is, or 0 when unknown. */
  max: number;
  message?: string;
  /** Why it failed, when `state` is `FAILED`. */
  error?: string;
}

/**
 * Follows the background tasks of `PROGRESS` flow steps (`UIProgress` on the server). A client polls {@link status} until the
 * job is no longer `RUNNING` and then answers the step, which lets the action go on.
 */
export class JobsApi {
  private static readonly PATH = '/api/app/jobs';

  constructor(private readonly http: HttpClient) {
  }

  /** @returns the status of the job; rejects with a 404 `DynamiaApiError` when it expired or is not yours */
  status(jobId: string): Promise<JobStatus> {
    return this.http.get<JobStatus>(`${JobsApi.PATH}/${encodeURIComponent(jobId)}`);
  }
}
