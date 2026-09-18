package com.example.tasktracker.controller;

import com.example.tasktracker.dto.*;
import com.example.tasktracker.model.Task;
import com.example.tasktracker.model.TaskPriority;
import com.example.tasktracker.model.TaskStatus;
import com.example.tasktracker.service.TaskService;
import jakarta.validation.Valid;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;
    public TaskController(TaskService taskService){
        this.taskService=taskService;
    }




    @GetMapping("/statistics")
    public TaskStatisticsResponse statisticsResponse(){

        return taskService.getStatistics();
    }
    @GetMapping("/{id}")
    public TaskResponse findById(@PathVariable Long id){
        Task task = taskService.findById(id);
         return new TaskResponse(task.getId(),task.getTitle(),task.getDescription(),task.getPriority(),task.getStatus(),task.getTags());
    }
    @GetMapping
    public List<TaskResponse> findByAll(@RequestParam ( required = false)TaskStatus status,@RequestParam(required = false)
    TaskPriority priority) {
        List<Task> list = taskService.findByAll(status,priority);
        List<TaskResponse> list2 = new ArrayList<>();
        for (Task t:list){
            TaskResponse taskResponse = new TaskResponse(t.getId(),t.getTitle(),t.getDescription(),t.getPriority(),t.getStatus(),t.getTags());
            list2.add(taskResponse);
        }
        return list2;
    }
    @PostMapping
    public ResponseEntity<TaskResponse> save(@RequestBody @Valid CreateTaskRequest createTaskRequest){
        Task task = taskService.save(createTaskRequest);
        TaskResponse taskResponse = new TaskResponse(task.getId(),task.getTitle(),task.getDescription(),task.getPriority(),task.getStatus(),task.getTags());
      java.net.URI location = java.net.URI.create("/api/tasks/" + task.getId());
        return ResponseEntity.created(location).body(taskResponse);
    }


    @PatchMapping("/{id}/status")
    public TaskResponse newStatus(@PathVariable Long id ,@Valid  @RequestBody  UpdateTaskStatusRequest request){
Task task = taskService.newStatus(id,request);
return new TaskResponse(task.getId(),
        task.getTitle(),
        task.getDescription(),
        task.getPriority(),
        task.getStatus(),
        task.getTags());
    }
    @PatchMapping("/{id}/priority")
    public TaskResponse changePriority(@PathVariable Long id, @Valid  @RequestBody UpdateTaskPriorityRequest priority){
        Task task = taskService.newPriority(id, priority.getPriority());
        return new TaskResponse(task.getId(),task.getTitle(),task.getDescription(),task.getPriority(),task.getStatus(),task.getTags());
    }
    @PostMapping("/{id}/tags")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse addTags(@PathVariable Long id, @RequestBody @Valid AddTagRequest tag){
        Task task = taskService.addTags(id, tag.getTag());
        return new TaskResponse(task.getId(),task.getTitle(),task.getDescription(),task.getPriority(),task.getStatus(),task.getTags());

    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
         taskService.delete(id);
         return ResponseEntity.status(204).build();
    }
    @PutMapping("/{id}/update")
    public TaskResponse update(@PathVariable Long id,@RequestBody @Valid UpdateTaskRequest request){
        Task task1 = taskService.update(id,request);
        TaskResponse taskResponse1 = new TaskResponse(task1.getId(),task1.getTitle(),task1.getDescription(),task1.getPriority(),task1.getStatus(),task1.getTags());
        return taskResponse1;

    }
    



}
