package com.example.tasktracker.controller;

import com.example.tasktracker.dto.AddTagRequest;
import com.example.tasktracker.dto.CreateTaskRequest;
import com.example.tasktracker.dto.TaskStatisticsResponse;
import com.example.tasktracker.dto.UpdateTaskRequest;
import com.example.tasktracker.dto.UpdateTaskStatusRequest;
import com.example.tasktracker.exception.ConflictException;
import com.example.tasktracker.exception.InvalidStatusTransitionException;
import com.example.tasktracker.exception.TaskNotFoundException;
import com.example.tasktracker.model.Task;
import com.example.tasktracker.model.TaskPriority;
import com.example.tasktracker.model.TaskStatus;
import com.example.tasktracker.service.TaskService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;



    private Task task(Long id, String title, TaskStatus status) {
        return new Task(id, title, "Описание", TaskPriority.HIGH, status, new HashSet<>());
    }



    @Test
    void getTaskById_existingId_returns200WithBody() throws Exception {
        when(taskService.findById(1L)).thenReturn(task(1L, "Покормить кота", TaskStatus.NEW));

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Покормить кота"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void getTaskById_unknownId_returns404WithApiError() throws Exception {
        when(taskService.findById(anyLong()))
                .thenThrow(new TaskNotFoundException("Объект с таким ID не найден."));

        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Объект с таким ID не найден."))
                .andExpect(jsonPath("$.path").value("/api/tasks/999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }



    @Test
    void getTasks_validStatus_returns200AndFilteredList() throws Exception {
        when(taskService.findByAll(TaskStatus.NEW, null))
                .thenReturn(List.of(task(1L, "Задача 1", TaskStatus.NEW)));

        mockMvc.perform(get("/api/tasks").param("status", "NEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Задача 1"))
                .andExpect(jsonPath("$[0].status").value("NEW"));


        verify(taskService).findByAll(TaskStatus.NEW, null);
    }

    @Test
    void getTasks_unknownStatus_returns400TypeMismatch() throws Exception {
        mockMvc.perform(get("/api/tasks").param("status", "STATUS_GOOD"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TYPE_MISMATCH"))
                .andExpect(jsonPath("$.message").value(containsString("status")))
                .andExpect(jsonPath("$.path").value("/api/tasks"));

        verifyNoInteractions(taskService);
    }

    @Test
    void getTasks_emptyList_returns200AndEmptyArray() throws Exception {

        when(taskService.findByAll(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }



    @Test
    void createTask_validInput_returns201AndLocation() throws Exception {
        when(taskService.save(any(CreateTaskRequest.class)))
                .thenReturn(task(1L, "Покормить кота", TaskStatus.NEW));

        String json = "{\"title\":\"Покормить кота\",\"description\":\"Корм\",\"priority\":\"HIGH\"}";

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/tasks/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Покормить кота"))
                .andExpect(jsonPath("$.status").value("NEW"));

        ArgumentCaptor<CreateTaskRequest> captor = ArgumentCaptor.forClass(CreateTaskRequest.class);
        verify(taskService).save(captor.capture());
        CreateTaskRequest sent = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals("Покормить кота", sent.getTitle());
        org.junit.jupiter.api.Assertions.assertEquals("Корм", sent.getDescription());
        org.junit.jupiter.api.Assertions.assertEquals(TaskPriority.HIGH, sent.getPriority());
    }

    @Test
    void createTask_emptyTitle_returns400AndSkipsService() throws Exception {
        String json = "{\"title\":\"\",\"description\":\"Корм\",\"priority\":\"HIGH\"}";

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_EXCEPTION"))
                .andExpect(jsonPath("$.path").value("/api/tasks"));

        verifyNoInteractions(taskService);
    }

    @Test
    void createTask_tooLongTitle_returns400AndSkipsService() throws Exception {

        String longTitle = "a".repeat(1000);
        String json = "{\"title\":\"" + longTitle + "\",\"description\":\"Корм\",\"priority\":\"HIGH\"}";

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_EXCEPTION"));

        verifyNoInteractions(taskService);
    }



    @Test
    void updateStatus_validTransition_returns200() throws Exception {
        when(taskService.newStatus(eq(1L), any(UpdateTaskStatusRequest.class)))
                .thenReturn(task(1L, "Задача", TaskStatus.IN_PROGRESS));

        mockMvc.perform(patch("/api/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void updateStatus_forbiddenTransition_returns400WithApiError() throws Exception {
        when(taskService.newStatus(eq(1L), any(UpdateTaskStatusRequest.class)))
                .thenThrow(new InvalidStatusTransitionException("Нельзя изменить статус после перехода в DONE."));

        mockMvc.perform(patch("/api/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"NEW\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"))
                .andExpect(jsonPath("$.message").value("Нельзя изменить статус после перехода в DONE."))
                .andExpect(jsonPath("$.path").value("/api/tasks/1/status"));
    }



    @Test
    void changePriority_validRequest_returns200() throws Exception {
        Task updated = new Task(1L, "Задача", "Описание", TaskPriority.LOW, TaskStatus.NEW, new HashSet<>());
        when(taskService.newPriority(1L, TaskPriority.LOW)).thenReturn(updated);

        mockMvc.perform(patch("/api/tasks/1/priority")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"priority\":\"LOW\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.priority").value("LOW"));

        verify(taskService).newPriority(1L, TaskPriority.LOW);
    }

    @Test
    void changePriority_unknownTask_returns404WithApiError() throws Exception {
        when(taskService.newPriority(eq(999L), any()))
                .thenThrow(new TaskNotFoundException("Объект с таким id не найден."));

        mockMvc.perform(patch("/api/tasks/999/priority")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"priority\":\"LOW\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/tasks/999/priority"));
    }



    @Test
    void addTag_newTag_returns201WithTag() throws Exception {
        Task withTag = new Task(1L, "Задача", "Описание", TaskPriority.HIGH,
                TaskStatus.NEW, new HashSet<>(Set.of("java")));
        when(taskService.addTags(eq(1L), eq("java"))).thenReturn(withTag);

        mockMvc.perform(post("/api/tasks/1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tag\":\"java\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tags", hasSize(1)))
                .andExpect(jsonPath("$.tags[0]").value("java"));

        verify(taskService).addTags(1L, "java");
    }

    @Test
    void addTag_duplicateTag_returns409WithApiError() throws Exception {
        when(taskService.addTags(eq(1L), eq("spring")))
                .thenThrow(new ConflictException("Данный тег уже существует у обьекта."));

        mockMvc.perform(post("/api/tasks/1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tag\":\"spring\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT_EXCEPTION"))
                .andExpect(jsonPath("$.message").value("Данный тег уже существует у обьекта."))
                .andExpect(jsonPath("$.path").value("/api/tasks/1/tags"));
    }


    @Test
    void deleteTask_existingId_returns204AndCallsService() throws Exception {
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(taskService).delete(1L);
    }

    @Test
    void deleteTask_unknownId_returns404WithApiError() throws Exception {
        doThrow(new TaskNotFoundException("Объект с таким id не найден."))
                .when(taskService).delete(anyLong());

        mockMvc.perform(delete("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/tasks/999"));
    }


    @Test
    void updateTask_validRequest_returns200WithUpdatedBody() throws Exception {
        Task updated = new Task(1L, "Новый заголовок", "Новое описание",
                TaskPriority.HIGH, TaskStatus.NEW, new HashSet<>());
        when(taskService.update(eq(1L), any(UpdateTaskRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/tasks/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Новый заголовок\",\"description\":\"Новое описание\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Новый заголовок"))
                .andExpect(jsonPath("$.description").value("Новое описание"));
    }

    @Test
    void updateTask_emptyTitle_returns400AndSkipsService() throws Exception {
        mockMvc.perform(put("/api/tasks/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"description\":\"Описание\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_EXCEPTION"))
                .andExpect(jsonPath("$.path").value("/api/tasks/1/update"));

        verifyNoInteractions(taskService);
    }



    @Test
    void getStatistics_success_returns200WithBody() throws Exception {
        TaskStatisticsResponse stats = new TaskStatisticsResponse(
                Map.of(TaskStatus.NEW, 2),
                Map.of(TaskPriority.HIGH, 2));
        when(taskService.getStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/tasks/statistics"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(2)));

    }

    @Test
    void getStatistics_serviceFails_returns500WithApiError() throws Exception {
        when(taskService.getStatistics()).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/tasks/statistics"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                // внутренняя причина ("boom") не должна утекать клиенту
                .andExpect(jsonPath("$.message").value("Внутренняя ошибка сервера"))
                .andExpect(jsonPath("$.path").value("/api/tasks/statistics"));
    }
}