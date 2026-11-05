
package com.example.tasktracker.repository;

import com.example.tasktracker.model.AppUser;
import com.example.tasktracker.model.Task;
import com.example.tasktracker.model.TaskPriority;
import com.example.tasktracker.model.TaskStatus;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Testcontainers
@Tag("integration")
class TaskRepositoryTest {

    private static final String OWNER_USERNAME = "repository_test_user";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private EntityManager entityManager;

    private AppUser createOwner(String username) {
        AppUser owner = new AppUser(username, "hashed-password", "USER");
        return testEntityManager.persist(owner);
    }

    private Task createTask(
            AppUser owner,
            String title,
            TaskPriority priority,
            TaskStatus status
    ) {
        Task task = new Task(
                null,
                title,
                "desc",
                priority,
                status,
                new HashSet<>()
        );

        task.setOwner(owner);

        return testEntityManager.persist(task);
    }

    @Test
    @DisplayName("1. Поиск по статусу возвращает только задачи с этим статусом")
    void findByStatus_returnsOnlyMatchingTasks() {

        AppUser owner = createOwner(OWNER_USERNAME);

        createTask(owner, "Task A", TaskPriority.HIGH, TaskStatus.IN_PROGRESS);
        createTask(owner, "Task B", TaskPriority.LOW, TaskStatus.DONE);
        createTask(owner, "Task C", TaskPriority.MEDIUM, TaskStatus.IN_PROGRESS);

        testEntityManager.flush();
        testEntityManager.clear();

        List<Task> tasks = taskRepository.findByStatus(TaskStatus.IN_PROGRESS);

        assertThat(tasks)
                .hasSize(2)
                .allMatch(task -> task.getStatus() == TaskStatus.IN_PROGRESS)
                .extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Task A", "Task C");
    }

