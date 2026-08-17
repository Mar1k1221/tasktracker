package com.example.tasktracker.dto;

import com.example.tasktracker.model.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpdateTaskPriorityRequest {
    @NotNull
    private final TaskPriority priority;

    public UpdateTaskPriorityRequest(TaskPriority priority) {
        this.priority = priority;
    }

    public TaskPriority getPriority() {
        return priority;
    }
}
