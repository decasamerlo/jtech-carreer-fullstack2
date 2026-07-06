import { describe, it, expect, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import ListsView from '../ListsView.vue'
import { useListsStore } from '@/stores/lists'
import { useAuthStore } from '@/stores/auth'

describe('ListsView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    const auth = useAuthStore()
    auth.login('test@example.com', 'password')
  })

  const router = createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/', name: 'home', component: { template: '<div />' } },
      { path: '/login', name: 'login', component: { template: '<div />' } },
    ],
  })

  it('renders sidebar and content area', async () => {
    const wrapper = mount(ListsView, {
      global: { plugins: [router] },
    })
    await flushPromises()
    expect(wrapper.find('.sidebar').exists()).toBe(true)
    expect(wrapper.find('.content').exists()).toBe(true)
  })

  it('shows empty state when no list is selected', async () => {
    const wrapper = mount(ListsView, {
      global: { plugins: [router] },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('Select a list')
  })

  it('shows active list name in content area', async () => {
    const wrapper = mount(ListsView, {
      global: { plugins: [router] },
    })
    await flushPromises()
    const store = useListsStore()
    store.createList('My Tasks')
    await flushPromises()
    expect(wrapper.text()).toContain('My Tasks')
  })
})
