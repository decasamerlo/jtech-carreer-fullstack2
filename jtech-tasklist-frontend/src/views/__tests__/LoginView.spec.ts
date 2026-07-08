import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import { createVuetify } from 'vuetify'
import { useAuthStore } from '@/stores/auth'
import LoginView from '../LoginView.vue'

const vuetify = createVuetify()

describe('LoginView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  const registerRoute = {
    path: '/register',
    name: 'register',
    component: { template: '<div>Register</div>' },
  }

  const baseRoutes = [registerRoute]

  it('renders email and password fields', () => {
    const wrapper = mount(LoginView, {
      global: {
        plugins: [
          vuetify,
          createRouter({
            history: createWebHistory(),
            routes: baseRoutes,
          }),
        ],
      },
    })
    expect(wrapper.find('input[type="email"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
    expect(wrapper.find('button[type="submit"]').exists()).toBe(true)
  })

  it('shows validation errors when fields are empty on submit', async () => {
    const wrapper = mount(LoginView, {
      global: {
        plugins: [
          vuetify,
          createRouter({
            history: createWebHistory(),
            routes: baseRoutes,
          }),
        ],
      },
    })
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.text()).toContain('Email is required')
    expect(wrapper.text()).toContain('Password is required')
  })

  it('calls store.login and redirects on valid submit', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        ...baseRoutes,
        {
          path: '/',
          name: 'home',
          component: { template: '<div>Home</div>' },
        },
      ],
    })
    const pushSpy = vi.spyOn(router, 'push')

    const wrapper = mount(LoginView, {
      global: {
        plugins: [vuetify, router],
      },
    })
    await wrapper.find('input[type="email"]').setValue('john@example.com')
    await wrapper.find('input[type="password"]').setValue('secret')
    await wrapper.find('form').trigger('submit.prevent')

    const auth = useAuthStore()
    expect(auth.user?.email).toBe('john@example.com')
    expect(pushSpy).toHaveBeenCalledWith({ name: 'home' })
  })

  it('shows error message when login fails', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        ...baseRoutes,
        {
          path: '/',
          name: 'home',
          component: { template: '<div>Home</div>' },
        },
      ],
    })
    const auth = useAuthStore()
    vi.spyOn(auth, 'login').mockRejectedValue(new Error('Invalid credentials'))

    const wrapper = mount(LoginView, {
      global: {
        plugins: [vuetify, router],
      },
    })
    await wrapper.find('input[type="email"]').setValue('john@example.com')
    await wrapper.find('input[type="password"]').setValue('wrong')
    await wrapper.find('form').trigger('submit.prevent')

    expect(wrapper.text()).toContain('Invalid credentials')
  })
})
