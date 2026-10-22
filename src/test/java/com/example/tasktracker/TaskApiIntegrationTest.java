package com.example.tasktracker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.jdbc.core.simple.JdbcClient;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.security.user.name=testuser",
                "spring.security.user.password=testpass"
        }
)
@Testcontainers
@AutoConfigureWebTestClient
@Tag("integration")
class TaskApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JdbcClient jdbc;

    private WebTestClient rest;

    @BeforeEach
    void configureAuthenticatedClient() {
        rest = webTestClient.mutate()
                .defaultHeaders(headers ->
                        headers.setBasicAuth("testuser", "testpass"))
                .build();
    }

    @Test
    void fullTaskLifecycle_createReadUpdateList() {

        Map<?, ?> created = rest.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "title", "Задача из теста",
                        "description", "Описание",
                        "priority", "HIGH"
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(created);

        Object taskId = created.get("id");

        rest.get()
                .uri("/api/tasks/" + taskId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Задача из теста")
                .jsonPath("$.priority").isEqualTo("HIGH")
                .jsonPath("$.status").isEqualTo("NEW");

        rest.patch()
                .uri("/api/tasks/" + taskId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", "IN_PROGRESS"))
                .exchange()
                .expectStatus().isOk();

        rest.patch()
                .uri("/api/tasks/" + taskId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", "DONE"))
                .exchange()
                .expectStatus().isOk();

        rest.get()
                .uri("/api/tasks")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[?(@.id ==" + taskId + ")].status")
                .isEqualTo("DONE");
    }

    @Test
    void forbiddenStatusTransitions_return400() {

        Map<?, ?> task = rest.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "title", "Status rules",
                        "description", "Check transitions",
                        "priority", "HIGH"
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(task);

        Object taskId = task.get("id");

        rest.patch()
                .uri("/api/tasks/" + taskId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", "DONE"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code")
                .isEqualTo("INVALID_STATUS_TRANSITION");

        rest.patch()
                .uri("/api/tasks/" + taskId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", "CANCELLED"))
                .exchange()
                .expectStatus().isOk();

        rest.patch()
                .uri("/api/tasks/" + taskId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", "NEW"))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void addDuplicateTag_returns409Conflict_andDatabaseKeepsSingleRow() {

        Map<?, ?> task = rest.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "title", "Задача для тегов",
                        "description", "Описание",
                        "priority", "LOW"
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(task);

        Object taskId = task.get("id");

        rest.post()
                .uri("/api/tasks/" + taskId + "/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("tag", "backend"))
                .exchange()
                .expectStatus().isCreated();

        rest.post()
                .uri("/api/tasks/" + taskId + "/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("tag", "backend"))
                .exchange()
                .expectStatus().isEqualTo(409);

        Integer tagsCount = jdbc.sql(
                        "SELECT count(*) FROM task_tags WHERE task_id = :taskId"
                )
                .param("taskId", taskId)
                .query(Integer.class)
                .single();

        Assertions.assertEquals(1, tagsCount);
    }

    @Test
    void deleteTask_removeAssociatedTags_verifiedByDataBase() {

        Map<?, ?> task = rest.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "title", "Задача на удаление",
                        "description", "Проверка каскадного удаления",
                        "priority", "HIGH"
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(task);

        Object taskId = task.get("id");

        rest.post()
                .uri("/api/tasks/" + taskId + "/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("tag", "newtag"))
                .exchange()
                .expectStatus().isCreated();

        rest.delete()
                .uri("/api/tasks/" + taskId)
                .exchange()
                .expectStatus().isNoContent();

        Integer tagsCount = jdbc.sql(
                        "SELECT count(*) FROM task_tags WHERE task_id = :taskId"
                )
                .param("taskId", taskId)
                .query(Integer.class)
                .single();

        Assertions.assertEquals(0, tagsCount);
    }

    @Test
    void statisticsEndpiont_returnsSameNumbersAsManualGroupBy() {

        rest.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "title", "Для статы",
                        "description", "Считаем",
                        "priority", "MEDIUM"
                ))
                .exchange()
                .expectStatus().isCreated();

        Map<?, ?> apiResponse = rest.get()
                .uri("/api/tasks/statistics")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        List<Map<String, Object>> dbStats = jdbc.sql(
                        "SELECT status, count(*) AS cnt FROM tasks GROUP BY status"
                )
                .query()
                .listOfRows();

        Assertions.assertNotNull(apiResponse);

        Map<String, Long> expectedStatusCounts = new HashMap<>();

        for (Map<String, Object> row : dbStats) {
            expectedStatusCounts.put(
                    (String) row.get("status"),
                    ((Number) row.get("cnt")).longValue()
            );
        }

        Map<String, Object> actualStatusCounts =
                (Map<String, Object>) apiResponse.get("mapStatus");

        Assertions.assertNotNull(actualStatusCounts);

        Assertions.assertEquals(
                expectedStatusCounts.size(),
                actualStatusCounts.size()
        );

        expectedStatusCounts.forEach((status, count) ->
                Assertions.assertEquals(
                        count.longValue(),
                        ((Number) actualStatusCounts.get(status)).longValue()
                )
        );
    }
}