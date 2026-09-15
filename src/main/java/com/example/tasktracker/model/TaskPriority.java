package com.example.tasktracker.model;

public enum TaskPriority {
    LOW, MEDIUM, HIGH, CRITICAL;



    public boolean requiresManagerApproval(){
        return this == HIGH || this == CRITICAL;
    }
}

