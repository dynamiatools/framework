import { ref, watch } from 'vue';

const SKIN_COOKIE = 'skin';
const DEFAULT_SKIN = 'blue';

function readSkinCookie(): string | null {
  const match = document.cookie.match(/(?:^|;\s*)skin=([^;]+)/);
  return match?.[1] ? decodeURIComponent(match[1]) : null;
}

/**
 * Applies the active skin as `data-skin` on `<html>` (see src/styles/base.css) and persists it
 * in the same `skin` cookie `CommonController.setupSkin` already reads server-side — so a ZK
 * fallback page reached via `/page/...` (see resolveEmbedSrc.ts) honors the same skin choice.
 *
 * The "dark" skin is also the package's dark mode: ThemeProvider (@dynamia-tools/tailadmin-vue)
 * seeds itself from `localStorage.theme` on mount, so it is written here, before the app mounts.
 * Once mounted, AppShell keeps the two in sync in both directions (skin picker <-> theme toggle).
 */
export function useSkin() {
  const skin = ref(readSkinCookie() ?? DEFAULT_SKIN);

  function apply(value: string): void {
    document.documentElement.setAttribute('data-skin', value);
    document.cookie = `${SKIN_COOKIE}=${encodeURIComponent(value)};path=/;max-age=31536000`;
    try {
      localStorage.setItem('theme', value === 'dark' ? 'dark' : 'light');
    } catch {
      // storage blocked — dark mode just won't survive a reload
    }
  }

  function setSkin(value: string): void {
    skin.value = value;
  }

  watch(skin, apply, { immediate: true });

  return { skin, setSkin };
}
