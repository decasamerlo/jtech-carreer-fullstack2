import { describe, it, expect, vi, beforeEach } from 'vitest'
import { loginApi, registerApi } from '../authApi'
import api from '../api'

vi.mock('../api', () => ({
  default: {
    post: vi.fn(),
  },
}))

describe('authApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('loginApi posts to /api/v1/auth/login', async () => {
    const mockPost = vi.mocked(api.post)
    mockPost.mockResolvedValueOnce({
      data: { accessToken: 'at', refreshToken: 'rt', tokenType: 'Bearer' },
    })

    const result = await loginApi('john@example.com', 'secret')

    expect(mockPost).toHaveBeenCalledWith('/api/v1/auth/login', {
      email: 'john@example.com',
      password: 'secret',
    })
    expect(result.accessToken).toBe('at')
    expect(result.refreshToken).toBe('rt')
  })

  it('registerApi posts to /api/v1/auth/register', async () => {
    const mockPost = vi.mocked(api.post)
    mockPost.mockResolvedValueOnce({
      data: { accessToken: 'at2', refreshToken: 'rt2', tokenType: 'Bearer' },
    })

    const result = await registerApi('John', 'john@example.com', 'secret')

    expect(mockPost).toHaveBeenCalledWith('/api/v1/auth/register', {
      name: 'John',
      email: 'john@example.com',
      password: 'secret',
    })
    expect(result.accessToken).toBe('at2')
  })
})
