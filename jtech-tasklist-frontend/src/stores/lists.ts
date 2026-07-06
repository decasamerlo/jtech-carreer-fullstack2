import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type { TaskList } from '@/types/list'
import { useAuthStore } from '@/stores/auth'

export const useListsStore = defineStore(
  'lists',
  () => {
    const allLists = ref<TaskList[]>([])
    const activeListIdByEmail = ref<Record<string, string | null>>({})
    const auth = useAuthStore()

    const lists = computed(() =>
      allLists.value.filter((l) => l.ownerEmail === auth.user?.email),
    )

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
      // Case-insensitive: prevents "Work" and "work" from coexisting confusingly
      const duplicate = lists.value.find(
        (l) => l.name.toLowerCase() === trimmed.toLowerCase() && l.id !== excludeId,
      )
      if (duplicate) throw new Error('A list with this name already exists')
    }

    function createList(name: string): TaskList {
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

    function renameList(id: string, newName: string): void {
      const list = allLists.value.find(
        (l) => l.id === id && l.ownerEmail === auth.user?.email,
      )
      if (!list) throw new Error('List not found')
      validateName(newName, id)
      list.name = newName.trim()
      list.updatedAt = new Date().toISOString()
    }

    function deleteList(id: string): void {
      // TODO: Add dependency check when tasks exist (frontend-tasks-crud)
      const index = allLists.value.findIndex(
        (l) => l.id === id && l.ownerEmail === auth.user?.email,
      )
      if (index === -1) throw new Error('List not found')
      allLists.value.splice(index, 1)
      if (activeListId.value === id) {
        setActiveListForCurrentUser(lists.value[0]?.id ?? null)
      }
    }

    function setActiveList(id: string): void {
      setActiveListForCurrentUser(id)
    }

    return {
      lists,
      activeListId,
      activeList,
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
