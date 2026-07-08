import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { createVuetify } from 'vuetify'
import RenameListDialog from '../RenameListDialog.vue'

const vuetify = createVuetify()

describe('RenameListDialog', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  afterEach(() => {
    document.body.innerHTML = ''
  })

  it('renders input with current name when open', () => {
    mount(RenameListDialog, {
      props: { open: true, currentName: 'Old Name' },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    expect(document.body.querySelector('input')).toBeTruthy()
  })

  it('does not render when closed', () => {
    const wrapper = mount(RenameListDialog, {
      props: { open: false, currentName: 'Old Name' },
      global: { plugins: [vuetify] },
    })
    expect(wrapper.find('input').exists()).toBe(false)
  })

  it('emits close event on cancel', async () => {
    const wrapper = mount(RenameListDialog, {
      props: { open: true, currentName: 'Old Name' },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    const buttons = document.body.querySelectorAll('button')
    const cancelBtn = Array.from(buttons).find((b) => b.textContent === 'Cancel')
    expect(cancelBtn).toBeTruthy()
    cancelBtn!.click()
    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('shows error for empty name', async () => {
    const wrapper = mount(RenameListDialog, {
      props: { open: true, currentName: 'Old Name' },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    const input = document.body.querySelector('input') as HTMLInputElement
    input.value = ''
    input.dispatchEvent(new Event('input', { bubbles: true }))
    await new Promise(process.nextTick)
    const submitBtn = document.body.querySelector('button[type="submit"]') as HTMLElement
    submitBtn.click()
    await new Promise(process.nextTick)
    expect(document.body.textContent).toContain('List name is required')
  })

  it('emits rename with new name on valid submit', async () => {
    const wrapper = mount(RenameListDialog, {
      props: { open: true, currentName: 'Old Name' },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    const input = document.body.querySelector('input') as HTMLInputElement
    input.value = 'New Name'
    input.dispatchEvent(new Event('input', { bubbles: true }))
    await new Promise(process.nextTick)
    const submitBtn = document.body.querySelector('button[type="submit"]') as HTMLElement
    submitBtn.click()
    await new Promise(process.nextTick)
    expect(wrapper.emitted('rename')).toHaveLength(1)
    expect(wrapper.emitted('rename')![0]).toEqual(['New Name'])
  })
})
