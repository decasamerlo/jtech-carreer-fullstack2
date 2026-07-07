import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as taskApi from '../taskApi'
import api from '../api'

vi.mock('../api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('taskApi', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  describe('fetchTasks', () => {
    it('calls GET /api/v1/tasks with tasklistId param', async () => {
      vi.mocked(api.get).mockResolvedValue({
        data: [
          { id: '1', title: 'Task 1', completed: false, tasklistId: 'tl1' },
          { id: '2', title: 'Task 2', completed: true, tasklistId: 'tl1', description: 'desc' },
        ],
      })

      const tasks = await taskApi.fetchTasks('tl1')

      expect(api.get).toHaveBeenCalledWith('/api/v1/tasks', { params: { tasklistId: 'tl1' } })
      expect(tasks).toHaveLength(2)
      expect(tasks[0]).toEqual({
        id: '1',
        title: 'Task 1',
        completed: false,
        tasklistId: 'tl1',
        description: undefined,
      })
      expect(tasks[1].description).toBe('desc')
    })
  })

  describe('createTask', () => {
    it('calls POST /api/v1/tasks with tasklistId param and body', async () => {
      vi.mocked(api.post).mockResolvedValue({
        data: { id: '3', title: 'New', completed: false, tasklistId: 'tl1' },
      })

      const task = await taskApi.createTask('tl1', { title: 'New' })

      expect(api.post).toHaveBeenCalledWith(
        '/api/v1/tasks',
        { title: 'New' },
        { params: { tasklistId: 'tl1' } },
      )
      expect(task.title).toBe('New')
      expect(task.id).toBe('3')
    })
  })

  describe('updateTask', () => {
    it('calls PUT /api/v1/tasks/:id', async () => {
      vi.mocked(api.put).mockResolvedValue({
        data: { id: '1', title: 'Updated', completed: true, tasklistId: 'tl1' },
      })

      const task = await taskApi.updateTask('1', { title: 'Updated', completed: true })

      expect(api.put).toHaveBeenCalledWith('/api/v1/tasks/1', {
        title: 'Updated',
        completed: true,
      })
      expect(task.title).toBe('Updated')
      expect(task.completed).toBe(true)
    })
  })

  describe('deleteTask', () => {
    it('calls DELETE /api/v1/tasks/:id', async () => {
      vi.mocked(api.delete).mockResolvedValue({})

      await taskApi.deleteTask('1')

      expect(api.delete).toHaveBeenCalledWith('/api/v1/tasks/1')
    })
  })
})
