package com.example.tasktracker.service;

import com.example.tasktracker.dto.CreateTaskRequest;
import com.example.tasktracker.dto.TaskResponse;
import com.example.tasktracker.dto.TaskStatisticsResponse;
import com.example.tasktracker.dto.UpdateTaskStatusRequest;
import com.example.tasktracker.exception.ConflictException;
import com.example.tasktracker.exception.InvalidStatusTransitionException;
import com.example.tasktracker.exception.TaskNotFoundException;
import com.example.tasktracker.model.Task;
import com.example.tasktracker.model.TaskPriority;
import com.example.tasktracker.model.TaskStatus;
import com.example.tasktracker.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task save(CreateTaskRequest request) {

        Task newTask = new Task(
                request.getTitle(),
                request.getDescription(),
                request.getPriority(),
                TaskStatus.NEW,
                Set.of()
        );

        return taskRepository.save(newTask);
    }
@Transactional(readOnly = true)
    public Task findById(int id) {

        return taskRepository.findById(id)
                .orElseThrow(() ->
                        new TaskNotFoundException("Объект с таким ID не найден."));
    }
@Transactional(readOnly = true)
    public List<TaskResponse> findByAll(
            TaskStatus status,
            TaskPriority priority,
            String tag) {
List<Task> taskList;
if (status != null && priority != null){
    taskList = taskRepository.findByStatusAndPriority(status,priority);
} else if (status != null) {
    taskList = taskRepository.findByStatus(status);

} else {
    taskList = taskRepository.findAll();
}
List<TaskResponse> listResponse = new ArrayList<>();
for (Task t:taskList){
    if (tag != null && !t.getTags().contains(tag)){
        continue;
    }
    TaskResponse taskResponse = new TaskResponse(t.getId(),t.getTitle(),t.getDescription(),t.getPriority(),t.getStatus(),t.getTags());
    listResponse.add(taskResponse);

} return listResponse;
    }

    public boolean delete(int id) {

        findById(id);

        taskRepository.deleteById(id);

        return true;
    }
@Transactional
    public TaskResponse newStatus(
            int id,
            UpdateTaskStatusRequest statusRequest) {

        Task task = findById(id);

        if (task.getStatus() == TaskStatus.DONE
                && statusRequest.getStatus() != TaskStatus.DONE) {

            throw new InvalidStatusTransitionException(
                    "Нельзя изменить статус после перехода в DONE."
            );
        }

        task.setStatus(statusRequest.getStatus());

    return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(),task.getPriority(),task.getStatus(),new java.util.HashSet<>(task.getTags()));
    }
@Transactional
    public Task newPriority(
            int id,
            TaskPriority priority) {

        Task task = findById(id);

        if (task.getPriority() == priority) {

            throw new ConflictException(
                    "Данный приоритет уже имеется у объекта."
            );
        }

        task.setPriority(priority);

        return task;
    }
@Transactional
    public Task addTags(
            int id,
            String tag) {

        Task task = findById(id);

        if (task.getTags().contains(tag)) {

            throw new ConflictException(
                    "Данный тег уже существует у объекта."
            );
        }

        task.getTags().add(tag);
//        if (true){
//            throw new RuntimeException("Практика, проверка отката!");
//        }

       return task;
    }
@Transactional(readOnly = true)
    public TaskStatisticsResponse statisticsResponse() {

        List<Task> taskList = taskRepository.findAll();

        Map<TaskStatus, Integer> statusCount = new HashMap<>();
        Map<TaskPriority, Integer> priorityCount = new HashMap<>();

        for (Task task : taskList) {

            statusCount.put(
                    task.getStatus(),
                    statusCount.getOrDefault(task.getStatus(), 0) + 1
            );

            priorityCount.put(
                    task.getPriority(),
                    priorityCount.getOrDefault(task.getPriority(), 0) + 1
            );
        }

        return new TaskStatisticsResponse(
                statusCount,
                priorityCount
        );
    }
@Transactional(readOnly = true)
    public Page<Task> searchByTitle(String title,Pageable pageable){
     return taskRepository.findByTitleContainingIgnoreCase(title,pageable);
}@Transactional
public void testRollbackCreation(){
        Task task = new Task();
        task.setTitle("Test Task");
        task.setDescription("Эта задача не должна попасть в базу");
        task.setPriority(TaskPriority.HIGH);
        task.setStatus(TaskStatus.NEW);
        task.setCreated_at(LocalDateTime.now());
        taskRepository.save(task);
        if (true){
            throw new RuntimeException("Авария, проверяем роллбэк.");
        }
        task.getTags().add("test1");
        task.getTags().add("test2");

}
}