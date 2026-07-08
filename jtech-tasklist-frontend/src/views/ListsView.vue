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
  <v-layout class="lists-layout">
    <TaskListSidebar />
    <v-main class="content">
      <v-app-bar flat border>
        <v-btn icon="mdi-home" variant="text" :to="{ name: 'home' }" />
        <v-spacer />
        <v-btn color="error" variant="text" @click="handleLogout">Sign Out</v-btn>
      </v-app-bar>

      <v-container v-if="!listsStore.initialized" class="fill-height">
        <v-row align="center" justify="center">
          <v-col cols="auto"><v-progress-circular indeterminate /></v-col>
        </v-row>
      </v-container>

      <v-container v-else-if="listsStore.activeList" fluid>
        <div class="d-flex align-center mb-2">
          <h1 class="text-h4 flex-grow-1">{{ listsStore.activeList.name }}</h1>
          <v-btn color="primary" prepend-icon="mdi-plus" @click="openCreateDialog">
            Add Task
          </v-btn>
        </div>
        <p class="text-body-2 text-grey mb-4">
          Created: {{ listsStore.activeList.createdAt ? new Date(listsStore.activeList.createdAt).toLocaleDateString() : '—' }}
        </p>

        <v-alert v-if="toggleError" type="error" closable class="mb-4" @click:close="toggleError = ''">
          {{ toggleError }}
        </v-alert>

        <v-list v-if="tasksStore.tasksForActiveList.length > 0">
          <TaskItem
            v-for="task in tasksStore.tasksForActiveList"
            :key="task.id"
            :task="task"
            @toggle="handleToggleComplete"
            @edit="openEditDialog"
            @delete="openDeleteDialog"
          />
        </v-list>
        <div v-else class="d-flex flex-column align-center justify-center pa-6 text-grey">
          <p class="text-h6">No tasks yet</p>
          <p class="text-body-2">Add one to get started!</p>
        </div>
      </v-container>

      <v-container v-else class="fill-height">
        <v-row align="center" justify="center">
          <v-col cols="auto" class="text-center">
            <h2 class="text-h5">Select a list</h2>
            <p class="text-body-1 text-grey">Choose a list from the sidebar or create a new one.</p>
          </v-col>
        </v-row>
      </v-container>
    </v-main>

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
  </v-layout>
</template>
