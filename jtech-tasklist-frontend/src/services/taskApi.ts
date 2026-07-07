import api from './api'
import type { Task } from '@/types/task'

interface TaskResponse {
  id: string
  title: string
  description?: string
  completed: boolean
  tasklistId: string
}

export async function fetchTasks(tasklistId: string): Promise<Task[]> {
  const { data } = await api.get<TaskResponse[]>('/api/v1/tasks', {
    params: { tasklistId },
  })
  return data.map((item) => ({
    id: item.id,
    title: item.title,
    description: item.description,
    completed: item.completed,
    tasklistId: item.tasklistId,
  }))
}

export async function createTask(
  tasklistId: string,
  payload: { title: string; description?: string; completed?: boolean },
): Promise<Task> {
  const { data } = await api.post<TaskResponse>('/api/v1/tasks', payload, {
    params: { tasklistId },
  })
  return {
    id: data.id,
    title: data.title,
    description: data.description,
    completed: data.completed,
    tasklistId: data.tasklistId,
  }
}

export async function updateTask(
  id: string,
  payload: { title: string; description?: string; completed?: boolean },
): Promise<Task> {
  const { data } = await api.put<TaskResponse>(`/api/v1/tasks/${id}`, payload)
  return {
    id: data.id,
    title: data.title,
    description: data.description,
    completed: data.completed,
    tasklistId: data.tasklistId,
  }
}

export async function deleteTask(id: string): Promise<void> {
  await api.delete(`/api/v1/tasks/${id}`)
}
