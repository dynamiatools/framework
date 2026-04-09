import type { CrudMode } from '../types/state.js';

export type CrudActionState = 'READ' | 'CREATE' | 'UPDATE' | 'DELETE';
export type CrudActionStateAlias = CrudMode | CrudActionState | Lowercase<CrudActionState>;

const CRUD_MODE_TO_ACTION_STATE: Record<CrudMode, CrudActionState> = {
  list: 'READ',
  create: 'CREATE',
  edit: 'UPDATE',
};

export function crudModeToActionState(mode: CrudMode): CrudActionState {
  return CRUD_MODE_TO_ACTION_STATE[mode];
}

export function normalizeCrudActionState(state?: CrudActionStateAlias | null): CrudActionState | null {
  if (!state) return null;

  switch (String(state).trim().toUpperCase()) {
    case 'LIST':
    case 'READ':
      return 'READ';
    case 'CREATE':
      return 'CREATE';
    case 'EDIT':
    case 'UPDATE':
      return 'UPDATE';
    case 'DELETE':
      return 'DELETE';
    default:
      return null;
  }
}

export function isCrudActionStateApplicable(
  currentState?: CrudActionStateAlias | null,
  applicableStates?: readonly string[] | null,
): boolean {
  if (!applicableStates || applicableStates.length === 0) {
    return true;
  }

  const normalizedCurrentState = normalizeCrudActionState(currentState);
  if (!normalizedCurrentState) {
    return false;
  }

  return applicableStates.some(state => normalizeCrudActionState(state as CrudActionStateAlias) === normalizedCurrentState);
}

