<script setup lang="ts">
defineProps<{ open: boolean; listName: string }>()
const emit = defineEmits<{ close: []; delete: [] }>()
</script>

<template>
  <div v-if="open" class="dialog-overlay" @click.self="emit('close')">
    <div class="dialog">
      <h2>Delete List</h2>
      <p>
        Are you sure you want to delete "<strong>{{ listName }}</strong>"? This action cannot be undone.
      </p>
      <div class="dialog-actions">
        <button type="button" class="cancel-btn" @click="emit('close')">Cancel</button>
        <button type="button" class="confirm-btn" @click="emit('delete')">Delete</button>
      </div>
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

.dialog p {
  margin-bottom: 1.5rem;
  line-height: 1.5;
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

.cancel-btn {
  background-color: #eee;
}

.confirm-btn {
  background-color: #e74c3c;
  color: white;
}
</style>
