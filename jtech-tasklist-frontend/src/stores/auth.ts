import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export interface AuthUser {
  username: string
  email: string
}

export const useAuthStore = defineStore(
  'auth',
  () => {
    const user = ref<AuthUser | null>(null)

    const isAuthenticated = computed(() => user.value !== null)

    function login(username: string, _password: string) {
      user.value = {
        username,
        email: `${username}@example.com`,
      }
    }

    function logout() {
      user.value = null
    }

    return { user, isAuthenticated, login, logout }
  },
  {
    persist: true,
  },
)
