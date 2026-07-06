import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type { TaskList } from '@/types/list'
import { useAuthStore } from '@/stores/auth'
import {
  fetchTasklists as apiFetchTasklists,
  createTasklist as apiCreateTasklist,
  updateTasklist as apiUpdateTasklist,
  deleteTasklist as apiDeleteTasklist,
} from '@/services/tasklistApi'

export const useListsStore = defineStore(
  'lists',
  () => {
    const auth = useAuthStore()

    // Mock mode state
    const allLists = ref<TaskList[]>([])
    const activeListIdByEmail = ref<Record<string, string | null>>({})

    // API mode state
    const apiLists = ref<TaskList[]>([])
    const initialized = ref(false)

    // Unified lists computed — mock filters by ownerEmail, API returns all (backend filters)
    const lists = computed(() => {
      if (auth.mode === 'api') return apiLists.value
      return allLists.value.filter((l) => l.ownerEmail === auth.user?.email)
    })

    const activeListId = computed(() => {
      const email = auth.user?.email
      if (!email) return null
      return activeListIdByEmail.value[email] ?? null
    })

    const activeList = computed(() =>
      lists.value.find((l) => l.id === activeListId.value) ?? null,
    )

    function setActiveListForCurrentUser(id: string | null): void {
      const email = auth.user?.email
      if (!email) return
      activeListIdByEmail.value[email] = id
    }

    function validateName(name: string, excludeId?: string): void {
      const trimmed = name.trim()
      if (!trimmed) throw new Error('List name is required')
      if (trimmed.length > 50) throw new Error('List name must be 50 characters or less')
      const duplicate = lists.value.find(
        (l) => l.name.toLowerCase() === trimmed.toLowerCase() && l.id !== excludeId,
      )
      if (duplicate) throw new Error('A list with this name already exists')
    }

    // --- Mock mode helpers ---

    function mockCreateList(name: string): TaskList {
      if (!auth.user) throw new Error('Cannot create list: not authenticated')
      validateName(name)
      const now = new Date().toISOString()
      const list: TaskList = {
        id: crypto.randomUUID(),
        name: name.trim(),
        ownerEmail: auth.user.email,
        createdAt: now,
        updatedAt: now,
      }
      allLists.value.push(list)
      setActiveListForCurrentUser(list.id)
      return list
    }

    function mockRenameList(id: string, newName: string): void {
      const list = allLists.value.find(
        (l) => l.id === id && l.ownerEmail === auth.user?.email,
      )
      if (!list) throw new Error('List not found')
      validateName(newName, id)
      list.name = newName.trim()
      list.updatedAt = new Date().toISOString()
    }

    function mockDeleteList(id: string): void {
      const index = allLists.value.findIndex(
        (l) => l.id === id && l.ownerEmail === auth.user?.email,
      )
      if (index === -1) throw new Error('List not found')
      allLists.value.splice(index, 1)
      if (activeListId.value === id) {
        setActiveListForCurrentUser(lists.value[0]?.id ?? null)
      }
    }

    // --- API mode helpers ---

    async function apiFetchLists(): Promise<void> {
      if (!auth.user) throw new Error('Cannot fetch lists: not authenticated')
      const fetched = await apiFetchTasklists()
      apiLists.value = fetched
      initialized.value = true
    }

    async function apiCreateList(name: string): Promise<TaskList> {
      if (!auth.user) throw new Error('Cannot create list: not authenticated')
      validateName(name)
      await apiCreateTasklist(name.trim())
      await apiFetchLists()
      const trimmed = name.trim()
      const created = apiLists.value.find((l) => l.name === trimmed)
      if (!created) throw new Error('Created list not found after refetch')
      setActiveListForCurrentUser(created.id)
      return created
    }

    async function apiRenameList(id: string, newName: string): Promise<void> {
      const list = apiLists.value.find((l) => l.id === id)
      if (!list) throw new Error('List not found')
      validateName(newName, id)
      await apiUpdateTasklist(id, newName.trim())
      await apiFetchLists()
    }

    async function apiDeleteList(id: string): Promise<void> {
      const exists = apiLists.value.find((l) => l.id === id)
      if (!exists) throw new Error('List not found')
      await apiDeleteTasklist(id)
      await apiFetchLists()
      if (activeListId.value === id) {
        setActiveListForCurrentUser(lists.value[0]?.id ?? null)
      }
    }

    // --- Public API (branches on auth.mode) ---

    async function fetchLists(): Promise<void> {
      if (auth.mode === 'api') await apiFetchLists()
      // mock mode: lists already populated via persistence, no fetch needed
    }

    async function createList(name: string): Promise<TaskList> {
      if (!auth.user) throw new Error('Cannot create list: not authenticated')
      if (auth.mode === 'api') return apiCreateList(name)
      return mockCreateList(name)
    }

    async function renameList(id: string, newName: string): Promise<void> {
      if (auth.mode === 'api') {
        await apiRenameList(id, newName)
        return
      }
      mockRenameList(id, newName)
    }

    async function deleteList(id: string): Promise<void> {
      if (auth.mode === 'api') {
        await apiDeleteList(id)
        return
      }
      mockDeleteList(id)
    }

    function setActiveList(id: string): void {
      setActiveListForCurrentUser(id)
    }

    return {
      lists,
      activeListId,
      activeList,
      initialized,
      fetchLists,
      createList,
      renameList,
      deleteList,
      setActiveList,
    }
  },
  {
    persist: {
      pick: ['allLists', 'activeListIdByEmail'],
    },
  },
)
