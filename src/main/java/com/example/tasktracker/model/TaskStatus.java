package com.example.tasktracker.model;

public enum TaskStatus {
    NEW, IN_PROGRESS, DONE, CANCELLED;

   public boolean status_transition(TaskStatus taskStatus){
       return switch  (this){
           case NEW -> taskStatus == IN_PROGRESS || taskStatus ==CANCELLED;
           case IN_PROGRESS -> taskStatus == DONE || taskStatus == CANCELLED || taskStatus == NEW;
           case DONE -> taskStatus==IN_PROGRESS || taskStatus == CANCELLED;
           case CANCELLED -> false;
       };
   }


}