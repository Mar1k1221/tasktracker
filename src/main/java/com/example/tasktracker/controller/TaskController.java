package com.example.tasktracker.controller;

import com.example.tasktracker.dto.*;
import com.example.tasktracker.model.Task;
import com.example.tasktracker.model.TaskPriority;
import com.example.tasktracker.model.TaskStatus;
import com.example.tasktracker.service.TaskService;
import jakarta.validation.Valid;



import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return taskService.statisticsResponse();
    }
    @GetMapping("/{id}")
    public TaskResponse findById(@PathVariable int id){
        Task task = taskService.findById(id);
         return new TaskResponse(task.getId(),task.getTitle(),task.getDescription(),task.getPriority(),task.getStatus(),task.getTags());
    }
    @GetMapping
    public List<TaskResponse> findByAll(@RequestParam ( required = false)TaskStatus status,@RequestParam(required = false)
    TaskPriority priority,@RequestParam(required = false) String tag) {

        return taskService.findByAll(status,priority,tag);
    }
    @PostMapping
    public ResponseEntity<TaskResponse> save(@RequestBody @Valid CreateTaskRequest createTaskRequest){
        Task task = taskService.save(createTaskRequest);
        TaskResponse taskResponse = new TaskResponse(task.getId(),task.getTitle(),task.getDescription(),task.getPriority(),task.getStatus(),task.getTags());
      java.net.URI location = java.net.URI.create("/api/tasks/" + task.getId());
        return ResponseEntity.created(location).body(taskResponse);
    }


    @PatchMapping("/{id}/status")
    public TaskResponse newStatus(@PathVariable int id ,@RequestBody UpdateTaskStatusRequest request){
Task task = taskService.newStatus(id,request);
return new TaskResponse(task.getId(),
        task.getTitle(),
        task.getDescription(),
        task.getPriority(),
        task.getStatus(),
        task.getTags());
    }
    @PatchMapping("/{id}/priority")
    public TaskResponse changePriority(@PathVariable int id, @RequestBody UpdateTaskPriorityRequest priority){
        Task task = taskService.newPriority(id, priority.getPriority());
        return new TaskResponse(task.getId(),task.getTitle(),task.getDescription(),task.getPriority(),task.getStatus(),task.getTags());
    }
    @PostMapping("/{id}/tags")
    public TaskResponse addTags(@PathVariable int id, @RequestBody @Valid AddTagRequest tag){
        Task task = taskService.addTags(id, tag.getTag());
        return new TaskResponse(task.getId(),task.getTitle(),task.getDescription(),task.getPriority(),task.getStatus(),task.getTags());

    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id){
         taskService.delete(id);
         return ResponseEntity.status(204).build();
    }



}
