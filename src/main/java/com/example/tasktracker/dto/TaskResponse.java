package com.example.tasktracker.dto;

import com.example.tasktracker.model.Task;
import com.example.tasktracker.model.TaskPriority;
import com.example.tasktracker.model.TaskStatus;

import java.util.Set;

public class TaskResponse {
    private final int id;
    private final String title;
    private final String description;
    private final TaskPriority priority;
    private final TaskStatus status;
    private final Set<String> tags;

    public TaskResponse(int id, String title, String description, TaskPriority priority, TaskStatus status, Set<String> tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.tags = tags;
    }

    public TaskResponse(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.priority = task.getPriority();
        this.status = task.getStatus();
        this.tags = task.getTags();
    }

    public int getId() {
        return id;
    }

    public Set<String> getTags() {
        return tags;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }
}
