import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useListsStore } from '../lists'
import { useAuthStore } from '@/stores/auth'

describe('Lists Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    const auth = useAuthStore()
    auth.login('test@example.com', 'password')
  })

  it('starts with empty lists', () => {
    const store = useListsStore()
    expect(store.lists).toEqual([])
    expect(store.activeListId).toBeNull()
  })

  it('creates a new list', () => {
    const store = useListsStore()
    const list = store.createList('My Tasks')
    expect(list.name).toBe('My Tasks')
    expect(list.id).toBeDefined()
    expect(list.ownerEmail).toBe('test@example.com')
    expect(store.lists).toHaveLength(1)
    expect(store.activeListId).toBe(list.id)
  })

  it('rejects duplicate names', () => {
    const store = useListsStore()
    store.createList('My Tasks')
    expect(() => store.createList('My Tasks')).toThrow('A list with this name already exists')
  })

  it('rejects empty names', () => {
    const store = useListsStore()
    expect(() => store.createList('')).toThrow('List name is required')
    expect(() => store.createList('   ')).toThrow('List name is required')
  })

  it('rejects names over 50 characters', () => {
    const store = useListsStore()
    expect(() => store.createList('a'.repeat(51))).toThrow(
      'List name must be 50 characters or less',
    )
  })

  it('renames a list', () => {
    const store = useListsStore()
    const list = store.createList('Old Name')
    store.renameList(list.id, 'New Name')
    expect(store.lists[0].name).toBe('New Name')
  })

  it('allows keeping same name on rename', () => {
    const store = useListsStore()
    const list = store.createList('My Tasks')
    expect(() => store.renameList(list.id, 'My Tasks')).not.toThrow()
  })

  it('rejects duplicate name on rename', () => {
    const store = useListsStore()
    store.createList('List 1')
    const list2 = store.createList('List 2')
    expect(() => store.renameList(list2.id, 'List 1')).toThrow(
      'A list with this name already exists',
    )
  })

  it('deletes a list', () => {
    const store = useListsStore()
    const list = store.createList('To Delete')
    store.deleteList(list.id)
    expect(store.lists).toHaveLength(0)
    expect(store.activeListId).toBeNull()
  })

  it('sets active list', () => {
    const store = useListsStore()
    const list = store.createList('My List')
    store.setActiveList(list.id)
    expect(store.activeListId).toBe(list.id)
  })

  it('returns active list object', () => {
    const store = useListsStore()
    const list = store.createList('My List')
    store.setActiveList(list.id)
    expect(store.activeList?.name).toBe('My List')
  })

  it('throws when creating list without auth', () => {
    const auth = useAuthStore()
    auth.logout()
    const store = useListsStore()
    expect(() => store.createList('My List')).toThrow('Cannot create list: not authenticated')
  })

  it('isolates lists between users', () => {
    const store = useListsStore()
    const aList = store.createList('User A List')

    const auth = useAuthStore()
    auth.login('other@example.com', 'password')

    expect(store.lists).toEqual([])
    expect(store.activeList).toBeNull()

    store.createList('User B List')
    expect(store.lists).toHaveLength(1)
    expect(store.lists[0].name).toBe('User B List')

    auth.login('test@example.com', 'password')
    expect(store.lists).toHaveLength(1)
    expect(store.lists[0].name).toBe('User A List')
    expect(store.activeListId).toBe(aList.id)
    expect(store.activeList?.id).toBe(aList.id)
  })

  it('throws when renaming another user list', () => {
    const store = useListsStore()
    const list = store.createList('My List')

    const auth = useAuthStore()
    auth.login('other@example.com', 'password')

    expect(() => store.renameList(list.id, 'Hijacked')).toThrow('List not found')
  })

  it('throws when deleting another user list', () => {
    const store = useListsStore()
    const list = store.createList('My List')

    const auth = useAuthStore()
    auth.login('other@example.com', 'password')

    expect(() => store.deleteList(list.id)).toThrow('List not found')

    auth.login('test@example.com', 'password')
    expect(store.lists.find((l) => l.id === list.id)).toBeDefined()
  })
})
