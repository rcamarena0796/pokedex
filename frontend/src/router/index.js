import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/', redirect: '/catalog' },
  { path: '/catalog', name: 'catalog', component: () => import('../views/CatalogView.vue') },
  {
    path: '/catalog/:idOrName',
    name: 'detail',
    component: () => import('../views/PokemonDetailView.vue'),
    props: true,
  },
  {
    path: '/pokedex',
    name: 'pokedex',
    component: () => import('../views/PokedexView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/pokedex/:id/edit',
    name: 'edit',
    component: () => import('../views/PokemonEditView.vue'),
    props: (route) => ({ id: Number(route.params.id) }),
    meta: { requiresAuth: true },
  },
  {
    path: '/users',
    name: 'users',
    component: () => import('../views/UsersView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  { path: '/login', name: 'login', component: () => import('../views/LoginView.vue'), meta: { guestOnly: true } },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/RegisterView.vue'),
    meta: { guestOnly: true },
  },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('../views/NotFoundView.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: (to, from, saved) => saved ?? { top: 0 },
})

// Protected vs public routes, mirroring the backend rules.
router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAdmin && !auth.isAdmin) return { name: 'catalog' }
  if (to.meta.guestOnly && auth.isAuthenticated) return { name: 'catalog' }
  return true
})

export default router
