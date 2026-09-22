// CrudPageResolver: resolves a NavigationNode of type "CrudPage" into entity metadata and view descriptor

import type {DynamiaClient, EntityMetadata, NavigationNode, ViewDescriptor} from '@dynamia-tools/sdk';

// ── Known navigation page types ───────────────────────────────────────────────

/**
 * Known navigation page-type identifiers returned by the Dynamia Platform server.
 * The `type` field on a {@link NavigationNode} is one of these values (or any custom string
 * defined server-side).
 */
export const NavigationPageTypes = {
    /** Top-level application module */
    Module: 'Module',
    /** Group of pages within a module */
    PageGroup: 'PageGroup',
    /** Generic leaf page (iframe / ZUL / etc.) */
    Page: 'Page',
    /**
     * Leaf page that is automatically backed by a full CRUD interface for a single entity.
     * `NavigationNode` carries no entity/class information — {@link CrudPageResolver.resolve}
     * resolves the entity server-side from the node's `internalPath` via
     * `GET /api/app/metadata/entities/by-path`.
     */
    CrudPage: 'CrudPage',
    /** Leaf page that renders a configuration panel. Not yet resolved by this module. */
    ConfigPage: 'ConfigPage',
    /** Leaf page rendered in an external iframe. Not yet resolved by this module. */
    ExternalPage: 'ExternalPage',
} as const;

export type NavigationPageType =
    | (typeof NavigationPageTypes)[keyof typeof NavigationPageTypes]
    | string;

// ── CrudPageContext ───────────────────────────────────────────────────────────

/**
 * Fully-resolved data required to render a CrudPage.
 * Produced by {@link CrudPageResolver.resolve}.
 */
export interface CrudPageContext {
    /** The original NavigationNode */
    node: NavigationNode;
    /**
     * Entity id (the entity class's simple name, e.g. `"Invoice"`) resolved server-side from the
     * node's `internalPath` — see {@link EntityMetadata.id}. Not a fully-qualified Java class name.
     */
    entityClass: string;
    /**ie
     * Virtual path taken from {@link NavigationNode.internalPath}.
     * Used as the base path for the CRUD resource API (`/api/{virtualPath}`).
     */
    virtualPath: string;
    /** Entity metadata loaded from the backend */
    entityMetadata: EntityMetadata;
    /** View descriptor resolved for the CRUD view (crud / table type) */
    descriptor: ViewDescriptor;
    /** Descriptor used by the DataSetView (table/tree), when available. */
    dataSetDescriptor: ViewDescriptor;
    /**
     * The dedicated form view descriptor (`view === "form"`), when available.
     * Used to build the FormView with its own fields, layout (columns) and fieldGroups.
     * Falls back to {@link descriptor} when no separate form descriptor exists.
     */
    formDescriptor: ViewDescriptor;
}

// ── CrudPageResolver ──────────────────────────────────────────────────────────

/**
 * Framework-agnostic utility that resolves a {@link NavigationNode} of type
 * `"CrudPage"` into the entity metadata and view descriptor needed to render a
 * full CRUD interface.
 *
 * Intended to be used by framework-specific composables (e.g. `useCrudPage` in the
 * `@dynamia-tools/vue` package).
 *
 * Example:
 * <pre>{@code
 * if (CrudPageResolver.isCrudPage(node)) {
 *   const ctx = await CrudPageResolver.resolve(node, client);
 *   // ctx.descriptor, ctx.entityMetadata, ctx.virtualPath …
 * }
 * }</pre>
 */
export class CrudPageResolver {
    /**
     * Returns `true` when the given node has `type === "CrudPage"`.
     */
    static isCrudPage(node: NavigationNode): boolean {
        return node.type === NavigationPageTypes.CrudPage;
    }

    /**
     * Resolves a `CrudPage` navigation node into its entity metadata and view descriptor.
     *
     * - Resolves entity metadata via `client.metadata.getEntityByPath(node.internalPath)` — the
     *   server maps the page's virtual path to its backing entity class; no entity/class
     *   information needs to travel in the navigation JSON.
     * - Fetches all view descriptors via `client.metadata.getEntityViews(entityMetadata.id)` and
     *   picks the first descriptor whose view name contains `"crud"` (case-insensitive),
     *   falling back to the first available descriptor.
     *
     * @param node   - NavigationNode with `type === "CrudPage"` and `internalPath` set.
     * @param client - {@link DynamiaClient} used to fetch metadata from the backend.
     * @throws {Error} when `node.internalPath` is missing, no CrudPage is registered at that path,
     *   or no descriptor exists.
     */
    static async resolve(node: NavigationNode, client: DynamiaClient): Promise<CrudPageContext> {
        if (!node.internalPath) {
            throw new Error(`CrudPage node "${node.id}" is missing "internalPath"`);
        }

        const virtualPath = node.internalPath;

        const entityMetadata = await client.metadata.getEntityByPath(virtualPath);
        const entityClass = entityMetadata.id;

        const descriptors = await client.metadata.getEntityViews(entityClass);

        // Prefer a descriptor explicitly typed "crud"; fall back to first available
        const chosen =
            descriptors.find(d => d.view?.toLowerCase().includes('crud')) ??
            descriptors[0];

        if (!chosen) {
            throw new Error(`No view descriptor found for entity "${entityClass}"`);
        }

        // Resolve the dedicated form descriptor (fields + layout + fieldGroups).
        // ZK CrudView does the same: it independently fetches the "form" descriptor
        // for its FormView while the outer CrudView uses the "crud" descriptor.
        const formDescriptor =
            descriptors.find(d => d.view?.toLowerCase() === 'form') ??
            chosen;

        const dataSetViewType = String(chosen.params?.['dataSetViewType'] ?? 'table').toLowerCase();
        const dataSetDescriptor =
            (dataSetViewType === 'tree'
                ? descriptors.find(d => d.view?.toLowerCase() === 'tree' || d.view?.toLowerCase().includes('tree'))
                : descriptors.find(d => d.view?.toLowerCase() === 'table' || d.view?.toLowerCase().includes('table'))
            )
            // Fallback for legacy descriptors where "table" view is missing but fields are present
            ?? descriptors.find(d => (d.fields?.length ?? 0) > 0 && d.view?.toLowerCase() !== 'form')
            ?? chosen;

        return {
            node,
            entityClass,
            virtualPath,
            entityMetadata,
            descriptor: chosen,
            dataSetDescriptor,
            formDescriptor,
        };
    }
}

