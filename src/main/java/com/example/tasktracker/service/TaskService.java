package com.example.tasktracker.service;

import com.example.tasktracker.dto.CreateTaskRequest;
import com.example.tasktracker.dto.TaskStatisticsResponse;
import com.example.tasktracker.dto.UpdateTaskRequest;
import com.example.tasktracker.dto.UpdateTaskStatusRequest;
import com.example.tasktracker.exception.ConflictException;
import com.example.tasktracker.exception.InvalidStatusTransitionException;
import com.example.tasktracker.exception.TaskNotFoundException;
import com.example.tasktracker.model.*;

import com.example.tasktracker.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
@Transactional
    public Task save(CreateTaskRequest task) {
     Task task1 = new Task(null,task.getTitle(),task.getDescription(),task.getPriority(),TaskStatus.NEW,new HashSet<>());
return taskRepository.save(task1);
    }
    @Transactional(readOnly = true)
    public Task findById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Объект с таким ID не найден."));
    }
@Transactional(readOnly = true)
    public List<Task> findByAll(TaskStatus status,TaskPriority priority) {
      return taskRepository.findByStatusAndPriority(status,priority);

    }
@Transactional
    public void delete(Long id) {
      Task task =  taskRepository.findById(id)
               .orElseThrow(()->new TaskNotFoundException("Обьект с таким id не найден."));
       taskRepository.delete(task);
    }


@Transactional
    public Task newStatus(Long id, UpdateTaskStatusRequest status) {
        Task t1=taskRepository.findById(id)
        .orElseThrow(() -> new TaskNotFoundException("Обьект с таким id не найден."));

        if (t1.getStatus() == TaskStatus.DONE && status.getStatus() != TaskStatus.DONE) {
            throw new InvalidStatusTransitionException("Нельзя изменить статус после перехода в DONE.");
        }
        t1.setStatus(status.getStatus());
        taskRepository.save(t1);
        return t1;
    }
    @Transactional
    public Task newPriority(Long id, TaskPriority priority) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Объект с таким id не найден."));

        if (task.getPriority() != priority) {
            task.setPriority(priority);
            taskRepository.save(task);
        }

        return task;
    }
@Transactional
    public Task addTags(Long id, String tags) {
        Task task = taskRepository.findById(id)
                .orElseThrow(()-> new TaskNotFoundException("Обьект с таким id не найден."));

        if (task.getTags().contains(tags)) {
            throw new ConflictException("Данный тег уже существует у обьекта.");
        }
        task.getTags().add(tags);
        taskRepository.save(task);
        return task;
    }
    @Transactional(readOnly = true)
public TaskStatisticsResponse getStatistics(){
     List<PriorityCount> priorityCounts = taskRepository.countTaskByPriority();
     List<StatusCount> statusCounts = taskRepository.countTaskByStatus();

     Map<TaskPriority,Integer> priorityMap = new HashMap<>();
     for (PriorityCount pr: priorityCounts){
         priorityMap.put(pr.getPriority(),pr.getCount().intValue());
     }
     Map<TaskStatus,Integer> statusMap = new HashMap<>();
     for (StatusCount st: statusCounts){
         statusMap.put(st.getStatus(),st.getCount().intValue());
     } return new TaskStatisticsResponse(statusMap,priorityMap);
}@Transactional
public Task update(Long id, UpdateTaskRequest request){
     Task t = taskRepository.findById(id)
             .orElseThrow(()->new TaskNotFoundException("Обьект с таким id не найден"));
     t.setTitle(request.getTitle());
     t.setDescription(request.getDescription());
     return taskRepository.save(t);

}

}
