import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import DeleteListDialog from '../DeleteListDialog.vue'

describe('DeleteListDialog', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders confirmation message with list name', () => {
    const wrapper = mount(DeleteListDialog, {
      props: { open: true, listName: 'My List' },
    })
    expect(wrapper.text()).toContain('My List')
    expect(wrapper.text()).toContain('Are you sure')
  })

  it('does not render when closed', () => {
    const wrapper = mount(DeleteListDialog, {
      props: { open: false, listName: 'My List' },
    })
    expect(wrapper.text()).not.toContain('Are you sure')
  })

  it('emits close event on cancel', async () => {
    const wrapper = mount(DeleteListDialog, {
      props: { open: true, listName: 'My List' },
    })
    await wrapper.find('button.cancel-btn').trigger('click')
    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('emits delete event on confirm', async () => {
    const wrapper = mount(DeleteListDialog, {
      props: { open: true, listName: 'My List' },
    })
    await wrapper.find('button.confirm-btn').trigger('click')
    expect(wrapper.emitted('delete')).toHaveLength(1)
  })
})
