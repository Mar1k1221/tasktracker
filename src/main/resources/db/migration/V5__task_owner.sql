ALTER TABLE tasks
ADD COLUMN owner_id BIGINT;

UPDATE tasks
SET owner_id = (
    SELECT id
    FROM users
    WHERE username = 'marat_test_01'
    );

ALTER TABLE tasks
ALTER COLUMN owner_id SET NOT NULL;

ALTER TABLE tasks
ADD CONSTRAINT fk_tasks_owner
FOREIGN KEY (owner_id) REFERENCES users(id);

CREATE INDEX idx_tasks_owner_id ON tasks(owner_id);