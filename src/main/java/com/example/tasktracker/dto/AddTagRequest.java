package com.example.tasktracker.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AddTagRequest {
    @NotBlank
    @Size(min = 3, max = 10)

    private final String tag;

    public AddTagRequest(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
