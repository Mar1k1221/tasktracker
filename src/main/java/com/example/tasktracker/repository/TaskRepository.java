package com.example.tasktracker.repository;

import com.example.tasktracker.model.Task;
import com.example.tasktracker.model.TaskPriority;
import com.example.tasktracker.model.TaskStatus;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TaskRepository {
    private final Map<Integer, Task> taskMap = new HashMap<>(Map.of(
            3,new Task(3,"Изучить Spring Boot","Разобраться с DI, Bean и архитектурой " +
                    "приложения", TaskPriority.HIGH,
                    TaskStatus.IN_PROGRESS, Set.of("java", "spring", "backend")),
                    4,new Task(4,"Изучить Docker","Создать Докерфайл и запустить приложение " +
                    "в контейнере",TaskPriority.LOW,
                    TaskStatus.NEW, Set.of("docker","devops")),
                    5, new Task(5,"Написать тесты","Добавить инит тесты для Service слоя",
                    TaskPriority.MEDIUM,TaskStatus.DONE,Set.of("testing", "junit"))
    ));
    public Task save(Task task){
        Task newTask = new Task(task.getId(), task.getTitle(), task.getDescription(), task.getPriority(),task.getStatus(),task.getTags());
        taskMap.put(task.getId(), newTask);
        return newTask;
    }
    public List<Task> findByAll(){
        return new ArrayList<>(taskMap.values());
    }
    public Task findById(int id){
        return taskMap.get(id);
    }
    public boolean deleteById(int id){
        return taskMap.remove(id) != null;
    }
    public boolean existsById(int id){
        Task taskBoolean = findById(id);
        if (taskBoolean == null){
            return false;
        } return true;
    }

}
