<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const name = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const error = ref<string | null>(null)
const errors = ref<{
  name?: string
  email?: string
  password?: string
  confirmPassword?: string
}>({})

function validate(): boolean {
  errors.value = {}

  if (!name.value.trim()) {
    errors.value.name = 'Name is required'
  }
  if (!email.value.trim()) {
    errors.value.email = 'Email is required'
  }
  if (!password.value.trim()) {
    errors.value.password = 'Password is required'
  } else if (password.value.length < 6) {
    errors.value.password = 'Password must be at least 6 characters'
  }
  if (password.value !== confirmPassword.value) {
    errors.value.confirmPassword = 'Passwords do not match'
  }

  return Object.keys(errors.value).length === 0
}

async function handleSubmit() {
  if (!validate()) return

  loading.value = true
  error.value = null

  try {
    await auth.register(name.value.trim(), email.value.trim(), password.value)
    router.push({ name: 'home' })
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Registration failed'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-container">
    <form class="register-form" @submit.prevent="handleSubmit">
      <h1>Create Account</h1>

      <p v-if="error" class="error-message">{{ error }}</p>

      <div class="field">
        <label for="name">Name</label>
        <input id="name" v-model="name" type="text" placeholder="Enter your name" />
        <p v-if="errors.name" class="field-error">{{ errors.name }}</p>
      </div>

      <div class="field">
        <label for="email">Email</label>
        <input id="email" v-model="email" type="email" placeholder="Enter your email" />
        <p v-if="errors.email" class="field-error">{{ errors.email }}</p>
      </div>

      <div class="field">
        <label for="password">Password</label>
        <input id="password" v-model="password" type="password" placeholder="Enter your password" />
        <p v-if="errors.password" class="field-error">{{ errors.password }}</p>
      </div>

      <div class="field">
        <label for="confirmPassword">Confirm Password</label>
        <input id="confirmPassword" v-model="confirmPassword" type="password" placeholder="Confirm your password" />
        <p v-if="errors.confirmPassword" class="field-error">{{ errors.confirmPassword }}</p>
      </div>

      <button type="submit" :disabled="loading">
        {{ loading ? 'Creating account...' : 'Create Account' }}
      </button>

      <p class="login-link">
        Already have an account?
        <router-link :to="{ name: 'login' }">Sign In</router-link>
      </p>
    </form>
  </div>
</template>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  grid-column: 1 / -1;
}

.register-form {
  width: 100%;
  max-width: 400px;
  padding: 2rem;
  border: 1px solid var(--color-border);
  border-radius: 8px;
}

.register-form h1 {
  margin-bottom: 1.5rem;
  text-align: center;
}

.error-message {
  color: #e74c3c;
  text-align: center;
  margin-bottom: 1rem;
  padding: 0.5rem;
  background: #fdf0ef;
  border-radius: 4px;
}

.field {
  margin-bottom: 1rem;
}

.field label {
  display: block;
  margin-bottom: 0.25rem;
  font-weight: 600;
}

.field input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: 1rem;
}

.field-error {
  color: #e74c3c;
  font-size: 0.85rem;
  margin-top: 0.25rem;
}

button[type='submit'] {
  width: 100%;
  padding: 0.75rem;
  background-color: hsla(160, 100%, 37%, 1);
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
}

button[type='submit']:hover {
  background-color: hsla(160, 100%, 30%, 1);
}

button[type='submit']:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-link {
  text-align: center;
  margin-top: 1rem;
  font-size: 0.9rem;
}
</style>
