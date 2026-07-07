CREATE UNIQUE INDEX uk_task_tasklist_title ON task(tasklist_id, title) WHERE deleted_at IS NULL;
