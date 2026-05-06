package com.carmeet.ms_auth_user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carmeet.ms_auth_user.dto.UserRequestDTO;
import com.carmeet.ms_auth_user.model.User;
import com.carmeet.ms_auth_user.service.UserService;

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
        // Devuelve 201 "created"
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
}
