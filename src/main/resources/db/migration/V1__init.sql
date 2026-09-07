CREATE TABLE tasks(
                      id BIGSERIAL PRIMARY KEY,
                      title VARCHAR(100) NOT NULL,
                      description VARCHAR(200) NOT NULL,
                      priority VARCHAR(20) NOT NULL,
                      status VARCHAR(20) NOT NULL,
                      created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_tasks_status ON tasks (status);
