package com.example.tasktracker.service;

import com.example.tasktracker.exception.ConflictException;
import com.example.tasktracker.model.AppUser;
import com.example.tasktracker.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
    }
    public AppUser register(String username, String rawPassword){
        if (userRepository.findByUsername(username).isPresent()){
            throw new ConflictException("Пользователь с таким именем уже существует.");
        }
        String passwordHash = passwordEncoder.encode(rawPassword);
        AppUser user = new AppUser(username,passwordHash,"USER");
        return userRepository.save(user);
    }



    }


