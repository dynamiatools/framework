import { resolve, newScope, test, UNRESOLVED, type Vars } from './expressions.js';

/**
 * The two block tags of the template language, parsed into a small tree and rendered without running any code:
 *
 *     {{#if paid && total > 0}} ... {{else}} ... {{/if}}
 *     {{#each items as item}} ${item.name} (${loop.number} of ${loop.count}) {{/each}}
 *
 * Same rules as Java's TemplateBlocks: tags are read from the template only (never from inserted values), nesting depth
 * and total iterations are capped, and a template whose tags do not pair up is left as it is.
 */

export const MAX_DEPTH = 10;
export const MAX_ITERATIONS = 10_000;

const TAG = /\{\{\s*(#if|#each|else|\/if|\/each)(?:\s+([^}]*?))?\s*\}\}/g;
const EACH = /^(.+?)\s+as\s+([A-Za-z_]\w*)$/;

type Node =
  | { type: 'text'; text: string }
  | { type: 'if'; condition: string; then: Node[]; otherwise: Node[] }
  | { type: 'each'; source: string; alias: string; body: Node[] };

interface Frame {
  kind: 'if' | 'each';
  argument: string;
  alias: string;
  then: Node[] | null;
  current: Node[];
}

export function hasBlocks(template: string): boolean {
  return template.includes('{{') && new RegExp(TAG.source).test(template);
}

/** @returns the tree, or null when a tag is unbalanced, malformed or nested too deep */
export function parse(template: string): Node[] | null {
  const root: Node[] = [];
  const stack: Frame[] = [];
  const top = (): Frame | undefined => stack[stack.length - 1];
  const re = new RegExp(TAG.source, 'g');
  let last = 0;
  for (let m = re.exec(template); m !== null; m = re.exec(template)) {
    const out = top()?.current ?? root;
    if (m.index > last) {
      out.push({ type: 'text', text: template.substring(last, m.index) });
    }
    last = m.index + m[0].length;
    const tag = m[1];
    const arg = (m[2] ?? '').trim();
    if (tag === '#if') {
      if (arg === '' || stack.length >= MAX_DEPTH) {
        return null;
      }
      stack.push({ kind: 'if', argument: arg, alias: '', then: null, current: [] });
    } else if (tag === '#each') {
      const e = EACH.exec(arg);
      if (!e || stack.length >= MAX_DEPTH) {
        return null;
      }
      stack.push({ kind: 'each', argument: (e[1] ?? '').trim(), alias: e[2] ?? '', then: null, current: [] });
    } else if (tag === 'else') {
      const t = top();
      if (!t || t.kind !== 'if' || t.then !== null) {
        return null;
      }
      t.then = t.current;
      t.current = [];
    } else {
      const t = top();
      if (!t || t.kind !== tag?.substring(1)) {
        return null;
      }
      stack.pop();
      const parent = top()?.current ?? root;
      parent.push(
        t.kind === 'if'
          ? { type: 'if', condition: t.argument, then: t.then ?? t.current, otherwise: t.then === null ? [] : t.current }
          : { type: 'each', source: t.argument, alias: t.alias, body: t.current },
      );
    }
  }
  if (stack.length > 0) {
    return null;
  }
  if (last < template.length) {
    root.push({ type: 'text', text: template.substring(last) });
  }
  return root;
}

/** @param text replaces the variables of a text node, given the data of that point of the template */
export function render(nodes: Node[], vars: Vars, text: (text: string, vars: Vars) => string): string {
  const counter = { n: 0 };
  let out = '';
  const walk = (list: Node[], scope: Vars): void => {
    for (const node of list) {
      if (node.type === 'text') {
        out += text(node.text, scope);
      } else if (node.type === 'if') {
        walk(test(node.condition, scope) ? node.then : node.otherwise, scope);
      } else {
        const items = itemsOf(resolve(scope, node.source));
        const count = items.length;
        for (let n = 0; n < count && counter.n < MAX_ITERATIONS; n++, counter.n++) {
          const inner = newScope(scope);
          inner[node.alias] = items[n];
          inner['loop'] = { index: n, number: n + 1, count, first: n === 0, last: n === count - 1 };
          walk(node.body, inner);
        }
      }
    }
  };
  walk(nodes, vars);
  return out;
}

function itemsOf(value: unknown): unknown[] {
  if (value === UNRESOLVED || value === null) {
    return [];
  }
  if (Array.isArray(value)) {
    return value.slice(0, MAX_ITERATIONS);
  }
  if (value instanceof Set) {
    return [...value].slice(0, MAX_ITERATIONS);
  }
  return [];
}
