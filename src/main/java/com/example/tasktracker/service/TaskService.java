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
import com.example.tasktracker.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class TaskService
{
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository
    ) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }
    @Transactional
    public Task save(CreateTaskRequest request, String username) {

        AppUser owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Пользователь не найден."
                ));

        Task task1 = new Task(
                null,
                request.getTitle(),
                request.getDescription(),
                request.getPriority(),
                TaskStatus.NEW,
                new HashSet<>()
        );

        task1.setOwner(owner);

        return taskRepository.save(task1);
    }
    @Transactional(readOnly = true)
    public Task findById(Long id,String username) {
        return taskRepository.findByIdAndOwner_Username(id,username)
                .orElseThrow(() -> new TaskNotFoundException("Объект с таким ID не найден."));
    }
@Transactional(readOnly = true)
    public List<Task> findByAll(TaskStatus status,TaskPriority priority,String username) {
    return taskRepository.findByOwnerAndStatusAndPriorityOptional(username,status,priority);

    }
@Transactional
    public void delete(Long id,String username) {
      Task task =  taskRepository.findByIdAndOwner_Username(id,username)
               .orElseThrow(()->new TaskNotFoundException("Обьект с таким id не найден."));
       taskRepository.delete(task);
    }


@Transactional
    public Task newStatus(Long id, UpdateTaskStatusRequest status,String username) {
        Task t1=taskRepository.findByIdAndOwner_Username(id,username)
        .orElseThrow(() -> new TaskNotFoundException("Обьект с таким id не найден."));

        if (!t1.getStatus().status_transition(status.getStatus())) {
            throw new InvalidStatusTransitionException("Недопустимый переход статуса: "
                    + t1.getStatus() + " -> " + status.getStatus());
        }
        t1.setStatus(status.getStatus());
        taskRepository.save(t1);
        return t1;
    }
    @Transactional
    public Task newPriority(Long id, TaskPriority priority,String username) {
        Task task = taskRepository.findByIdAndOwner_Username(id,username)
                .orElseThrow(() -> new TaskNotFoundException("Объект с таким id не найден."));

        if (task.getPriority() != priority) {
            task.setPriority(priority);
            taskRepository.save(task);
        }

        return task;
    }
@Transactional
    public Task addTags(Long id, String tags,String username) {
        Task task = taskRepository.findByIdAndOwner_Username(id,username)
                .orElseThrow(()-> new TaskNotFoundException("Обьект с таким id не найден."));

        if (task.getTags().contains(tags)) {
            throw new ConflictException("Данный тег уже существует у обьекта.");
        }
        task.getTags().add(tags);
        taskRepository.save(task);
        return task;
    }
    @Transactional(readOnly = true)
public TaskStatisticsResponse getStatistics(String username){
     List<PriorityCount> priorityCounts = taskRepository.countTaskByPriority(username);
     List<StatusCount> statusCounts = taskRepository.countTaskByStatus(username);

     Map<TaskPriority,Integer> priorityMap = new HashMap<>();
     for (PriorityCount pr: priorityCounts){
         priorityMap.put(pr.getPriority(),pr.getCount().intValue());
     }
     Map<TaskStatus,Integer> statusMap = new HashMap<>();
     for (StatusCount st: statusCounts){
         statusMap.put(st.getStatus(),st.getCount().intValue());
     } return new TaskStatisticsResponse(statusMap,priorityMap);
}@Transactional
public Task update(Long id, UpdateTaskRequest request,String username){
     Task t = taskRepository.findByIdAndOwner_Username(id,username)
             .orElseThrow(()->new TaskNotFoundException("Обьект с таким id не найден"));
     t.setTitle(request.getTitle());
     t.setDescription(request.getDescription());
     return taskRepository.save(t);

}
@Transactional(readOnly = true)
@PreAuthorize("hasRole('ADMIN')")

public List<Task> getAllTasksForAdmin(){
        return taskRepository.findAll();
}


}
