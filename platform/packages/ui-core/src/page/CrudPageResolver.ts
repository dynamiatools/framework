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
     * The `file` field contains the fully-qualified Java class name of the entity.
     */
    CrudPage: 'CrudPage',
    /**
     * Leaf page that renders a configuration panel.
     * The `file` field contains the configuration bean name.
     */
    ConfigPage: 'ConfigPage',
    /** Leaf page rendered in an external iframe (full URL in `file`). */
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
    /** Fully-qualified Java class name taken from {@link NavigationNode.file} */
    entityClass: string;
    /**
     * Virtual path taken from {@link NavigationNode.internalPath}.
     * Used as the base path for the CRUD resource API (`/api/{virtualPath}`).
     */
    virtualPath: string;
    /** Entity metadata loaded from the backend */
    entityMetadata: EntityMetadata;
    /** View descriptor resolved for the CRUD view */
    descriptor: ViewDescriptor;
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
     * - Fetches entity metadata via `client.metadata.getEntity(node.file)`
     * - Fetches all view descriptors via `client.metadata.getEntityViews(node.file)` and
     *   picks the first descriptor whose view name contains `"crud"` (case-insensitive),
     *   falling back to the first available descriptor.
     *
     * @param node   - NavigationNode with `type === "CrudPage"`, `file` and `internalPath` set.
     * @param client - {@link DynamiaClient} used to fetch metadata from the backend.
     * @throws {Error} when `node.file` or `node.internalPath` is missing, or no descriptor exists.
     */
    static async resolve(node: NavigationNode, client: DynamiaClient): Promise<CrudPageContext> {
        if (!node.file) {
            throw new Error(`CrudPage node "${node.id}" is missing the "file" field (entity class name)`);
        }
        if (!node.internalPath) {
            throw new Error(`CrudPage node "${node.id}" is missing "internalPath"`);
        }

        const entityClass = node.file;
        const virtualPath = node.internalPath;

        // Fetch entity metadata and all view descriptors in parallel
        const [entityMetadata, descriptors] = await Promise.all([
            client.metadata.getEntity(entityClass),
            client.metadata.getEntityViews(entityClass),
        ]);

        // Prefer a descriptor explicitly named "crud"; fall back to first available
        const chosen =
            descriptors.find(d => d.viewTypeName?.toLowerCase().includes('crud')) ??
            descriptors[0];

        if (!chosen) {
            throw new Error(`No view descriptor found for entity "${entityClass}"`);
        }

        return {
            node,
            entityClass,
            virtualPath,
            entityMetadata,
            descriptor: chosen,
        };
    }
}

