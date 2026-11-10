import { hasBlocks, parse, render } from './blocks.js';
import { applyFilters, arithmetic, isArithmetic, isTernary, newScope, resolve, str, ternary, UNRESOLVED, type Vars } from './expressions.js';

/**
 * One pass over the text, so values inserted by a replacement are never scanned again. A `$` right before `{{name}}`
 * is a currency symbol, not the start of `${...}`.
 */
const VARIABLE = /\$\{(?!\{)([^}]+)\}|\{\{([^}]+)\}\}|\$([A-Za-z_]\w*(?:\.[A-Za-z_]\w*)+)/g;

export interface TemplateEngineOptions {
  /** Language tag (`es-CO`) for `upper`, `lower`, `number`, `currency` and `date` when the data has no `locale`. */
  locale?: string;
  /**
   * Replaces `& < > " '` by HTML entities in every printed value, so data cannot inject markup into an HTML template.
   * A value that is trusted HTML is printed with the `raw` filter (`${body|raw}`). Text written in the template is never
   * escaped. Default false.
   */
  escapeHtml?: boolean;
}

/**
 * Replaces variables in text, with the same rules as the Java `DynamiaTemplateEngine`:
 * `${name}`, `{{name}}`, `$a.b`, property paths, filters (`${total|currency:0}`), ternaries
 * (`${paid ? 'PAID' : 'PENDING'}`), arithmetic (`${qty * price}`) and the blocks
 * `{{#if cond}}...{{else}}...{{/if}}` and `{{#each items as item}}...{{/each}}`.
 * Unknown variables are left as they are; null values become empty text.
 */
export class DynamiaTemplateEngine {
  private readonly locale: string | undefined;
  private readonly escapeHtml: boolean;

  constructor(options: TemplateEngineOptions = {}) {
    this.locale = options.locale;
    this.escapeHtml = options.escapeHtml === true;
  }

  evaluate(template: string | null | undefined, data: Vars = {}): string {
    if (template === null || template === undefined) {
      return '';
    }
    const vars = newScope(data);
    if (this.locale !== undefined && !Object.hasOwn(vars, 'locale')) {
      vars['locale'] = this.locale;
    }
    if (hasBlocks(template)) {
      const nodes = parse(template);
      if (nodes !== null) {
        return render(nodes, vars, (text, scope) => substitute(text, scope, this.escapeHtml));
      }
    }
    return substitute(template, vars, this.escapeHtml);
  }
}

/** Replaces the variables of a text that has no block tags. */
function substitute(template: string, vars: Vars, escapeHtml: boolean): string {
  return template.replace(VARIABLE, (match: string, g1?: string, g2?: string, g3?: string) => {
    const shortForm = g3 !== undefined;
    const expression = shortForm ? g3 : (g1 ?? g2 ?? '');
    if (!shortForm && isTernary(expression)) {
      // ${cond ? 'a' : 'b'}: the || of a condition is not a filter separator
      const chosen = ternary(expression, vars);
      const chosenText = chosen === null || chosen === undefined ? '' : str(chosen);
      return escapeHtml ? escape(chosenText) : chosenText;
    }
    const parts = shortForm ? [expression] : expression.split('|');
    const first = parts[0] ?? '';
    let value: unknown;
    if (!Object.hasOwn(vars, first.trim()) && isArithmetic(first)) {
      // ${d.qty * d.price}: a sum, difference, product or quotient of numbers and variables
      value = arithmetic(first, (name) => resolve(vars, name));
    } else {
      value = resolve(vars, first);
    }
    if (value === UNRESOLVED) {
      return match;
    }
    if (parts.length > 1) {
      value = applyFilters(value, parts.slice(1), vars);
    }
    const text = value === null || value === undefined ? '' : str(value);
    return escapeHtml && !parts.slice(1).some((p) => p.trim() === 'raw') ? escape(text) : text;
  });
}

const ENTITIES: Record<string, string> = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' };

/** Replaces `& < > " '` by their HTML entities, in one pass. */
function escape(text: string): string {
  return text.replace(/[&<>"']/g, (c) => ENTITIES[c] ?? c);
}
