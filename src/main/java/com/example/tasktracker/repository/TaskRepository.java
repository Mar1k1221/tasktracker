package com.example.tasktracker.repository;


import com.example.tasktracker.model.Task;
import com.example.tasktracker.model.TaskPriority;
import com.example.tasktracker.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByStatusAndPriority(TaskStatus status, TaskPriority priority);
    @Query("SELECT t FROM Task t WHERE lower(t.title) LIKE lower(concat('%',:title,'%') ) ")
    Page<Task> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);

}
