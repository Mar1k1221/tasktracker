package com.example.tasktracker.controller;

import com.example.tasktracker.dto.TaskResponse;
import com.example.tasktracker.model.Task;
import com.example.tasktracker.service.TaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/tasks")
public class AdminTaskController {
    private final TaskService taskService;

    public AdminTaskController(TaskService taskService) {
        this.taskService = taskService;
    }
    @GetMapping
    public List<TaskResponse> getAllTasks(){
        List<Task> tasks = taskService.getAllTasksForAdmin();
        List<TaskResponse> responses = new ArrayList<>();

        for (Task t:tasks){
            TaskResponse taskResponse = new TaskResponse(t.getId(),t.getTitle(),t.getDescription(),t.getPriority(),t.getStatus(),t.getTags());
            responses.add(taskResponse);
        }
        return responses;

    }

}
