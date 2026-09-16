package com.example.tasktracker.service;

import com.example.tasktracker.dto.CreateTaskRequest;
import com.example.tasktracker.dto.UpdateTaskRequest;
import com.example.tasktracker.dto.UpdateTaskStatusRequest;
import com.example.tasktracker.exception.ConflictException;
import com.example.tasktracker.exception.InvalidStatusTransitionException;
import com.example.tasktracker.exception.TaskNotFoundException;
import com.example.tasktracker.model.Task;
import com.example.tasktracker.model.TaskPriority;
import com.example.tasktracker.model.TaskStatus;
import com.example.tasktracker.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;
    @InjectMocks
    private TaskService taskService;

    @Test
    @DisplayName("Проверка что при создании обьекта добавляется статус - NEW")
    public void createTest_always_savesTaskWithStatusNew() {
        CreateTaskRequest createTaskRequest = new CreateTaskRequest("Новая задача", "Новое описание", TaskPriority.LOW);
        taskService.save(createTaskRequest);
        ArgumentCaptor<Task> taskcaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskcaptor.capture());
        Task savedTask = taskcaptor.getValue();
        assertEquals(TaskStatus.NEW, savedTask.getStatus());
    }

    @Test
    @DisplayName("Проверка: запрещенный переход статуса из DONE выбрасывает ошибку и не сохраняет данные")
    public void changeStatus_forbiddenTransition_throwsExceptionAndDoesNotSave() {
        Task task = new Task(1L, "Новая тест задача", "TEST", TaskPriority.HIGH, TaskStatus.DONE, new HashSet<>());
        UpdateTaskStatusRequest updateTaskStatusRequest = new UpdateTaskStatusRequest(TaskStatus.NEW);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        assertThrows(InvalidStatusTransitionException.class, () -> taskService.newStatus(1L, updateTaskStatusRequest));
        verify(taskRepository, never()).save(any());
    }


}