import type { HttpClient } from '@dynamia-tools/sdk';
import type { DashboardWidgetParams, DashboardWidgetResponse } from './types.js';

/**
 * Access the Dashboard extension REST API.
 * Base path: /api/dashboard
 *
 * The layout and the widget slots of a dashboard come from its view descriptor
 * (`client.metadata.getView(id)` and {@link resolveDashboardLayout}); this API only loads widget data.
 */
export class DashboardApi {
  private readonly http: HttpClient;

  constructor(http: HttpClient) {
    this.http = http;
  }

  /**
   * GET /api/dashboard/{descriptorId}/widgets/{field} — Loads a widget: its definition and current data.
   *
   * @param descriptorId - the dashboard view descriptor id
   * @param field - the descriptor field name the widget is bound to
   * @param params - optional query parameters, forwarded to the widget's `update(params)`
   * @throws DynamiaApiError with status 404 when the dashboard, field or widget does not exist
   */
  widget<T = unknown>(
    descriptorId: string,
    field: string,
    params?: DashboardWidgetParams,
  ): Promise<DashboardWidgetResponse<T>> {
    return this.http.get<DashboardWidgetResponse<T>>(
      `/api/dashboard/${encodeURIComponent(descriptorId)}/widgets/${encodeURIComponent(field)}`,
      params,
    );
  }
}
