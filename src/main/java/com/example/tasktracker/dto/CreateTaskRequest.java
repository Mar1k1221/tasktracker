package com.example.tasktracker.dto;

import com.example.tasktracker.model.TaskPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class CreateTaskRequest {
    @NotBlank
    @Size(max = 100)
    private final String title;
    @NotBlank
    private final String description;
    @NotNull
    private final TaskPriority priority;

    public CreateTaskRequest(String title, String description, TaskPriority priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskPriority getPriority() {
        return priority;
    }
}
