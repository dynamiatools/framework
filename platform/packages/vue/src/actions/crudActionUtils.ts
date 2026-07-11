import type { ActionMetadata } from '@dynamia-tools/sdk';

function normalize(value?: string | null): string {
  return (value ?? '').trim().toLowerCase();
}

export function matchesActionIdentity(action: ActionMetadata, ...identities: string[]): boolean {
  const id = normalize(action.id);
  const className = normalize(action.className);

  return identities.some(identity => {
    const normalizedIdentity = normalize(identity);
    return normalizedIdentity === id || normalizedIdentity === className;
  });
}

export function isCreateCrudAction(action: ActionMetadata): boolean {
  return matchesActionIdentity(action, 'newaction', 'new', 'create', 'createaction');
}

export function isEditCrudAction(action: ActionMetadata): boolean {
  return matchesActionIdentity(action, 'editaction', 'edit');
}

export function isDeleteCrudAction(action: ActionMetadata): boolean {
  return matchesActionIdentity(action, 'deleteaction', 'delete');
}

export function isSaveCrudAction(action: ActionMetadata): boolean {
  return matchesActionIdentity(action, 'saveaction', 'save');
}

export function isCancelCrudAction(action: ActionMetadata): boolean {
  return matchesActionIdentity(action, 'cancelaction', 'cancel');
}

