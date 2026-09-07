
CREATE TABLE tags_tasks(
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE ,
    tag VARCHAR(30) NOT NULL,
    CONSTRAINT uq_task_tags UNIQUE(task_id,tag)
);



















