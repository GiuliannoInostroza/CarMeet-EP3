package com.CarMeet.auth_user_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CarMeet.auth_user_service.dto.UserRequestDTO;
import com.CarMeet.auth_user_service.model.User;
import com.CarMeet.auth_user_service.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    @Autowired
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

        log.info("Usuario creado existosamente en la base de datos.");
        return userRepository.save(user);
    }
}
