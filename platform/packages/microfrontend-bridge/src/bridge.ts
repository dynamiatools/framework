import type { DynamiaHostContext, DynamiaMicrofrontendProps } from './types.js';

/**
 * Whether `target` looks like it's actually running inside a `tools.dynamia.zk.ui.MicroFrontend`
 * host, i.e. `dynamiaEmit` was injected. False for MicroFrontend's "auto" mode (no prop channel at
 * all) and for local/standalone dev runs outside any Dynamia host.
 *
 * @param target a custom element instance (pass `this`) or the props object a mount-fn/updateFn receives
 */
export function isDynamiaHost(target: DynamiaMicrofrontendProps | null | undefined): boolean {
    return typeof target?.dynamiaEmit === 'function';
}

/**
 * Sends `data` back to the server-side MicroFrontend component hosting this bundle, which fires
 * "onMicrofrontendEvent" server-side (bindable with `onMicrofrontendEvent="@command(...)"`). Works
 * the same whether `target` is a custom element instance (pass `this`) or the `props` object a
 * mount-fn/updateFn receives — both carry `dynamiaEmit` the same way. No-ops with a console warning
 * instead of throwing when not running inside a Dynamia host (see {@link isDynamiaHost}), so
 * bundles keep working standalone during local development.
 *
 * @param target a custom element instance (pass `this`) or the props object a mount-fn/updateFn receives
 * @param data JSON-serializable payload, delivered as `event.getData()` server-side
 */
export function emitToHost(target: DynamiaMicrofrontendProps, data: unknown): void {
    if (typeof target?.dynamiaEmit === 'function') {
        target.dynamiaEmit(data);
    } else if (typeof console !== 'undefined') {
        console.warn(
            '[@dynamia-tools/microfrontend-bridge] emitToHost() called but dynamiaEmit is not available ' +
            '- not running inside a MicroFrontend host, or this is "auto" mode (no prop channel).'
        );
    }
}

/**
 * Returns the host context merged from every registered `MicroFrontendHostContextProvider`, or
 * `{}` if none were registered (or not running inside a Dynamia host).
 *
 * @param target a custom element instance (pass `this`) or the props object a mount-fn/updateFn receives
 */
export function getHostContext(target: DynamiaMicrofrontendProps | null | undefined): DynamiaHostContext {
    return target?.dynamiaHost ?? {};
}
