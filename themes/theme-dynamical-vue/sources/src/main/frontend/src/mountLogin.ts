import { createApp } from 'vue';
import Login from './login/Login.vue';
import './styles/base.css';

/** Options of {@link mountLogin}. */
export interface MountLoginOptions {
  /** Element (or selector) to mount on. Defaults to `#app`. */
  mount?: string | Element;
  /** URL the credentials are posted to as JSON `{username, password}`. Defaults to `/login/json`. */
  endpoint?: string;
}

/**
 * Mounts the login page, a separate, minimal bundle (no @dynamia-tools/vue plugin, no dynamia-embed):
 * unauthenticated visitors never download the full app shell's JS/CSS. Only mirrors the active
 * skin so branding stays consistent with the authenticated app (see lib/useSkin.ts).
 */
export function mountLogin(options: MountLoginOptions = {}): void {
  const skinCookie = document.cookie.match(/(?:^|;\s*)skin=([^;]+)/)?.[1];
  if (skinCookie) {
    document.documentElement.setAttribute('data-skin', decodeURIComponent(skinCookie));
  }

  // Same for dark mode: the app shell persists it in localStorage.theme (see lib/useSkin.ts).
  try {
    if (localStorage.getItem('theme') === 'dark') document.documentElement.classList.add('dark');
  } catch {
    // storage blocked — light login page
  }

  createApp(Login, { endpoint: options.endpoint }).mount(options.mount ?? '#app');
}
