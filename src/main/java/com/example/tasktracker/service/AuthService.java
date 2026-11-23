package com.example.tasktracker.service;

import com.example.tasktracker.dto.LoginResponse;
import com.example.tasktracker.exception.ConflictException;
import com.example.tasktracker.model.AppUser;
import com.example.tasktracker.repository.UserRepository;

import com.example.tasktracker.security.JwtProperties;
import com.example.tasktracker.security.JwtService;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import org.slf4j.Logger;



@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,JwtService jwtService,JwtProperties jwtProperties){
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
        this.jwtService=jwtService;
        this.jwtProperties=jwtProperties;
    }
    public AppUser register(String username, String rawPassword){
        if (userRepository.findByUsername(username).isPresent()){
            throw new ConflictException("Пользователь с таким именем уже существует.");
        }
        String passwordHash = passwordEncoder.encode(rawPassword);
        AppUser user = new AppUser(username,passwordHash,"USER");
        AppUser saved = userRepository.save(user);
        log.info("Пользователь зарегистрирован username={}",saved.getUsername());
        return saved;
    }
    public LoginResponse login(String username, String rawPassword){
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Неудачная попытка входа username={}",username);

        return new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Неверное имя пользователя или пароль.");
    });

        if (!passwordEncoder.matches(rawPassword,user.getPasswordHash())){
            log.warn("Неудачная попытка входа username={}",username);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Неверное имя пользователя или пароль.");

        }
        String token = jwtService.issue(user.getUsername(), List.of(user.getRole()));

        return new LoginResponse(token,jwtProperties.lifetime().toSeconds());
    }



    }


