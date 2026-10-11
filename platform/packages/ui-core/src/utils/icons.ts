// icons.ts — resolves a bare NavigationNode/ActionMetadata icon token into a CSS class string

/**
 * Resolves an icon token (`NavigationNode.icon`, `ActionMetadata.icon`, …) into a CSS class
 * string for a font-icon library.
 *
 * These tokens are set server-side as bare, library-agnostic names (e.g. `"book"`,
 * `"clipboard-list"`) — resolving them to a specific icon *font*'s class names (Font Awesome,
 * Bootstrap Icons, …) is a presentation/theme concern, not something the framework encodes.
 * Already-qualified class strings (containing a space, e.g. `"fab fa-github"`) are passed
 * through unchanged, mirroring the same convention the framework's own ZK
 * `FontAwesomeIconsProvider` uses server-side.
 *
 * @param icon - The raw icon token, if any
 * @param prefix - Prefix to prepend to a bare token (default `"fa fa-"`, Font Awesome solid)
 *
 * @example
 * resolveIconClass('book')             // → 'fa fa-book'
 * resolveIconClass('fab fa-github')    // → 'fab fa-github' (already qualified, untouched)
 * resolveIconClass(undefined)          // → ''
 */
export function resolveIconClass(icon: string | undefined | null, prefix = 'fa fa-'): string {
  if (!icon) return '';
  if (icon.includes(' ')) return icon;
  return `${prefix}${ACTION_ICON_ALIASES[icon] ?? icon}`;
}

/**
 * Logical icon names the server sends for the standard actions (`delete`, `find`, `edit`…) that are not Font Awesome
 * names. Same values as the ZK mapping `META-INF/dynamia/fa-icons.properties`, limited to the common action tokens so
 * both front ends draw the same glyph for the same action.
 */
const ACTION_ICON_ALIASES: Readonly<Record<string, string>> = {
  add: 'plus',
  cancel: 'ban',
  check: 'check-circle',
  close: 'times',
  delete: 'trash-alt',
  edit: 'pencil-alt',
  find: 'search',
  more: 'ellipsis-h',
  refresh: 'sync',
};
