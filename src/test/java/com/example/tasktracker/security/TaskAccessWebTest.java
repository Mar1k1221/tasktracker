
package com.example.tasktracker.security;

import com.example.tasktracker.config.SecurityConfig;
import com.example.tasktracker.exception.TaskNotFoundException;
import com.example.tasktracker.model.Task;
import com.example.tasktracker.model.TaskPriority;
import com.example.tasktracker.model.TaskStatus;
import com.example.tasktracker.service.TaskService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.HashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = {
                com.example.tasktracker.controller.TaskController.class,
                com.example.tasktracker.controller.AdminTaskController.class
        },
        properties = "security.jwt.secret=0123456789abcdef0123456789abcdef"
)
@Import(SecurityConfig.class)
class TaskAccessWebTest {

    private static final String USERNAME = "anna";
    private static final String TASK_ID = "42";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    // Создаём тестовую аутентификацию обычного пользователя.
    private RequestPostProcessor asUser(String username) {
        return jwt()
                .jwt(token -> token
                        .subject(username)
                        .claim("roles", List.of("USER")))
                .authorities(
                        new SimpleGrantedAuthority("ROLE_USER")
                );
    }

    // Создаём тестовую аутентификацию администратора.
    private RequestPostProcessor asAdmin() {
        return jwt()
                .jwt(token -> token
                        .subject("admin")
                        .claim("roles", List.of("ADMIN")))
                .authorities(
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                );
    }

    private Task createTask() {
        return new Task(
                42L,
                "My task",
                "Test description",
                TaskPriority.HIGH,
                TaskStatus.NEW,
                new HashSet<>()
        );
    }

    // 1. Без токена нельзя получить список задач.
    @Test
    void getTasks_withoutToken_returns401() throws Exception {

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(taskService);
    }

    // 2. Без токена нельзя создавать задачи.
    @Test
    void createTask_withoutToken_returns401() throws Exception {

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(taskService);
    }

    // 3. Без токена нельзя получить административный список.
    @Test
    void getAdminTasks_withoutToken_returns401() throws Exception {

        mockMvc.perform(get("/api/admin/tasks"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(taskService);
    }

    // 4. USER не имеет доступа к административному списку.
    @Test
    void getAdminTasks_userRole_returns403() throws Exception {

        mockMvc.perform(get("/api/admin/tasks")
                        .with(asUser(USERNAME)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(taskService);
    }

    // 5. ADMIN может получить все задачи.
    @Test
    void getAdminTasks_adminRole_returns200() throws Exception {

        when(taskService.getAllTasksForAdmin())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/admin/tasks")
                        .with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(taskService).getAllTasksForAdmin();
    }

    // 6. Пользователь получает список своих задач.
    @Test
    void getTasks_authenticatedUser_returnsOwnTasks() throws Exception {

        when(taskService.findByAll(null, null, USERNAME))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/tasks")
                        .with(asUser(USERNAME)))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(taskService).findByAll(null, null, USERNAME);
    }

    // 7. Пользователь может получить свою задачу.
    @Test
    void getTask_ownTask_returns200() throws Exception {

        when(taskService.findById(42L, USERNAME))
                .thenReturn(createTask());

        mockMvc.perform(get("/api/tasks/" + TASK_ID)
                        .with(asUser(USERNAME)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("My task"));

        verify(taskService).findById(42L, USERNAME);
    }

    // 8. Чужая задача не видна по ID.
    @Test
    void getTask_foreignTask_returns404() throws Exception {

        when(taskService.findById(42L, USERNAME))
                .thenThrow(new TaskNotFoundException("Task not found"));

        mockMvc.perform(get("/api/tasks/" + TASK_ID)
                        .with(asUser(USERNAME)))
                .andExpect(status().isNotFound());

        verify(taskService).findById(42L, USERNAME);
    }

    // 9. Нельзя менять статус чужой задачи.
    @Test
    void updateStatus_foreignTask_returns404() throws Exception {

        doThrow(new TaskNotFoundException("Task not found"))
                .when(taskService)
                .newStatus(
                        eq(42L),
                        any(com.example.tasktracker.dto.UpdateTaskStatusRequest.class),
                        eq(USERNAME)
                );

        mockMvc.perform(patch("/api/tasks/" + TASK_ID + "/status")
                        .with(asUser(USERNAME))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"IN_PROGRESS"}
                                """))
                .andExpect(status().isNotFound());

        verify(taskService).newStatus(
                eq(42L),
                any(com.example.tasktracker.dto.UpdateTaskStatusRequest.class),
                eq(USERNAME)
        );
    }

    // 10. Нельзя менять приоритет чужой задачи.
    @Test
    void updatePriority_foreignTask_returns404() throws Exception {

        doThrow(new TaskNotFoundException("Task not found"))
                .when(taskService)
                .newPriority(42L, TaskPriority.LOW, USERNAME);

        mockMvc.perform(patch("/api/tasks/" + TASK_ID + "/priority")
                        .with(asUser(USERNAME))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"priority":"LOW"}
                                """))
                .andExpect(status().isNotFound());

        verify(taskService).newPriority(42L, TaskPriority.LOW, USERNAME);
    }

    // 11. Нельзя добавлять тег к чужой задаче.
    @Test
    void addTag_foreignTask_returns404() throws Exception {

        doThrow(new TaskNotFoundException("Task not found"))
                .when(taskService)
                .addTags(42L, "important", USERNAME);

        mockMvc.perform(post("/api/tasks/" + TASK_ID + "/tags")
                        .with(asUser(USERNAME))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tag":"important"}
                                """))
                .andExpect(status().isNotFound());

        verify(taskService).addTags(42L, "important", USERNAME);
    }

    // 12. Нельзя редактировать чужую задачу.
    @Test
    void updateTask_foreignTask_returns404() throws Exception {

        doThrow(new TaskNotFoundException("Task not found"))
                .when(taskService)
                .update(
                        eq(42L),
                        any(com.example.tasktracker.dto.UpdateTaskRequest.class),
                        eq(USERNAME)
                );

        mockMvc.perform(put("/api/tasks/" + TASK_ID + "/update")
                        .with(asUser(USERNAME))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Hacked title",
                                  "description":"Hacked description"
                                }
                                """))
                .andExpect(status().isNotFound());

        verify(taskService).update(
                eq(42L),
                any(com.example.tasktracker.dto.UpdateTaskRequest.class),
                eq(USERNAME)
        );
    }

    // 13. Нельзя удалить чужую задачу.
    @Test
    void deleteTask_foreignTask_returns404() throws Exception {

        doThrow(new TaskNotFoundException("Task not found"))
                .when(taskService)
                .delete(42L, USERNAME);

        mockMvc.perform(delete("/api/tasks/" + TASK_ID)
                        .with(asUser(USERNAME)))
                .andExpect(status().isNotFound());

        verify(taskService).delete(42L, USERNAME);
    }
}
