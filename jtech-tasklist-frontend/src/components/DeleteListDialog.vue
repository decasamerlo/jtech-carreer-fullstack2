<script setup lang="ts">
defineProps<{ open: boolean; listName: string }>()
const emit = defineEmits<{ close: []; delete: [] }>()
</script>

<template>
  <v-dialog :model-value="open" max-width="400" @update:model-value="emit('close')">
    <v-card>
      <v-card-title>Delete List</v-card-title>
      <v-card-text>
        <p class="text-body-1">
          Are you sure you want to delete "<strong>{{ listName }}</strong>"? This action cannot be undone.
        </p>
        <v-card-actions class="pa-0 pt-2">
          <v-spacer />
          <v-btn variant="text" @click="emit('close')">Cancel</v-btn>
          <v-btn color="error" @click="emit('delete')">Delete</v-btn>
        </v-card-actions>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>
