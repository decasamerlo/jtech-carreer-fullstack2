<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const email = ref('')
const password = ref('')
const loading = ref(false)
const error = ref<string | null>(null)
const errors = ref<{ email?: string; password?: string }>({})

function validate(): boolean {
  errors.value = {}

  if (!email.value.trim()) {
    errors.value.email = 'Email is required'
  }
  if (!password.value.trim()) {
    errors.value.password = 'Password is required'
  }

  return Object.keys(errors.value).length === 0
}

async function handleSubmit() {
  if (!validate()) return

  loading.value = true
  error.value = null

  try {
    await auth.login(email.value.trim(), password.value)
    router.push({ name: 'home' })
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Login failed'
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
          <v-card-title class="text-center text-h5">Sign In</v-card-title>
          <v-card-text>
            <v-alert v-if="error" type="error" closable class="mb-4" @click:close="error = null">
              {{ error }}
            </v-alert>
            <v-form @submit.prevent="handleSubmit">
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
              <v-btn type="submit" color="primary" block :loading="loading" class="mt-2">
                {{ loading ? 'Signing in...' : 'Sign In' }}
              </v-btn>
            </v-form>
            <v-divider class="my-4" />
            <p class="text-center text-body-2">
              Don't have an account?
              <router-link :to="{ name: 'register' }">Register</router-link>
            </p>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>
