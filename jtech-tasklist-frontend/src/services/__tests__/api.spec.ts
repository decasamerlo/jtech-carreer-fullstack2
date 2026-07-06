import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import api from '../api'

interface RefreshTokenResponse {
  accessToken: string
  refreshToken: string
}

interface ApiWithRefreshFn {
  refreshFn: (refreshToken: string) => Promise<RefreshTokenResponse>
}

describe('api interceptor', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.restoreAllMocks()
  })

  it('attaches Bearer token to requests when accessToken exists', async () => {
    const auth = useAuthStore()
    auth.accessToken = 'test-token'

    const config = { headers: {} as Record<string, string>, url: '/api/v1/tasks' }
    const handler = api.interceptors.request.handlers![0].fulfilled!
    const result = await handler(config as never)

    expect(result.headers.Authorization).toBe('Bearer test-token')
  })

  it('does not attach token to refresh requests', async () => {
    const auth = useAuthStore()
    auth.accessToken = 'test-token'

    const config = { headers: {} as Record<string, string>, url: '/api/v1/auth/refresh' }
    const handler = api.interceptors.request.handlers![0].fulfilled!
    const result = await handler(config as never)

    expect(result.headers.Authorization).toBeUndefined()
  })

  it('logs out when no refresh token exists', async () => {
    const auth = useAuthStore()

    const originalRequest = {
      _retry: false,
      headers: {},
      url: '/api/v1/tasks',
    }
    const error = { response: { status: 401 }, config: originalRequest }
    const handler = api.interceptors.response.handlers![0].rejected!

    await expect(handler(error)).rejects.toThrow()
    expect(auth.user).toBeNull()
  })

  it('does not retry non-401 errors', async () => {
    const error = { response: { status: 500 }, config: { headers: {} } }
    const handler = api.interceptors.response.handlers![0].rejected!

    await expect(handler(error)).rejects.toThrow()
  })

  it('calls refresh and updates tokens on 401', async () => {
    const auth = useAuthStore()
    auth.accessToken = 'expired-token'
    auth.refreshToken = 'refresh-token'

    const originalRefreshFn = (api as unknown as ApiWithRefreshFn).refreshFn
    ;(api as unknown as ApiWithRefreshFn).refreshFn = vi.fn().mockResolvedValue({
      accessToken: 'new-access',
      refreshToken: 'new-refresh',
    })

    const originalRequest = {
      _retry: false,
      headers: { Authorization: 'Bearer expired-token' },
      url: '/api/v1/tasks',
    }
    const error = { response: { status: 401 }, config: originalRequest }
    const handler = api.interceptors.response.handlers![0].rejected!

    // refresh + token update succeeds; the retry XHR fails in jsdom — that's expected
    await handler(error).catch(() => {})

    expect((api as unknown as ApiWithRefreshFn).refreshFn).toHaveBeenCalledWith('refresh-token')
    expect(auth.accessToken).toBe('new-access')
    expect(auth.refreshToken).toBe('new-refresh')

    ;(api as unknown as ApiWithRefreshFn).refreshFn = originalRefreshFn
  })

  it('logs out on failed refresh', async () => {
    const auth = useAuthStore()
    auth.accessToken = 'expired-token'
    auth.refreshToken = 'bad-refresh'

    const originalRefreshFn = (api as unknown as ApiWithRefreshFn).refreshFn
    ;(api as unknown as ApiWithRefreshFn).refreshFn = vi
      .fn()
      .mockRejectedValue({ response: { status: 401 } })

    const originalRequest = {
      _retry: false,
      headers: { Authorization: 'Bearer expired-token' },
      url: '/api/v1/tasks',
    }
    const error = { response: { status: 401 }, config: originalRequest }
    const handler = api.interceptors.response.handlers![0].rejected!

    await expect(handler(error)).rejects.toThrow()
    expect(auth.user).toBeNull()
    expect(auth.accessToken).toBeNull()
    expect(auth.refreshToken).toBeNull()

    ;(api as unknown as ApiWithRefreshFn).refreshFn = originalRefreshFn
  })

  it('queues concurrent 401s and resolves both after single refresh', async () => {
    const auth = useAuthStore()
    auth.accessToken = 'expired-token'
    auth.refreshToken = 'refresh-token'

    let refreshResolve!: (value: RefreshTokenResponse) => void
    const refreshPromise = new Promise<RefreshTokenResponse>((resolve) => {
      refreshResolve = resolve
    })

    const originalRefreshFn = (api as unknown as ApiWithRefreshFn).refreshFn
    ;(api as unknown as ApiWithRefreshFn).refreshFn = vi.fn().mockReturnValue(refreshPromise)

    const handler = api.interceptors.response.handlers![0].rejected!

    // Request A hits 401 first — starts the refresh cycle
    const reqA = {
      _retry: false,
      headers: { Authorization: 'Bearer expired-token' },
      url: '/api/v1/tasks/1',
    }
    const errorA = { response: { status: 401 }, config: reqA }
    const promiseA = handler(errorA).catch(() => {})

    // Request B hits 401 while refresh is in flight — should be queued, not trigger a second refresh
    const reqB = {
      _retry: false,
      headers: { Authorization: 'Bearer expired-token' },
      url: '/api/v1/tasks/2',
    }
    const errorB = { response: { status: 401 }, config: reqB }
    const promiseB = handler(errorB).catch(() => {})

    // Both requests should have _retry set before queuing
    expect(reqA._retry).toBe(true)
    expect(reqB._retry).toBe(true)

    // Only one refresh call should have been made
    expect((api as unknown as ApiWithRefreshFn).refreshFn).toHaveBeenCalledTimes(1)
    expect((api as unknown as ApiWithRefreshFn).refreshFn).toHaveBeenCalledWith('refresh-token')

    // Resolve the refresh — both queued requests should resolve
    refreshResolve({ accessToken: 'new-access', refreshToken: 'new-refresh' })
    await Promise.all([promiseA, promiseB])

    expect(auth.accessToken).toBe('new-access')
    expect(auth.refreshToken).toBe('new-refresh')

    ;(api as unknown as ApiWithRefreshFn).refreshFn = originalRefreshFn
  })
})
