import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import { createVuetify } from 'vuetify'
import AboutView from '../AboutView.vue'

const vuetify = createVuetify()

describe('AboutView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders the about title and description', () => {
    const wrapper = mount(AboutView, {
      global: { plugins: [vuetify] },
    })
    expect(wrapper.text()).toContain('About this app')
    expect(wrapper.text()).toContain('JTech challenge')
  })
})
