# Task 10: Frontend — Update ListsView to show tasks

## Status: DONE

## Summary

Updated `ListsView.vue` to integrate full task CRUD UI within the active list view.

## Changes Made

**Modified:** `jtech-tasklist-frontend/src/views/ListsView.vue`

- Added imports: `ref`, `watch` from Vue, `useTasksStore`, `TaskItem`, `CreateTaskDialog`, `EditTaskDialog`, `DeleteTaskDialog`, `Task` type
- Renamed store variable from `store` to `listsStore` for clarity
- Added tasks store integration: `useTasksStore()`
- Added dialog state refs: `showCreateDialog`, `showEditDialog`, `showDeleteDialog`
- Added task selection refs: `selectedTask`, `selectedTaskId`, `selectedTaskForDelete`
- Added error refs: `createError`, `editError`, `deleteError`
- Added watcher on `listsStore.activeListId` to fetch tasks when list changes
- Added CRUD handlers with try/catch error handling:
  - `openCreateDialog()` — clears error before opening
  - `handleCreateTask()` — creates task in active list
  - `openEditDialog()` — sets selected task and clears error
  - `handleEditTask()` — updates task, clears selection on success
  - `openDeleteDialog()` — sets selected task ID
  - `handleDeleteTask()` — removes task, clears selection on success
  - `handleToggleComplete()` — toggles task completion
- Added watcher on `selectedTaskId` to resolve task object for delete dialog
- Added template: list header with "+ Add Task" button, task list with `TaskItem` components, empty state
- Added dialog components: `CreateTaskDialog`, `EditTaskDialog`, `DeleteTaskDialog`
- Updated styles: added `.list-header`, `.add-task-btn`, `.task-list`, `.empty-tasks` styles

## Verification

- **Type-check:** PASS (1 pre-existing error in `TaskItem.spec.ts:66` — `.element.checked` type issue, unrelated)
- **Lint:** PASS (0 errors)
- **Tests:** ALL 127 PASS (17 test files)

## Concerns

None. All task requirements implemented exactly per plan.
