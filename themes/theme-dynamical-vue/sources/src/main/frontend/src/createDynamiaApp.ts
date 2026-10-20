import { createApp, h, type App, type Component } from 'vue';
import { DynamiaVue } from '@dynamia-tools/vue';
import { registerDynamiaEmbed } from '@dynamia-tools/ui-core/embed';
import ThemeProvider from '@dynamia-tools/tailadmin-vue/components/layout/ThemeProvider.vue';
import SidebarProvider from '@dynamia-tools/tailadmin-vue/components/layout/SidebarProvider.vue';
import AppShell from './layout/AppShell.vue';
import { router } from './router.js';
import { client } from './lib/client.js';
import { registerThemeFieldComponents } from './lib/fieldComponents.js';
import './styles/app.css';

/** Options of {@link createDynamiaApp}. */
export interface DynamiaAppOptions {
  /** Element (or selector) to mount on. Defaults to `#app`. */
  mount?: string | Element;
  /** Components mounted once inside the shell, e.g. hosts that render app-specific flow steps. */
  hosts?: Component[];
  /** Called with the app before it is mounted, to install plugins or register more field components. */
  setup?: (app: App) => void;
}

/**
 * Builds and mounts the authenticated shell: sidebar and header driven by the navigation tree, CRUD / config /
 * embedded pages, and the DynamiaVue plugin wired to the shell's client.
 *
 * <pre>{@code
 * createDynamiaApp({ hosts: [StarRatingHost] })
 * }</pre>
 *
 * @returns the mounted Vue app
 */
export function createDynamiaApp(options: DynamiaAppOptions = {}): App {
  registerDynamiaEmbed();
  registerThemeFieldComponents();

  const hosts = options.hosts ?? [];
  const app = createApp({
    render: () =>
      h(ThemeProvider, null, {
        default: () =>
          h(SidebarProvider, null, {
            default: () => h(AppShell, null, { hosts: () => hosts.map(host => h(host)) }),
          }),
      }),
  });

  app.use(router);
  app.use(DynamiaVue, { client });
  options.setup?.(app);
  app.mount(options.mount ?? '#app');
  return app;
}
