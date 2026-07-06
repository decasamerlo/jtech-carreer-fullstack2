import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useListsStore } from '../lists'
import { useAuthStore } from '@/stores/auth'
import * as tasklistApi from '@/services/tasklistApi'

vi.mock('@/services/tasklistApi')

describe('Lists Store', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    setActivePinia(createPinia())
    const auth = useAuthStore()
    auth.login('test@example.com', 'password')
  })

  describe('mock mode', () => {
    it('starts with empty lists', () => {
      const store = useListsStore()
      expect(store.lists).toEqual([])
      expect(store.activeListId).toBeNull()
    })

    it('creates a new list', async () => {
      const store = useListsStore()
      const list = await store.createList('My Tasks')
      expect(list.name).toBe('My Tasks')
      expect(list.id).toBeDefined()
      expect(list.ownerEmail).toBe('test@example.com')
      expect(store.lists).toHaveLength(1)
      expect(store.activeListId).toBe(list.id)
    })

    it('rejects duplicate names', async () => {
      const store = useListsStore()
      await store.createList('My Tasks')
      await expect(store.createList('My Tasks')).rejects.toThrow(
        'A list with this name already exists',
      )
    })

    it('rejects empty names', async () => {
      const store = useListsStore()
      await expect(store.createList('')).rejects.toThrow('List name is required')
      await expect(store.createList('   ')).rejects.toThrow('List name is required')
    })

    it('rejects names over 50 characters', async () => {
      const store = useListsStore()
      await expect(store.createList('a'.repeat(51))).rejects.toThrow(
        'List name must be 50 characters or less',
      )
    })

    it('renames a list', async () => {
      const store = useListsStore()
      const list = await store.createList('Old Name')
      await store.renameList(list.id, 'New Name')
      expect(store.lists[0].name).toBe('New Name')
    })

    it('allows keeping same name on rename', async () => {
      const store = useListsStore()
      const list = await store.createList('My Tasks')
      await expect(store.renameList(list.id, 'My Tasks')).resolves.not.toThrow()
    })

    it('rejects duplicate name on rename', async () => {
      const store = useListsStore()
      await store.createList('List 1')
      const list2 = await store.createList('List 2')
      await expect(store.renameList(list2.id, 'List 1')).rejects.toThrow(
        'A list with this name already exists',
      )
    })

    it('deletes a list', async () => {
      const store = useListsStore()
      const list = await store.createList('To Delete')
      await store.deleteList(list.id)
      expect(store.lists).toHaveLength(0)
      expect(store.activeListId).toBeNull()
    })

    it('sets active list', async () => {
      const store = useListsStore()
      const list = await store.createList('My List')
      store.setActiveList(list.id)
      expect(store.activeListId).toBe(list.id)
    })

    it('returns active list object', async () => {
      const store = useListsStore()
      const list = await store.createList('My List')
      store.setActiveList(list.id)
      expect(store.activeList?.name).toBe('My List')
    })

    it('throws when creating list without auth', async () => {
      const auth = useAuthStore()
      auth.logout()
      const store = useListsStore()
      await expect(store.createList('My List')).rejects.toThrow(
        'Cannot create list: not authenticated',
      )
    })

    it('isolates lists between users', async () => {
      const store = useListsStore()
      const aList = await store.createList('User A List')

      const auth = useAuthStore()
      auth.login('other@example.com', 'password')

      expect(store.lists).toEqual([])
      expect(store.activeList).toBeNull()

      await store.createList('User B List')
      expect(store.lists).toHaveLength(1)
      expect(store.lists[0].name).toBe('User B List')

      auth.login('test@example.com', 'password')
      expect(store.lists).toHaveLength(1)
      expect(store.lists[0].name).toBe('User A List')
      expect(store.activeListId).toBe(aList.id)
      expect(store.activeList?.id).toBe(aList.id)
    })

    it('throws when renaming another user list', async () => {
      const store = useListsStore()
      const list = await store.createList('My List')

      const auth = useAuthStore()
      auth.login('other@example.com', 'password')

      await expect(store.renameList(list.id, 'Hijacked')).rejects.toThrow('List not found')
    })

    it('throws when deleting another user list', async () => {
      const store = useListsStore()
      const list = await store.createList('My List')

      const auth = useAuthStore()
      auth.login('other@example.com', 'password')

      await expect(store.deleteList(list.id)).rejects.toThrow('List not found')

      auth.login('test@example.com', 'password')
      expect(store.lists.find((l) => l.id === list.id)).toBeDefined()
    })
  })

  describe('api mode', () => {
    beforeEach(() => {
      const auth = useAuthStore()
      auth.mode = 'api'
    })

    it('fetches lists from the API', async () => {
      const mockLists = [
        { id: '1', name: 'Server List 1' },
        { id: '2', name: 'Server List 2' },
      ]
      vi.mocked(tasklistApi.fetchTasklists).mockResolvedValue(mockLists)

      const store = useListsStore()
      await store.fetchLists()

      expect(tasklistApi.fetchTasklists).toHaveBeenCalledOnce()
      expect(store.lists).toEqual(mockLists)
      expect(store.initialized).toBe(true)
    })

    it('creates a list via API then refetches', async () => {
      vi.mocked(tasklistApi.createTasklist).mockResolvedValue({ id: '', name: 'New' })
      vi.mocked(tasklistApi.fetchTasklists).mockResolvedValue([
        { id: 'server-id', name: 'New' },
      ])

      const store = useListsStore()
      const list = await store.createList('New')

      expect(tasklistApi.createTasklist).toHaveBeenCalledWith('New')
      expect(tasklistApi.fetchTasklists).toHaveBeenCalled()
      expect(store.lists).toHaveLength(1)
      expect(list.id).toBe('server-id')
      expect(store.activeListId).toBe('server-id')
    })

    it('renames a list via API then refetches', async () => {
      vi.mocked(tasklistApi.fetchTasklists).mockResolvedValue([
        { id: '1', name: 'Old' },
      ])
      vi.mocked(tasklistApi.updateTasklist).mockResolvedValue({ id: '1', name: 'Updated' })

      const store = useListsStore()
      await store.fetchLists()

      vi.mocked(tasklistApi.fetchTasklists).mockResolvedValue([
        { id: '1', name: 'Updated' },
      ])
      await store.renameList('1', 'Updated')

      expect(tasklistApi.updateTasklist).toHaveBeenCalledWith('1', 'Updated')
      expect(store.lists[0].name).toBe('Updated')
    })

    it('deletes a list via API then refetches', async () => {
      vi.mocked(tasklistApi.fetchTasklists).mockResolvedValue([
        { id: '1', name: 'ToDelete' },
      ])
      vi.mocked(tasklistApi.deleteTasklist).mockResolvedValue(undefined)

      const store = useListsStore()
      await store.fetchLists()

      vi.mocked(tasklistApi.fetchTasklists).mockResolvedValue([])
      await store.deleteList('1')

      expect(tasklistApi.deleteTasklist).toHaveBeenCalledWith('1')
      expect(store.lists).toHaveLength(0)
    })

    it('throws when renaming non-existent list', async () => {
      vi.mocked(tasklistApi.fetchTasklists).mockResolvedValue([])

      const store = useListsStore()
      await store.fetchLists()

      await expect(store.renameList('nonexistent', 'New')).rejects.toThrow('List not found')
    })

    it('throws when deleting non-existent list', async () => {
      vi.mocked(tasklistApi.fetchTasklists).mockResolvedValue([])

      const store = useListsStore()
      await store.fetchLists()

      await expect(store.deleteList('nonexistent')).rejects.toThrow('List not found')
    })

    it('sets active list after API create', async () => {
      vi.mocked(tasklistApi.createTasklist).mockResolvedValue({ id: '', name: 'A' })
      vi.mocked(tasklistApi.fetchTasklists).mockResolvedValue([
        { id: 's1', name: 'A' },
        { id: 's2', name: 'B' },
      ])

      const store = useListsStore()
      await store.createList('A')
      expect(store.activeListId).toBe('s1')
      expect(store.activeList?.name).toBe('A')

      store.setActiveList('s2')
      expect(store.activeListId).toBe('s2')
      expect(store.activeList?.name).toBe('B')
    })

    it('rejects duplicate names via API', async () => {
      vi.mocked(tasklistApi.fetchTasklists).mockResolvedValue([
        { id: '1', name: 'Existing' },
      ])

      const store = useListsStore()
      await store.fetchLists()

      await expect(store.createList('Existing')).rejects.toThrow(
        'A list with this name already exists',
      )
    })

    it('rejects empty names in API mode', async () => {
      const store = useListsStore()
      await expect(store.createList('')).rejects.toThrow('List name is required')
      await expect(store.createList('   ')).rejects.toThrow('List name is required')
    })

    it('rejects names over 50 characters in API mode', async () => {
      const store = useListsStore()
      await expect(store.createList('a'.repeat(51))).rejects.toThrow(
        'List name must be 50 characters or less',
      )
    })
  })
})
