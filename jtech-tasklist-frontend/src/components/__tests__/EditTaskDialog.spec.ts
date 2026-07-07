import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import EditTaskDialog from '../EditTaskDialog.vue'
import type { Task } from '@/types/task'

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

  it('renders pre-filled inputs when open with task', () => {
    const wrapper = mount(EditTaskDialog, {
      props: { open: true, task: mockTask },
    })
    expect((wrapper.find('#task-title').element as HTMLInputElement).value).toBe('Buy groceries')
    expect((wrapper.find('#task-description').element as HTMLTextAreaElement).value).toBe(
      'Milk, eggs',
    )
  })

  it('does not render when closed', () => {
    const wrapper = mount(EditTaskDialog, {
      props: { open: false, task: mockTask },
    })
    expect(wrapper.find('#task-title').exists()).toBe(false)
  })

  it('emits close event on cancel', async () => {
    const wrapper = mount(EditTaskDialog, {
      props: { open: true, task: mockTask },
    })
    await wrapper.find('button.cancel-btn').trigger('click')
    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('shows validation error for empty title', async () => {
    const wrapper = mount(EditTaskDialog, {
      props: { open: true, task: mockTask },
    })
    await wrapper.find('#task-title').setValue('')
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.text()).toContain('Task title is required')
  })

  it('shows validation error for title over 255 chars', async () => {
    const wrapper = mount(EditTaskDialog, {
      props: { open: true, task: mockTask },
    })
    await wrapper.find('#task-title').setValue('a'.repeat(256))
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.text()).toContain('255 characters or less')
  })

  it('emits save with id, title, and description (does not self-close)', async () => {
    const wrapper = mount(EditTaskDialog, {
      props: { open: true, task: mockTask },
    })
    await wrapper.find('#task-title').setValue('Updated Task')
    await wrapper.find('#task-description').setValue('New desc')
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.emitted('save')).toHaveLength(1)
    expect(wrapper.emitted('save')![0]).toEqual(['task-1', 'Updated Task', 'New desc'])
    expect(wrapper.emitted('close')).toBeUndefined()
  })

  it('emits save with undefined description when empty', async () => {
    const wrapper = mount(EditTaskDialog, {
      props: { open: true, task: mockTask },
    })
    await wrapper.find('#task-title').setValue('Updated Task')
    await wrapper.find('#task-description').setValue('')
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.emitted('save')![0]).toEqual(['task-1', 'Updated Task', undefined])
  })

  it('displays async error from parent prop', async () => {
    const wrapper = mount(EditTaskDialog, {
      props: { open: true, task: mockTask, error: 'Duplicate title' },
    })
    expect(wrapper.text()).toContain('Duplicate title')
  })
})
