package com.example.tasktracker.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


class TaskPriorityTest {
   @ParameterizedTest(name= "{0} - обьект должен быть равен true условию.")
   @CsvSource({"" +
           "HIGH", "CRITICAL"})
    public void priority_test(TaskPriority priority){
       assertThat(priority.requiresManagerApproval()).isTrue();

    }
    @ParameterizedTest(name ="{0} - обьект должен быть равен false условию")
    @CsvSource({
            "LOW","MEDIUM"
    })
    public void priority_test_isFalse(TaskPriority priority){
       assertThat(priority.requiresManagerApproval()).isFalse();

    }

}