    @Test
    @DisplayName("2. Поиск по части названия не зависит от регистра и работает с кириллицей")
    void findByTitleContainingIgnoreCase_handlesCyrillicAndCase() {

        AppUser owner = createOwner(OWNER_USERNAME);

        createTask(owner, "Переписать отчет", TaskPriority.HIGH, TaskStatus.IN_PROGRESS);
        createTask(owner, "Сдать ОТЧЕТ директору", TaskPriority.LOW, TaskStatus.IN_PROGRESS);
        createTask(owner, "Починить кофемашину", TaskPriority.LOW, TaskStatus.IN_PROGRESS);

        testEntityManager.flush();
        testEntityManager.clear();

        var result = taskRepository.findByTitleContainingIgnoreCase(
                "оТчЕт",
                org.springframework.data.domain.PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(2);

        assertThat(result.getContent())
                .extracting(Task::getTitle)
                .containsExactlyInAnyOrder(
                        "Переписать отчет",
                        "Сдать ОТЧЕТ директору"
                );
    }

    @Test
    @DisplayName("3. Статистика по статусам совпадает с ручным SQL GROUP BY")
    void countTasksByStatusGrouped_matchesManualGroupBy() {

        AppUser owner = createOwner(OWNER_USERNAME);

        createTask(owner, "Task 1", TaskPriority.LOW, TaskStatus.IN_PROGRESS);
        createTask(owner, "Task 2", TaskPriority.LOW, TaskStatus.IN_PROGRESS);
        createTask(owner, "Task 3", TaskPriority.LOW, TaskStatus.DONE);

        testEntityManager.flush();
        testEntityManager.clear();

        var repoStats = taskRepository.countTasksByStatusGrouped()
                .stream()
                .collect(Collectors.toMap(
                        projection -> projection.getStatus().name(),
                        TaskRepository.TaskStatusCountProjection::getCount
                ));

        @SuppressWarnings("unchecked")
        List<Object[]> rawStats = entityManager.createNativeQuery(
                        "SELECT status, count(*) FROM tasks GROUP BY status"
                )
                .getResultList();

        Map<String, Long> manualSqlStats = rawStats.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).longValue()
                ));

        assertThat(repoStats).isEqualTo(manualSqlStats);
        assertThat(repoStats.get("IN_PROGRESS")).isEqualTo(2L);
        assertThat(repoStats.get("DONE")).isEqualTo(1L);
    }

    @Test
    @DisplayName("4. Удаление задачи удаляет её теги")
    void deleteTask_cascadesDeletionToTaskTagsTable() {

        AppUser owner = createOwner(OWNER_USERNAME);

        Task task = new Task(
                null,
                "Удаляемая задача",
                "desc",
                TaskPriority.HIGH,
                TaskStatus.IN_PROGRESS,
                new HashSet<>()
        );

        task.setOwner(owner);
        task.getTags().add("backend");
        task.getTags().add("urgent");

        Task saved = testEntityManager.persistAndFlush(task);
        Long taskId = saved.getId();

        testEntityManager.clear();

        Number tagsBefore = (Number) entityManager.createNativeQuery(
                        "SELECT count(*) FROM task_tags WHERE task_id = :taskId"
                )
                .setParameter("taskId", taskId)
                .getSingleResult();

        assertThat(tagsBefore.intValue()).isEqualTo(2);

        taskRepository.deleteById(taskId);

        testEntityManager.flush();
        testEntityManager.clear();

        Number tagsAfter = (Number) entityManager.createNativeQuery(
                        "SELECT count(*) FROM task_tags WHERE task_id = :taskId"
                )
                .setParameter("taskId", taskId)
                .getSingleResult();

        assertThat(tagsAfter.intValue()).isZero();
    }

    @Test
    @DisplayName("5. Повторная пара задача-тег нарушает ограничение UNIQUE")
    void duplicateTaskTagPair_throwsDataIntegrityViolationException() {

        AppUser owner = createOwner(OWNER_USERNAME);

        Task task = new Task(
                null,
                "Задача под дубликат",
                "desc",
                TaskPriority.LOW,
                TaskStatus.IN_PROGRESS,
                new HashSet<>()
        );

        task.setOwner(owner);

        Task saved = testEntityManager.persistAndFlush(task);
        Long taskId = saved.getId();

        entityManager.createNativeQuery(
                        "INSERT INTO task_tags (task_id, tag) VALUES (:taskId, :tag)"
                )
                .setParameter("taskId", taskId)
                .setParameter("tag", "java")
                .executeUpdate();

        assertThatThrownBy(() -> {
            entityManager.createNativeQuery(
                            "INSERT INTO task_tags (task_id, tag) VALUES (:taskId, :tag)"
                    )
                    .setParameter("taskId", taskId)
                    .setParameter("tag", "java")
                    .executeUpdate();

            entityManager.flush();
        })
                .hasRootCauseInstanceOf(org.postgresql.util.PSQLException.class)
                .hasMessageContaining("duplicate key value violates unique constraint");
    }

    @Test
    @DisplayName("6. Поиск задач владельца с тегами использует EntityGraph")
    void findByOwnerAndStatusAndPriorityOptional_withTags_doesNotProduceQueryPerTask() {

        AppUser owner = createOwner(OWNER_USERNAME);

        for (int i = 0; i < 3; i++) {
            Task task = new Task(
                    null,
                    "Задача " + i,
                    "desc",
                    TaskPriority.LOW,
                    TaskStatus.IN_PROGRESS,
                    new HashSet<>()
            );

            task.setOwner(owner);
            task.getTags().add("tag" + i);
            task.getTags().add("general");

            testEntityManager.persist(task);
        }

        testEntityManager.flush();
        testEntityManager.clear();

        Statistics statistics = entityManager.unwrap(Session.class)
                .getSessionFactory()
                .getStatistics();

        statistics.clear();

        taskRepository.findByOwnerAndStatusAndPriorityOptional(
                        OWNER_USERNAME,
                        null,
                        null
                )
                .forEach(task -> assertThat(task.getTags()).isNotEmpty());

        assertThat(statistics.getPrepareStatementCount())
                .isLessThanOrEqualTo(2L);
    }

    @Test
    @DisplayName("7. Поиск задач возвращает только задачи указанного владельца")
    void findByOwnerAndStatusAndPriorityOptional_returnsOnlyOwnerTasks() {

        AppUser marat = createOwner("marat");
        AppUser alex = createOwner("alex");

        createTask(marat, "Задача Марата", TaskPriority.HIGH, TaskStatus.NEW);
        createTask(alex, "Задача Алекса", TaskPriority.HIGH, TaskStatus.NEW);

        testEntityManager.flush();
        testEntityManager.clear();

        List<Task> result = taskRepository.findByOwnerAndStatusAndPriorityOptional(
                "marat",
                null,
                null
        );

        assertThat(result)
                .hasSize(1)
                .allMatch(task -> task.getOwner().getUsername().equals("marat"))
                .extracting(Task::getTitle)
                .containsExactly("Задача Марата");
    }
}
