package com.example.tasktracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "Имя пользователя обязательно.")
    @Size(min = 5,max = 50, message = "Имя пользователя должно содержать более 5-ти символов и не больше 50")
    private String username;
    @NotBlank
    @Size(min = 8,max = 100, message = "Пароль должен содержать 8 символов и не больше 100.")
    private String password;
    public RegisterRequest(){

    }
    public String getUsername(){
        return username;
    }
    public String getPassword(){
        return password;

    }
    public void setUsername(String username){
        this.username=username;
    }
    public void setPassword(String password){
        this.password=password;
    }


}
