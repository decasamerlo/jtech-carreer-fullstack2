import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { createVuetify } from 'vuetify'
import DeleteTaskDialog from '../DeleteTaskDialog.vue'

const vuetify = createVuetify()

describe('DeleteTaskDialog', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  afterEach(() => {
    document.body.innerHTML = ''
  })

  it('renders task title in confirmation message', () => {
    mount(DeleteTaskDialog, {
      props: { open: true, taskTitle: 'Buy groceries' },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    expect(document.body.textContent).toContain('Buy groceries')
  })

  it('does not render when closed', () => {
    const wrapper = mount(DeleteTaskDialog, {
      props: { open: false, taskTitle: 'Task' },
      global: { plugins: [vuetify] },
    })
    expect(wrapper.text()).not.toContain('Delete Task')
  })

  it('emits close on cancel', async () => {
    const wrapper = mount(DeleteTaskDialog, {
      props: { open: true, taskTitle: 'Task' },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    const buttons = document.body.querySelectorAll('button')
    const cancelBtn = Array.from(buttons).find((b) => b.textContent === 'Cancel')
    expect(cancelBtn).toBeTruthy()
    cancelBtn!.click()
    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('emits delete on confirm', async () => {
    const wrapper = mount(DeleteTaskDialog, {
      props: { open: true, taskTitle: 'Task' },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    const buttons = document.body.querySelectorAll('button')
    const deleteBtn = Array.from(buttons).find((b) => b.textContent === 'Delete')
    expect(deleteBtn).toBeTruthy()
    deleteBtn!.click()
    expect(wrapper.emitted('delete')).toHaveLength(1)
  })

  it('displays async error from parent prop', () => {
    mount(DeleteTaskDialog, {
      props: { open: true, taskTitle: 'Task', error: 'Failed to delete' },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    expect(document.body.textContent).toContain('Failed to delete')
  })
})
