import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type { Task } from '@/types/task'
import { useAuthStore } from '@/stores/auth'
import {
  fetchTasks as apiFetchTasks,
  createTask as apiCreateTask,
  updateTask as apiUpdateTask,
  deleteTask as apiDeleteTask,
} from '@/services/taskApi'

export const useTasksStore = defineStore(
  'tasks',
  () => {
    const auth = useAuthStore()

    // Mock mode state
    const allTasks = ref<Task[]>([])

    // API mode state
    const apiTasks = ref<Task[]>([])
    const initialized = ref(false)
    const currentTasklistId = ref<string | null>(null)

    // Unified tasks — mock filters by ownerEmail via list ownership, API returns server-filtered
    const tasksForActiveList = computed(() => {
      const listId = currentTasklistId.value
      if (!listId) return []
      if (auth.mode === 'api') {
        return apiTasks.value.filter((t) => t.tasklistId === listId)
      }
      return allTasks.value.filter((t) => t.tasklistId === listId)
    })

    function validateTitle(title: string, tasklistId: string, excludeId?: string): void {
      const trimmed = title.trim()
      if (!trimmed) throw new Error('Task title is required')
      if (trimmed.length > 255) throw new Error('Task title must be 255 characters or less')
      const sourceTasks = auth.mode === 'api' ? apiTasks.value : allTasks.value
      const duplicate = sourceTasks.find(
        (t) =>
          t.tasklistId === tasklistId &&
          t.title.toLowerCase() === trimmed.toLowerCase() &&
          t.id !== excludeId,
      )
      if (duplicate) throw new Error('A task with this title already exists in this list')
    }

    // --- Mock mode helpers ---

    function mockFetchTasks(tasklistId: string): void {
      currentTasklistId.value = tasklistId
      initialized.value = true
    }

    function mockAddTask(tasklistId: string, title: string, description?: string): Task {
      if (!auth.user) throw new Error('Cannot create task: not authenticated')
      validateTitle(title, tasklistId)
      currentTasklistId.value = tasklistId
      const now = new Date().toISOString()
      const task: Task = {
        id: crypto.randomUUID(),
        title: title.trim(),
        description: description?.trim() || undefined,
        completed: false,
        tasklistId,
        createdAt: now,
        updatedAt: now,
      }
      allTasks.value.push(task)
      return task
    }

    function mockEditTask(id: string, title: string, description?: string): void {
      const task = allTasks.value.find((t) => t.id === id)
      if (!task) throw new Error('Task not found')
      validateTitle(title, task.tasklistId, id)
      task.title = title.trim()
      task.description = description?.trim() || undefined
      task.updatedAt = new Date().toISOString()
    }

    function mockRemoveTask(id: string): void {
      const index = allTasks.value.findIndex((t) => t.id === id)
      if (index === -1) throw new Error('Task not found')
      allTasks.value.splice(index, 1)
    }

    function mockToggleComplete(id: string): void {
      const task = allTasks.value.find((t) => t.id === id)
      if (!task) throw new Error('Task not found')
      task.completed = !task.completed
      task.updatedAt = new Date().toISOString()
    }

    // --- API mode helpers ---

    async function apiFetchTaskList(tasklistId: string): Promise<void> {
      if (!auth.user) throw new Error('Cannot fetch tasks: not authenticated')
      currentTasklistId.value = tasklistId
      const fetched = await apiFetchTasks(tasklistId)
      apiTasks.value = fetched
      initialized.value = true
    }

    async function apiAddTask(
      tasklistId: string,
      title: string,
      description?: string,
    ): Promise<Task> {
      if (!auth.user) throw new Error('Cannot create task: not authenticated')
      currentTasklistId.value = tasklistId
      validateTitle(title, tasklistId)
      const created = await apiCreateTask(tasklistId, {
        title: title.trim(),
        description: description?.trim() || undefined,
      })
      apiTasks.value = [...apiTasks.value, created]
      return created
    }

    async function apiEditTask(id: string, title: string, description?: string): Promise<void> {
      const task = apiTasks.value.find((t) => t.id === id)
      if (!task) throw new Error('Task not found')
      validateTitle(title, task.tasklistId, id)
      const updated = await apiUpdateTask(id, {
        title: title.trim(),
        description: description?.trim() || undefined,
      })
      const idx = apiTasks.value.findIndex((t) => t.id === id)
      if (idx !== -1) apiTasks.value[idx] = updated
    }

    async function apiRemoveTask(id: string): Promise<void> {
      const exists = apiTasks.value.find((t) => t.id === id)
      if (!exists) throw new Error('Task not found')
      await apiDeleteTask(id)
      apiTasks.value = apiTasks.value.filter((t) => t.id !== id)
    }

    async function apiToggleComplete(id: string): Promise<void> {
      const task = apiTasks.value.find((t) => t.id === id)
      if (!task) throw new Error('Task not found')
      const updated = await apiUpdateTask(id, {
        title: task.title,
        description: task.description,
        completed: !task.completed,
      })
      const idx = apiTasks.value.findIndex((t) => t.id === id)
      if (idx !== -1) apiTasks.value[idx] = updated
    }

    // --- Public API (branches on auth.mode) ---

    async function fetchTasks(tasklistId: string): Promise<void> {
      if (auth.mode === 'api') {
        await apiFetchTaskList(tasklistId)
      } else {
        mockFetchTasks(tasklistId)
      }
    }

    async function addTask(
      tasklistId: string,
      title: string,
      description?: string,
    ): Promise<Task> {
      if (!auth.user) throw new Error('Cannot create task: not authenticated')
      if (auth.mode === 'api') return apiAddTask(tasklistId, title, description)
      return mockAddTask(tasklistId, title, description)
    }

    async function editTask(id: string, title: string, description?: string): Promise<void> {
      if (auth.mode === 'api') {
        await apiEditTask(id, title, description)
        return
      }
      mockEditTask(id, title, description)
    }

    async function removeTask(id: string): Promise<void> {
      if (auth.mode === 'api') {
        await apiRemoveTask(id)
        return
      }
      mockRemoveTask(id)
    }

    async function toggleComplete(id: string): Promise<void> {
      if (auth.mode === 'api') {
        await apiToggleComplete(id)
        return
      }
      mockToggleComplete(id)
    }

    return {
      tasksForActiveList,
      initialized,
      currentTasklistId,
      fetchTasks,
      addTask,
      editTask,
      removeTask,
      toggleComplete,
    }
  },
  {
    persist: {
      pick: ['allTasks'],
    },
  },
)
