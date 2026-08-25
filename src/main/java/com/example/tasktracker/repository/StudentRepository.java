package com.example.tasktracker.repository;

import com.example.tasktracker.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student,Integer> {
List<Student> findByGroupName(String group);
Optional<Student> findByRecordBookNumber(String recordNumber);
}
