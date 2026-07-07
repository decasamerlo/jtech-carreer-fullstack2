CREATE TABLE task (
    id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    tasklist_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    updated_by UUID,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by UUID,
    version INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_task_tasklist FOREIGN KEY (tasklist_id) REFERENCES tasklist(id),
    CONSTRAINT fk_task_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_task_user_id ON task(user_id);
CREATE INDEX idx_task_tasklist_id ON task(tasklist_id);
CREATE INDEX idx_task_tasklist_user ON task(tasklist_id, user_id);
