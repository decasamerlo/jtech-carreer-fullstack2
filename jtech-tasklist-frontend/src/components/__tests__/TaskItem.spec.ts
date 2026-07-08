import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { createVuetify } from 'vuetify'
import TaskItem from '../TaskItem.vue'
import type { Task } from '@/types/task'

const vuetify = createVuetify()

const mockTask: Task = {
  id: 'task-1',
  title: 'Buy groceries',
  description: 'Milk, eggs, bread',
  completed: false,
  tasklistId: 'list-1',
}

describe('TaskItem', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders task title', () => {
    const wrapper = mount(TaskItem, { props: { task: mockTask }, global: { plugins: [vuetify] } })
    expect(wrapper.text()).toContain('Buy groceries')
  })

  it('renders task description', () => {
    const wrapper = mount(TaskItem, { props: { task: mockTask }, global: { plugins: [vuetify] } })
    expect(wrapper.text()).toContain('Milk, eggs, bread')
  })

  it('does not render description when absent', () => {
    const task = { ...mockTask, description: undefined }
    const wrapper = mount(TaskItem, { props: { task }, global: { plugins: [vuetify] } })
    expect(wrapper.text()).not.toContain('Milk, eggs, bread')
  })

  it('emits toggle event with task id', async () => {
    const wrapper = mount(TaskItem, { props: { task: mockTask }, global: { plugins: [vuetify] } })
    const checkbox = wrapper.find('input[type="checkbox"]')
    if (checkbox.exists()) {
      await checkbox.trigger('change')
    }
    expect(wrapper.emitted('toggle')).toHaveLength(1)
    expect(wrapper.emitted('toggle')![0]).toEqual(['task-1'])
  })

  it('emits edit event with task object', async () => {
    const wrapper = mount(TaskItem, { props: { task: mockTask }, global: { plugins: [vuetify] } })
    const btns = wrapper.findAll('button')
    if (btns.length > 0) {
      await btns[0].trigger('click')
    }
    expect(wrapper.emitted('edit')).toHaveLength(1)
    expect(wrapper.emitted('edit')![0]).toEqual([mockTask])
  })

  it('emits delete event with task id', async () => {
    const wrapper = mount(TaskItem, { props: { task: mockTask }, global: { plugins: [vuetify] } })
    const btns = wrapper.findAll('button')
    if (btns.length > 1) {
      await btns[1].trigger('click')
    }
    expect(wrapper.emitted('delete')).toHaveLength(1)
    expect(wrapper.emitted('delete')![0]).toEqual(['task-1'])
  })

  it('applies completed class when task is completed', () => {
    const task = { ...mockTask, completed: true }
    const wrapper = mount(TaskItem, { props: { task }, global: { plugins: [vuetify] } })
    const listItem = wrapper.find('.v-list-item')
    expect(listItem.classes()).toContain('text-decoration-line-through')
  })

  it('checkbox is checked when task is completed', () => {
    const task = { ...mockTask, completed: true }
    const wrapper = mount(TaskItem, { props: { task }, global: { plugins: [vuetify] } })
    const checkbox = wrapper.find('input[type="checkbox"]')
    expect(checkbox.exists()).toBe(true)
    expect((checkbox.element as HTMLInputElement).checked).toBe(true)
  })
})
