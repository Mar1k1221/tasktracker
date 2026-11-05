
package com.example.tasktracker.service;

import com.example.tasktracker.dto.CreateTaskRequest;
import com.example.tasktracker.dto.UpdateTaskStatusRequest;
import com.example.tasktracker.exception.ConflictException;
import com.example.tasktracker.exception.InvalidStatusTransitionException;
import com.example.tasktracker.exception.TaskNotFoundException;
import com.example.tasktracker.model.AppUser;
import com.example.tasktracker.model.Task;
import com.example.tasktracker.model.TaskPriority;
import com.example.tasktracker.model.TaskStatus;
import com.example.tasktracker.repository.TaskRepository;
import com.example.tasktracker.repository.UserRepository;

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

    private static final String USERNAME = "marat_test_01";

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    @DisplayName("Создание задачи: статус NEW и назначение владельца")
    void createTest_always_savesTaskWithStatusNew() {

        CreateTaskRequest request = new CreateTaskRequest(
                "Новая задача",
                "Новое описание",
                TaskPriority.LOW
        );

        AppUser owner = new AppUser(
                USERNAME,
                "hashed-password",
                "USER"
        );

        when(userRepository.findByUsername(USERNAME))
                .thenReturn(Optional.of(owner));

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        taskService.save(request, USERNAME);

        ArgumentCaptor<Task> captor =
                ArgumentCaptor.forClass(Task.class);

        verify(taskRepository).save(captor.capture());

        Task savedTask = captor.getValue();

        assertEquals(TaskStatus.NEW, savedTask.getStatus());
        assertThat(savedTask.getOwner()).isSameAs(owner);
    }

    @Test
    @DisplayName("Запрещённый переход из DONE")
    void changeStatus_forbiddenTransition_throwsExceptionAndDoesNotSave() {

        Task task = new Task(
                1L, "Новая тест задача", "TEST",
                TaskPriority.HIGH, TaskStatus.DONE, new HashSet<>()
        );

        UpdateTaskStatusRequest request =
                new UpdateTaskStatusRequest(TaskStatus.NEW);

        when(taskRepository.findByIdAndOwner_Username(1L, USERNAME))
                .thenReturn(Optional.of(task));

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> taskService.newStatus(1L, request, USERNAME)
        );

        verify(taskRepository, never()).save(any());
    }

    @Test
    void changeStatus_newToDone_throwsExceptionAndDoesNotSave() {

        Task task = new Task(
                1L, "Task", "TEST",
                TaskPriority.HIGH, TaskStatus.NEW, new HashSet<>()
        );

        when(taskRepository.findByIdAndOwner_Username(1L, USERNAME))
                .thenReturn(Optional.of(task));

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> taskService.newStatus(
                        1L,
                        new UpdateTaskStatusRequest(TaskStatus.DONE),
                        USERNAME
                )
        );

        verify(taskRepository, never()).save(any());
    }

    @Test
    void changeStatus_cancelledToNew_throwsExceptionAndDoesNotSave() {

        Task task = new Task(
                1L, "Task", "TEST",
                TaskPriority.HIGH, TaskStatus.CANCELLED, new HashSet<>()
        );

        when(taskRepository.findByIdAndOwner_Username(1L, USERNAME))
                .thenReturn(Optional.of(task));

        assertThrows(
                InvalidStatusTransitionException.class,
                () -> taskService.newStatus(
                        1L,
                        new UpdateTaskStatusRequest(TaskStatus.NEW),
                        USERNAME
                )
        );

        verify(taskRepository, never()).save(any());
    }

    @ParameterizedTest(name = "{0} — задача не найдена")
    @ValueSource(longs = {400L, 300L, 500L})
    void test_for_locating_againstNonexistentOne_ThrowsException(Long id) {

        when(taskRepository.findByIdAndOwner_Username(id, USERNAME))
                .thenReturn(Optional.empty());

        assertThrows(
                TaskNotFoundException.class,
                () -> taskService.findById(id, USERNAME)
        );
    }

    @Test
    @DisplayName("Изменение приоритета собственной задачи")
    void test_newPriority_Positive() {

        Task task = new Task(
                1L, "psdp", "psdfpds",
                TaskPriority.LOW, TaskStatus.NEW, new HashSet<>()
        );

        when(taskRepository.findByIdAndOwner_Username(1L, USERNAME))
                .thenReturn(Optional.of(task));

        Task result = taskService.newPriority(
                1L,
                TaskPriority.MEDIUM,
                USERNAME
        );

        assertThat(result.getPriority())
                .isEqualTo(TaskPriority.MEDIUM);

        verify(taskRepository).save(task);
    }

    @ParameterizedTest(name = "Добавление тега в задачу {0}")
    @ValueSource(longs = {300L, 200L, 15L})
    void exceptionWhenAddTag_toExistent_Task(Long id) {

        when(taskRepository.findByIdAndOwner_Username(id, USERNAME))
                .thenReturn(Optional.empty());

        assertThrows(
                TaskNotFoundException.class,
                () -> taskService.addTags(id, "TTT", USERNAME)
        );

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Повторный тег вызывает ConflictException")
    void conflictAvoidedTaskStateUnchanged() {

        Set<String> tags = new HashSet<>(Set.of("ttt"));

        Task task = new Task(
                1L, "sdd", "sdaasd",
                TaskPriority.MEDIUM, TaskStatus.NEW, tags
        );

        when(taskRepository.findByIdAndOwner_Username(1L, USERNAME))
                .thenReturn(Optional.of(task));

        assertThrows(
                ConflictException.class,
                () -> taskService.addTags(1L, "ttt", USERNAME)
        );

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Удаление собственной задачи")
    void test_deleteObject_positive() {

        Task task = new Task(
                1L, "sdadsa", "asdas",
                TaskPriority.MEDIUM, TaskStatus.NEW, new HashSet<>()
        );

        when(taskRepository.findByIdAndOwner_Username(1L, USERNAME))
                .thenReturn(Optional.of(task));

        taskService.delete(1L, USERNAME);

        verify(taskRepository).delete(task);
    }
}
