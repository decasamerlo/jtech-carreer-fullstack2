<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{ open: boolean; error?: string }>()
const emit = defineEmits<{ close: []; create: [title: string, description?: string] }>()

const title = ref('')
const description = ref('')
const validationError = ref('')

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      title.value = ''
      description.value = ''
      validationError.value = ''
    }
  },
)

function validate(): boolean {
  validationError.value = ''
  const trimmed = title.value.trim()
  if (!trimmed) {
    validationError.value = 'Task title is required'
    return false
  }
  if (trimmed.length > 255) {
    validationError.value = 'Task title must be 255 characters or less'
    return false
  }
  if (description.value.length > 5000) {
    validationError.value = 'Description must be 5000 characters or less'
    return false
  }
  return true
}

function handleSubmit() {
  if (!validate()) return
  emit('create', title.value.trim(), description.value.trim() || undefined)
}
</script>

<template>
  <div v-if="open" class="dialog-overlay" @click.self="emit('close')">
    <div class="dialog">
      <h2>Add New Task</h2>
      <form @submit.prevent="handleSubmit">
        <div class="field">
          <label for="task-title">Title</label>
          <input
            id="task-title"
            v-model="title"
            type="text"
            placeholder="Enter task title"
            maxlength="256"
          />
          <p v-if="validationError" class="field-error">{{ validationError }}</p>
          <p v-if="error" class="field-error">{{ error }}</p>
        </div>
        <div class="field">
          <label for="task-description">Description (optional)</label>
          <textarea
            id="task-description"
            v-model="description"
            placeholder="Enter description"
            rows="3"
          ></textarea>
        </div>
        <div class="dialog-actions">
          <button type="button" class="cancel-btn" @click="emit('close')">Cancel</button>
          <button type="submit">Add</button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  width: 100%;
  max-width: 400px;
}

.dialog h2 {
  margin-bottom: 1rem;
}

.field {
  margin-bottom: 1rem;
}

.field label {
  display: block;
  margin-bottom: 0.25rem;
  font-weight: 600;
}

.field input,
.field textarea {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
  box-sizing: border-box;
}

.field textarea {
  resize: vertical;
}

.field-error {
  color: #e74c3c;
  font-size: 0.85rem;
  margin-top: 0.25rem;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

.dialog-actions button {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.dialog-actions button[type='submit'] {
  background-color: hsla(160, 100%, 37%, 1);
  color: white;
}

.cancel-btn {
  background-color: #eee;
}
</style>
