package com.example.tasktracker.controller;

import com.example.tasktracker.dto.AddTagRequest;
import com.example.tasktracker.dto.CreateTaskRequest;
import com.example.tasktracker.dto.UpdateTaskStatusRequest;
import com.example.tasktracker.exception.ConflictException;
import com.example.tasktracker.exception.InvalidStatusTransitionException;
import com.example.tasktracker.exception.TaskNotFoundException;
import com.example.tasktracker.model.Task;
import com.example.tasktracker.model.TaskPriority;
import com.example.tasktracker.model.TaskStatus;
import com.example.tasktracker.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;


@WebMvcTest(TaskController.class)
class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private TaskService taskService;


    @Test
    public void getTaskById_inknownId_returns404WithMessage() throws Exception {
        when(taskService.findById(anyLong())).thenThrow(new TaskNotFoundException("Обьект с таким ID не найден."));
        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/tasks/999"));

    }

    @Test
    public void createTask_validInput_returns201andLocationHandler() throws Exception {
        Task task = new Task(1L, "SDSA", "DSASD", TaskPriority.HIGH, TaskStatus.NEW, new HashSet<>());
        when(taskService.save(any())).thenReturn(task);
        String jsonRequest = "{\"title\":\"Покормить кота\", \"description\":\"Корм\", \"priority\":\"HIGH\"}";
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/tasks/1"));


    }

    @Test
    public void createTask_emptyTitle_returns400() throws Exception {
        String jsonRequest = "{\"title\":\"\",\"description\":\"корm\",\"priority\":\"HIGH\"}";
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());


    }

    @Test
    public void getTasks_validStatus_returns200AndFilterList() throws Exception {
        Task task = new Task(1L, "SAS", "SSS", TaskPriority.HIGH, TaskStatus.NEW, new HashSet<>());
        List<Task> list = List.of(task);
        when(taskService.findByAll(TaskStatus.NEW, null)).thenReturn(list);
        mockMvc.perform(get("/api/tasks")
                        .param("status", "NEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].status").value("NEW"));


    }

    @Test
    public void getTasks_notValidStatus_returns400() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .param("status", "STATUS_GOOD"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateStatus_validTransition_return200ok() throws Exception {
        Task task = new Task(1L, "SDDS", "ASDAS", TaskPriority.HIGH, TaskStatus.IN_PROGRESS, new HashSet<>());
        when(taskService.newStatus(eq(1L), any(UpdateTaskStatusRequest.class))).thenReturn(task);
        mockMvc.perform(patch("/api/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));


    }

    @Test
    public void updateStatus_notValidTransition_return400() throws Exception {
        when(taskService.newStatus(eq(1L), any(UpdateTaskStatusRequest.class)))
                .thenThrow(new InvalidStatusTransitionException("Нельзя изменить статус после перехода в DONE."));
        mockMvc.perform(patch("/api/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void add_tags_positive201ok() throws Exception {
        HashSet<String> set = new HashSet<>(Set.of("java"));
        Task task = new Task(1L, "SDDS", "ASDAS", TaskPriority.HIGH, TaskStatus.IN_PROGRESS, set);

        when(taskService.addTags(eq(1l), any())).thenReturn(task);
        mockMvc.perform(post("/api/tasks/1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tag\":\"java\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tags[0]").value("java"));


    }

    @Test
    public void addTags_duplicateTag_returns409Conflict() throws Exception {

        when(taskService.addTags(eq(1L), any()))
                .thenThrow(new ConflictException("Такой тег уже существует у задачи"));


        mockMvc.perform(post("/api/tasks/1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tag\":\"spring\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    public void deleteTask_validId_returns204NoContent() throws Exception {


        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
    }

}