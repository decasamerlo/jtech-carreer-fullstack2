import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import CreateTaskDialog from '../CreateTaskDialog.vue'

describe('CreateTaskDialog', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders inputs and buttons when open', () => {
    const wrapper = mount(CreateTaskDialog, { props: { open: true } })
    expect(wrapper.find('#task-title').exists()).toBe(true)
    expect(wrapper.find('#task-description').exists()).toBe(true)
    expect(wrapper.find('button[type="submit"]').exists()).toBe(true)
  })

  it('does not render when closed', () => {
    const wrapper = mount(CreateTaskDialog, { props: { open: false } })
    expect(wrapper.find('#task-title').exists()).toBe(false)
  })

  it('emits close event on cancel', async () => {
    const wrapper = mount(CreateTaskDialog, { props: { open: true } })
    await wrapper.find('button.cancel-btn').trigger('click')
    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('shows validation error for empty title', async () => {
    const wrapper = mount(CreateTaskDialog, { props: { open: true } })
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.text()).toContain('Task title is required')
  })

  it('shows validation error for title over 255 chars', async () => {
    const wrapper = mount(CreateTaskDialog, { props: { open: true } })
    await wrapper.find('#task-title').setValue('a'.repeat(256))
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.text()).toContain('255 characters or less')
  })

  it('emits create with title and description (does not self-close)', async () => {
    const wrapper = mount(CreateTaskDialog, { props: { open: true } })
    await wrapper.find('#task-title').setValue('New Task')
    await wrapper.find('#task-description').setValue('Some description')
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.emitted('create')).toHaveLength(1)
    expect(wrapper.emitted('create')![0]).toEqual(['New Task', 'Some description'])
    expect(wrapper.emitted('close')).toBeUndefined()
  })

  it('emits create with title only when description empty', async () => {
    const wrapper = mount(CreateTaskDialog, { props: { open: true } })
    await wrapper.find('#task-title').setValue('New Task')
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.emitted('create')![0]).toEqual(['New Task', undefined])
  })

  it('displays async error from parent prop', async () => {
    const wrapper = mount(CreateTaskDialog, {
      props: { open: true, error: 'A task with this title already exists in this list' },
    })
    expect(wrapper.text()).toContain('A task with this title already exists in this list')
  })

  it('resets form when dialog opens', async () => {
    const wrapper = mount(CreateTaskDialog, { props: { open: false } })
    await wrapper.setProps({ open: true })
    expect((wrapper.find('#task-title').element as HTMLInputElement).value).toBe('')
  })
})
