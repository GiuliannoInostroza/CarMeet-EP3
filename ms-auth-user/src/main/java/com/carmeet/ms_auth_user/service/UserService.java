package com.carmeet.ms_auth_user.service;

import org.springframework.stereotype.Service;

import com.carmeet.ms_auth_user.dto.UserRequestDTO;
import com.carmeet.ms_auth_user.model.User;
import com.carmeet.ms_auth_user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public User createUser(UserRequestDTO dto) {
        log.info("Iniciando registro para el RUT: {}", dto.getRut());

        if (userRepository.existsByRut(dto.getRut())) {
            log.warn("Fallo de registro: El RUT {} ya está en el sistema", dto.getRut());
            throw new IllegalArgumentException("El RUT ya está registrado");
        }

        User user = new User();
        user.setRut(dto.getRut());
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());

        log.info("Usuario creado exitosamente en la base de datos.");
        return userRepository.save(user);
    }
}
