import { isFullDocumentDesign, parseDesign } from '../model/validate.js';
import type { Design } from '../model/types.js';
import { toMjml } from './mjml.js';

/** What the MJML library gives back. */
interface MjmlOutput {
  html: string;
  errors?: { formattedMessage?: string; message?: string }[];
}
export type MjmlFunction = (mjml: string, options?: Record<string, unknown>) => MjmlOutput | Promise<MjmlOutput>;

let loader: () => Promise<MjmlFunction> = async () => {
  // mjml-browser is big (about 1.3 MB): it is loaded the first time something is compiled, not with the editor.
  const mod = (await import('mjml-browser')) as unknown as { default?: MjmlFunction } & MjmlFunction;
  return (mod.default ?? mod) as MjmlFunction;
};

/** Replaces how the MJML library is loaded (tests, or a host that bundles it itself). */
export function setMjmlLoader(next: () => Promise<MjmlFunction>): void {
  loader = next;
}

export interface CompileResult {
  /** The HTML to send: table based, responsive, with the template variables untouched. */
  html: string;
  /** The MJML it came from; empty for a design that is a whole HTML document. */
  mjml: string;
  /** Problems MJML reported. */
  errors: string[];
  /** Things the compiler skipped or changed. */
  warnings: string[];
}

/**
 * Compiles a design to the HTML a template stores. A design that is one whole HTML document (what migrating a template
 * produces) is returned as it is, without MJML, so nothing about the look of the old email changes.
 *
 * @param design a design or its JSON text
 */
export async function compile(design: Design | string): Promise<CompileResult> {
  const d = typeof design === 'string' ? parseDesign(design) : design;
  if (isFullDocumentDesign(d)) {
    return { html: String(d.blocks[0]?.props['html'] ?? ''), mjml: '', errors: [], warnings: [] };
  }
  const { mjml, warnings } = toMjml(d);
  const mjml2html = await loader();
  const out = await mjml2html(mjml, { validationLevel: 'soft', minify: false, beautify: false });
  const errors = (out.errors ?? []).map((e) => e.formattedMessage ?? e.message ?? String(e));
  return { html: out.html, mjml, errors, warnings };
}
