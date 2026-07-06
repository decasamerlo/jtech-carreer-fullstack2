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
  <main>
    <h1>Welcome, {{ auth.user?.name }}!</h1>
    <p>You are signed in as {{ auth.user?.email }}</p>
    <button @click="handleLogout">Sign Out</button>
    <router-link :to="{ name: 'lists' }" class="lists-link">Go to Lists</router-link>
  </main>
</template>

<style scoped>
main {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  grid-column: 1 / -1;
}

.lists-link {
  margin-top: 1rem;
  color: hsla(160, 100%, 37%, 1);
}

button {
  margin-top: 1rem;
  padding: 0.5rem 1rem;
  background-color: #e74c3c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>
