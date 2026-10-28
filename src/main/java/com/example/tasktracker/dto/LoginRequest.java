package com.example.tasktracker.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank(message = "Имя пользователя обязательно.")
    private String username;
    @NotBlank(message = "Пароль обязателен.")
    private String password;

    public LoginRequest(){

    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
