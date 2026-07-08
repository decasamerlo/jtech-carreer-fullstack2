import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import { createVuetify } from 'vuetify'
import { useAuthStore } from '@/stores/auth'
import HomeView from '../HomeView.vue'

const vuetify = createVuetify()

describe('HomeView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  const listsRoute = {
    path: '/lists',
    name: 'lists',
    component: { template: '<div>Lists</div>' },
  }

  it('greets the signed-in user by name and email', () => {
    const auth = useAuthStore()
    auth.user = { name: 'John', email: 'john@example.com', role: 'ROLE_USER' }

    const wrapper = mount(HomeView, {
      global: {
        plugins: [
          vuetify,
          createRouter({
            history: createWebHistory(),
            routes: [listsRoute],
          }),
        ],
      },
    })

    expect(wrapper.text()).toContain('Welcome, John!')
    expect(wrapper.text()).toContain('john@example.com')
  })

  it('calls logout and redirects to login on sign out', async () => {
    const auth = useAuthStore()
    auth.user = { name: 'John', email: 'john@example.com', role: 'ROLE_USER' }

    const router = createRouter({
      history: createWebHistory(),
      routes: [
        listsRoute,
        {
          path: '/login',
          name: 'login',
          component: { template: '<div>Login</div>' },
        },
      ],
    })
    const pushSpy = vi.spyOn(router, 'push')
    const logoutSpy = vi.spyOn(auth, 'logout')

    const wrapper = mount(HomeView, {
      global: { plugins: [vuetify, router] },
    })

    const signOut = wrapper.findAll('button').find((b) => b.text().includes('Sign Out'))
    expect(signOut).toBeTruthy()
    await signOut!.trigger('click')

    expect(logoutSpy).toHaveBeenCalled()
    expect(pushSpy).toHaveBeenCalledWith({ name: 'login' })
  })
})
