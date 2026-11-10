/** A variable the editor can insert: its path, an optional description and a sample value for the preview. */
export interface TemplateVariable {
  name: string;
  label?: string;
  sample?: unknown;
}

/**
 * Builds nested sample data from variable paths: `user.name` becomes `{ user: { name } }`. A variable with no sample
 * shows its own name in brackets, so the preview makes clear which value is missing.
 */
export function sampleData(variables: TemplateVariable[]): Record<string, unknown> {
  const root: Record<string, unknown> = {};
  for (const variable of variables) {
    const parts = variable.name.split('.');
    let current = root;
    let reachable = true;
    for (let i = 0; i < parts.length - 1 && reachable; i++) {
      const key = parts[i] as string;
      const next = current[key] ?? (current[key] = {});
      if (typeof next === 'object' && next !== null && !Array.isArray(next)) {
        current = next as Record<string, unknown>;
      } else {
        reachable = false;
      }
    }
    if (reachable) {
      current[parts[parts.length - 1] as string] = variable.sample ?? `[${variable.name}]`;
    }
  }
  return root;
}
