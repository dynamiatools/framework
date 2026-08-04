import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { ToastManager } from '../../src/feedback/ToastManager.js';

describe('ToastManager', () => {
  let manager: ToastManager;

  beforeEach(() => {
    vi.useFakeTimers();
    manager = new ToastManager();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('shows a toast with defaults applied', () => {
    manager.show({ message: 'Saved' });
    const item = manager.list()[0];
    expect(item?.message).toBe('Saved');
    expect(item?.variant).toBe('info');
    expect(item?.duration).toBe(4000);
  });

  it('returns a unique id usable with dismiss', () => {
    const id = manager.show({ message: 'Hi' });
    expect(manager.list()).toHaveLength(1);
    manager.dismiss(id);
    expect(manager.list()).toHaveLength(0);
  });

  it('dismiss is a no-op for an unknown id', () => {
    manager.show({ message: 'Hi' });
    manager.dismiss('unknown-id');
    expect(manager.list()).toHaveLength(1);
  });

  it('auto-dismisses after the given duration', () => {
    manager.show({ message: 'Bye', duration: 1000 });
    expect(manager.list()).toHaveLength(1);
    vi.advanceTimersByTime(999);
    expect(manager.list()).toHaveLength(1);
    vi.advanceTimersByTime(1);
    expect(manager.list()).toHaveLength(0);
  });

  it('duration 0 never auto-dismisses', () => {
    manager.show({ message: 'Sticky', duration: 0 });
    vi.advanceTimersByTime(100_000);
    expect(manager.list()).toHaveLength(1);
  });

  it('clear() removes all toasts and cancels pending timers', () => {
    manager.show({ message: 'A' });
    manager.show({ message: 'B' });
    manager.clear();
    expect(manager.list()).toHaveLength(0);
    vi.advanceTimersByTime(10_000);
    expect(manager.list()).toHaveLength(0);
  });

  it('notifies subscribers on show and dismiss', () => {
    const handler = vi.fn();
    manager.on(handler);
    const id = manager.show({ message: 'Hi' });
    expect(handler).toHaveBeenCalledTimes(1);
    expect(handler).toHaveBeenLastCalledWith(manager.list());
    manager.dismiss(id);
    expect(handler).toHaveBeenCalledTimes(2);
  });

  it('unsubscribe stops further notifications', () => {
    const handler = vi.fn();
    const unsubscribe = manager.on(handler);
    unsubscribe();
    manager.show({ message: 'Hi' });
    expect(handler).not.toHaveBeenCalled();
  });

  it('off() removes a previously registered handler', () => {
    const handler = vi.fn();
    manager.on(handler);
    manager.off(handler);
    manager.show({ message: 'Hi' });
    expect(handler).not.toHaveBeenCalled();
  });

  it('list() returns oldest-first snapshots that do not mutate internal state', () => {
    manager.show({ message: 'A' });
    manager.show({ message: 'B' });
    const snapshot = manager.list();
    expect(snapshot.map(t => t.message)).toEqual(['A', 'B']);
    snapshot.pop();
    expect(manager.list()).toHaveLength(2);
  });
});
