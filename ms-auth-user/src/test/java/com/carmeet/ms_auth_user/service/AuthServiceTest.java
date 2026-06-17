package com.carmeet.ms_auth_user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.carmeet.ms_auth_user.dto.AuthResponse;
import com.carmeet.ms_auth_user.dto.LoginRequest;
import com.carmeet.ms_auth_user.dto.RegisterRequest;
import com.carmeet.ms_auth_user.model.RefreshToken;
import com.carmeet.ms_auth_user.model.Usuario;
import com.carmeet.ms_auth_user.repository.RefreshTokenRepository;
import com.carmeet.ms_auth_user.repository.UsuarioRepository;
import com.carmeet.ms_auth_user.security.JwtUtil;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepo;

    @Mock
    private RefreshTokenRepository refreshRepo;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService service;

    // METODO: register(RegisterRequest req)
    @Test
    void register_CuandoUsuarioYaExiste_DebeLanzarRuntimeException() {
        // Arrange
        RegisterRequest req = new RegisterRequest();
        req.setUsername("john");
        req.setPassword("password");
        req.setRole("ROLE_USER");
        when(usuarioRepo.findByUsername("john")).thenReturn(Optional.of(new Usuario()));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            service.register(req);
        });

        verify(usuarioRepo, times(1)).findByUsername("john");
        verify(usuarioRepo, never()).save(any(Usuario.class));
    }

    @Test
    void register_CuandoUsuarioNoExiste_DebeRegistrarConRoleEspectadorYRetornarTokens() {
        // Arrange
        RegisterRequest req = new RegisterRequest();
        req.setUsername("john");
        req.setPassword("password");
        req.setRole(null);
        Usuario savedUser = new Usuario();
        savedUser.setUsername("john");
        savedUser.setRole("ROLE_ESPECTADOR");

        when(usuarioRepo.findByUsername("john")).thenReturn(Optional.empty());
        when(encoder.encode("password")).thenReturn("encodedPassword");
        when(usuarioRepo.save(any(Usuario.class))).thenReturn(savedUser);
        when(jwtUtil.generarToken("john", "ROLE_ESPECTADOR")).thenReturn("accessToken");
        when(jwtUtil.generarRefreshToken("john")).thenReturn("refreshToken");

        // Act
        AuthResponse response = service.register(req);

        // Assert
        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("ROLE_ESPECTADOR", response.getRole());
        verify(usuarioRepo, times(1)).save(any(Usuario.class));
        verify(refreshRepo, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void register_CuandoSePideAdminOUser_DebeRegistrarConRoleEspectador() {
        // Arrange
        RegisterRequest req = new RegisterRequest();
        req.setUsername("john");
        req.setPassword("password");
        req.setRole("ROLE_ADMIN");
        when(usuarioRepo.findByUsername("john")).thenReturn(Optional.empty());
        when(encoder.encode("password")).thenReturn("encodedPassword");
        when(jwtUtil.generarToken("john", "ROLE_ESPECTADOR")).thenReturn("accessToken");
        when(jwtUtil.generarRefreshToken("john")).thenReturn("refreshToken");

        // Act
        AuthResponse response = service.register(req);

        // Assert
        assertNotNull(response);
        assertEquals("ROLE_ESPECTADOR", response.getRole());

        // Also test ROLE_USER
        req.setRole("ROLE_USER");
        AuthResponse response2 = service.register(req);
        assertEquals("ROLE_ESPECTADOR", response2.getRole());
        
        // Also test empty role
        req.setRole("   ");
        AuthResponse response3 = service.register(req);
        assertEquals("ROLE_ESPECTADOR", response3.getRole());
    }

    @Test
    void register_CuandoSePideOtroRol_DebeRegistrarConEseRolFormateado() {
        // Arrange
        RegisterRequest req = new RegisterRequest();
        req.setUsername("john");
        req.setPassword("password");
        req.setRole("organizador");
        when(usuarioRepo.findByUsername("john")).thenReturn(Optional.empty());
        when(encoder.encode("password")).thenReturn("encodedPassword");
        when(jwtUtil.generarToken("john", "ROLE_ORGANIZADOR")).thenReturn("accessToken");
        when(jwtUtil.generarRefreshToken("john")).thenReturn("refreshToken");

        // Act
        AuthResponse response = service.register(req);

        // Assert
        assertNotNull(response);
        assertEquals("ROLE_ORGANIZADOR", response.getRole());
    }

    // METODO: login(LoginRequest req)
    @Test
    void login_DebeAutenticarYRetornarTokens() {
        // Arrange
        LoginRequest req = new LoginRequest("john", "password");
        Usuario user = new Usuario();
        user.setUsername("john");
        user.setRole("ROLE_ESPECTADOR");

        when(usuarioRepo.findByUsername("john")).thenReturn(Optional.of(user));
        when(jwtUtil.generarToken("john", "ROLE_ESPECTADOR")).thenReturn("accessToken");
        when(jwtUtil.generarRefreshToken("john")).thenReturn("refreshToken");

        // Act
        AuthResponse response = service.login(req);

        // Assert
        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("ROLE_ESPECTADOR", response.getRole());
        verify(authManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    // METODO: refresh(String refreshToken)
    @Test
    void refresh_CuandoTokenNoExisteEnDb_DebeLanzarRuntimeException() {
        // Arrange
        String rToken = "invalidToken";
        when(refreshRepo.findByToken(rToken)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            service.refresh(rToken);
        });

        verify(refreshRepo, times(1)).findByToken(rToken);
    }

    @Test
    void refresh_CuandoTokenEsInvalidoONoEsRefreshToken_DebeLanzarRuntimeException() {
        // Arrange
        String rToken = "badToken";
        RefreshToken rt = new RefreshToken();
        rt.setUsername("john");
        rt.setToken(rToken);

        when(refreshRepo.findByToken(rToken)).thenReturn(Optional.of(rt));
        when(jwtUtil.esValido(rToken)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            service.refresh(rToken);
        });
        
        when(jwtUtil.esValido(rToken)).thenReturn(true);
        when(jwtUtil.esRefreshToken(rToken)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> {
            service.refresh(rToken);
        });
    }

    @Test
    void refresh_CuandoTodoEsValido_DebeRetornarNuevoAccessToken() {
        // Arrange
        String rToken = "goodToken";
        RefreshToken rt = new RefreshToken();
        rt.setUsername("john");
        rt.setToken(rToken);

        Usuario user = new Usuario();
        user.setUsername("john");
        user.setRole("ROLE_ESPECTADOR");

        when(refreshRepo.findByToken(rToken)).thenReturn(Optional.of(rt));
        when(jwtUtil.esValido(rToken)).thenReturn(true);
        when(jwtUtil.esRefreshToken(rToken)).thenReturn(true);
        when(usuarioRepo.findByUsername("john")).thenReturn(Optional.of(user));
        when(jwtUtil.generarToken("john", "ROLE_ESPECTADOR")).thenReturn("newAccessToken");

        // Act
        AuthResponse response = service.refresh(rToken);

        // Assert
        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertEquals(rToken, response.getRefreshToken());
        assertEquals("ROLE_ESPECTADOR", response.getRole());
    }

    // METODO: promoverAAdmin(String username)
    @Test
    void promoverAAdmin_CuandoExiste_DebeCambiarRolAAdminYGuardar() {
        // Arrange
        String username = "john";
        Usuario user = new Usuario();
        user.setUsername(username);
        user.setRole("ROLE_ESPECTADOR");

        when(usuarioRepo.findByUsername(username)).thenReturn(Optional.of(user));
        when(usuarioRepo.save(user)).thenReturn(user);

        // Act
        Usuario resultado = service.promoverAAdmin(username);

        // Assert
        assertNotNull(resultado);
        assertEquals("ROLE_ADMIN", resultado.getRole());
        verify(usuarioRepo, times(1)).save(user);
    }

    @Test
    void promoverAAdmin_CuandoNoExiste_DebeLanzarRuntimeException() {
        // Arrange
        String username = "john";
        when(usuarioRepo.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            service.promoverAAdmin(username);
        });
    }

    // METODO: degradarAUser(String username)
    @Test
    void degradarAUser_CuandoExiste_DebeCambiarRolAEspectadorYGuardar() {
        // Arrange
        String username = "john";
        Usuario user = new Usuario();
        user.setUsername(username);
        user.setRole("ROLE_ADMIN");

        when(usuarioRepo.findByUsername(username)).thenReturn(Optional.of(user));
        when(usuarioRepo.save(user)).thenReturn(user);

        // Act
        Usuario resultado = service.degradarAUser(username);

        // Assert
        assertNotNull(resultado);
        assertEquals("ROLE_ESPECTADOR", resultado.getRole());
        verify(usuarioRepo, times(1)).save(user);
    }

    @Test
    void degradarAUser_CuandoNoExiste_DebeLanzarRuntimeException() {
        String username = "john";
        when(usuarioRepo.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            service.degradarAUser(username);
        });
    }

    // METODO: obtenerPorUsername(String username)
    @Test
    void obtenerPorUsername_CuandoExiste_DebeRetornarUsuario() {
        // Arrange
        String username = "john";
        Usuario user = new Usuario();
        user.setUsername(username);

        when(usuarioRepo.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        Usuario resultado = service.obtenerPorUsername(username);

        // Assert
        assertNotNull(resultado);
        assertEquals(username, resultado.getUsername());
    }

    @Test
    void obtenerPorUsername_CuandoNoExiste_DebeLanzarRuntimeException() {
        String username = "john";
        when(usuarioRepo.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            service.obtenerPorUsername(username);
        });
    }

    // METODO: listarUsuarios()
    @Test
    void listarUsuarios_DebeRetornarTodosLosUsuarios() {
        // Arrange
        List<Usuario> list = Arrays.asList(new Usuario(), new Usuario());
        when(usuarioRepo.findAll()).thenReturn(list);

        // Act
        List<Usuario> resultado = service.listarUsuarios();

        // Assert
        assertEquals(2, resultado.size());
        verify(usuarioRepo, times(1)).findAll();
    }
}
