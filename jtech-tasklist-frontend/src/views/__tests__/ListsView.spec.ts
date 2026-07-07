import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import ListsView from '../ListsView.vue'
import { useListsStore } from '@/stores/lists'
import { useTasksStore } from '@/stores/tasks'
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

  it('renders tasks when tasks exist in the store', async () => {
    const wrapper = mount(ListsView, {
      global: { plugins: [router] },
    })
    await flushPromises()
    const listsStore = useListsStore()
    listsStore.createList('My Tasks')
    await flushPromises()

    const tasksStore = useTasksStore()
    await tasksStore.addTask(listsStore.activeListId!, 'Buy groceries', 'Milk, eggs, bread')
    await flushPromises()

    expect(wrapper.text()).toContain('Buy groceries')
    expect(wrapper.find('.task-item').exists()).toBe(true)
  })

  it('opens the create task dialog when "+ Add Task" is clicked', async () => {
    const wrapper = mount(ListsView, {
      global: { plugins: [router] },
    })
    await flushPromises()
    const listsStore = useListsStore()
    listsStore.createList('My Tasks')
    await flushPromises()

    expect(wrapper.find('.add-task-btn').exists()).toBe(true)
    await wrapper.find('.add-task-btn').trigger('click')
    await flushPromises()

    expect(wrapper.findComponent({ name: 'CreateTaskDialog' }).props('open')).toBe(true)
  })

  it('shows empty state when no tasks exist', async () => {
    const wrapper = mount(ListsView, {
      global: { plugins: [router] },
    })
    await flushPromises()
    const listsStore = useListsStore()
    listsStore.createList('My Tasks')
    await flushPromises()

    expect(wrapper.find('.empty-tasks').exists()).toBe(true)
    expect(wrapper.text()).toContain('No tasks yet')
  })

  it('shows toggle error when toggleComplete fails', async () => {
    const wrapper = mount(ListsView, {
      global: { plugins: [router] },
    })
    await flushPromises()
    const listsStore = useListsStore()
    listsStore.createList('My Tasks')
    await flushPromises()

    const tasksStore = useTasksStore()
    await tasksStore.addTask(listsStore.activeListId!, 'Toggle me')
    await flushPromises()

    vi.spyOn(tasksStore, 'toggleComplete').mockRejectedValueOnce(
      new Error('Could not reach server'),
    )

    await wrapper.find('input[type="checkbox"]').trigger('change')
    await flushPromises()

    expect(wrapper.find('.toggle-error').exists()).toBe(true)
    expect(wrapper.text()).toContain('Could not reach server')
  })
})
