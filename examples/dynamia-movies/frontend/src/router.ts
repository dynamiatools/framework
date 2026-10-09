import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior: (_to, _from, saved) => saved ?? { left: 0, top: 0 },
  routes: [
    { path: '/', name: 'home', component: () => import('./views/HomeView.vue'), meta: { title: 'Dynamia Movies' } },
    { path: '/search', name: 'search', component: () => import('./views/SearchView.vue'), meta: { title: 'Search' } },
    { path: '/movie/:id(\\d+)', name: 'movie', component: () => import('./views/MovieView.vue'), props: true },
    { path: '/person/:id(\\d+)', name: 'person', component: () => import('./views/PersonView.vue'), props: true },
    { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('./views/NotFoundView.vue'), meta: { title: 'Not found' } },
  ],
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title as string} · Dynamia Movies` : 'Dynamia Movies'
})

export default router
