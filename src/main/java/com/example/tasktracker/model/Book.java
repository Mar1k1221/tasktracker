package com.example.tasktracker.model;

import jakarta.persistence.*;

@Entity
@Table(name="books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(length = 100,nullable = false)
    private String title;
    @Column(nullable = false,length = 100)
    private String author;
    @Column(nullable = false,length = 100)
    @Enumerated(EnumType.STRING)
    private TaskStatus status;
    @Column(nullable = false)
    private int year;

    public Book( String title, TaskStatus status, String author,int year) {
        this.title = title;
        this.status = status;
        this.author = author;
        this.year=year;

    }
protected Book (){

}
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public int getYear() {
        return year;
    }
}
