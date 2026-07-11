import { describe, it, expect, beforeEach, vi } from 'vitest';
import type { ActionMetadata } from '@dynamia-tools/sdk';
import {
  ClientActionRegistry,
  registerClientAction,
  isClientActionApplicable,
} from '../../src/actions/ClientAction.js';
import type { ClientAction, ClientActionContext } from '../../src/actions/ClientAction.js';

// Helper: minimal ActionMetadata
function meta(id: string, className?: string): ActionMetadata {
  return { id, name: id, ...(className ? { className } : {}) };
}

describe('ClientActionRegistry — resolution', () => {
  beforeEach(() => ClientActionRegistry.clear());

  it('resolves a registered action by id (exact)', () => {
    registerClientAction({ id: 'ExportAction', execute: vi.fn() });
    expect(ClientActionRegistry.resolve(meta('ExportAction'))).not.toBeNull();
  });

  it('resolves by id case-insensitively', () => {
    registerClientAction({ id: 'ExportAction', execute: vi.fn() });
    expect(ClientActionRegistry.resolve(meta('exportaction'))).not.toBeNull();
    expect(ClientActionRegistry.resolve(meta('EXPORTACTION'))).not.toBeNull();
  });

  it('resolves by action.className when id does not match', () => {
    registerClientAction({ id: 'ExportAction', execute: vi.fn() });
    const result = ClientActionRegistry.resolve(meta('something-else', 'ExportAction'));
    expect(result).not.toBeNull();
  });

  it('returns null for an unregistered action', () => {
    expect(ClientActionRegistry.resolve(meta('UnknownAction'))).toBeNull();
  });

  it('calls execute with the provided context', async () => {
    const execute = vi.fn();
    registerClientAction({ id: 'TestAction', execute });
    const ctx: ClientActionContext = { action: meta('TestAction'), request: { data: { id: 1 } } };
    await ClientActionRegistry.resolve(meta('TestAction'))!.execute(ctx);
    expect(execute).toHaveBeenCalledWith(ctx);
  });

  it('supports async execute', async () => {
    let resolved = false;
    registerClientAction({ id: 'AsyncAction', async execute() { await Promise.resolve(); resolved = true; } });
    await ClientActionRegistry.resolve(meta('AsyncAction'))!.execute({ action: meta('AsyncAction'), request: {} });
    expect(resolved).toBe(true);
  });

  it('last registration wins for the same id', () => {
    const first = vi.fn();
    const second = vi.fn();
    registerClientAction({ id: 'DupAction', execute: first });
    registerClientAction({ id: 'DupAction', execute: second });
    expect(ClientActionRegistry.resolve(meta('DupAction'))!.execute).toBe(second);
  });

  it('clear() removes all registered actions', () => {
    registerClientAction({ id: 'SomeAction', execute: vi.fn() });
    ClientActionRegistry.clear();
    expect(ClientActionRegistry.resolve(meta('SomeAction'))).toBeNull();
  });

  it('carries optional metadata fields (name, description, icon, renderer)', () => {
    const action: ClientAction = {
      id: 'RichAction', name: 'Rich', description: 'Desc', icon: 'star', renderer: 'custom-btn',
      execute: vi.fn(),
    };
    registerClientAction(action);
    const found = ClientActionRegistry.resolve(meta('RichAction'))!;
    expect(found.name).toBe('Rich');
    expect(found.description).toBe('Desc');
    expect(found.icon).toBe('star');
    expect(found.renderer).toBe('custom-btn');
  });
});

// ── isClientActionApplicable ─────────────────────────────────────────────────

