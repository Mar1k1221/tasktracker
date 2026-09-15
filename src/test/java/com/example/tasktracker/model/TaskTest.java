package com.example.tasktracker.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;



class TaskTest {

    @Test
    @DisplayName("Внести изменения можно при условияя TaskStatus.NEW.")
  public void isEdit_test(){
        Task task = new Task();

    assertThat(task.isEditable(TaskStatus.NEW)).isTrue();

  }

  @ParameterizedTest(name = "{0}  изменение у данного статуса  запрещено.")
  @CsvSource({"CANCELLED",
          "DONE",
          "IN_PROGRESS"})
  public void isEditFalse_test(TaskStatus status){
      Task task = new Task();
        assertThat(task.isEditable(status)).isFalse();

  }
}