import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { createVuetify } from 'vuetify'
import CreateTaskDialog from '../CreateTaskDialog.vue'

const vuetify = createVuetify()

describe('CreateTaskDialog', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  afterEach(() => {
    document.body.innerHTML = ''
  })

  it('renders inputs and buttons when open', () => {
    mount(CreateTaskDialog, {
      props: { open: true },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    expect(document.body.querySelector('input')).toBeTruthy()
    expect(document.body.querySelector('button[type="submit"]')).toBeTruthy()
  })

  it('does not render when closed', () => {
    const wrapper = mount(CreateTaskDialog, {
      props: { open: false },
      global: { plugins: [vuetify] },
    })
    expect(wrapper.find('input').exists()).toBe(false)
  })

  it('emits close event on cancel', async () => {
    const wrapper = mount(CreateTaskDialog, {
      props: { open: true },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    const buttons = document.body.querySelectorAll('button')
    const cancelBtn = Array.from(buttons).find((b) => b.textContent === 'Cancel')
    expect(cancelBtn).toBeTruthy()
    cancelBtn!.click()
    await new Promise(process.nextTick)
    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('shows validation error for empty title', async () => {
    mount(CreateTaskDialog, {
      props: { open: true },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    const submitBtn = document.body.querySelector('button[type="submit"]') as HTMLElement
    submitBtn.click()
    await new Promise(process.nextTick)
    expect(document.body.textContent).toContain('Task title is required')
  })

  it('shows validation error for title over 255 chars', async () => {
    mount(CreateTaskDialog, {
      props: { open: true },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    const input = document.body.querySelector('input') as HTMLInputElement
    input.value = 'a'.repeat(256)
    input.dispatchEvent(new Event('input', { bubbles: true }))
    await new Promise(process.nextTick)
    const submitBtn = document.body.querySelector('button[type="submit"]') as HTMLElement
    submitBtn.click()
    await new Promise(process.nextTick)
    expect(document.body.textContent).toContain('255 characters or less')
  })

  it('emits create with title and description (does not self-close)', async () => {
    const wrapper = mount(CreateTaskDialog, {
      props: { open: true },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    const inputs = document.body.querySelectorAll('input')
    const titleInput = inputs[0] as HTMLInputElement
    titleInput.value = 'New Task'
    titleInput.dispatchEvent(new Event('input', { bubbles: true }))
    const textareas = document.body.querySelectorAll('textarea')
    if (textareas.length > 0) {
      const descTextarea = textareas[0] as HTMLTextAreaElement
      descTextarea.value = 'Some description'
      descTextarea.dispatchEvent(new Event('input', { bubbles: true }))
    }
    await new Promise(process.nextTick)
    const submitBtn = document.body.querySelector('button[type="submit"]') as HTMLElement
    submitBtn.click()
    await new Promise(process.nextTick)
    expect(wrapper.emitted('create')).toHaveLength(1)
    expect(wrapper.emitted('create')![0]).toEqual(['New Task', 'Some description'])
    expect(wrapper.emitted('close')).toBeUndefined()
  })

  it('emits create with title only when description empty', async () => {
    const wrapper = mount(CreateTaskDialog, {
      props: { open: true },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    const inputs = document.body.querySelectorAll('input')
    const titleInput = inputs[0] as HTMLInputElement
    titleInput.value = 'New Task'
    titleInput.dispatchEvent(new Event('input', { bubbles: true }))
    const textareas = document.body.querySelectorAll('textarea')
    if (textareas.length > 0) {
      const descTextarea = textareas[0] as HTMLTextAreaElement
      descTextarea.value = ''
      descTextarea.dispatchEvent(new Event('input', { bubbles: true }))
    }
    await new Promise(process.nextTick)
    const submitBtn = document.body.querySelector('button[type="submit"]') as HTMLElement
    submitBtn.click()
    await new Promise(process.nextTick)
    expect(wrapper.emitted('create')![0]).toEqual(['New Task', undefined])
  })

  it('displays async error from parent prop', () => {
    mount(CreateTaskDialog, {
      props: { open: true, error: 'A task with this title already exists in this list' },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    expect(document.body.textContent).toContain('A task with this title already exists in this list')
  })

  it('resets form when dialog opens', async () => {
    const wrapper = mount(CreateTaskDialog, {
      props: { open: false },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    await wrapper.setProps({ open: true })
    const input = document.body.querySelector('input') as HTMLInputElement
    expect(input).toBeTruthy()
    expect(input.value).toBe('')
  })
})
