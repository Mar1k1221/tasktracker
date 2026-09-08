CREATE TABLE task_tags(
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE ,
    tag VARCHAR(20) NOT NULL,
    CONSTRAINT ug_task_tags UNIQUE(task_id,tag)
);