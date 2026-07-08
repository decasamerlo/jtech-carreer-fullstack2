-- Requires a fresh/dev database; run docker compose down -v first if case-insensitive duplicate names already exist
CREATE UNIQUE INDEX uk_tasklist_user_name ON tasklist(user_id, LOWER(name)) WHERE deleted_at IS NULL;
