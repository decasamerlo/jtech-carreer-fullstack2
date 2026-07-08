<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

function handleLogout() {
  auth.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6" class="text-center">
        <v-card>
          <v-card-title class="text-h4">Welcome, {{ auth.user?.name }}!</v-card-title>
          <v-card-subtitle class="text-body-1 mt-2">
            You are signed in as {{ auth.user?.email }}
          </v-card-subtitle>
          <v-card-actions class="justify-center pb-4">
            <v-btn color="primary" variant="elevated" :to="{ name: 'lists' }" class="mr-2">
              Go to Lists
            </v-btn>
            <v-btn color="error" variant="outlined" @click="handleLogout">
              Sign Out
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>
