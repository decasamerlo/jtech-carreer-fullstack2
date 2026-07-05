import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../LoginView.vue'

describe('LoginView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders username and password fields', () => {
    const wrapper = mount(LoginView, {
      global: {
        plugins: [
          createRouter({
            history: createWebHistory(),
            routes: [],
          }),
        ],
      },
    })
    expect(wrapper.find('input[type="text"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
    expect(wrapper.find('button[type="submit"]').exists()).toBe(true)
  })

  it('shows validation errors when fields are empty on submit', async () => {
    const wrapper = mount(LoginView, {
      global: {
        plugins: [
          createRouter({
            history: createWebHistory(),
            routes: [],
          }),
        ],
      },
    })
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.text()).toContain('Username is required')
    expect(wrapper.text()).toContain('Password is required')
  })

  it('calls store.login and redirects on valid submit', async () => {
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        {
          path: '/',
          name: 'home',
          component: { template: '<div>Home</div>' },
        },
      ],
    })
    const pushSpy = vi.spyOn(router, 'push')
    const auth = (await import('@/stores/auth')).useAuthStore()

    const wrapper = mount(LoginView, {
      global: {
        plugins: [router],
      },
    })
    await wrapper.find('input[type="text"]').setValue('john')
    await wrapper.find('input[type="password"]').setValue('secret')
    await wrapper.find('form').trigger('submit.prevent')

    expect(auth.user?.username).toBe('john')
    expect(pushSpy).toHaveBeenCalledWith({ name: 'home' })
  })
})
