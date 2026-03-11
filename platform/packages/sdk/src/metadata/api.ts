import type { HttpClient } from '../http.js';
import type {
  ApplicationMetadata,
  ApplicationMetadataActions,
  ApplicationMetadataEntities,
  EntityMetadata,
  NavigationTree,
  ViewDescriptor,
  ViewDescriptorMetadata,
} from './types.js';

/**
 * Provides access to application metadata endpoints.
 * Base path: /api/app/metadata
 */
export class MetadataApi {
  private readonly http: HttpClient;

  constructor(http: HttpClient) {
    this.http = http;
  }

  /** GET /api/app/metadata — Application-level metadata */
  getApp(): Promise<ApplicationMetadata> {
    return this.http.get('/api/app/metadata');
  }

  /** GET /api/app/metadata/navigation — Full navigation tree */
  getNavigation(): Promise<NavigationTree> {
    return this.http.get('/api/app/metadata/navigation');
  }

  /** GET /api/app/metadata/actions — All global actions */
  getGlobalActions(): Promise<ApplicationMetadataActions> {
    return this.http.get('/api/app/metadata/actions');
  }

  /** GET /api/app/metadata/entities — All entity metadata */
  getEntities(): Promise<ApplicationMetadataEntities> {
    return this.http.get('/api/app/metadata/entities');
  }

  /** GET /api/app/metadata/entities/{className} — Single entity metadata */
  getEntity(className: string): Promise<EntityMetadata> {
    return this.http.get(`/api/app/metadata/entities/${encodeURIComponent(className)}`);
  }

  /** GET /api/app/metadata/entities/{className}/views — All view descriptors for an entity */
  getEntityViews(className: string): Promise<ViewDescriptorMetadata[]> {
    return this.http.get(`/api/app/metadata/entities/${encodeURIComponent(className)}/views`);
  }

  /** GET /api/app/metadata/entities/{className}/views/{view} — Specific view descriptor */
  getEntityView(className: string, view: string): Promise<ViewDescriptor> {
    return this.http.get(
      `/api/app/metadata/entities/${encodeURIComponent(className)}/views/${encodeURIComponent(view)}`,
    );
  }
}
