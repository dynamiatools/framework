import { createApp } from 'vue';
import Login from './login/Login.vue';
import './styles/base.css';

// Login page is a separate, minimal bundle (no @dynamia-tools/vue plugin, no dynamia-embed) —
// unauthenticated visitors never download the full app shell's JS/CSS. Only mirror the active
// skin so branding stays consistent with the authenticated app (see lib/useSkin.ts).
const skinCookie = document.cookie.match(/(?:^|;\s*)skin=([^;]+)/)?.[1];
if (skinCookie) {
  document.documentElement.setAttribute('data-skin', decodeURIComponent(skinCookie));
}

createApp(Login).mount('#app');
