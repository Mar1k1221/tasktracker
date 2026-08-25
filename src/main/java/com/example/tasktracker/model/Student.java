package com.example.tasktracker.model;

import jakarta.persistence.*;
import jakarta.persistence.Id;

@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false, length = 50)
    private String groupName;

    @Column(nullable = false, unique = true, length = 30)
    private String recordBookNumber;

    protected Student() {
    }

    public Student(int id, String firstName, String lastName, int age, String groupName, String recordBookNumber) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.groupName = groupName;
        this.recordBookNumber = recordBookNumber;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getAge() {
        return age;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getRecordBookNumber() {
        return recordBookNumber;
    }
}