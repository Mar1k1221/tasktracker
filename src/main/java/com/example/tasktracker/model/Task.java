package com.example.tasktracker.model;

import jakarta.persistence.*;


import java.util.HashSet;
import java.util.Set;
@Entity
@Table(name ="tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;
    @Column(nullable = false,length = 100)
    private  String title;
    @Column(length = 500)
    private  String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private  TaskPriority priority;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private  TaskStatus status;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="task_tags",joinColumns = @JoinColumn(name="task_id"))
    @Column(name = "tag")
    private   Set<String> tags;

    public Task(Long id, String title, String description, TaskPriority priority, TaskStatus status, Set<String> tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.tags = new HashSet<>(tags);
    }
    public Task(){

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

    public long getId() {
        return id;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
    public void setPriority(TaskPriority priority){
        this.priority=priority;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

public  boolean isEditable(TaskStatus status){
    return status == TaskStatus.NEW;
}
}


