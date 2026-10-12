// @dynamia-tools/tailadmin-vue ships raw .vue source (no build step) and its own type-check
// relies on this same shim; a consumer needs the equivalent since ambient .d.ts declarations
// don't propagate across a package boundary. See its README's "Requirements" section.
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}
