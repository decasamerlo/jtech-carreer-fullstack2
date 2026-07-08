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
  <v-navigation-drawer permanent>
    <template v-slot:prepend>
      <v-list-item title="Lists" class="text-h6">
        <template v-slot:append>
          <v-btn icon="mdi-plus" size="small" color="primary" data-testid="btn-create-list" @click="showCreateDialog = true" />
        </template>
      </v-list-item>
      <v-divider />
    </template>

    <v-list v-if="store.lists.length > 0">
      <v-list-item
        v-for="list in store.lists"
        :key="list.id"
        :active="list.id === store.activeListId"
        data-testid="list-item"
        @click="store.setActiveList(list.id)"
      >
        <template v-slot:title>
          <span class="text-truncate">{{ list.name }}</span>
        </template>
        <template v-slot:append>
          <v-btn icon="mdi-pencil" size="x-small" variant="text" data-testid="btn-rename" @click.stop="openRenameDialog(list.id)" />
          <v-btn icon="mdi-delete" size="x-small" variant="text" color="error" data-testid="btn-delete" @click.stop="openDeleteDialog(list.id)" />
        </template>
      </v-list-item>
    </v-list>

    <v-list-item v-else class="text-center text-grey text-caption font-italic">
      No lists yet. Create one to get started!
    </v-list-item>

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
  </v-navigation-drawer>
</template>
