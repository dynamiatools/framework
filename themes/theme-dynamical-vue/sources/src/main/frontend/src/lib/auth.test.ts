import { describe, expect, it, vi } from 'vitest';
import { withAuthGuard } from './auth.js';

const isLogin = (pathname: string): boolean => pathname === '/login';

function guarded(response: Response) {
  const onUnauthorized = vi.fn();
  const guard = withAuthGuard(async () => response, isLogin, onUnauthorized);
  return { guard, onUnauthorized };
}

describe('withAuthGuard', () => {
  it('reports a 401 once and still returns the response', async () => {
    const response = new Response('', { status: 401 });
    const { guard, onUnauthorized } = guarded(response);

    expect(await guard('/api/x')).toBe(response);
    await guard('/api/y');

    expect(onUnauthorized).toHaveBeenCalledTimes(1);
  });

  it('reports a redirect that ends on the login page', async () => {
    const response = new Response('<html>', { status: 200 });
    Object.defineProperties(response, { redirected: { value: true }, url: { value: 'http://app/login' } });
    const { guard, onUnauthorized } = guarded(response);

    await guard('/api/x');

    expect(onUnauthorized).toHaveBeenCalledTimes(1);
  });

  it('ignores successful responses and other redirects', async () => {
    const other = new Response('{}', { status: 200 });
    Object.defineProperties(other, { redirected: { value: true }, url: { value: 'http://app/api/y' } });
    const { guard, onUnauthorized } = guarded(other);

    await guard('/api/x');

    expect(onUnauthorized).not.toHaveBeenCalled();
  });
});
