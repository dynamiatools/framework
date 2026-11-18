/**
 * Undo and redo over immutable states. Typing in a field creates many states a second, so `push` can be given a key:
 * states with the same key that come within `coalesceMs` of each other replace each other and count as one step.
 */
export class History<T> {
  private past: T[] = [];
  private future: T[] = [];
  private current: T;
  private lastKey: string | null = null;
  private lastAt = 0;

  constructor(
    initial: T,
    private readonly limit = 100,
    private readonly coalesceMs = 800,
    private readonly now: () => number = () => Date.now(),
  ) {
    this.current = initial;
  }

  get present(): T {
    return this.current;
  }

  get canUndo(): boolean {
    return this.past.length > 0;
  }

  get canRedo(): boolean {
    return this.future.length > 0;
  }

  /** Makes `state` the present. A state equal (same reference) to the present is ignored. */
  push(state: T, key: string | null = null): void {
    if (state === this.current) {
      return;
    }
    const at = this.now();
    const merge = key !== null && key === this.lastKey && at - this.lastAt <= this.coalesceMs && this.past.length > 0;
    if (!merge) {
      this.past.push(this.current);
      if (this.past.length > this.limit) {
        this.past.shift();
      }
    }
    this.current = state;
    this.future = [];
    this.lastKey = key;
    this.lastAt = at;
  }

  undo(): T {
    const previous = this.past.pop();
    if (previous !== undefined) {
      this.future.push(this.current);
      this.current = previous;
      this.lastKey = null;
    }
    return this.current;
  }

  redo(): T {
    const next = this.future.pop();
    if (next !== undefined) {
      this.past.push(this.current);
      this.current = next;
      this.lastKey = null;
    }
    return this.current;
  }

  /** Forgets the past and the future and makes `state` the present (after loading or saving). */
  reset(state: T): void {
    this.past = [];
    this.future = [];
    this.current = state;
    this.lastKey = null;
  }
}
