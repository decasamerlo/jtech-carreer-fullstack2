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
  <v-list-item
    :class="{ 'text-decoration-line-through text-grey': task.completed }"
  >
    <template v-slot:prepend>
      <v-checkbox-btn
        :model-value="task.completed"
        @change="emit('toggle', task.id)"
        hide-details
      />
    </template>
    <v-list-item-title>{{ task.title }}</v-list-item-title>
    <v-list-item-subtitle v-if="task.description" class="mt-1">
      {{ task.description }}
    </v-list-item-subtitle>
    <template v-slot:append>
      <v-btn icon="mdi-pencil" size="small" variant="text" @click="emit('edit', task)" />
      <v-btn icon="mdi-delete" size="small" variant="text" color="error" @click="emit('delete', task.id)" />
    </template>
  </v-list-item>
</template>
