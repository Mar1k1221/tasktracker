CREATE TABLE users(
    id BIGSERIAL PRIMARY KEY ,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ug_users_username UNIQUE (username)
);
