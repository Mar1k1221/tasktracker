package com.example.tasktracker.config;

import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {



@Bean
    public SecurityFilterChain api(HttpSecurity http) throws Exception{
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST,"/api/auth/**")
                        .permitAll()
                        .requestMatchers("/api/tasks","/api/tasks/**")
                        .authenticated()
                        .anyRequest()
                        .denyAll()
                )
                .httpBasic(Customizer.withDefaults())
                .build();


//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/**")
//                        .permitAll()
//                        .requestMatchers("/api/tasks","/api/tasks/**")
//                        .authenticated()
//                        .anyRequest()
//                        .denyAll()
//                ) Ожидаем 200ок
//                .httpBasic(Customizer.withDefaults())
//                .build();

//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/tasks", "/api/tasks/**")
//                        .authenticated()
//                        .requestMatchers("/api/**")
//                        .permitAll()
//                        .anyRequest()
//                        .denyAll()
//                )
//                .httpBasic(Customizer.withDefaults())
//                .build(); Ожидаем 401

    }
}
