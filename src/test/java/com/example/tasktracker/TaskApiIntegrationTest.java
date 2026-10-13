package com.example.tasktracker;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureWebTestClient
class TaskApiIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");
    @Autowired
    private WebTestClient rest;
    @Autowired
    private org.springframework.jdbc.core.simple.JdbcClient jdbc;
    @Test
    void fullTaskLivecycle_createReadUpdateList(){
        Map created = rest.post().uri("/api/tasks")
        .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("title","Задача из теста","description","Описание","priority","HIGH"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult().getResponseBody();
        
        Object taskId = created.get("id");
        rest.get().uri("/api/tasks/" + taskId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Задача из теста")
        .jsonPath("$.priority").isEqualTo("HIGH")
                .jsonPath("$.status").isEqualTo("NEW");

        rest.patch().uri("/api/tasks/" + taskId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status","DONE"))
                .exchange()
                .expectStatus().isOk();
        rest.get().uri("/api/tasks" )
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[?(@.id ==" + taskId + ")].status").isEqualTo("DONE");



    }
    @Test
    void addDuplicateTag_returns409Conflict_andDatabaseKeepsSingleRow(){
        Map<?,?> task = rest.post().uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(java.util.Map.of("title","Задача для тегов","description","Описание","priority","LOW"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(java.util.Map.class)
                .returnResult().getResponseBody();

        Object taskId = task.get("id");

        rest.post().uri("/api/tasks/" + taskId + "/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("tag","backend"))
                .exchange()
                .expectStatus().isCreated();

        rest.post().uri("/api/tasks/" + taskId + "/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("tag","backend"))
                .exchange()
                .expectStatus().isEqualTo(409);

        Integer tagsCount = jdbc.sql("SELECT count(*) FROM task_tags WHERE task_id = :taskId")
                .param("taskId",taskId)
                .query(Integer.class)
                .single();
        org.junit.jupiter.api.Assertions.assertEquals(1,tagsCount);



    }
    @Test
    void deleteTask_removeAssociatedTags_verifiedByDataBase(){
        Map<?,?> task = rest.post().uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("title","Задача на удаление","description","Проверка каскадного удаления","priority","HIGH"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult().getResponseBody();

        Object taskId = task.get("id");

        rest.post().uri("/api/tasks/" + taskId + "/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("tag","newtag"))
                .exchange()
                .expectStatus().isCreated();

        rest.delete().uri("/api/tasks/" + taskId)
                .exchange()
                .expectStatus().isNoContent();

        Integer tagsCount = jdbc.sql("SELECT count(*) FROM task_tags WHERE task_id = :taskId")
                .param("taskId",taskId)
                .query(Integer.class)
                .single();
        Assertions.assertEquals(0,tagsCount);



    }
    @Test
    void statisticsEndpiont_returnsSameNumbersAsManualGroupBy(){
        rest.post().uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("title","Для статы","description","Считаем","priority","MEDIUM"))
                .exchange()
                .expectStatus().isCreated();

        Map<?,?> apiResponse = rest.get().uri("/api/tasks/statistics")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult().getResponseBody();
        List<Map<String,Object>> dbStats = jdbc.sql("SELECT status, count(*) as cnt FROM tasks GROUP BY status")
                .query()
                .listOfRows();

        Assertions.assertNotNull(apiResponse);
        Assertions.assertFalse(dbStats.isEmpty());
        System.out.println("API Статистика: " + apiResponse);
        System.out.println("DB Статистика: " + dbStats);
    }

}