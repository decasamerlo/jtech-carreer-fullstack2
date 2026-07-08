-- Replaces the case-sensitive unique index from V007 with a case-insensitive one.
-- Requires a fresh/dev database; run docker compose down -v first if case-insensitive
-- duplicate titles already exist in the same tasklist.
DROP INDEX uk_task_tasklist_title;
CREATE UNIQUE INDEX uk_task_tasklist_title ON task(tasklist_id, LOWER(title)) WHERE deleted_at IS NULL;
