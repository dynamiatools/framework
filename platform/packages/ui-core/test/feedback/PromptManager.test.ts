import { describe, it, expect, beforeEach, vi } from 'vitest';
import { PromptManager } from '../../src/feedback/PromptManager.js';

describe('PromptManager', () => {
  let manager: PromptManager;

  beforeEach(() => {
    manager = new PromptManager();
  });

  it('exposes the request as current() once queued, with defaults applied', () => {
    void manager.prompt({ message: 'New quantity?' });
    const current = manager.current();
    expect(current?.message).toBe('New quantity?');
    expect(current?.confirmLabel).toBe('OK');
    expect(current?.cancelLabel).toBe('Cancel');
    expect(current?.inputType).toBe('text');
  });

  it('carries optional fields through when supplied', () => {
    void manager.prompt({
      message: 'New quantity?',
      title: 'Restock',
      defaultValue: '10',
      placeholder: 'e.g. 10',
      inputType: 'number',
    });
    const current = manager.current();
    expect(current?.title).toBe('Restock');
    expect(current?.defaultValue).toBe('10');
    expect(current?.placeholder).toBe('e.g. 10');
    expect(current?.inputType).toBe('number');
  });

  it('resolves with the answered value', async () => {
    const promise = manager.prompt({ message: 'New quantity?' });
    manager.answer(manager.current()!.id, '42');
    await expect(promise).resolves.toBe('42');
  });

  it('resolves with null when cancelled', async () => {
    const promise = manager.prompt({ message: 'New quantity?' });
    manager.answer(manager.current()!.id, null);
    await expect(promise).resolves.toBeNull();
  });

  it('clears current() after being answered', () => {
    void manager.prompt({ message: 'New quantity?' });
    manager.answer(manager.current()!.id, '42');
    expect(manager.current()).toBeNull();
  });

  it('queues a second request behind the first and surfaces it once answered', async () => {
    const first = manager.prompt({ message: 'First?' });
    const second = manager.prompt({ message: 'Second?' });
    expect(manager.current()?.message).toBe('First?');

    manager.answer(manager.current()!.id, 'a');
    await expect(first).resolves.toBe('a');
    expect(manager.current()?.message).toBe('Second?');

    manager.answer(manager.current()!.id, null);
    await expect(second).resolves.toBeNull();
    expect(manager.current()).toBeNull();
  });

  it('answer() is a no-op when the id does not match the current request', async () => {
    const promise = manager.prompt({ message: 'New quantity?' });
    manager.answer('some-other-id', '42');
    expect(manager.current()).not.toBeNull();
    manager.answer(manager.current()!.id, '42');
    await expect(promise).resolves.toBe('42');
  });

  it('notifies subscribers when a request is queued and when it is answered', () => {
    const handler = vi.fn();
    manager.on(handler);
    void manager.prompt({ message: 'New quantity?' });
    expect(handler).toHaveBeenCalledTimes(1);
    manager.answer(manager.current()!.id, '42');
    expect(handler).toHaveBeenCalledTimes(2);
    expect(handler).toHaveBeenLastCalledWith(null);
  });

  it('unsubscribe stops further notifications', () => {
    const handler = vi.fn();
    const unsubscribe = manager.on(handler);
    unsubscribe();
    void manager.prompt({ message: 'New quantity?' });
    expect(handler).not.toHaveBeenCalled();
  });
});
