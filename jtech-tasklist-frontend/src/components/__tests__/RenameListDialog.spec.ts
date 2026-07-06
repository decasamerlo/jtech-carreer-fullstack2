import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import RenameListDialog from '../RenameListDialog.vue'

describe('RenameListDialog', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders input with current name when open', () => {
    const wrapper = mount(RenameListDialog, {
      props: { open: true, currentName: 'Old Name' },
    })
    expect(wrapper.find('input').element.value).toBe('Old Name')
  })

  it('does not render when closed', () => {
    const wrapper = mount(RenameListDialog, {
      props: { open: false, currentName: 'Old Name' },
    })
    expect(wrapper.find('input').exists()).toBe(false)
  })

  it('emits close event on cancel', async () => {
    const wrapper = mount(RenameListDialog, {
      props: { open: true, currentName: 'Old Name' },
    })
    await wrapper.find('button.cancel-btn').trigger('click')
    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('shows error for empty name', async () => {
    const wrapper = mount(RenameListDialog, {
      props: { open: true, currentName: 'Old Name' },
    })
    await wrapper.find('input').setValue('')
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.text()).toContain('List name is required')
  })

  it('emits rename with new name on valid submit', async () => {
    const wrapper = mount(RenameListDialog, {
      props: { open: true, currentName: 'Old Name' },
    })
    await wrapper.find('input').setValue('New Name')
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.emitted('rename')).toHaveLength(1)
    expect(wrapper.emitted('rename')![0]).toEqual(['New Name'])
  })
})
