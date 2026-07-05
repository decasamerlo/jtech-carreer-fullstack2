CREATE TABLE users (
    id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    updated_by UUID,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_by UUID,
    version INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);
