<script setup lang="ts">
import { ref, computed } from 'vue'
import { useListsStore } from '@/stores/lists'
import CreateListDialog from './CreateListDialog.vue'
import RenameListDialog from './RenameListDialog.vue'
import DeleteListDialog from './DeleteListDialog.vue'

const store = useListsStore()

const showCreateDialog = ref(false)
const showRenameDialog = ref(false)
const showDeleteDialog = ref(false)
const selectedListId = ref<string | null>(null)

const selectedList = computed(() => store.lists.find((l) => l.id === selectedListId.value))

function handleCreate(name: string) {
  store.createList(name)
}

function openRenameDialog(id: string) {
  selectedListId.value = id
  showRenameDialog.value = true
}

function handleRename(name: string) {
  if (selectedListId.value) {
    store.renameList(selectedListId.value, name)
  }
}

function openDeleteDialog(id: string) {
  selectedListId.value = id
  showDeleteDialog.value = true
}

function handleDelete() {
  if (selectedListId.value) {
    store.deleteList(selectedListId.value)
    showDeleteDialog.value = false
  }
}
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <h2>Lists</h2>
      <button class="add-btn" @click="showCreateDialog = true">+</button>
    </div>

    <ul class="list-items">
      <li
        v-for="list in store.lists"
        :key="list.id"
        :class="{ active: list.id === store.activeListId }"
        @click="store.setActiveList(list.id)"
      >
        <span class="list-name">{{ list.name }}</span>
        <div class="list-actions">
          <button class="action-btn" @click.stop="openRenameDialog(list.id)">Rename</button>
          <button class="action-btn delete" @click.stop="openDeleteDialog(list.id)">Delete</button>
        </div>
      </li>
    </ul>

    <p v-if="store.lists.length === 0" class="empty-state">
      No lists yet. Create one to get started!
    </p>

    <CreateListDialog
      :open="showCreateDialog"
      @close="showCreateDialog = false"
      @create="handleCreate"
    />
    <RenameListDialog
      :open="showRenameDialog"
      :current-name="selectedList?.name ?? ''"
      @close="showRenameDialog = false"
      @rename="handleRename"
    />
    <DeleteListDialog
      :open="showDeleteDialog"
      :list-name="selectedList?.name ?? ''"
      @close="showDeleteDialog = false"
      @delete="handleDelete"
    />
  </aside>
</template>

<style scoped>
.sidebar {
  width: 250px;
  border-right: 1px solid var(--color-border);
  padding: 1rem;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 1.25rem;
}

.add-btn {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 4px;
  background-color: hsla(160, 100%, 37%, 1);
  color: white;
  font-size: 1.25rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.list-items {
  list-style: none;
  padding: 0;
  margin: 0;
  flex: 1;
  overflow-y: auto;
}

.list-items li {
  padding: 0.5rem;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.25rem;
}

.list-items li:hover {
  background-color: #f5f5f5;
}

.list-items li.active {
  background-color: hsla(160, 100%, 37%, 0.1);
}

.list-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.list-actions {
  display: none;
  gap: 0.25rem;
}

.list-items li:hover .list-actions {
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

.empty-state {
  color: #888;
  text-align: center;
  font-style: italic;
}
</style>
