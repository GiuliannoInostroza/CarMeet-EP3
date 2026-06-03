package com.carmeet.ms_auth_user.controller;

import com.carmeet.ms_auth_user.dto.*;
import com.carmeet.ms_auth_user.model.Usuario;
import com.carmeet.ms_auth_user.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
// o import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Autenticación", description = "Microservicio de autenticación, registro de usuarios y generacion de tokens JWT")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

        private final AuthService service;

        @Operation(summary = "Registrar un nuevo usuario", description = "Registra un nuevo usuario con username, password y ROLE")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Solicitud invalida") })
        @PostMapping("/register")
        // Evaluar lo del profe: public
        // ResponseEntity<EntityModel<ApiResponse><AuthResponse>>
        public ResponseEntity<ApiResponse<EntityModel<AuthResponse>>> register(
                        @Valid @RequestBody RegisterRequest req) {
                log.info("POST /api/v1/auth/register - usuario: {}", req.getUsername());
                AuthResponse res = service.register(req);
                EntityModel<AuthResponse> recurso = EntityModel.of(res);
                recurso.add(linkTo(methodOn(AuthController.class)
                                .login(new LoginRequest(req.getUsername(), req.getPassword()))).withRel("login"));

                return ResponseEntity.status(201).body(
                                ApiResponse.<EntityModel<AuthResponse>>builder()
                                                .success(true)
                                                .message("Usuario registrado con rol: " + res.getRole())
                                                .data(recurso)
                                                .build());
        }

        @Operation(summary = "Iniciar sesión", description = "Autentica al usuario y devuelve accessToken y refreshToken")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login exitoso"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciales invalidas") })
        @PostMapping("/login")
        public ResponseEntity<ApiResponse<EntityModel<AuthResponse>>> login(@Valid @RequestBody LoginRequest req) {
                log.info("POST /api/v1/auth/login - usuario: {}", req.getUsername());
                AuthResponse res = service.login(req);
                EntityModel<AuthResponse> recurso = EntityModel.of(res);
                recurso.add(linkTo(methodOn(AuthController.class).obtenerUsuario(req.getUsername()))
                                .withRel("usuario_info"));

                // linkTo(methodOn(AuthController.class).usuario)

                return ResponseEntity.ok(
                                ApiResponse.<EntityModel<AuthResponse>>builder()
                                                .success(true)
                                                .message("Login exitoso - rol: " + res.getRole())
                                                .data(recurso)
                                                .build());
        }

        @Operation(summary = "Renovar token", description = "Genera un nuevo accessToken usando un refreshToken valido")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token renovado exitosamente"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "RefreshToken invalido o expirado") })
        @PostMapping("/refresh")
        public ResponseEntity<ApiResponse<EntityModel<AuthResponse>>> refresh(@RequestBody RefreshRequest req) {
                AuthResponse res = service.refresh(req.getRefreshToken());
                EntityModel<AuthResponse> recurso = EntityModel.of(res);

                return ResponseEntity.ok(
                                ApiResponse.<EntityModel<AuthResponse>>builder()
                                                .success(true)
                                                .message("Token renovado")
                                                .data(recurso)
                                                .build());
        }

        @Operation(summary = "Obtener usuario actual", description = "Devuelve el username y rol del usuario autenticado en la sesion")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario obtenido"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado") })
        @GetMapping("/me")
        public ResponseEntity<ApiResponse<EntityModel<Map<String, String>>>> me(Authentication auth) {
                String username = auth.getName();
                Usuario user = service.obtenerPorUsername(username);
                Map<String, String> info = Map.of("username", user.getUsername(), "role", user.getRole());
                EntityModel<Map<String, String>> recurso = EntityModel.of(info);
                recurso.add(linkTo(methodOn(AuthController.class).obtenerUsuario(username)).withSelfRel());

                return ResponseEntity.ok(
                                ApiResponse.<EntityModel<Map<String, String>>>builder()
                                                .success(true)
                                                .message("Usuario actual")
                                                .data(recurso)
                                                .build());
        }

        @Operation(summary = "Promover usuario a ADMIN", description = "Solo ADMIN: cambia el rol de un usuario a ROLE_ADMIN")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario promovido"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado") })
        @PutMapping("/promote/{username}")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<EntityModel<Map<String, String>>>> promote(
                        @Parameter(description = "Username del usuario a promover", example = "MvlbeK") @PathVariable String username) {
                Usuario user = service.promoverAAdmin(username);
                Map<String, String> info = Map.of("username", user.getUsername(), "role", user.getRole());
                EntityModel<Map<String, String>> recurso = EntityModel.of(info);
                recurso.add(linkTo(methodOn(AuthController.class).obtenerUsuario(username)).withSelfRel());
                recurso.add(linkTo(methodOn(AuthController.class).listarUsuarios()).withRel("todos"));

                return ResponseEntity.ok(
                                ApiResponse.<EntityModel<Map<String, String>>>builder()
                                                .success(true)
                                                .message("Usuario promovido a ADMIN exitosamente")
                                                .data(recurso)
                                                .build());
        }

        @Operation(summary = "Degradar usuario a USER", description = "Solo ADMIN: cambia el rol de un usuario a ROLE_USER")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario degradado"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado") })
        @PutMapping("/demote/{username}")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<EntityModel<Map<String, String>>>> demote(
                        @Parameter(description = "Username del usuario a degradar", example = "juanito99") @PathVariable String username) {
                Usuario user = service.degradarAUser(username);
                Map<String, String> info = Map.of("username", user.getUsername(), "role", user.getRole());
                EntityModel<Map<String, String>> recurso = EntityModel.of(info);
                recurso.add(linkTo(methodOn(AuthController.class).obtenerUsuario(username)).withSelfRel());
                recurso.add(linkTo(methodOn(AuthController.class).listarUsuarios()).withRel("todos"));

                return ResponseEntity.ok(
                                ApiResponse.<EntityModel<Map<String, String>>>builder()
                                                .success(true)
                                                .message("Usuario degradado a USER exitosamente")
                                                .data(recurso)
                                                .build());
        }

        @Operation(summary = "Listar todos los usuarios", description = "Solo ADMIN: retorna la lista completa de usuarios registrados")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista obtenida"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado") })
        @GetMapping("/usuarios")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<CollectionModel<EntityModel<Map<String, String>>>>> listarUsuarios() {
                List<EntityModel<Map<String, String>>> lista = service.listarUsuarios().stream()
                                .map(u -> {
                                        Map<String, String> m = Map.of("username", u.getUsername(), "role",
                                                        u.getRole());
                                        return EntityModel.of(m,
                                                        linkTo(methodOn(AuthController.class)
                                                                        .obtenerUsuario(u.getUsername()))
                                                                        .withSelfRel());
                                })
                                .collect(Collectors.toList());

                CollectionModel<EntityModel<Map<String, String>>> recurso = CollectionModel.of(lista,
                                linkTo(methodOn(AuthController.class).listarUsuarios()).withSelfRel());

                return ResponseEntity.ok(
                                ApiResponse.<CollectionModel<EntityModel<Map<String, String>>>>builder()
                                                .success(true)
                                                .message("Lista de usuarios")
                                                .data(recurso)
                                                .build());
        }

        @Operation(summary = "Obtener usuario por username", description = "Retorna el username y rol de un usuario especifico")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario encontrado"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado") })
        @GetMapping("/usuarios/{username}")
        public ResponseEntity<ApiResponse<EntityModel<Map<String, String>>>> obtenerUsuario(
                        @Parameter(description = "Username del usuario a buscar", example = "juanito99") @PathVariable String username) {
                Usuario user = service.obtenerPorUsername(username);
                Map<String, String> info = Map.of("username", user.getUsername(), "role", user.getRole());
                EntityModel<Map<String, String>> recurso = EntityModel.of(info);
                recurso.add(linkTo(methodOn(AuthController.class).obtenerUsuario(username)).withSelfRel());
                // Link is conditional? No, standard. It requires roles for these. We'll just
                // add the standard paths.

                return ResponseEntity.ok(
                                ApiResponse.<EntityModel<Map<String, String>>>builder()
                                                .success(true)
                                                .message("Usuario encontrado")
                                                .data(recurso)
                                                .build());
        }
}
