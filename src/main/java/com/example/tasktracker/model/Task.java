package com.example.tasktracker.model;

import java.util.HashSet;
import java.util.Set;

public class Task {
    private final int id;
    private final String title;
    private final String description;
    private  TaskPriority priority;
    private  TaskStatus status;
    private final  Set<String> tags;

    public Task(int id, String title, String description, TaskPriority priority, TaskStatus status, Set<String> tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.tags = new HashSet<>(tags);
    }

    public Set<String> getTags() {
        return tags;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
    public void setPriority(TaskPriority priority){
        this.priority=priority;
    }


}
