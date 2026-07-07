<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useListsStore } from '@/stores/lists'
import { useTasksStore } from '@/stores/tasks'
import { useAuthStore } from '@/stores/auth'
import TaskListSidebar from '@/components/TaskListSidebar.vue'
import TaskItem from '@/components/TaskItem.vue'
import CreateTaskDialog from '@/components/CreateTaskDialog.vue'
import EditTaskDialog from '@/components/EditTaskDialog.vue'
import DeleteTaskDialog from '@/components/DeleteTaskDialog.vue'
import type { Task } from '@/types/task'

const router = useRouter()
const listsStore = useListsStore()
const tasksStore = useTasksStore()
const auth = useAuthStore()

const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const showDeleteDialog = ref(false)
const selectedTask = ref<Task | null>(null)
const selectedTaskId = ref<string | null>(null)
const createError = ref('')
const editError = ref('')
const deleteError = ref('')
const toggleError = ref('')

onMounted(() => {
  listsStore.fetchLists()
})

watch(
  () => listsStore.activeListId,
  (listId) => {
    if (listId) {
      tasksStore.fetchTasks(listId)
    }
  },
  { immediate: true },
)

function handleLogout() {
  auth.logout()
  router.push({ name: 'login' })
}

function openCreateDialog() {
  createError.value = ''
  showCreateDialog.value = true
}

async function handleCreateTask(title: string, description?: string) {
  if (!listsStore.activeListId) return
  createError.value = ''
  try {
    await tasksStore.addTask(listsStore.activeListId, title, description)
    showCreateDialog.value = false
  } catch (e: unknown) {
    createError.value = e instanceof Error ? e.message : 'Failed to create task'
  }
}

function openEditDialog(task: Task) {
  selectedTask.value = task
  editError.value = ''
  showEditDialog.value = true
}

async function handleEditTask(id: string, title: string, description?: string) {
  editError.value = ''
  try {
    await tasksStore.editTask(id, title, description)
    showEditDialog.value = false
    selectedTask.value = null
  } catch (e: unknown) {
    editError.value = e instanceof Error ? e.message : 'Failed to update task'
  }
}

function openDeleteDialog(id: string) {
  selectedTaskId.value = id
  showDeleteDialog.value = true
}

const selectedTaskForDelete = ref<Task | null>(null)

watch(selectedTaskId, (id) => {
  if (id) {
    selectedTaskForDelete.value = tasksStore.tasksForActiveList.find((t) => t.id === id) ?? null
  }
})

async function handleDeleteTask() {
  if (!selectedTaskId.value) return
  deleteError.value = ''
  try {
    await tasksStore.removeTask(selectedTaskId.value)
    showDeleteDialog.value = false
    selectedTaskId.value = null
  } catch (e: unknown) {
    deleteError.value = e instanceof Error ? e.message : 'Failed to delete task'
  }
}

async function handleToggleComplete(id: string) {
  toggleError.value = ''
  try {
    await tasksStore.toggleComplete(id)
  } catch (e: unknown) {
    toggleError.value = e instanceof Error ? e.message : 'Failed to toggle task'
  }
}
</script>

<template>
  <div class="lists-layout">
    <TaskListSidebar />
    <main class="content">
      <header class="content-header">
        <router-link :to="{ name: 'home' }" class="back-link">&larr; Home</router-link>
        <button class="sign-out-btn" @click="handleLogout">Sign Out</button>
      </header>
      <div v-if="!listsStore.initialized" class="loading-state">
        <p>Loading...</p>
      </div>
      <div v-else-if="listsStore.activeList" class="list-content">
        <div class="list-header">
          <h1>{{ listsStore.activeList.name }}</h1>
          <button class="add-task-btn" @click="openCreateDialog">+ Add Task</button>
        </div>
        <p class="list-meta">
          Created:
          {{
            listsStore.activeList.createdAt
              ? new Date(listsStore.activeList.createdAt).toLocaleDateString()
              : '—'
          }}
        </p>
        <p v-if="toggleError" class="toggle-error" role="alert" aria-live="polite">
          {{ toggleError }}
        </p>
        <div v-if="tasksStore.tasksForActiveList.length === 0" class="empty-tasks">
          <p>No tasks yet. Add one to get started!</p>
        </div>
        <ul v-else class="task-list">
          <TaskItem
            v-for="task in tasksStore.tasksForActiveList"
            :key="task.id"
            :task="task"
            @toggle="handleToggleComplete"
            @edit="openEditDialog"
            @delete="openDeleteDialog"
          />
        </ul>
      </div>
      <div v-else class="empty-state">
        <h2>Select a list</h2>
        <p>Choose a list from the sidebar or create a new one.</p>
      </div>
    </main>

    <CreateTaskDialog
      :open="showCreateDialog"
      :error="createError"
      @close="showCreateDialog = false"
      @create="handleCreateTask"
    />
    <EditTaskDialog
      :open="showEditDialog"
      :task="selectedTask"
      :error="editError"
      @close="showEditDialog = false"
      @save="handleEditTask"
    />
    <DeleteTaskDialog
      :open="showDeleteDialog"
      :task-title="selectedTaskForDelete?.title ?? ''"
      :error="deleteError"
      @close="showDeleteDialog = false"
      @delete="handleDeleteTask"
    />
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

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.list-header h1 {
  margin: 0;
}

.add-task-btn {
  padding: 0.5rem 1rem;
  background-color: hsla(160, 100%, 37%, 1);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
}

.list-meta {
  color: #888;
  font-size: 0.875rem;
  margin-bottom: 1.5rem;
}

.toggle-error {
  color: #e74c3c;
  font-size: 0.875rem;
  margin-bottom: 1.5rem;
}

.task-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.empty-tasks,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #888;
  padding: 3rem 0;
}

.empty-state {
  height: 100%;
}

.empty-state h2 {
  margin-bottom: 0.5rem;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #888;
}
</style>
