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
  return icon.includes(' ') ? icon : `${prefix}${icon}`;
}
