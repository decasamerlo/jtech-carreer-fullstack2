import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { createVuetify } from 'vuetify'
import CreateListDialog from '../CreateListDialog.vue'

const vuetify = createVuetify()

describe('CreateListDialog', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  afterEach(() => {
    document.body.innerHTML = ''
  })

  it('renders input and buttons when open', () => {
    mount(CreateListDialog, {
      props: { open: true },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    expect(document.body.querySelector('input')).toBeTruthy()
    expect(document.body.querySelector('button[type="submit"]')).toBeTruthy()
  })

  it('does not render when closed', () => {
    const wrapper = mount(CreateListDialog, {
      props: { open: false },
      global: { plugins: [vuetify] },
    })
    expect(wrapper.find('input').exists()).toBe(false)
  })

  it('emits close event on cancel', async () => {
    const wrapper = mount(CreateListDialog, {
      props: { open: true },
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
    mount(CreateListDialog, {
      props: { open: true },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    const submitBtn = document.body.querySelector('button[type="submit"]') as HTMLElement
    expect(submitBtn).toBeTruthy()
    submitBtn.click()
    await new Promise(process.nextTick)
    expect(document.body.textContent).toContain('List name is required')
  })

  it('shows error for name over 50 chars', async () => {
    mount(CreateListDialog, {
      props: { open: true },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    const input = document.body.querySelector('input') as HTMLInputElement
    expect(input).toBeTruthy()
    const nativeInput = input
    nativeInput.value = 'a'.repeat(51)
    nativeInput.dispatchEvent(new Event('input', { bubbles: true }))
    await new Promise(process.nextTick)
    const submitBtn = document.body.querySelector('button[type="submit"]') as HTMLElement
    submitBtn.click()
    await new Promise(process.nextTick)
    expect(document.body.textContent).toContain('50 characters or less')
  })

  it('displays the async error passed via the error prop', () => {
    mount(CreateListDialog, {
      props: { open: true, error: 'A list with this name already exists' },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    expect(document.body.textContent).toContain('A list with this name already exists')
  })

  it('emits create but not close on valid submit (parent closes on success)', async () => {
    const wrapper = mount(CreateListDialog, {
      props: { open: true },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    const input = document.body.querySelector('input') as HTMLInputElement
    input.value = 'Work'
    input.dispatchEvent(new Event('input', { bubbles: true }))
    await new Promise(process.nextTick)
    const submitBtn = document.body.querySelector('button[type="submit"]') as HTMLElement
    submitBtn.click()
    await new Promise(process.nextTick)
    expect(wrapper.emitted('create')).toEqual([['Work']])
    expect(wrapper.emitted('close')).toBeUndefined()
  })
})
