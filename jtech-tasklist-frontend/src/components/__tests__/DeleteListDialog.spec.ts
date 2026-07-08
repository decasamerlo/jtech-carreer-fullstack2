import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { createVuetify } from 'vuetify'
import DeleteListDialog from '../DeleteListDialog.vue'

const vuetify = createVuetify()

describe('DeleteListDialog', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  afterEach(() => {
    document.body.innerHTML = ''
  })

  it('renders confirmation message with list name', () => {
    mount(DeleteListDialog, {
      props: { open: true, listName: 'My List' },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    expect(document.body.textContent).toContain('My List')
    expect(document.body.textContent).toContain('Are you sure')
  })

  it('does not render when closed', () => {
    const wrapper = mount(DeleteListDialog, {
      props: { open: false, listName: 'My List' },
      global: { plugins: [vuetify] },
    })
    expect(wrapper.text()).not.toContain('Are you sure')
  })

  it('emits close event on cancel', async () => {
    const wrapper = mount(DeleteListDialog, {
      props: { open: true, listName: 'My List' },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    const buttons = document.body.querySelectorAll('button')
    const cancelBtn = Array.from(buttons).find((b) => b.textContent === 'Cancel')
    expect(cancelBtn).toBeTruthy()
    cancelBtn!.click()
    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('emits delete event on confirm', async () => {
    const wrapper = mount(DeleteListDialog, {
      props: { open: true, listName: 'My List' },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    const buttons = document.body.querySelectorAll('button')
    const deleteBtn = Array.from(buttons).find((b) => b.textContent === 'Delete')
    expect(deleteBtn).toBeTruthy()
    deleteBtn!.click()
    expect(wrapper.emitted('delete')).toHaveLength(1)
  })
})
