/**
 * The backend exposes no "current user" endpoint, so the login page remembers the username the user
 * typed (sessionStorage — gone with the browser session, like the session cookie itself) for the
 * header's user menu. Purely cosmetic: nothing authorizes off it.
 */
const KEY = 'dynamia.username';

export function rememberUsername(username: string): void {
  try {
    sessionStorage.setItem(KEY, username);
  } catch {
    // storage blocked (private mode...) — the menu just shows the generic label
  }
}

export function currentUsername(): string {
  try {
    return sessionStorage.getItem(KEY) || 'User';
  } catch {
    return 'User';
  }
}

/** Initials avatar as an inline SVG data URI — the user menu wants an image URL, and there is no photo. */
export function initialsAvatar(name: string): string {
  const initial = (name.trim()[0] ?? '?').toUpperCase();
  const svg =
    `<svg xmlns="http://www.w3.org/2000/svg" width="44" height="44">` +
    `<rect width="44" height="44" fill="#465fff"/>` +
    `<text x="50%" y="50%" dy=".35em" text-anchor="middle" fill="#fff" font-family="sans-serif" font-size="20">${initial.replace(/[<&>]/g, '')}</text>` +
    `</svg>`;
  return `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`;
}
