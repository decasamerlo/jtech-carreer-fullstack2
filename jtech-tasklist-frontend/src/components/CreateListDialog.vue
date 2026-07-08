<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{ open: boolean; error?: string }>()
const emit = defineEmits<{ close: []; create: [name: string] }>()

const name = ref('')
const validationError = ref('')

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      name.value = ''
      validationError.value = ''
    }
  },
)

function validate(): boolean {
  validationError.value = ''
  const trimmed = name.value.trim()
  if (!trimmed) {
    validationError.value = 'List name is required'
    return false
  }
  if (trimmed.length > 50) {
    validationError.value = 'List name must be 50 characters or less'
    return false
  }
  return true
}

// Emits only `create`; the parent closes on success and feeds async failures
// (duplicate name, API error) back in via the `error` prop.
function handleSubmit() {
  if (!validate()) return
  emit('create', name.value.trim())
}
</script>

<template>
  <v-dialog :model-value="open" max-width="400" @update:model-value="emit('close')">
    <v-card>
      <v-card-title>Create New List</v-card-title>
      <v-card-text>
        <v-form @submit.prevent="handleSubmit">
          <v-text-field
            v-model="name"
            label="List Name"
            :error-messages="validationError || error"
            @input="validationError = ''"
          />
          <v-card-actions class="pa-0 pt-2">
            <v-spacer />
            <v-btn variant="text" @click="emit('close')">Cancel</v-btn>
            <v-btn type="submit" color="primary">Create</v-btn>
          </v-card-actions>
        </v-form>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>
