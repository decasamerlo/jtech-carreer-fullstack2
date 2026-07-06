import api from './api'
import type { TaskList } from '@/types/list'

interface TasklistResponse {
  id: string
  name: string
}

export async function fetchTasklists(): Promise<TaskList[]> {
  const { data } = await api.get<TasklistResponse[]>('/api/v1/tasklists')
  return data.map((item) => ({ id: item.id, name: item.name }))
}

export async function createTasklist(name: string): Promise<TaskList> {
  const { data } = await api.post<TasklistResponse>('/api/v1/tasklists', { name })
  return { id: data.id, name: data.name }
}

export async function updateTasklist(id: string, name: string): Promise<TaskList> {
  const { data } = await api.put<TasklistResponse>(`/api/v1/tasklists/${id}`, { name })
  return { id: data.id, name: data.name }
}

export async function deleteTasklist(id: string): Promise<void> {
  await api.delete(`/api/v1/tasklists/${id}`)
}
