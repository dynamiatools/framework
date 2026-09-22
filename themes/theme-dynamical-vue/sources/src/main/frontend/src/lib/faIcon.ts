import { h, type Component, type FunctionalComponent } from 'vue';

const cache = new Map<string, Component>();

/**
 * Wraps a Font Awesome class string (`"fa fa-book"`, see withResolvedIcons/resolveIconClass) as a
 * component, because the package's menu components take an icon *component* per item. The
 * component is cached per class so the sidebar doesn't remount icons on every navigation tree update.
 */
export function faIcon(iconClass: string | undefined): Component | undefined {
  if (!iconClass) return undefined;
  let component = cache.get(iconClass);
  if (!component) {
    const icon: FunctionalComponent = (_props, { attrs }) =>
      h('i', { ...attrs, class: [iconClass, 'w-5 text-center', attrs.class], 'aria-hidden': 'true' });
    component = icon;
    cache.set(iconClass, component);
  }
  return component;
}
