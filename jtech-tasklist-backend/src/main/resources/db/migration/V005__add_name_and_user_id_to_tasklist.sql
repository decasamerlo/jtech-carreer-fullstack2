ALTER TABLE tasklist
    ADD COLUMN name VARCHAR(100) NOT NULL DEFAULT 'Unnamed List',
    ADD COLUMN user_id UUID NOT NULL,
    ADD CONSTRAINT fk_tasklist_user FOREIGN KEY (user_id) REFERENCES users(id);

CREATE INDEX idx_tasklist_user_id ON tasklist(user_id);
