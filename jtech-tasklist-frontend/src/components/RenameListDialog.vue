<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{ open: boolean; currentName: string }>()
const emit = defineEmits<{ close: []; rename: [name: string] }>()

const name = ref('')
const error = ref('')

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      name.value = props.currentName
      error.value = ''
    }
  },
  { immediate: true },
)

function validate(): boolean {
  error.value = ''
  const trimmed = name.value.trim()
  if (!trimmed) {
    error.value = 'List name is required'
    return false
  }
  if (trimmed.length > 50) {
    error.value = 'List name must be 50 characters or less'
    return false
  }
  return true
}

function handleSubmit() {
  if (!validate()) return
  emit('rename', name.value.trim())
  emit('close')
}
</script>

<template>
  <div v-if="open" class="dialog-overlay" @click.self="emit('close')">
    <div class="dialog">
      <h2>Rename List</h2>
      <form @submit.prevent="handleSubmit">
        <div class="field">
          <label for="list-name">List Name</label>
          <!-- maxlength=51: allows typing past 50 so validation error is visible, not silently capped -->
          <input
            id="list-name"
            v-model="name"
            type="text"
            placeholder="Enter list name"
            maxlength="51"
          />
          <p v-if="error" class="field-error">{{ error }}</p>
        </div>
        <div class="dialog-actions">
          <button type="button" class="cancel-btn" @click="emit('close')">Cancel</button>
          <button type="submit">Rename</button>
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

.field input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
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
