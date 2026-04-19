import type {HttpClient} from '../http.js';
import type {
    ApplicationMetadata,
    ApplicationMetadataActions,
    ApplicationMetadataEntities,
    EntityMetadata,
    EntityReference,
    NavigationTree,
    ViewDescriptor,
} from './types.js';

/**
 * Provides access to application metadata endpoints.
 * Base path: /api/app/metadata
 *
 * View descriptors are cached in-memory because they rarely change at runtime.
 * Call {@link clearViewCache} to invalidate after a hot-reload or forced refresh.
 */
export class MetadataApi {
    private readonly http: HttpClient;

    /** Cache: `"className:viewType"` → single ViewDescriptor */
    private readonly _viewCache = new Map<string, ViewDescriptor>();
    /** Cache: `className` → full list of ViewDescriptors */
    private readonly _viewsCache = new Map<string, ViewDescriptor[]>();

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

    /** GET /api/app/metadata/entities/{className} — Single entity metadata */
    getEntityReference(alias: string, id: string | number): Promise<EntityReference> {
        return this.http.get(`/api/app/metadata/entities/ref/${encodeURIComponent(alias)}/${encodeURIComponent(id)}`);
    }

    /** GET /api/app/metadata/entities/{className} — Single entity metadata */
    findEntityReferences(alias: string, query: string): Promise<EntityReference[]> {
        return this.http.get(`/api/app/metadata/entities/ref/${encodeURIComponent(alias)}/search?q=${encodeURIComponent(query)}`);
    }

    /**
     * GET /api/app/metadata/entities/{className}/views — All view descriptors for an entity.
     *
     * Results are cached after the first successful fetch.
     * Individual descriptors are also stored in the per-view cache.
     */
    async getEntityViews(className: string): Promise<ViewDescriptor[]> {
        const cached = this._viewsCache.get(className);
        if (cached !== undefined) return cached;

        const descriptors = await this.http.get<ViewDescriptor[]>(
            `/api/app/metadata/entities/${encodeURIComponent(className)}/views`,
        );

        this._viewsCache.set(className, descriptors);
        // Populate per-view cache from the bulk result to avoid redundant round-trips
        for (const d of descriptors) {
            if (d.view) {
                this._viewCache.set(`${className}:${d.view}`, d);
            }
        }
        return descriptors;
    }

    /**
     * GET /api/app/metadata/entities/{className}/views/{view} — Specific view descriptor.
     *
     * The result is cached after the first successful fetch.
     */
    async getEntityView(className: string, view: string): Promise<ViewDescriptor> {
        const key = `${className}:${view}`;
        const cached = this._viewCache.get(key);
        if (cached !== undefined) return cached;

        const descriptor = await this.http.get<ViewDescriptor>(
            `/api/app/metadata/entities/${encodeURIComponent(className)}/views/${encodeURIComponent(view)}`,
        );

        this._viewCache.set(key, descriptor);
        return descriptor;
    }

    /**
     * Clears the in-memory ViewDescriptor cache.
     *
     * @param className - When provided, only the cache entries for that entity class
     *   are removed. When omitted, the entire cache is cleared.
     *
     * @example
     * // invalidate a single entity after a backend hot-reload
     * client.metadata.clearViewCache('mybookstore.domain.Book');
     * // invalidate everything
     * client.metadata.clearViewCache();
     */
    clearViewCache(className?: string): void {
        if (className !== undefined) {
            this._viewsCache.delete(className);
            const prefix = `${className}:`;
            for (const key of this._viewCache.keys()) {
                if (key.startsWith(prefix)) this._viewCache.delete(key);
            }
        } else {
            this._viewCache.clear();
            this._viewsCache.clear();
        }
    }
}
