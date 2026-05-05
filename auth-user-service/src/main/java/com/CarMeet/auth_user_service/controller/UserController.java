package com.CarMeet.auth_user_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CarMeet.auth_user_service.dto.UserRequestDTO;
import com.CarMeet.auth_user_service.model.User;
import com.CarMeet.auth_user_service.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @PostMapping
    // @Valid activa las validaciones del DTO
    public ResponseEntity<User> register(@Valid @RequestBody UserRequestDTO dto) {
        User createdUser = service.createUser(dto);
        // Devuelve 201 CREATED
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser); 
    }
}
