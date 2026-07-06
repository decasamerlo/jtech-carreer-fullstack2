import { describe, it, expect, vi, beforeEach } from 'vitest'
import { fetchTasklists, createTasklist, updateTasklist, deleteTasklist } from '../tasklistApi'
import api from '../api'

vi.mock('../api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('tasklistApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('fetchTasklists', () => {
    it('GETs /api/v1/tasklists and maps response', async () => {
      const mockGet = vi.mocked(api.get)
      mockGet.mockResolvedValueOnce({
        data: [
          { id: '1', name: 'Work' },
          { id: '2', name: 'Personal' },
        ],
      })

      const result = await fetchTasklists()

      expect(mockGet).toHaveBeenCalledWith('/api/v1/tasklists')
      expect(result).toEqual([
        { id: '1', name: 'Work' },
        { id: '2', name: 'Personal' },
      ])
    })

    it('returns empty array when no tasklists exist', async () => {
      const mockGet = vi.mocked(api.get)
      mockGet.mockResolvedValueOnce({ data: [] })

      const result = await fetchTasklists()

      expect(result).toEqual([])
    })
  })

  describe('createTasklist', () => {
    it('POSTs to /api/v1/tasklists with name and returns created object', async () => {
      const mockPost = vi.mocked(api.post)
      mockPost.mockResolvedValueOnce({
        data: { id: 'new-id', name: 'Groceries' },
      })

      const result = await createTasklist('Groceries')

      expect(mockPost).toHaveBeenCalledWith('/api/v1/tasklists', { name: 'Groceries' })
      expect(result).toEqual({ id: 'new-id', name: 'Groceries' })
    })
  })

  describe('updateTasklist', () => {
    it('PUTs to /api/v1/tasklists/{id} with new name', async () => {
      const mockPut = vi.mocked(api.put)
      mockPut.mockResolvedValueOnce({
        data: { id: 'abc', name: 'Updated Name' },
      })

      const result = await updateTasklist('abc', 'Updated Name')

      expect(mockPut).toHaveBeenCalledWith('/api/v1/tasklists/abc', { name: 'Updated Name' })
      expect(result).toEqual({ id: 'abc', name: 'Updated Name' })
    })
  })

  describe('deleteTasklist', () => {
    it('DELETEs /api/v1/tasklists/{id}', async () => {
      const mockDelete = vi.mocked(api.delete)
      mockDelete.mockResolvedValueOnce({ status: 204 })

      await deleteTasklist('xyz')

      expect(mockDelete).toHaveBeenCalledWith('/api/v1/tasklists/xyz')
    })
  })
})
