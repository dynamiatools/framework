import { ref, watch } from 'vue';

const SKIN_COOKIE = 'skin';
const DEFAULT_SKIN = 'blue';

function readSkinCookie(): string | null {
  const match = document.cookie.match(/(?:^|;\s*)skin=([^;]+)/);
  return match?.[1] ? decodeURIComponent(match[1]) : null;
}

/**
 * Applies the active skin as `data-skin` on `<html>` (see src/styles/main.css) and persists it
 * in the same `skin` cookie `CommonController.setupSkin` already reads server-side — so a ZK
 * fallback page reached via `/page/...` (see resolveEmbedSrc.ts) honors the same skin choice.
 */
export function useSkin() {
  const skin = ref(readSkinCookie() ?? DEFAULT_SKIN);

  function apply(value: string): void {
    document.documentElement.setAttribute('data-skin', value);
    document.cookie = `skin=${encodeURIComponent(value)};path=/;max-age=31536000`;
  }

  function setSkin(value: string): void {
    skin.value = value;
  }

  watch(skin, apply, { immediate: true });

  return { skin, setSkin };
}
