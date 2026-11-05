
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
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "security.jwt.secret=0123456789abcdef0123456789abcdef"
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

    private static final String TEST_PASSWORD = "Password123!";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JdbcClient jdbc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private WebTestClient rest;

    private String username;

    // Каждый тест получает отдельного пользователя и настоящий JWT.
    @BeforeEach
    void configureAuthenticatedClient() {

        username = "integration_" +
                UUID.randomUUID().toString().substring(0, 8);

        registerUser(username, TEST_PASSWORD);

        String token = loginUser(username, TEST_PASSWORD);

        rest = authenticatedClient(token);
    }

    // ---------------------------------------------------------
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ---------------------------------------------------------

    private Map<?, ?> registerUser(String username, String password) {

        Map<?, ?> response = webTestClient.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "username", username,
                        "password", password
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(response);

        // Пароль и хеш не должны возвращаться клиенту.
        Assertions.assertFalse(response.containsKey("password"));
        Assertions.assertFalse(response.containsKey("passwordHash"));
        Assertions.assertFalse(response.containsKey("password_hash"));

        return response;
    }

    private String loginUser(String username, String password) {

        Map<?, ?> response = webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "username", username,
                        "password", password
                ))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(response);

        Assertions.assertNotNull(response.get("token"));
        Assertions.assertNotNull(response.get("expiresIn"));

        Assertions.assertFalse(response.containsKey("password"));
        Assertions.assertFalse(response.containsKey("passwordHash"));
        Assertions.assertFalse(response.containsKey("password_hash"));

        return (String) response.get("token");
    }

    private WebTestClient authenticatedClient(String token) {

        return webTestClient.mutate()
                .defaultHeaders(headers ->
                        headers.setBearerAuth(token))
                .build();
    }

    private String registerAndLogin(String username) {

        registerUser(username, TEST_PASSWORD);

        return loginUser(username, TEST_PASSWORD);
    }

    private String createTask(WebTestClient client, String title) {

        Map<?, ?> response = client.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "title", title,
                        "description", "Integration test description",
                        "priority", "HIGH"
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.get("id"));

        return String.valueOf(response.get("id"));
    }

    private long countTasksById(String taskId) {

        return jdbc.sql(
                        "SELECT count(*) FROM tasks WHERE id = :taskId"
                )
                .param("taskId", Long.parseLong(taskId))
                .query(Long.class)
                .single();
    }

    // ---------------------------------------------------------
    // 1. ПОЛНЫЙ ЖИЗНЕННЫЙ ЦИКЛ ЗАДАЧИ
    // ---------------------------------------------------------

    @Test
    void fullTaskLifecycle_createReadUpdateList() {

        Map<?, ?> created = rest.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "title", "Task from integration test",
                        "description", "Description",
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
                .jsonPath("$.title")
                .isEqualTo("Task from integration test")
                .jsonPath("$.priority")
                .isEqualTo("HIGH")
                .jsonPath("$.status")
                .isEqualTo("NEW");

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

    // ---------------------------------------------------------
    // 2. НЕДОПУСТИМЫЕ ПЕРЕХОДЫ СТАТУСОВ
    // ---------------------------------------------------------

    @Test
    void forbiddenStatusTransitions_return400() {

        String taskId = createTask(rest, "Status rules");

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

    // ---------------------------------------------------------
    // 3. ДУБЛИРУЮЩИЕСЯ ТЕГИ
    // ---------------------------------------------------------

    @Test
    void addDuplicateTag_returns409_andDatabaseKeepsSingleRow() {

        String taskId = createTask(rest, "Task for tags");

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
                .param("taskId", Long.parseLong(taskId))
                .query(Integer.class)
                .single();

        Assertions.assertEquals(1, tagsCount);
    }

    // ---------------------------------------------------------
    // 4. УДАЛЕНИЕ ЗАДАЧИ УДАЛЯЕТ СВЯЗАННЫЕ ТЕГИ
    // ---------------------------------------------------------

    @Test
    void deleteTask_removeAssociatedTags() {

        String taskId = createTask(rest, "Task for deletion");

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
                .param("taskId", Long.parseLong(taskId))
                .query(Integer.class)
                .single();

        Assertions.assertEquals(0, tagsCount);
    }

    // ---------------------------------------------------------
    // 5. СТАТИСТИКА ТОЛЬКО ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ
    // ---------------------------------------------------------

    @Test
    void statistics_returnsOnlyCurrentUsersNumbers() {

        rest.post()
                .uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "title", "Statistics task",
                        "description", "Check statistics",
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
                        "SELECT status, count(*) AS cnt " +
                                "FROM tasks " +
                                "WHERE owner_id = " +
                                "(SELECT id FROM users WHERE username = :username) " +
                                "GROUP BY status"
                )
                .param("username", username)
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

    // ---------------------------------------------------------
    // 6. БЕЗ ТОКЕНА ДОСТУПА К ЗАДАЧАМ НЕТ
    // ---------------------------------------------------------

    @Test
    void getTasks_withoutToken_returns401() {

        webTestClient.get()
                .uri("/api/tasks")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ---------------------------------------------------------
    // 7. ПОЛЬЗОВАТЕЛЬ ВИДИТ ТОЛЬКО СВОИ ЗАДАЧИ
    // ---------------------------------------------------------

    @Test
    void getTasks_returnsOnlyOwnTasks() {

        createTask(rest, "Anna task");

        String bobToken = registerAndLogin(
                "bob_" + UUID.randomUUID().toString().substring(0, 8)
        );

        WebTestClient bobClient = authenticatedClient(bobToken);

        createTask(bobClient, "Bob task");

        List<Map> tasks = rest.get()
                .uri("/api/tasks")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Map.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(tasks);

        List<String> titles = tasks.stream()
                .map(task -> (String) task.get("title"))
                .toList();

        Assertions.assertTrue(titles.contains("Anna task"));
        Assertions.assertFalse(titles.contains("Bob task"));
    }

    // ---------------------------------------------------------
    // 8. ЧУЖУЮ ЗАДАЧУ НЕЛЬЗЯ ПРОЧИТАТЬ
    // ---------------------------------------------------------

    @Test
    void getForeignTask_returns404_andTaskStillExists() {

        String taskId = createTask(rest, "Anna private task");

        String bobToken = registerAndLogin(
                "bob_" + UUID.randomUUID().toString().substring(0, 8)
        );

        WebTestClient bobClient = authenticatedClient(bobToken);

        bobClient.get()
                .uri("/api/tasks/" + taskId)
                .exchange()
                .expectStatus().isNotFound();

        Assertions.assertEquals(1L, countTasksById(taskId));
    }

    // ---------------------------------------------------------
    // 9. ЧУЖУЮ ЗАДАЧУ НЕЛЬЗЯ ИЗМЕНИТЬ
    // ---------------------------------------------------------

    @Test
    void updateForeignTask_returns404_andDataDoesNotChange() {

        String taskId = createTask(rest, "Original title");

        String bobToken = registerAndLogin(
                "bob_" + UUID.randomUUID().toString().substring(0, 8)
        );

        WebTestClient bobClient = authenticatedClient(bobToken);

        bobClient.put()
                .uri("/api/tasks/" + taskId + "/update")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "title", "Hacked title",
                        "description", "Hacked description"
                ))
                .exchange()
                .expectStatus().isNotFound();

        String actualTitle = jdbc.sql(
                        "SELECT title FROM tasks WHERE id = :taskId"
                )
                .param("taskId", Long.parseLong(taskId))
                .query(String.class)
                .single();

        Assertions.assertEquals("Original title", actualTitle);
    }

    // ---------------------------------------------------------
    // 10. ЧУЖУЮ ЗАДАЧУ НЕЛЬЗЯ УДАЛИТЬ
    // ---------------------------------------------------------

    @Test
    void deleteForeignTask_returns404_andTaskStillExists() {

        String taskId = createTask(rest, "Anna task");

        String bobToken = registerAndLogin(
                "bob_" + UUID.randomUUID().toString().substring(0, 8)
        );

        WebTestClient bobClient = authenticatedClient(bobToken);

        bobClient.delete()
                .uri("/api/tasks/" + taskId)
                .exchange()
                .expectStatus().isNotFound();

        Assertions.assertEquals(1L, countTasksById(taskId));
    }

    // ---------------------------------------------------------
    // 11. ЧУЖУЮ ЗАДАЧУ НЕЛЬЗЯ ПЕРЕВЕСТИ В ДРУГОЙ СТАТУС
    // ---------------------------------------------------------

    @Test
    void changeForeignTaskStatus_returns404_andStatusUnchanged() {

        String taskId = createTask(rest, "Anna status task");

        String bobToken = registerAndLogin(
                "bob_" + UUID.randomUUID().toString().substring(0, 8)
        );

        WebTestClient bobClient = authenticatedClient(bobToken);

        bobClient.patch()
                .uri("/api/tasks/" + taskId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", "IN_PROGRESS"))
                .exchange()
                .expectStatus().isNotFound();

        String actualStatus = jdbc.sql(
                        "SELECT status FROM tasks WHERE id = :taskId"
                )
                .param("taskId", Long.parseLong(taskId))
                .query(String.class)
                .single();

        Assertions.assertEquals("NEW", actualStatus);
    }

    // ---------------------------------------------------------
    // 12. ЧУЖУЮ ЗАДАЧУ НЕЛЬЗЯ ИЗМЕНИТЬ ПО ПРИОРИТЕТУ
    // ---------------------------------------------------------

    @Test
    void changeForeignTaskPriority_returns404_andPriorityUnchanged() {

        String taskId = createTask(rest, "Anna priority task");

        String bobToken = registerAndLogin(
                "bob_" + UUID.randomUUID().toString().substring(0, 8)
        );

        WebTestClient bobClient = authenticatedClient(bobToken);

        bobClient.patch()
                .uri("/api/tasks/" + taskId + "/priority")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("priority", "LOW"))
                .exchange()
                .expectStatus().isNotFound();

        String actualPriority = jdbc.sql(
                        "SELECT priority FROM tasks WHERE id = :taskId"
                )
                .param("taskId", Long.parseLong(taskId))
                .query(String.class)
                .single();

        Assertions.assertEquals("HIGH", actualPriority);
    }

    // ---------------------------------------------------------
    // 13. НЕЛЬЗЯ ДОБАВИТЬ ТЕГ К ЧУЖОЙ ЗАДАЧЕ
    // ---------------------------------------------------------

    @Test
    void addTagToForeignTask_returns404_andTagNotCreated() {

        String taskId = createTask(rest, "Anna tag task");

        String bobToken = registerAndLogin(
                "bob_" + UUID.randomUUID().toString().substring(0, 8)
        );

        WebTestClient bobClient = authenticatedClient(bobToken);

        bobClient.post()
                .uri("/api/tasks/" + taskId + "/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("tag", "hacked"))
                .exchange()
                .expectStatus().isNotFound();

        Integer tagCount = jdbc.sql(
                        "SELECT count(*) FROM task_tags WHERE task_id = :taskId"
                )
                .param("taskId", Long.parseLong(taskId))
                .query(Integer.class)
                .single();

        Assertions.assertEquals(0, tagCount);
    }

    // ---------------------------------------------------------
    // 14. USER НЕ ИМЕЕТ ДОСТУПА К АДМИНСКОМУ СПИСКУ
    // ---------------------------------------------------------

    @Test
    void adminTasks_userRole_returns403() {

        rest.get()
                .uri("/api/admin/tasks")
                .exchange()
                .expectStatus().isForbidden();
    }

    // ---------------------------------------------------------
    // 15. ADMIN МОЖЕТ ВИДЕТЬ ВСЕ ЗАДАЧИ
    // ---------------------------------------------------------

    @Test
    void adminTasks_adminRole_returns200() {

        jdbc.sql(
                        "UPDATE users SET role = 'ADMIN' WHERE username = :username"
                )
                .param("username", username)
                .update();

        // Получаем новый JWT: роль записана в токен при входе.
        String adminToken = loginUser(username, TEST_PASSWORD);

        WebTestClient adminClient = authenticatedClient(adminToken);

        adminClient.get()
                .uri("/api/admin/tasks")
                .exchange()
                .expectStatus().isOk();
    }

    // ---------------------------------------------------------
    // 16. ПАРОЛЬ ХРАНИТСЯ ХЕШЕМ, А НЕ ОТКРЫТЫМ ТЕКСТОМ
    // ---------------------------------------------------------

    @Test
    void registration_passwordIsHashedAndNotReturned() {

        String newUsername = "secure_" +
                UUID.randomUUID().toString().substring(0, 8);

        Map<?, ?> response = registerUser(
                newUsername,
                TEST_PASSWORD
        );

        Assertions.assertNotNull(response.get("id"));
        Assertions.assertEquals(newUsername, response.get("username"));
        Assertions.assertEquals("USER", response.get("role"));

        String passwordHash = jdbc.sql(
                        "SELECT password_hash FROM users WHERE username = :username"
                )
                .param("username", newUsername)
                .query(String.class)
                .single();

        Assertions.assertNotEquals(TEST_PASSWORD, passwordHash);

        Assertions.assertTrue(
                passwordHash.startsWith("$2a$") ||
                        passwordHash.startsWith("$2b$") ||
                        passwordHash.startsWith("$2y$")
        );

        Assertions.assertTrue(
                passwordEncoder.matches(TEST_PASSWORD, passwordHash)
        );
    }

    // ---------------------------------------------------------
    // 17. ПОВТОРНАЯ РЕГИСТРАЦИЯ ВОЗВРАЩАЕТ 409
    // ---------------------------------------------------------

    @Test
    void registerDuplicateUsername_returns409_andDatabaseKeepsOneUser() {

        webTestClient.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "username", username,
                        "password", TEST_PASSWORD
                ))
                .exchange()
                .expectStatus().isEqualTo(409);

        Integer userCount = jdbc.sql(
                        "SELECT count(*) FROM users WHERE username = :username"
                )
                .param("username", username)
                .query(Integer.class)
                .single();

        Assertions.assertEquals(1, userCount);
    }

    // ---------------------------------------------------------
    // 18. НЕВЕРНЫЙ ПАРОЛЬ И НЕСУЩЕСТВУЮЩИЙ USERNAME
    // ДАЮТ ОДИНАКОВЫЙ 401
    // ---------------------------------------------------------

    @Test
    void login_wrongUsernameAndWrongPassword_returnsSame401Error() {

        Map<?, ?> wrongUsername = webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "username", "missing_" +
                                UUID.randomUUID().toString().substring(0, 8),
                        "password", TEST_PASSWORD
                ))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        Map<?, ?> wrongPassword = webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "username", username,
                        "password", "WrongPassword123!"
                ))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(wrongUsername);
        Assertions.assertNotNull(wrongPassword);

        Assertions.assertEquals(
                wrongUsername.get("code"),
                wrongPassword.get("code")
        );

        Assertions.assertEquals(
                wrongUsername.get("message"),
                wrongPassword.get("message")
        );

        Assertions.assertEquals(
                wrongUsername.get("path"),
                wrongPassword.get("path")
        );
    }

    // ---------------------------------------------------------
    // 19. LOGIN НЕ ВОЗВРАЩАЕТ ПАРОЛЬ И ЕГО ХЕШ
    // ---------------------------------------------------------

    @Test
    void login_responseDoesNotContainPasswordOrPasswordHash() {

        Map<?, ?> response = webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "username", username,
                        "password", TEST_PASSWORD
                ))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(response);

        Assertions.assertNotNull(response.get("token"));

        Assertions.assertFalse(response.containsKey("password"));
        Assertions.assertFalse(response.containsKey("passwordHash"));
        Assertions.assertFalse(response.containsKey("password_hash"));
    }
}
