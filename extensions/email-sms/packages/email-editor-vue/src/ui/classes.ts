/** Tailwind classes shared by the buttons and fields of the editor (the utilities are prefixed with "de:"). */

/** There is no preflight, so boxes and fonts are set here instead of relying on a reset. */
const base = 'de:box-border de:[font-family:inherit]';
const focus = 'de:focus-visible:outline-2 de:focus-visible:outline-offset-1 de:focus-visible:outline-blue-500';

/** A secondary button: white with a thin border. */
export const btn = `${base} de:inline-flex de:cursor-pointer de:items-center de:gap-1.5 de:whitespace-nowrap de:rounded-md de:border de:border-slate-200 de:bg-white de:px-3 de:py-1.5 de:text-sm de:font-medium de:text-slate-700 de:shadow-sm de:transition de:hover:border-slate-300 de:hover:bg-slate-50 de:disabled:cursor-default de:disabled:opacity-40 de:disabled:hover:bg-white ${focus}`;

/** The main action of a screen. */
export const btnPrimary = `${base} de:inline-flex de:cursor-pointer de:items-center de:gap-1.5 de:whitespace-nowrap de:rounded-md de:border de:border-blue-600 de:bg-blue-600 de:px-3.5 de:py-1.5 de:text-sm de:font-semibold de:text-white de:shadow-sm de:transition de:hover:bg-blue-700 de:disabled:cursor-default de:disabled:border-slate-200 de:disabled:bg-slate-200 de:disabled:text-slate-400 ${focus}`;

/** A button with only an icon. */
export const btnIcon = `${base} de:inline-flex de:size-7 de:cursor-pointer de:items-center de:justify-center de:rounded-md de:text-slate-500 de:transition de:hover:bg-slate-100 de:hover:text-slate-800 de:disabled:cursor-default de:disabled:opacity-40 ${focus}`;

/** A text field. */
export const field = `${base} de:w-full de:rounded-md de:border de:border-slate-300 de:bg-white de:px-2.5 de:py-1.5 de:text-sm de:text-slate-800 de:shadow-sm de:placeholder:text-slate-400 de:focus:border-blue-500 de:focus:outline-2 de:focus:outline-blue-200`;
