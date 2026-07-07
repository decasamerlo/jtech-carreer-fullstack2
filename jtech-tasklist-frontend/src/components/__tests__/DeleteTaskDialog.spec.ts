import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import DeleteTaskDialog from '../DeleteTaskDialog.vue'

describe('DeleteTaskDialog', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders task title in confirmation message', () => {
    const wrapper = mount(DeleteTaskDialog, {
      props: { open: true, taskTitle: 'Buy groceries' },
    })
    expect(wrapper.text()).toContain('Buy groceries')
  })

  it('does not render when closed', () => {
    const wrapper = mount(DeleteTaskDialog, {
      props: { open: false, taskTitle: 'Task' },
    })
    expect(wrapper.text()).not.toContain('Delete Task')
  })

  it('emits close on cancel', async () => {
    const wrapper = mount(DeleteTaskDialog, {
      props: { open: true, taskTitle: 'Task' },
    })
    await wrapper.find('.cancel-btn').trigger('click')
    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('emits delete on confirm', async () => {
    const wrapper = mount(DeleteTaskDialog, {
      props: { open: true, taskTitle: 'Task' },
    })
    await wrapper.find('.delete-btn').trigger('click')
    expect(wrapper.emitted('delete')).toHaveLength(1)
  })

  it('displays async error from parent prop', async () => {
    const wrapper = mount(DeleteTaskDialog, {
      props: { open: true, taskTitle: 'Task', error: 'Failed to delete' },
    })
    expect(wrapper.text()).toContain('Failed to delete')
  })
})
