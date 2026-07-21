// register.ts — idempotent customElements.define() wrapper for <dynamia-embed>

import { DynamiaEmbed } from './DynamiaEmbed.js';

/**
 * Registers `<dynamia-embed>` (or a custom tag name) as a custom element.
 * Safe to call more than once — including from multiple independently
 * bundled micro-frontends sharing the same document — since it no-ops when
 * the tag is already defined.
 *
 * Example:
 * <pre>{@code
 * import { registerDynamiaEmbed } from '@dynamia-tools/ui-core/embed';
 * registerDynamiaEmbed();
 * }</pre>
 */
export function registerDynamiaEmbed(tagName: string = DynamiaEmbed.tagName): void {
  if (typeof customElements === 'undefined') return; // non-browser environment
  if (!customElements.get(tagName)) {
    customElements.define(tagName, DynamiaEmbed);
  }
}
