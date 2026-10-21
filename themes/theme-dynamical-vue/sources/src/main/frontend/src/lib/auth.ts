/**
 * How the shell reacts to a missing or expired session. The defaults match the backend that serves the
 * theme (Spring Security with `JsonUsernamePasswordAuthFilter`); an app with another sign-in overrides them
 * through `createDynamiaApp({ auth })`.
 */
export interface DynamiaAuthOptions {
  /** Page that signs the user in. Defaults to `/login`. */
  loginUrl?: string;
  /** Called when the backend answers 401, or redirects an API call to the login page. Defaults to going to `loginUrl`. */
  onUnauthorized?: () => void;
  /** Called by the user menu's sign out. Defaults to `POST /logout` and then going to `loginUrl`. */
  logout?: () => Promise<void> | void;
}

const DEFAULT_LOGIN_URL = '/login';

let options: DynamiaAuthOptions = {};

/** Replaces the auth behavior; call it before the first request (createDynamiaApp does). */
export function configureAuth(next: DynamiaAuthOptions): void {
  options = next;
}

export function loginUrl(): string {
  return options.loginUrl ?? DEFAULT_LOGIN_URL;
}

export function handleUnauthorized(): void {
  if (options.onUnauthorized) options.onUnauthorized();
  else window.location.href = loginUrl();
}

export async function signOut(): Promise<void> {
  if (options.logout) {
    await options.logout();
    return;
  }
  await fetch('/logout', { method: 'POST', credentials: 'include' });
  window.location.href = loginUrl();
}

/**
 * Wraps `fetch` so a 401, or a redirect that ends on the login page, calls `onUnauthorized` once.
 * The response is returned untouched, so the caller still sees the failure.
 */
export function withAuthGuard(
  base: typeof fetch,
  isLoginPath: (pathname: string) => boolean,
  onUnauthorized: () => void,
): typeof fetch {
  let fired = false;
  return async (input, init) => {
    const response = await base(input, init);
    const toLogin = response.redirected && isLoginPath(new URL(response.url).pathname);
    if ((response.status === 401 || toLogin) && !fired) {
      fired = true;
      onUnauthorized();
    }
    return response;
  };
}
