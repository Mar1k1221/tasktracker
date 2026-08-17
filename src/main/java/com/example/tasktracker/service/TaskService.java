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
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task save(CreateTaskRequest task) {
        List<Task> newList = new ArrayList<>(taskRepository.findByAll());
        int max = 0;
        for (Task t : newList) {
            if (max < t.getId()) {
                max = t.getId();
            }

        }
        int newId = max + 1;
        Task newTask = new Task(newId, task.getTitle(), task.getDescription(), task.getPriority(), TaskStatus.NEW, Set.of());
        return taskRepository.save(newTask);


    }

    public Task findById(int id) {
        Task t = taskRepository.findById(id);
        if (t == null) {
            throw new TaskNotFoundException("Обьект с таким ID не найден.");
        }
        return t;
    }

    public List<TaskResponse> findByAll(TaskStatus status,TaskPriority priority,String tag) {
        List<Task> tasks = taskRepository.findByAll();
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
        return taskRepository.deleteById(id);
    }



    public Task newStatus(int id, UpdateTaskStatusRequest status) {
        Task t1 = taskRepository.findById(id);
        if (t1 == null) {
            throw new TaskNotFoundException("Обьект не найден.");
        }
        if (t1.getStatus() == TaskStatus.DONE && status.getStatus() != TaskStatus.DONE) {
            throw new InvalidStatusTransitionException("Нельзя изменить статус после перехода в DONE.");
        }
        t1.setStatus(status.getStatus());
        taskRepository.save(t1);
        return t1;
    }

    public Task newPriority(int id, TaskPriority priority) {
        Task task = taskRepository.findById(id);
        if (task == null) {
            throw new TaskNotFoundException("Обьект не найден.");
        }
        if (task.getPriority() == priority) {
            throw new ConflictException("Данный приоритет уже имеется у обьекта.Выберите другой приоритет.");
        } else {
            task.setPriority(priority);
        }
        taskRepository.save(task);
        return task;


    }

    public Task addTags(int id, String tags) {

        Task task = taskRepository.findById(id);
        if (task == null) {
            throw new TaskNotFoundException("Обьект с таким Id не найден.");
        }
        if (task.getTags().contains(tags)) {
            throw new ConflictException("Данный тег уже существует у обьекта.");
        }
        task.getTags().add(tags);
        taskRepository.save(task);
        return task;
    }
public TaskStatisticsResponse statisticsResponse(){
        List<Task> taskList = taskRepository.findByAll();
        Map<TaskStatus,Integer> statusCount = new HashMap<>();
        Map<TaskPriority,Integer> priorityCount = new HashMap<>();
        for (Task t: taskList){
            statusCount.put(t.getStatus(),statusCount.getOrDefault(t.getStatus(),0) + 1);

            priorityCount.put(t.getPriority(),priorityCount.getOrDefault(t.getPriority(),0)+1);
        }
        return new TaskStatisticsResponse(statusCount,priorityCount);
}

}
