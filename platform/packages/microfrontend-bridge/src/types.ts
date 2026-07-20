/**
 * Cross-cutting context values registered server-side via one or more
 * `tools.dynamia.zk.ui.MicroFrontendHostContextProvider` beans, merged and delivered as the
 * `dynamiaHost` prop on every mount (except MicroFrontend's "auto" mode, which has no prop channel
 * at all). Shape is entirely app-defined; the fields below are just common examples.
 */
export interface DynamiaHostContext {
    tenantId?: string;
    locale?: string;
    apiBaseUrl?: string;

    [key: string]: unknown;
}

/**
 * Shape of what `tools.dynamia.zk.ui.MicroFrontend` actually delivers to a mounted bundle:
 * - In "custom-element" mode, these are assigned as properties directly on the element instance
 *   (`this.dynamiaEmit`, `this.dynamiaHost`, ...).
 * - In "mount-fn" mode, they're the second argument passed to `mountFn(container, props)` /
 *   `updateFn(container, props)`.
 *
 * Any other key is an app-defined prop bound via a ZUL attribute (e.g. `userId="@bind(vm.userId)"`)
 * or `MicroFrontend#addProp`/`#setProps`.
 */
export interface DynamiaMicrofrontendProps {
    /**
     * Sends `data` back to the server-side MicroFrontend component, which fires
     * "onMicrofrontendEvent" (bindable with `onMicrofrontendEvent="@command(...)"`). Undefined
     * when not running inside a Dynamia host — see {@link isDynamiaHost}.
     */
    dynamiaEmit?: (data: unknown) => void;
    /** Merged {@link DynamiaHostContext} from every registered MicroFrontendHostContextProvider. */
    dynamiaHost?: DynamiaHostContext;

    [key: string]: unknown;
}
