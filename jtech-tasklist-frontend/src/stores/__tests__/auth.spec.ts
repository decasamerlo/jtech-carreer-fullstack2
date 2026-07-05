import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '../auth'

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('starts with null user', () => {
    const store = useAuthStore()
    expect(store.user).toBeNull()
  })

  it('isAuthenticated is false when no user', () => {
    const store = useAuthStore()
    expect(store.isAuthenticated).toBe(false)
  })

  it('login sets user with derived email', () => {
    const store = useAuthStore()
    store.login('john', 'secret')
    expect(store.user).toEqual({
      username: 'john',
      email: 'john@example.com',
    })
    expect(store.isAuthenticated).toBe(true)
  })

  it('logout clears user', () => {
    const store = useAuthStore()
    store.login('john', 'secret')
    store.logout()
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })
})
