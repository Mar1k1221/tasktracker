
package com.example.tasktracker.security;

import com.example.tasktracker.repository.TaskRepository;
import com.example.tasktracker.repository.UserRepository;
import com.example.tasktracker.service.TaskService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = TaskMethodSecurityTest.TestConfig.class)
class TaskMethodSecurityTest {

    @Configuration
    @EnableMethodSecurity
    static class TestConfig {

        @Bean
        TaskRepository taskRepository() {
            return mock(TaskRepository.class);
        }

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        TaskService taskService(
                TaskRepository taskRepository,
                UserRepository userRepository
        ) {
            return new TaskService(taskRepository, userRepository);
        }
    }

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void resetRepositoryMock() {
        reset(taskRepository);
    }

    // ADMIN может вызвать административный метод.
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllTasksForAdmin_adminRole_isAllowed() {

        when(taskRepository.findAll()).thenReturn(List.of());

        taskService.getAllTasksForAdmin();

        verify(taskRepository).findAll();
    }

    // USER не может вызвать административный метод.
    @Test
    @WithMockUser(username = "anna", roles = {"USER"})
    void getAllTasksForAdmin_userRole_isDenied() {

        assertThrows(
                AccessDeniedException.class,
                () -> taskService.getAllTasksForAdmin()
        );

        verifyNoInteractions(taskRepository);
    }

    // Анонимный пользователь не может вызвать административный метод.
    @Test
    @WithAnonymousUser
    void getAllTasksForAdmin_anonymousUser_isDenied() {

        assertThrows(
                AccessDeniedException.class,
                () -> taskService.getAllTasksForAdmin()
        );

        verifyNoInteractions(taskRepository);
    }
}
