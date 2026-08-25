package com.example.tasktracker.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
@Entity
@Table(name="tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  int id;
    @Column(nullable = false, length = 100)
    private  String title;
    @Column(nullable = false,length = 100)
    private  String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private  TaskPriority priority;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private  TaskStatus status;
    @Transient
    private   Set<String> tags;
    @Column(nullable = false,updatable = false)
    private LocalDateTime created_at;

    public Task( String title, String description, TaskPriority priority, TaskStatus status, Set<String> tags) {

        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.tags = new HashSet<>(tags);
        this.created_at=LocalDateTime.now();
    }

    protected Task() {

    }

    public Set<String> getTags() {
        return tags;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
    public void setPriority(TaskPriority priority){
        this.priority=priority;
    }


}
