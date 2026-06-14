package com.carmeet.ms_auth_user.controller;

import com.carmeet.ms_auth_user.dto.*;
import com.carmeet.ms_auth_user.model.Usuario;
import com.carmeet.ms_auth_user.service.AuthService;
import com.carmeet.ms_auth_user.security.JwtUtil;
import com.carmeet.ms_auth_user.security.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService service;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void debeRegistrarUsuario() throws Exception {
        RegisterRequest req = new RegisterRequest("testuser", "password123", "ROLE_ESPECTADOR");
        AuthResponse res = new AuthResponse("accessToken", "refreshToken", "ROLE_ESPECTADOR");

        when(service.register(any(RegisterRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuario registrado con rol: ROLE_ESPECTADOR"))
                .andExpect(jsonPath("$.data.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.data.role").value("ROLE_ESPECTADOR"));
    }

    @Test
    void debeHacerLogin() throws Exception {
        LoginRequest req = new LoginRequest("testuser", "password123");
        AuthResponse res = new AuthResponse("accessToken", "refreshToken", "ROLE_ESPECTADOR");

        when(service.login(any(LoginRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login exitoso - rol: ROLE_ESPECTADOR"))
                .andExpect(jsonPath("$.data.accessToken").value("accessToken"));
    }

    @Test
    void debeRefrescarToken() throws Exception {
        RefreshRequest req = new RefreshRequest("oldRefreshToken");
        AuthResponse res = new AuthResponse("newAccessToken", "newRefreshToken", "ROLE_ESPECTADOR");

        when(service.refresh("oldRefreshToken")).thenReturn(res);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token renovado"))
                .andExpect(jsonPath("$.data.accessToken").value("newAccessToken"));
    }

    @Test
    void debeObtenerUsuarioActual() throws Exception {
        Usuario u = new Usuario(1L, "testuser", "password", "ROLE_ESPECTADOR");
        when(service.obtenerPorUsername("testuser")).thenReturn(u);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("testuser", null, List.of());

        mockMvc.perform(get("/api/v1/auth/me").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuario actual"))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.role").value("ROLE_ESPECTADOR"));
    }

    @Test
    void debePromoverUsuario() throws Exception {
        Usuario u = new Usuario(1L, "testuser", "password", "ROLE_ADMIN");
        when(service.promoverAAdmin("testuser")).thenReturn(u);

        mockMvc.perform(put("/api/v1/auth/promote/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuario promovido a ADMIN exitosamente"))
                .andExpect(jsonPath("$.data.role").value("ROLE_ADMIN"));
    }

    @Test
    void debeDegradarUsuario() throws Exception {
        Usuario u = new Usuario(1L, "testuser", "password", "ROLE_ESPECTADOR");
        when(service.degradarAUser("testuser")).thenReturn(u);

        mockMvc.perform(put("/api/v1/auth/demote/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuario degradado a USER exitosamente"))
                .andExpect(jsonPath("$.data.role").value("ROLE_ESPECTADOR"));
    }

    @Test
    void debeListarUsuarios() throws Exception {
        Usuario u = new Usuario(1L, "testuser", "password", "ROLE_ESPECTADOR");
        when(service.listarUsuarios()).thenReturn(List.of(u));

        mockMvc.perform(get("/api/v1/auth/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lista de usuarios"))
                .andExpect(jsonPath("$.data.content[0].username").value("testuser"));
    }

    @Test
    void debeObtenerUsuarioPorUsername() throws Exception {
        Usuario u = new Usuario(1L, "testuser", "password", "ROLE_ESPECTADOR");
        when(service.obtenerPorUsername("testuser")).thenReturn(u);

        mockMvc.perform(get("/api/v1/auth/usuarios/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuario encontrado"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }
}