describe('isClientActionApplicable', () => {
  const noop = vi.fn();

  it('returns true when applicableClass and applicableState are absent', () => {
    expect(isClientActionApplicable({ id: 'A', execute: noop }, 'Book', 'READ')).toBe(true);
  });

  it('matches by simple class name (case-insensitive)', () => {
    const a: ClientAction = { id: 'A', applicableClass: 'Book', execute: noop };
    expect(isClientActionApplicable(a, 'mybookstore.domain.Book')).toBe(true);
    expect(isClientActionApplicable(a, 'book')).toBe(true);
    expect(isClientActionApplicable(a, 'Author')).toBe(false);
  });

  it('matches multiple classes (array)', () => {
    const a: ClientAction = { id: 'A', applicableClass: ['Book', 'Author'], execute: noop };
    expect(isClientActionApplicable(a, 'Book')).toBe(true);
    expect(isClientActionApplicable(a, 'Author')).toBe(true);
    expect(isClientActionApplicable(a, 'Invoice')).toBe(false);
  });

  it('"all" wildcard matches any class', () => {
    const a: ClientAction = { id: 'A', applicableClass: 'all', execute: noop };
    expect(isClientActionApplicable(a, 'Book')).toBe(true);
    expect(isClientActionApplicable(a, 'Invoice')).toBe(true);
  });

  it('returns false when class required but none provided', () => {
    const a: ClientAction = { id: 'A', applicableClass: 'Book', execute: noop };
    expect(isClientActionApplicable(a, null)).toBe(false);
  });

  it('matches state aliases (list→READ, edit→UPDATE)', () => {
    const a: ClientAction = { id: 'A', applicableState: 'READ', execute: noop };
    expect(isClientActionApplicable(a, null, 'list')).toBe(true);
    expect(isClientActionApplicable(a, null, 'READ')).toBe(true);
    expect(isClientActionApplicable(a, null, 'CREATE')).toBe(false);
  });

  it('matches multiple states (array)', () => {
    const a: ClientAction = { id: 'A', applicableState: ['CREATE', 'UPDATE'], execute: noop };
    expect(isClientActionApplicable(a, null, 'create')).toBe(true);
    expect(isClientActionApplicable(a, null, 'edit')).toBe(true);
    expect(isClientActionApplicable(a, null, 'READ')).toBe(false);
  });

  it('returns false when state required but none provided', () => {
    const a: ClientAction = { id: 'A', applicableState: 'READ', execute: noop };
    expect(isClientActionApplicable(a, null, null)).toBe(false);
  });

  it('combines class AND state checks', () => {
    const a: ClientAction = { id: 'A', applicableClass: 'Book', applicableState: 'READ', execute: noop };
    expect(isClientActionApplicable(a, 'Book', 'READ')).toBe(true);
    expect(isClientActionApplicable(a, 'Book', 'CREATE')).toBe(false);
    expect(isClientActionApplicable(a, 'Author', 'READ')).toBe(false);
  });
});

// ── findApplicable ────────────────────────────────────────────────────────────

describe('ClientActionRegistry.findApplicable', () => {
  beforeEach(() => ClientActionRegistry.clear());

  it('returns all actions when no class/state given', () => {
    registerClientAction({ id: 'A', execute: vi.fn() });
    registerClientAction({ id: 'B', execute: vi.fn() });
    expect(ClientActionRegistry.findApplicable()).toHaveLength(2);
  });

  it('filters by class name', () => {
    registerClientAction({ id: 'BookExport', applicableClass: 'Book', execute: vi.fn() });
    registerClientAction({ id: 'AuthorExport', applicableClass: 'Author', execute: vi.fn() });
    registerClientAction({ id: 'Global', execute: vi.fn() });

    const result = ClientActionRegistry.findApplicable('Book');
    expect(result.map(a => a.id)).toEqual(expect.arrayContaining(['BookExport', 'Global']));
    expect(result.find(a => a.id === 'AuthorExport')).toBeUndefined();
  });

  it('filters by class AND state', () => {
    registerClientAction({ id: 'NewBook', applicableClass: 'Book', applicableState: 'READ', execute: vi.fn() });
    registerClientAction({ id: 'SaveBook', applicableClass: 'Book', applicableState: ['CREATE', 'UPDATE'], execute: vi.fn() });
    registerClientAction({ id: 'Global', execute: vi.fn() });

    const read = ClientActionRegistry.findApplicable('Book', 'READ');
    expect(read.map(a => a.id)).toEqual(expect.arrayContaining(['NewBook', 'Global']));
    expect(read.find(a => a.id === 'SaveBook')).toBeUndefined();

    const create = ClientActionRegistry.findApplicable('Book', 'create');
    expect(create.map(a => a.id)).toEqual(expect.arrayContaining(['SaveBook', 'Global']));
    expect(create.find(a => a.id === 'NewBook')).toBeUndefined();
  });

  it('uses filter() predicate directly for custom queries', () => {
    registerClientAction({ id: 'A', renderer: 'custom', execute: vi.fn() });
    registerClientAction({ id: 'B', execute: vi.fn() });
    const withRenderer = ClientActionRegistry.filter(a => a.renderer === 'custom');
    expect(withRenderer).toHaveLength(1);
    expect(withRenderer[0].id).toBe('A');
  });
});
