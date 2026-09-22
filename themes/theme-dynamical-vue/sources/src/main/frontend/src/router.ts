import { createRouter, createWebHashHistory } from 'vue-router';

/**
 * The package's layout components (sidebar links, breadcrumb, user menu) are built on vue-router, so
 * the app has one. It routes on the navigation tree's `internalPath` (e.g. `/pages/store/books`) with
 * hash history: the Spring Boot backend only serves `/`, so deep links must live after the `#`.
 *
 * There is a single catch-all route with no component on purpose — App.vue resolves the current
 * path to a NavigationNode and renders it (CrudPage or embed), see layout/ContentArea.vue.
 */
export const router = createRouter({
  history: createWebHashHistory(),
  routes: [{ path: '/:pathMatch(.*)*', component: { render: () => null } }],
});

/** NavigationNode.internalPath has no leading slash (`library/books`); route paths do (`/library/books`). */
export const toRoutePath = (internalPath: string): string => `/${internalPath.replace(/^\//, '')}`;
export const toInternalPath = (routePath: string): string => routePath.replace(/^\//, '');
