import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { createVuetify } from 'vuetify'
import EditTaskDialog from '../EditTaskDialog.vue'
import type { Task } from '@/types/task'

const vuetify = createVuetify()

const mockTask: Task = {
  id: 'task-1',
  title: 'Buy groceries',
  description: 'Milk, eggs',
  completed: false,
  tasklistId: 'list-1',
}

describe('EditTaskDialog', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  afterEach(() => {
    document.body.innerHTML = ''
  })

  it('renders pre-filled inputs when open with task', () => {
    mount(EditTaskDialog, {
      props: { open: true, task: mockTask },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    const input = document.body.querySelector('input') as HTMLInputElement
    expect(input).toBeTruthy()
    expect(input.value).toBe('Buy groceries')
  })

  it('does not render when closed', () => {
    const wrapper = mount(EditTaskDialog, {
      props: { open: false, task: mockTask },
      global: { plugins: [vuetify] },
    })
    expect(wrapper.find('input').exists()).toBe(false)
  })

  it('emits close event on cancel', async () => {
    const wrapper = mount(EditTaskDialog, {
      props: { open: true, task: mockTask },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    const buttons = document.body.querySelectorAll('button')
    const cancelBtn = Array.from(buttons).find((b) => b.textContent === 'Cancel')
    expect(cancelBtn).toBeTruthy()
    cancelBtn!.click()
    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('shows validation error for empty title', async () => {
    mount(EditTaskDialog, {
      props: { open: true, task: mockTask },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    const inputs = document.body.querySelectorAll('input')
    const titleInput = inputs[0] as HTMLInputElement
    titleInput.value = ''
    titleInput.dispatchEvent(new Event('input', { bubbles: true }))
    await new Promise(process.nextTick)
    const submitBtn = document.body.querySelector('button[type="submit"]') as HTMLElement
    submitBtn.click()
    await new Promise(process.nextTick)
    expect(document.body.textContent).toContain('Task title is required')
  })

  it('shows validation error for title over 255 chars', async () => {
    mount(EditTaskDialog, {
      props: { open: true, task: mockTask },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    const inputs = document.body.querySelectorAll('input')
    const titleInput = inputs[0] as HTMLInputElement
    titleInput.value = 'a'.repeat(256)
    titleInput.dispatchEvent(new Event('input', { bubbles: true }))
    await new Promise(process.nextTick)
    const submitBtn = document.body.querySelector('button[type="submit"]') as HTMLElement
    submitBtn.click()
    await new Promise(process.nextTick)
    expect(document.body.textContent).toContain('255 characters or less')
  })

  it('emits save with id, title, and description (does not self-close)', async () => {
    const wrapper = mount(EditTaskDialog, {
      props: { open: true, task: mockTask },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    const inputs = document.body.querySelectorAll('input')
    const titleInput = inputs[0] as HTMLInputElement
    titleInput.value = 'Updated Task'
    titleInput.dispatchEvent(new Event('input', { bubbles: true }))
    const textareas = document.body.querySelectorAll('textarea')
    if (textareas.length > 0) {
      const descTextarea = textareas[0] as HTMLTextAreaElement
      descTextarea.value = 'New desc'
      descTextarea.dispatchEvent(new Event('input', { bubbles: true }))
    }
    await new Promise(process.nextTick)
    const submitBtn = document.body.querySelector('button[type="submit"]') as HTMLElement
    submitBtn.click()
    await new Promise(process.nextTick)
    expect(wrapper.emitted('save')).toHaveLength(1)
    expect(wrapper.emitted('save')![0]).toEqual(['task-1', 'Updated Task', 'New desc'])
    expect(wrapper.emitted('close')).toBeUndefined()
  })

  it('emits save with undefined description when empty', async () => {
    const wrapper = mount(EditTaskDialog, {
      props: { open: true, task: mockTask },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    const inputs = document.body.querySelectorAll('input')
    const titleInput = inputs[0] as HTMLInputElement
    titleInput.value = 'Updated Task'
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
    expect(wrapper.emitted('save')![0]).toEqual(['task-1', 'Updated Task', undefined])
  })

  it('displays async error from parent prop', () => {
    mount(EditTaskDialog, {
      props: { open: true, task: mockTask, error: 'Duplicate title' },
      global: { plugins: [vuetify] },
      attachTo: document.body,
    })
    expect(document.body.textContent).toContain('Duplicate title')
  })
})
