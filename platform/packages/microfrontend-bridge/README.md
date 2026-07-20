# @dynamia-tools/microfrontend-bridge

> Typed helpers for JS/TS bundles (Vue, React, Svelte, plain JS) embedded via `tools.dynamia.zk.ui.MicroFrontend`.

`MicroFrontend` embeds an external JS bundle inside a ZK page and talks to it through a small,
ad-hoc protocol: an injected `dynamiaEmit(data)` callback to notify the server, and a
`dynamiaHost` prop with server-registered context (tenant, locale, API base URL, ...). This
package wraps that protocol in three small, fully-typed functions so bundle authors don't have to
know the wire details or duck-type props by hand.

Zero runtime dependencies, works with any bundler/framework — this only touches plain objects.

## Installation

```bash
npm install @dynamia-tools/microfrontend-bridge
```

## API

- `isDynamiaHost(target)` — whether `dynamiaEmit` was actually injected (false in local/standalone
  dev, and always false in `MicroFrontend`'s `"auto"` mode, which has no prop channel at all).
- `emitToHost(target, data)` — sends `data` back to the server, which fires `onMicrofrontendEvent`.
  No-ops with a console warning (never throws) when not running inside a Dynamia host.
- `getHostContext(target)` — reads the merged `dynamiaHost` context, or `{}` if none was registered.
- Types: `DynamiaMicrofrontendProps`, `DynamiaHostContext`.

In every function, `target` is whatever object actually carries the props: the custom element
instance itself (`this`) in `MODE_CUSTOM_ELEMENT`, or the `props` argument in `MODE_MOUNT_FN`'s
`mountFn`/`updateFn`.

## Usage

### Custom element (e.g. Vue's `defineCustomElement`)

```ts
import { defineCustomElement } from 'vue';
import { emitToHost, getHostContext, type DynamiaMicrofrontendProps } from '@dynamia-tools/microfrontend-bridge';
import App from './App.vue';

const MyWidget = defineCustomElement({
  props: ['userId'],
  setup(props) {
    // `this` inside a Vue defineCustomElement's methods is the element instance itself,
    // which is exactly what MicroFrontend assigns dynamiaEmit/dynamiaHost onto.
    function notifyHost(data: unknown) {
      emitToHost(this as unknown as DynamiaMicrofrontendProps, data);
    }
    const host = getHostContext(this as unknown as DynamiaMicrofrontendProps);
    // ...use props.userId, host.tenantId, host.locale, etc.
  },
});

customElements.define('my-widget', MyWidget);
```

```xml
<!-- ZUL side -->
<microfrontend src="/bundles/my-widget.js" tag="my-widget" userId="${user.id}"
               onMicrofrontendEvent="@command('handleWidgetEvent', data=event.data)"/>
```

### `mount-fn` convention (single-spa style)

```ts
import { emitToHost, getHostContext, type DynamiaMicrofrontendProps } from '@dynamia-tools/microfrontend-bridge';

window.mountMyApp = (container: HTMLElement, props: DynamiaMicrofrontendProps) => {
  const host = getHostContext(props);
  fetch(`${host.apiBaseUrl ?? '/api'}/things?tenant=${host.tenantId}`)
    .then((res) => res.json())
    .then((things) => render(container, things, () => emitToHost(props, { type: 'clicked' })));
};

window.updateMyApp = (container: HTMLElement, props: DynamiaMicrofrontendProps) => {
  // called instead of a full unmount+mount when only props changed (MicroFrontend's updateFn=)
  rerender(container, props);
};

window.unmountMyApp = (container: HTMLElement) => {
  container.innerHTML = '';
};
```

```xml
<!-- ZUL side -->
<microfrontend src="/bundles/my-app.js" mode="mount-fn"
               mountFn="mountMyApp" unmountFn="unmountMyApp" updateFn="updateMyApp"/>
```

## Providing host context (Java side)

```java
@Component
public class TenantHostContextProvider implements MicroFrontendHostContextProvider {
    @Override
    public Map<String, Object> getHostContext() {
        Account account = AccountServiceAPI.getCurrentAccount();
        return Map.of(
            "tenantId", account.getId(),
            "locale", LocaleUtils.getCurrentLocale().toLanguageTag(),
            "apiBaseUrl", "/api"
        );
    }
}
```

## License

Apache-2.0
