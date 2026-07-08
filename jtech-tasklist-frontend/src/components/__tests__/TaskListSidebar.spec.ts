import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import type { VueWrapper } from '@vue/test-utils'
import { nextTick } from 'vue'
import { setActivePinia, createPinia } from 'pinia'
import { createVuetify } from 'vuetify'
import TaskListSidebar from '../TaskListSidebar.vue'
import { useListsStore } from '@/stores/lists'
import { useAuthStore } from '@/stores/auth'

async function flushSidebar(wrapper: VueWrapper) {
  await nextTick()
  await wrapper.vm.$nextTick()
}

const vuetify = createVuetify()

// VNavigationDrawer requires a Vuetify layout/app context; stub it to a
// passthrough so the sidebar's own list-rendering logic can be tested in isolation.
const navigationDrawerStub = {
  name: 'VNavigationDrawer',
  template: '<div class="v-navigation-drawer"><slot name="prepend" /><slot /></div>',
}

describe('TaskListSidebar', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    const auth = useAuthStore()
    auth.user = { name: 'John', email: 'john@example.com', role: 'ROLE_USER' }
  })

  function mountSidebar() {
    return mount(TaskListSidebar, {
      global: {
        plugins: [vuetify],
        stubs: { VNavigationDrawer: navigationDrawerStub },
      },
    })
  }

  it('renders one list item per list', () => {
    const store = useListsStore()
    store.createList('Work')
    store.createList('Personal')

    const wrapper = mountSidebar()
    const items = wrapper.findAll('.v-list-item--link')
    expect(items.length).toBe(2)
    expect(wrapper.text()).toContain('Work')
    expect(wrapper.text()).toContain('Personal')
  })

  it('shows empty-state message when there are no lists', () => {
    const wrapper = mountSidebar()
    expect(wrapper.text()).toContain('No lists yet')
  })

  it('calls setActiveList when a list item is clicked', async () => {
    const store = useListsStore()
    store.createList('Work')
    const workId = store.activeListId
    store.createList('Personal')

    const spy = vi.spyOn(store, 'setActiveList')

    const wrapper = mountSidebar()
    const items = wrapper.findAll('.v-list-item--link')
    await items[0].trigger('click')
    expect(spy).toHaveBeenCalledWith(workId)
  })

  it('marks the active list as active', async () => {
    const store = useListsStore()
    store.createList('Work')
    store.createList('Personal')
    const personalId = store.activeListId
    store.setActiveList(personalId!)

    const wrapper = mountSidebar()
    const items = wrapper.findAll('.v-list-item.v-list-item--link')
    expect(items[1].classes()).toContain('v-list-item--active')
  })

  it('opens the create dialog when the plus button is clicked', async () => {
    const wrapper = mountSidebar()
    const plusBtn = wrapper.findAll('button').find((b) => b.find('.mdi-plus').exists())!
    await plusBtn.trigger('click')
    await flushSidebar(wrapper)
    expect(wrapper.findComponent({ name: 'CreateListDialog' }).props('open')).toBe(true)
  })

  it('opens the rename dialog with the selected list name', async () => {
    const store = useListsStore()
    store.createList('Work')

    const wrapper = mountSidebar()
    const renameBtn = wrapper.findAll('button').find((b) => b.find('.mdi-pencil').exists())!
    await renameBtn.trigger('click')
    await flushSidebar(wrapper)
    const renameDialog = wrapper.findComponent({ name: 'RenameListDialog' })
    expect(renameDialog.props('open')).toBe(true)
    expect(renameDialog.props('currentName')).toBe('Work')
  })

  it('opens the delete dialog with the selected list name', async () => {
    const store = useListsStore()
    store.createList('Work')

    const wrapper = mountSidebar()
    const deleteBtn = wrapper.findAll('button').find((b) => b.find('.mdi-delete').exists())!
    await deleteBtn.trigger('click')
    await flushSidebar(wrapper)
    const deleteDialog = wrapper.findComponent({ name: 'DeleteListDialog' })
    expect(deleteDialog.props('open')).toBe(true)
    expect(deleteDialog.props('listName')).toBe('Work')
  })
})
