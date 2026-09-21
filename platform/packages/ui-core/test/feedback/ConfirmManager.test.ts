import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ConfirmManager } from '../../src/feedback/ConfirmManager.js';

describe('ConfirmManager', () => {
  let manager: ConfirmManager;

  beforeEach(() => {
    manager = new ConfirmManager();
  });

  it('exposes the request as current() once queued', () => {
    void manager.confirm({ message: 'Delete?' });
    const current = manager.current();
    expect(current?.message).toBe('Delete?');
    expect(current?.confirmLabel).toBe('Yes');
    expect(current?.cancelLabel).toBe('No');
    expect(current?.variant).toBe('warning');
  });

  it('resolves true when answered with true', async () => {
    const promise = manager.confirm({ message: 'Delete?' });
    manager.answer(manager.current()!.id, true);
    await expect(promise).resolves.toBe(true);
  });

  it('resolves false when answered with false', async () => {
    const promise = manager.confirm({ message: 'Delete?' });
    manager.answer(manager.current()!.id, false);
    await expect(promise).resolves.toBe(false);
  });

  it('clears current() after being answered', () => {
    void manager.confirm({ message: 'Delete?' });
    manager.answer(manager.current()!.id, true);
    expect(manager.current()).toBeNull();
  });

  it('queues a second request behind the first and surfaces it once answered', async () => {
    const first = manager.confirm({ message: 'First?' });
    const second = manager.confirm({ message: 'Second?' });
    expect(manager.current()?.message).toBe('First?');

    manager.answer(manager.current()!.id, true);
    await expect(first).resolves.toBe(true);
    expect(manager.current()?.message).toBe('Second?');

    manager.answer(manager.current()!.id, false);
    await expect(second).resolves.toBe(false);
    expect(manager.current()).toBeNull();
  });

  it('answer() is a no-op when the id does not match the current request', async () => {
    const promise = manager.confirm({ message: 'Delete?' });
    manager.answer('some-other-id', true);
    expect(manager.current()).not.toBeNull();
    manager.answer(manager.current()!.id, true);
    await expect(promise).resolves.toBe(true);
  });

  it('notifies subscribers when a request is queued and when it is answered', () => {
    const handler = vi.fn();
    manager.on(handler);
    void manager.confirm({ message: 'Delete?' });
    expect(handler).toHaveBeenCalledTimes(1);
    manager.answer(manager.current()!.id, true);
    expect(handler).toHaveBeenCalledTimes(2);
    expect(handler).toHaveBeenLastCalledWith(null);
  });

  it('does not re-emit while a second request is merely queued (still hidden behind the first)', () => {
    const handler = vi.fn();
    manager.on(handler);
    void manager.confirm({ message: 'First?' });
    void manager.confirm({ message: 'Second?' });
    expect(handler).toHaveBeenCalledTimes(1);
  });

  it('unsubscribe stops further notifications', () => {
    const handler = vi.fn();
    const unsubscribe = manager.on(handler);
    unsubscribe();
    void manager.confirm({ message: 'Delete?' });
    expect(handler).not.toHaveBeenCalled();
  });
});
