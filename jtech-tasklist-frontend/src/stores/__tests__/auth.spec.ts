import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '../auth'

vi.mock('@/services/authApi', () => ({
  loginApi: vi.fn(),
  registerApi: vi.fn(),
}))

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    ;(import.meta.env as any).VITE_AUTH_MODE = 'mock'
  })

  it('starts with null user', () => {
    const store = useAuthStore()
    expect(store.user).toBeNull()
  })

  it('isAuthenticated is false when no user', () => {
    const store = useAuthStore()
    expect(store.isAuthenticated).toBe(false)
  })

  it('login in mock mode sets user with name and email', async () => {
    const store = useAuthStore()
    await store.login('john@example.com', 'secret')
    expect(store.user).toEqual({
      name: 'john',
      email: 'john@example.com',
      role: 'ROLE_USER',
    })
    expect(store.isAuthenticated).toBe(true)
  })

  it('register in mock mode sets user', async () => {
    const store = useAuthStore()
    await store.register('John Doe', 'john@example.com', 'secret')
    expect(store.user).toEqual({
      name: 'John Doe',
      email: 'john@example.com',
      role: 'ROLE_USER',
    })
    expect(store.isAuthenticated).toBe(true)
  })

  it('logout clears user and tokens', () => {
    const store = useAuthStore()
    store.login('john@example.com', 'secret')
    store.logout()
    expect(store.user).toBeNull()
    expect(store.accessToken).toBeNull()
    expect(store.refreshToken).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })
})
