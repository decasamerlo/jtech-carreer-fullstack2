export interface Task {
  id: string
  title: string
  description?: string
  completed: boolean
  tasklistId: string
  createdAt?: string
  updatedAt?: string
}
