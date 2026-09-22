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

    /** Cache: `"entityId:viewType"` → single ViewDescriptor */
    private readonly _viewCache = new Map<string, ViewDescriptor>();
    /** Cache: `entityId` → full list of ViewDescriptors */
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

    /**
     * GET /api/app/metadata/entities/{id} — Single entity metadata.
     *
     * @param id - the entity id (its simple class name, e.g. `"Invoice"` — see {@link EntityMetadata}).
     *   Not a fully-qualified Java class name.
     */
    getEntity(id: string): Promise<EntityMetadata> {
        return this.http.get(`/api/app/metadata/entities/${encodeURIComponent(id)}`);
    }

    /**
     * GET /api/app/metadata/entities/by-path?path={virtualPath} — Resolves the entity metadata for
     * the `CrudPage` registered at a navigation virtual path (`NavigationNode.internalPath`), without
     * needing to know the entity's id beforehand. This is how {@link https://www.npmjs.com/package/@dynamia-tools/ui-core | CrudPageResolver}
     * turns a `CrudPage` node into an entity, since `NavigationNode` carries no entity/class information.
     */
    getEntityByPath(virtualPath: string): Promise<EntityMetadata> {
        return this.http.get(`/api/app/metadata/entities/by-path?path=${encodeURIComponent(virtualPath)}`);
    }

    /** GET /api/app/metadata/entities/ref/{alias}/{id} — Single entity reference */
    getEntityReference(alias: string, id: string | number): Promise<EntityReference> {
        return this.http.get(`/api/app/metadata/entities/ref/${encodeURIComponent(alias)}/${encodeURIComponent(id)}`);
    }

    /** GET /api/app/metadata/entities/ref/{alias}/search?q={query} — Search entity references */
    findEntityReferences(alias: string, query: string): Promise<EntityReference[]> {
        return this.http.get(`/api/app/metadata/entities/ref/${encodeURIComponent(alias)}/search?q=${encodeURIComponent(query)}`);
    }

    /**
     * GET /api/app/metadata/entities/{id}/views — All view descriptors for an entity.
     *
     * Results are cached after the first successful fetch.
     * Individual descriptors are also stored in the per-view cache.
     *
     * @param id - the entity id (see {@link getEntity}).
     */
    async getEntityViews(id: string): Promise<ViewDescriptor[]> {
        const cached = this._viewsCache.get(id);
        if (cached !== undefined) return cached;

        const descriptors = await this.http.get<ViewDescriptor[]>(
            `/api/app/metadata/entities/${encodeURIComponent(id)}/views`,
        );

        this._viewsCache.set(id, descriptors);
        // Populate per-view cache from the bulk result to avoid redundant round-trips
        for (const d of descriptors) {
            if (d.view) {
                this._viewCache.set(`${id}:${d.view}`, d);
            }
        }
        return descriptors;
    }

    /**
     * GET /api/app/metadata/entities/{id}/views/{view} — Specific view descriptor.
     *
     * The result is cached after the first successful fetch.
     *
     * @param id - the entity id (see {@link getEntity}).
     */
    async getEntityView(id: string, view: string): Promise<ViewDescriptor> {
        const key = `${id}:${view}`;
        const cached = this._viewCache.get(key);
        if (cached !== undefined) return cached;

        const descriptor = await this.http.get<ViewDescriptor>(
            `/api/app/metadata/entities/${encodeURIComponent(id)}/views/${encodeURIComponent(view)}`,
        );

        this._viewCache.set(key, descriptor);
        return descriptor;
    }

    /**
     * Clears the in-memory ViewDescriptor cache.
     *
     * @param id - When provided, only the cache entries for that entity id
     *   are removed. When omitted, the entire cache is cleared.
     *
     * @example
     * // invalidate a single entity after a backend hot-reload
     * client.metadata.clearViewCache('Book');
     * // invalidate everything
     * client.metadata.clearViewCache();
     */
    clearViewCache(id?: string): void {
        if (id !== undefined) {
            this._viewsCache.delete(id);
            const prefix = `${id}:`;
            for (const key of this._viewCache.keys()) {
                if (key.startsWith(prefix)) this._viewCache.delete(key);
            }
        } else {
            this._viewCache.clear();
            this._viewsCache.clear();
        }
    }
}
