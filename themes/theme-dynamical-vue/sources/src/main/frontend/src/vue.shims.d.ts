// @dynamia-tools/tailadmin-vue is published as raw .vue source; vue-tsc needs this ambient
// declaration to type those imports (it doesn't propagate across the package boundary — see the
// package's own docs). Local .vue files are still resolved normally.
declare module '*.vue' {
  import type { DefineComponent } from 'vue';
  const component: DefineComponent<{}, {}, any>;
  export default component;
}
