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
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="4">
        <v-card>
          <v-card-title class="text-center text-h5">Create Account</v-card-title>
          <v-card-text>
            <v-alert v-if="error" type="error" closable class="mb-4" @click:close="error = null">
              {{ error }}
            </v-alert>
            <v-form @submit.prevent="handleSubmit">
              <v-text-field
                v-model="name"
                label="Name"
                prepend-icon="mdi-account"
                data-testid="input-name"
                :error-messages="errors.name"
                @input="errors.name = ''"
              />
              <v-text-field
                v-model="email"
                label="Email"
                type="email"
                prepend-icon="mdi-email"
                :error-messages="errors.email"
                @input="errors.email = ''"
              />
              <v-text-field
                v-model="password"
                label="Password"
                type="password"
                prepend-icon="mdi-lock"
                :error-messages="errors.password"
                @input="errors.password = ''"
              />
              <v-text-field
                v-model="confirmPassword"
                label="Confirm Password"
                type="password"
                prepend-icon="mdi-lock-check"
                :error-messages="errors.confirmPassword"
                @input="errors.confirmPassword = ''"
              />
              <v-btn type="submit" color="primary" block :loading="loading" class="mt-2">
                {{ loading ? 'Creating account...' : 'Create Account' }}
              </v-btn>
            </v-form>
            <v-divider class="my-4" />
            <p class="text-center text-body-2">
              Already have an account?
              <router-link :to="{ name: 'login' }">Sign In</router-link>
            </p>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>
