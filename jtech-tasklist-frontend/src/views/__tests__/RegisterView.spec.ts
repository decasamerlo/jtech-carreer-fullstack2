import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import { createVuetify } from 'vuetify'
import RegisterView from '../RegisterView.vue'

const vuetify = createVuetify()

describe('RegisterView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  const loginRoute = {
    path: '/login',
    name: 'login',
    component: { template: '<div>Login</div>' },
  }

  const baseRoutes = [loginRoute]

  it('renders name, email, password and confirm fields', () => {
    const wrapper = mount(RegisterView, {
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
    expect(wrapper.findAll('input[type="password"]').length).toBe(2)
    expect(wrapper.find('button[type="submit"]').exists()).toBe(true)
  })

  it('shows validation errors when fields are empty on submit', async () => {
    const wrapper = mount(RegisterView, {
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
    expect(wrapper.text()).toContain('Name is required')
    expect(wrapper.text()).toContain('Email is required')
    expect(wrapper.text()).toContain('Password is required')
  })

  it('shows error when password is shorter than 6 characters', async () => {
    const wrapper = mount(RegisterView, {
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
    await wrapper.find('input[type="text"]').setValue('John')
    await wrapper.find('input[type="email"]').setValue('john@example.com')
    await wrapper.findAll('input[type="password"]')[0].setValue('abc')
    await wrapper.findAll('input[type="password"]')[1].setValue('abc')
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.text()).toContain('Password must be at least 6 characters')
  })

  it('shows error when passwords do not match', async () => {
    const wrapper = mount(RegisterView, {
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
    await wrapper.find('input[type="text"]').setValue('John')
    await wrapper.find('input[type="email"]').setValue('john@example.com')
    await wrapper.findAll('input[type="password"]')[0].setValue('secret1')
    await wrapper.findAll('input[type="password"]')[1].setValue('secret2')
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.text()).toContain('Passwords do not match')
  })

  it('calls store.register and redirects on valid submit', async () => {
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

    const wrapper = mount(RegisterView, {
      global: {
        plugins: [vuetify, router],
      },
    })
    await wrapper.find('input[type="text"]').setValue('John')
    await wrapper.find('input[type="email"]').setValue('john@example.com')
    await wrapper.findAll('input[type="password"]')[0].setValue('secret1')
    await wrapper.findAll('input[type="password"]')[1].setValue('secret1')
    await wrapper.find('form').trigger('submit.prevent')

    const auth = (await import('@/stores/auth')).useAuthStore()
    expect(auth.user?.email).toBe('john@example.com')
    expect(auth.user?.name).toBe('John')
    expect(pushSpy).toHaveBeenCalledWith({ name: 'home' })
  })

  it('shows error message when registration fails', async () => {
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
    const auth = (await import('@/stores/auth')).useAuthStore()
    vi.spyOn(auth, 'register').mockRejectedValue(new Error('Email already registered'))

    const wrapper = mount(RegisterView, {
      global: {
        plugins: [vuetify, router],
      },
    })
    await wrapper.find('input[type="text"]').setValue('John')
    await wrapper.find('input[type="email"]').setValue('john@example.com')
    await wrapper.findAll('input[type="password"]')[0].setValue('secret1')
    await wrapper.findAll('input[type="password"]')[1].setValue('secret1')
    await wrapper.find('form').trigger('submit.prevent')

    expect(wrapper.text()).toContain('Email already registered')
  })
})
