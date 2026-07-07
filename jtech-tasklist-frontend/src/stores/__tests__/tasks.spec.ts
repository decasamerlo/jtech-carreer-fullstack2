import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useTasksStore } from '../tasks'
import { useAuthStore } from '@/stores/auth'
import * as taskApi from '@/services/taskApi'

vi.mock('@/services/taskApi')

describe('Tasks Store', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    setActivePinia(createPinia())
    const auth = useAuthStore()
    auth.login('test@example.com', 'password')
  })

  describe('mock mode', () => {
    it('starts with empty tasks', async () => {
      const store = useTasksStore()
      await store.fetchTasks('list-1')
      expect(store.tasksForActiveList).toEqual([])
    })

    it('adds a task to a list', async () => {
      const store = useTasksStore()
      await store.fetchTasks('list-1')
      const task = await store.addTask('list-1', 'Buy groceries')
      expect(task.title).toBe('Buy groceries')
      expect(task.completed).toBe(false)
      expect(task.tasklistId).toBe('list-1')
      expect(store.tasksForActiveList).toHaveLength(1)
    })

    it('rejects empty task title', async () => {
      const store = useTasksStore()
      await store.fetchTasks('list-1')
      await expect(store.addTask('list-1', '')).rejects.toThrow('Task title is required')
      await expect(store.addTask('list-1', '   ')).rejects.toThrow('Task title is required')
    })

    it('rejects task title over 255 characters', async () => {
      const store = useTasksStore()
      await store.fetchTasks('list-1')
      await expect(store.addTask('list-1', 'a'.repeat(256))).rejects.toThrow(
        'Task title must be 255 characters or less',
      )
    })

    it('rejects duplicate task titles within a list', async () => {
      const store = useTasksStore()
      await store.fetchTasks('list-1')
      await store.addTask('list-1', 'Task A')
      await expect(store.addTask('list-1', 'Task A')).rejects.toThrow(
        'A task with this title already exists in this list',
      )
    })

    it('rejects duplicate titles case-insensitively', async () => {
      const store = useTasksStore()
      await store.fetchTasks('list-1')
      await store.addTask('list-1', 'Buy Milk')
      await expect(store.addTask('list-1', 'buy milk')).rejects.toThrow(
        'A task with this title already exists in this list',
      )
    })

    it('allows same title in different lists', async () => {
      const store = useTasksStore()
      await store.fetchTasks('list-1')
      await store.addTask('list-1', 'Shared Title')
      await store.fetchTasks('list-2')
      await expect(store.addTask('list-2', 'Shared Title')).resolves.not.toThrow()
    })

    it('edits a task title', async () => {
      const store = useTasksStore()
      await store.fetchTasks('list-1')
      const task = await store.addTask('list-1', 'Old Title')
      await store.editTask(task.id, 'New Title')
      expect(store.tasksForActiveList[0].title).toBe('New Title')
    })

    it('allows keeping same title on edit', async () => {
      const store = useTasksStore()
      await store.fetchTasks('list-1')
      const task = await store.addTask('list-1', 'My Task')
      await expect(store.editTask(task.id, 'My Task')).resolves.not.toThrow()
    })

    it('rejects duplicate title on edit', async () => {
      const store = useTasksStore()
      await store.fetchTasks('list-1')
      await store.addTask('list-1', 'Task A')
      const taskB = await store.addTask('list-1', 'Task B')
      await expect(store.editTask(taskB.id, 'Task A')).rejects.toThrow(
        'A task with this title already exists in this list',
      )
    })

    it('removes a task', async () => {
      const store = useTasksStore()
      await store.fetchTasks('list-1')
      const task = await store.addTask('list-1', 'To Delete')
      await store.removeTask(task.id)
      expect(store.tasksForActiveList).toHaveLength(0)
    })

    it('toggles task completion', async () => {
      const store = useTasksStore()
      await store.fetchTasks('list-1')
      const task = await store.addTask('list-1', 'Toggle Me')
      expect(task.completed).toBe(false)
      await store.toggleComplete(task.id)
      expect(store.tasksForActiveList[0].completed).toBe(true)
      await store.toggleComplete(task.id)
      expect(store.tasksForActiveList[0].completed).toBe(false)
    })

    it('isolates tasks between lists', async () => {
      const store = useTasksStore()
      await store.fetchTasks('list-1')
      await store.addTask('list-1', 'List 1 Task')

      await store.fetchTasks('list-2')
      await store.addTask('list-2', 'List 2 Task')

      expect(store.tasksForActiveList).toHaveLength(1)
      expect(store.tasksForActiveList[0].title).toBe('List 2 Task')

      await store.fetchTasks('list-1')
      expect(store.tasksForActiveList).toHaveLength(1)
      expect(store.tasksForActiveList[0].title).toBe('List 1 Task')
    })

    it('throws when creating task without auth', async () => {
      const auth = useAuthStore()
      auth.logout()
      const store = useTasksStore()
      await expect(store.addTask('list-1', 'Task')).rejects.toThrow(
        'Cannot create task: not authenticated',
      )
    })

    it('throws when editing non-existent task', async () => {
      const store = useTasksStore()
      await store.fetchTasks('list-1')
      await expect(store.editTask('nonexistent', 'New')).rejects.toThrow('Task not found')
    })

    it('throws when removing non-existent task', async () => {
      const store = useTasksStore()
      await store.fetchTasks('list-1')
      await expect(store.removeTask('nonexistent')).rejects.toThrow('Task not found')
    })

    it('throws when toggling non-existent task', async () => {
      const store = useTasksStore()
      await store.fetchTasks('list-1')
      await expect(store.toggleComplete('nonexistent')).rejects.toThrow('Task not found')
    })
  })

  describe('api mode', () => {
    beforeEach(() => {
      const auth = useAuthStore()
      auth.mode = 'api'
    })

    it('fetches tasks from the API', async () => {
      vi.mocked(taskApi.fetchTasks).mockResolvedValue([
        { id: '1', title: 'Server Task', completed: false, tasklistId: 'tl1' },
      ])

      const store = useTasksStore()
      await store.fetchTasks('tl1')

      expect(taskApi.fetchTasks).toHaveBeenCalledWith('tl1')
      expect(store.tasksForActiveList).toHaveLength(1)
      expect(store.tasksForActiveList[0].title).toBe('Server Task')
      expect(store.initialized).toBe(true)
    })

    it('adds a task via API', async () => {
      vi.mocked(taskApi.createTask).mockResolvedValue({
        id: 'server-id',
        title: 'New',
        completed: false,
        tasklistId: 'tl1',
      })

      const store = useTasksStore()
      const task = await store.addTask('tl1', 'New')

      expect(taskApi.createTask).toHaveBeenCalledWith('tl1', {
        title: 'New',
        description: undefined,
      })
      expect(task.id).toBe('server-id')
      expect(store.tasksForActiveList).toHaveLength(1)
    })

    it('edits a task via API', async () => {
      vi.mocked(taskApi.fetchTasks).mockResolvedValue([
        { id: '1', title: 'Old', completed: false, tasklistId: 'tl1' },
      ])
      vi.mocked(taskApi.updateTask).mockResolvedValue({
        id: '1',
        title: 'Updated',
        completed: false,
        tasklistId: 'tl1',
      })

      const store = useTasksStore()
      await store.fetchTasks('tl1')
      await store.editTask('1', 'Updated')

      expect(taskApi.updateTask).toHaveBeenCalledWith('1', {
        title: 'Updated',
        description: undefined,
      })
      expect(store.tasksForActiveList[0].title).toBe('Updated')
    })

    it('removes a task via API', async () => {
      vi.mocked(taskApi.fetchTasks).mockResolvedValue([
        { id: '1', title: 'To Delete', completed: false, tasklistId: 'tl1' },
      ])
      vi.mocked(taskApi.deleteTask).mockResolvedValue(undefined)

      const store = useTasksStore()
      await store.fetchTasks('tl1')
      await store.removeTask('1')

      expect(taskApi.deleteTask).toHaveBeenCalledWith('1')
      expect(store.tasksForActiveList).toHaveLength(0)
    })

    it('toggles completion via API', async () => {
      vi.mocked(taskApi.fetchTasks).mockResolvedValue([
        { id: '1', title: 'Toggle', completed: false, tasklistId: 'tl1' },
      ])
      vi.mocked(taskApi.updateTask).mockResolvedValue({
        id: '1',
        title: 'Toggle',
        completed: true,
        tasklistId: 'tl1',
      })

      const store = useTasksStore()
      await store.fetchTasks('tl1')
      await store.toggleComplete('1')

      expect(taskApi.updateTask).toHaveBeenCalledWith('1', {
        title: 'Toggle',
        description: undefined,
        completed: true,
      })
      expect(store.tasksForActiveList[0].completed).toBe(true)
    })

    it('rejects duplicate titles in API mode', async () => {
      vi.mocked(taskApi.fetchTasks).mockResolvedValue([
        { id: '1', title: 'Existing', completed: false, tasklistId: 'tl1' },
      ])

      const store = useTasksStore()
      await store.fetchTasks('tl1')

      await expect(store.addTask('tl1', 'Existing')).rejects.toThrow(
        'A task with this title already exists in this list',
      )
    })

    it('rejects empty titles in API mode', async () => {
      const store = useTasksStore()
      await expect(store.addTask('tl1', '')).rejects.toThrow('Task title is required')
      await expect(store.addTask('tl1', '   ')).rejects.toThrow('Task title is required')
    })

    it('rejects titles over 255 chars in API mode', async () => {
      const store = useTasksStore()
      await expect(store.addTask('tl1', 'a'.repeat(256))).rejects.toThrow(
        'Task title must be 255 characters or less',
      )
    })
  })
})
