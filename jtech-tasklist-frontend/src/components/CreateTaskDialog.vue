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
  <v-dialog :model-value="open" max-width="500" @update:model-value="emit('close')">
    <v-card>
      <v-card-title>Add New Task</v-card-title>
      <v-card-text>
        <v-form @submit.prevent="handleSubmit">
          <v-text-field
            v-model="title"
            label="Title"
            :error-messages="validationError || error"
            @input="validationError = ''"
            counter
            maxlength="256"
          />
          <v-textarea
            v-model="description"
            label="Description (optional)"
            rows="3"
          />
          <v-card-actions class="pa-0 pt-2">
            <v-spacer />
            <v-btn variant="text" @click="emit('close')">Cancel</v-btn>
            <v-btn type="submit" color="primary">Add</v-btn>
          </v-card-actions>
        </v-form>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>
