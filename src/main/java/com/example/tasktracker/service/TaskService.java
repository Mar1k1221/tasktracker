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

    public Task findById(int id) {

        return taskRepository.findById(id)
                .orElseThrow(() ->
                        new TaskNotFoundException("Объект с таким ID не найден."));
    }

    public List<TaskResponse> findByAll(
            TaskStatus status,
            TaskPriority priority,
            String tag) {

        List<Task> tasks = taskRepository.findAll();
        List<TaskResponse> responses = new ArrayList<>();

        for (Task task : tasks) {

            if (status != null && task.getStatus() != status) {
                continue;
            }

            if (priority != null && task.getPriority() != priority) {
                continue;
            }

            if (tag != null && !task.getTags().contains(tag)) {
                continue;
            }

            responses.add(new TaskResponse(task));
        }

        return responses;
    }

    public boolean delete(int id) {

        findById(id);

        taskRepository.deleteById(id);

        return true;
    }

    public Task newStatus(
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

        return taskRepository.save(task);
    }

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

        return taskRepository.save(task);
    }

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

        return taskRepository.save(task);
    }

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

    public Page<Task> searchByTitle(String title,Pageable pageable){
     return taskRepository.findByTitleContainingIgnoreCase(title,pageable);
}
}