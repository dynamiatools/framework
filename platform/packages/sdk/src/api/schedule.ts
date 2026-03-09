import type { HttpClient } from '../http.js';

/**
 * Manually trigger periodic tasks registered in the platform.
 * Base path: /schedule/execute-tasks
 */
export class ScheduleApi {
  private readonly http: HttpClient;

  constructor(http: HttpClient) {
    this.http = http;
  }

  /** GET /schedule/execute-tasks/morning */
  executeMorning(): Promise<void> {
    return this.http.get<void>('/schedule/execute-tasks/morning');
  }

  /** GET /schedule/execute-tasks/midday */
  executeMidday(): Promise<void> {
    return this.http.get<void>('/schedule/execute-tasks/midday');
  }

  /** GET /schedule/execute-tasks/afternoon */
  executeAfternoon(): Promise<void> {
    return this.http.get<void>('/schedule/execute-tasks/afternoon');
  }

  /** GET /schedule/execute-tasks/evening */
  executeEvening(): Promise<void> {
    return this.http.get<void>('/schedule/execute-tasks/evening');
  }
}

