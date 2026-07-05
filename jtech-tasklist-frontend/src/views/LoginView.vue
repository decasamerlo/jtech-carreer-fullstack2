<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const errors = ref<{ username?: string; password?: string }>({})

function validate(): boolean {
  errors.value = {}

  if (!username.value.trim()) {
    errors.value.username = 'Username is required'
  }
  if (!password.value.trim()) {
    errors.value.password = 'Password is required'
  }

  return Object.keys(errors.value).length === 0
}

function handleSubmit() {
  if (!validate()) return

  auth.login(username.value.trim(), password.value)
  router.push({ name: 'home' })
}
</script>

<template>
  <div class="login-container">
    <form class="login-form" @submit.prevent="handleSubmit">
      <h1>Sign In</h1>

      <div class="field">
        <label for="username">Username</label>
        <input id="username" v-model="username" type="text" placeholder="Enter your username" />
        <p v-if="errors.username" class="error">{{ errors.username }}</p>
      </div>

      <div class="field">
        <label for="password">Password</label>
        <input id="password" v-model="password" type="password" placeholder="Enter your password" />
        <p v-if="errors.password" class="error">{{ errors.password }}</p>
      </div>

      <button type="submit">Sign In</button>
    </form>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  grid-column: 1 / -1;
}

.login-form {
  width: 100%;
  max-width: 400px;
  padding: 2rem;
  border: 1px solid var(--color-border);
  border-radius: 8px;
}

.login-form h1 {
  margin-bottom: 1.5rem;
  text-align: center;
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

.field .error {
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
</style>
