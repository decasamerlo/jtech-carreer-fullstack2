import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { authGuard } from '@/router'
import { createRouter, createWebHistory } from 'vue-router'

function createTestRouter(routes: ReturnType<typeof createRouter>['options']['routes']) {
  const router = createRouter({
    history: createWebHistory(),
    routes,
  })

  router.beforeEach(authGuard)

  return router
}

describe('router guard', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('redirects unauthenticated user to /login when accessing protected route', async () => {
    const router = createTestRouter([
      {
        path: '/',
        name: 'home',
        component: { template: '<div>Home</div>' },
        meta: { requiresAuth: true },
      },
      {
        path: '/login',
        name: 'login',
        component: { template: '<div>Login</div>' },
      },
    ])

    await router.push('/')
    await router.isReady()

    expect(router.currentRoute.value.name).toBe('login')
  })

  it('allows authenticated user to access protected route', async () => {
    const store = useAuthStore()
    store.login('john', 'secret')
    const router = createTestRouter([
      {
        path: '/',
        name: 'home',
        component: { template: '<div>Home</div>' },
        meta: { requiresAuth: true },
      },
      {
        path: '/login',
        name: 'login',
        component: { template: '<div>Login</div>' },
      },
    ])

    await router.push('/')
    await router.isReady()

    expect(router.currentRoute.value.name).toBe('home')
  })

  it('redirects authenticated user away from /login to /', async () => {
    const store = useAuthStore()
    store.login('john', 'secret')
    const router = createTestRouter([
      {
        path: '/',
        name: 'home',
        component: { template: '<div>Home</div>' },
        meta: { requiresAuth: true },
      },
      {
        path: '/login',
        name: 'login',
        component: { template: '<div>Login</div>' },
      },
    ])

    await router.push('/login')
    await router.isReady()

    expect(router.currentRoute.value.name).toBe('home')
  })
})
