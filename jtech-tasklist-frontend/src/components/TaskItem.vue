<script setup lang="ts">
import type { Task } from '@/types/task'

defineProps<{ task: Task }>()
const emit = defineEmits<{
  toggle: [id: string]
  edit: [task: Task]
  delete: [id: string]
}>()
</script>

<template>
  <li class="task-item" :class="{ completed: task.completed }">
    <label class="task-checkbox">
      <input type="checkbox" :checked="task.completed" @change="emit('toggle', task.id)" />
      <span class="task-title">{{ task.title }}</span>
    </label>
    <p v-if="task.description" class="task-description">{{ task.description }}</p>
    <div class="task-actions">
      <button class="action-btn" @click="emit('edit', task)">Edit</button>
      <button class="action-btn delete" @click="emit('delete', task.id)">Delete</button>
    </div>
  </li>
</template>

<style scoped>
.task-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  margin-bottom: 0.5rem;
  transition: background-color 0.15s;
}

.task-item:hover {
  background-color: #f9f9f9;
}

.task-item.completed .task-title {
  text-decoration: line-through;
  color: #888;
}

.task-checkbox {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  flex: 1;
  cursor: pointer;
}

.task-checkbox input {
  margin-top: 0.2rem;
  cursor: pointer;
}

.task-title {
  flex: 1;
  word-break: break-word;
}

.task-description {
  width: 100%;
  margin: 0.25rem 0 0;
  color: #666;
  font-size: 0.85rem;
}

.task-actions {
  display: none;
  gap: 0.25rem;
  flex-shrink: 0;
}

.task-item:hover .task-actions {
  display: flex;
}

.action-btn {
  padding: 0.25rem 0.5rem;
  border: none;
  border-radius: 4px;
  background: #eee;
  cursor: pointer;
  font-size: 0.75rem;
}

.action-btn.delete {
  background: #e74c3c;
  color: white;
}
</style>
