import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import CreateListDialog from '../CreateListDialog.vue'

describe('CreateListDialog', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders input and buttons when open', () => {
    const wrapper = mount(CreateListDialog, { props: { open: true } })
    expect(wrapper.find('input').exists()).toBe(true)
    expect(wrapper.find('button[type="submit"]').exists()).toBe(true)
  })

  it('does not render when closed', () => {
    const wrapper = mount(CreateListDialog, { props: { open: false } })
    expect(wrapper.find('input').exists()).toBe(false)
  })

  it('emits close event on cancel', async () => {
    const wrapper = mount(CreateListDialog, { props: { open: true } })
    await wrapper.find('button.cancel-btn').trigger('click')
    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('shows error for empty name', async () => {
    const wrapper = mount(CreateListDialog, { props: { open: true } })
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.text()).toContain('List name is required')
  })

  it('shows error for name over 50 chars', async () => {
    const wrapper = mount(CreateListDialog, { props: { open: true } })
    await wrapper.find('input').setValue('a'.repeat(51))
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.text()).toContain('50 characters or less')
  })
})
