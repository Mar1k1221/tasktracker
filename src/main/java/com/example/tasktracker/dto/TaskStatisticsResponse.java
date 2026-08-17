package com.example.tasktracker.dto;


import com.example.tasktracker.model.TaskPriority;
import com.example.tasktracker.model.TaskStatus;


import java.util.Map;

public class TaskStatisticsResponse {
private final Map<TaskStatus, Integer> mapStatus;
private final Map<TaskPriority, Integer> mapPriority;

    public TaskStatisticsResponse(Map<TaskStatus, Integer> mapStatus, Map<TaskPriority, Integer> mapPriority) {
        this.mapStatus = mapStatus;
        this.mapPriority = mapPriority;
    }

    public Map<TaskStatus, Integer> getMapStatus() {
        return mapStatus;
    }

    public Map<TaskPriority, Integer> getMapPriority() {
        return mapPriority;
    }
}
