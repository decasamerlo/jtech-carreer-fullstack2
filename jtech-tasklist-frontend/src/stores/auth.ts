import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { loginApi, registerApi } from '@/services/authApi'

export interface AuthUser {
  email: string
  name: string
  role: string
}

export interface StoredTokens {
  accessToken: string
  refreshToken: string
}

// display only, not verified — trust boundary is the server
function decodeJwtPayload(token: string): Record<string, unknown> | null {
  try {
    const payload = token.split('.')[1]
    return JSON.parse(atob(payload))
  } catch {
    return null
  }
}

export const useAuthStore = defineStore(
  'auth',
  () => {
    const user = ref<AuthUser | null>(null)
    const accessToken = ref<string | null>(null)
    const refreshToken = ref<string | null>(null)

    const mode = ref<'mock' | 'api'>(import.meta.env.VITE_AUTH_MODE === 'api' ? 'api' : 'mock')

    const isAuthenticated = computed(() => user.value !== null)

    async function login(email: string, password: string) {
      if (mode.value === 'mock') {
        user.value = {
          name: email.split('@')[0],
          email,
          role: 'ROLE_USER',
        }
        return
      }

      const data = await loginApi(email, password)
      accessToken.value = data.accessToken
      refreshToken.value = data.refreshToken

      const payload = decodeJwtPayload(data.accessToken)
      user.value = {
        email: (payload?.email as string) || email,
        name: (payload?.name as string) || email.split('@')[0],
        role: (payload?.role as string) || 'ROLE_USER',
      }
    }

    async function register(name: string, email: string, password: string) {
      if (mode.value === 'mock') {
        user.value = { name, email, role: 'ROLE_USER' }
        return
      }

      const data = await registerApi(name, email, password)
      accessToken.value = data.accessToken
      refreshToken.value = data.refreshToken

      const payload = decodeJwtPayload(data.accessToken)
      user.value = {
        email: (payload?.email as string) || email,
        name: (payload?.name as string) || name,
        role: (payload?.role as string) || 'ROLE_USER',
      }
    }

    function logout() {
      user.value = null
      accessToken.value = null
      refreshToken.value = null
    }

    return {
      user,
      accessToken,
      refreshToken,
      mode,
      isAuthenticated,
      login,
      register,
      logout,
    }
  },
  {
    persist: {
      pick: ['user', 'accessToken', 'refreshToken'],
    },
  },
)
