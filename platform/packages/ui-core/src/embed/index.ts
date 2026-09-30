// @dynamia-tools/ui-core/embed — <dynamia-embed> Web Component. Import this subpath explicitly:
// it touches `HTMLElement`/`customElements` at module-evaluation time, so it does not belong in
// (and is not re-exported from) the framework-agnostic `.` package entry.

export { DynamiaEmbed } from './DynamiaEmbed.js';
export { registerDynamiaEmbed } from './register.js';
export { mountInline } from './inline.js';
export type { InlineHandle, InlineMountOptions } from './inline.js';
export { detectEmbedType } from './detectType.js';
export type { EmbedContentType, DetectTypeOptions } from './detectType.js';
