package com.example.tasktracker.repository;


import com.example.tasktracker.model.*;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TaskRepository extends JpaRepository<Task,Long> {
    @Query("SELECT t FROM Task t WHERE(:status IS NULL OR t.status = :status) AND (:priority IS NULL OR t.priority = :priority)")
List<Task> findByStatusAndPriority(@Param("status") TaskStatus status, @Param("priority") TaskPriority priority);
    @Query("SELECT t.priority AS priority, COUNT(t) AS count FROM Task t GROUP BY t.priority")
List<PriorityCount> countTaskByPriority();
    @Query("SELECT t.status AS status,COUNT(t) AS count FROM Task t GROUP BY t.status")
    List<StatusCount> countTaskByStatus();


}
