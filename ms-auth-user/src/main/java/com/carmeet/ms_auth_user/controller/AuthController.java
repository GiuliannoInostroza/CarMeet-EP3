package com.carmeet.ms_auth_user.controller;

import com.carmeet.ms_auth_user.dto.*;
import com.carmeet.ms_auth_user.model.Usuario;
import com.carmeet.ms_auth_user.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService service;

    // ── ENDPOINTS PÚBLICOS ────────────────────────────────────────────────────

    /** POST /api/v1/auth/register — Registro de nuevos usuarios (público) */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest req) {
        log.info("POST /api/v1/auth/register - usuario: {}", req.getUsername());

        AuthResponse res = service.register(req);

        return ResponseEntity.status(201).body(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("Usuario registrado con rol: " + res.getRole())
                        .data(res)
                        .build()
        );
    }

    /** POST /api/v1/auth/login — Login (público) */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        log.info("POST /api/v1/auth/login - usuario: {}", req.getUsername());

        AuthResponse res = service.login(req);

        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("Login exitoso - rol: " + res.getRole())
                        .data(res)
                        .build()
        );
    }

    /** POST /api/v1/auth/refresh — Renovar access token (público) */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestBody RefreshRequest req) {

        AuthResponse res = service.refresh(req.getRefreshToken());

        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("Token renovado")
                        .data(res)
                        .build()
        );
    }

    // ── ENDPOINTS AUTENTICADOS ────────────────────────────────────────────────

    /** GET /api/v1/auth/me — Datos del usuario autenticado */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, String>>> me(Authentication auth) {

        String username = auth.getName();
        Usuario user = service.obtenerPorUsername(username);

        Map<String, String> info = Map.of(
                "username", user.getUsername(),
                "role", user.getRole()
        );

        return ResponseEntity.ok(
                ApiResponse.<Map<String, String>>builder()
                        .success(true)
                        .message("Usuario actual")
                        .data(info)
                        .build()
        );
    }

    // ── ENDPOINTS ADMIN ───────────────────────────────────────────────────────

    /** PUT /api/v1/auth/promote/{username} — Solo ADMIN: promover a admin */
    @PutMapping("/promote/{username}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> promote(@PathVariable String username) {

        Usuario user = service.promoverAAdmin(username);

        Map<String, String> info = Map.of(
                "username", user.getUsername(),
                "role", user.getRole()
        );

        return ResponseEntity.ok(
                ApiResponse.<Map<String, String>>builder()
                        .success(true)
                        .message("Usuario promovido a ADMIN exitosamente")
                        .data(info)
                        .build()
        );
    }

    /** PUT /api/v1/auth/demote/{username} — Solo ADMIN: degradar a user */
    @PutMapping("/demote/{username}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> demote(@PathVariable String username) {

        Usuario user = service.degradarAUser(username);

        Map<String, String> info = Map.of(
                "username", user.getUsername(),
                "role", user.getRole()
        );

        return ResponseEntity.ok(
                ApiResponse.<Map<String, String>>builder()
                        .success(true)
                        .message("Usuario degradado a USER exitosamente")
                        .data(info)
                        .build()
        );
    }

    /** GET /api/v1/auth/usuarios — Solo ADMIN: lista todos los usuarios */
    @GetMapping("/usuarios")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> listarUsuarios() {
        List<Map<String, String>> lista = service.listarUsuarios().stream()
                .map(u -> Map.of("username", u.getUsername(), "role", u.getRole()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.<List<Map<String, String>>>builder()
                        .success(true)
                        .message("Lista de usuarios")
                        .data(lista)
                        .build()
        );
    }

    /** GET /api/v1/auth/usuarios/{username} — Obtener usuario por username */
    @GetMapping("/usuarios/{username}")
    public ResponseEntity<ApiResponse<Map<String, String>>> obtenerUsuario(@PathVariable String username) {
        Usuario user = service.obtenerPorUsername(username);

        Map<String, String> info = Map.of(
                "username", user.getUsername(),
                "role", user.getRole()
        );

        return ResponseEntity.ok(
                ApiResponse.<Map<String, String>>builder()
                        .success(true)
                        .message("Usuario encontrado")
                        .data(info)
                        .build()
        );
    }
}
