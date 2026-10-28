package com.example.tasktracker.service;

import com.example.tasktracker.dto.LoginResponse;
import com.example.tasktracker.exception.ConflictException;
import com.example.tasktracker.model.AppUser;
import com.example.tasktracker.repository.UserRepository;

import com.example.tasktracker.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,JwtService jwtService){
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
        this.jwtService=jwtService;
    }
    public AppUser register(String username, String rawPassword){
        if (userRepository.findByUsername(username).isPresent()){
            throw new ConflictException("Пользователь с таким именем уже существует.");
        }
        String passwordHash = passwordEncoder.encode(rawPassword);
        AppUser user = new AppUser(username,passwordHash,"USER");
        return userRepository.save(user);
    }
    public LoginResponse login(String username, String rawPassword){
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Неверное имя пользователя или пароль."));

        if (!passwordEncoder.matches(rawPassword,user.getPasswordHash())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Неверное имя пользователя или пароль.");

        }
        String token = jwtService.issue(user.getUsername(), List.of(user.getRole()));

        return new LoginResponse(token,900L);
    }



    }


