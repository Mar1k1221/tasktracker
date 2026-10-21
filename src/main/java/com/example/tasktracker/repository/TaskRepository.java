package com.example.tasktracker.repository;

import com.example.tasktracker.model.PriorityCount;
import com.example.tasktracker.model.StatusCount;
import com.example.tasktracker.model.Task;
import com.example.tasktracker.model.TaskPriority;
import com.example.tasktracker.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(TaskStatus status);

    @EntityGraph(attributePaths = {"tags"})
    @Query("SELECT t FROM Task t WHERE (:status IS NULL OR t.status = :status) AND (:priority IS NULL OR t.priority = :priority)")
    List<Task> findByStatusAndPriorityOptional(@Param("status") TaskStatus status, @Param("priority") TaskPriority priority);

    Page<Task> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"tags"})
    Page<Task> findAll(Pageable pageable);

    @Query("SELECT t.priority AS priority, COUNT(t) AS count FROM Task t GROUP BY t.priority")
    List<PriorityCount> countTaskByPriority();

    @Query("SELECT t.status AS status, COUNT(t) AS count FROM Task t GROUP BY t.status")
    List<StatusCount> countTaskByStatus();

    @Query("SELECT t.status AS status, COUNT(t) AS count FROM Task t GROUP BY t.status")
    List<TaskStatusCountProjection> countTasksByStatusGrouped();

    interface TaskStatusCountProjection {
        TaskStatus getStatus();
        long getCount();
    }
}
