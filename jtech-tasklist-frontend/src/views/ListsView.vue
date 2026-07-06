<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useListsStore } from '@/stores/lists'
import { useAuthStore } from '@/stores/auth'
import TaskListSidebar from '@/components/TaskListSidebar.vue'

const router = useRouter()
const store = useListsStore()
const auth = useAuthStore()

function handleLogout() {
  auth.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="lists-layout">
    <TaskListSidebar />
    <main class="content">
      <header class="content-header">
        <router-link :to="{ name: 'home' }" class="back-link">← Home</router-link>
        <button class="sign-out-btn" @click="handleLogout">Sign Out</button>
      </header>
      <div v-if="store.activeList" class="list-content">
        <h1>{{ store.activeList.name }}</h1>
        <p class="list-meta">
          Created: {{ store.activeList.createdAt ? new Date(store.activeList.createdAt).toLocaleDateString() : '—' }}
        </p>
      </div>
      <div v-else class="empty-state">
        <h2>Select a list</h2>
        <p>Choose a list from the sidebar or create a new one.</p>
      </div>
    </main>
  </div>
</template>

<style scoped>
.lists-layout {
  display: flex;
  height: 100vh;
}

.content {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.back-link {
  color: hsla(160, 100%, 37%, 1);
  text-decoration: none;
}

.back-link:hover {
  text-decoration: underline;
}

.sign-out-btn {
  padding: 0.4rem 0.8rem;
  background-color: #e74c3c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
}

.list-content h1 {
  margin-bottom: 0.5rem;
}

.list-meta {
  color: #888;
  font-size: 0.875rem;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #888;
}

.empty-state h2 {
  margin-bottom: 0.5rem;
}
</style>
