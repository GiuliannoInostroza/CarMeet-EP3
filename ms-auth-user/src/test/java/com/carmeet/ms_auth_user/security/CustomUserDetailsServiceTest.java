package com.carmeet.ms_auth_user.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.carmeet.ms_auth_user.model.Usuario;
import com.carmeet.ms_auth_user.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository repo;

    @InjectMocks
    private CustomUserDetailsService service;

    // METODO: loadUserByUsername(String username)
    @Test
    void loadUserByUsername_CuandoUsuarioExiste_DebeRetornarUserDetails() {
        // Arrange
        String username = "john";
        Usuario user = new Usuario();
        user.setUsername(username);
        user.setPassword("encodedPassword");
        user.setRole("ROLE_ESPECTADOR");

        when(repo.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        UserDetails result = service.loadUserByUsername(username);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ESPECTADOR".equals(a.getAuthority())));
        verify(repo, times(1)).findByUsername(username);
    }

    @Test
    void loadUserByUsername_CuandoUsuarioNoExiste_DebeLanzarUsernameNotFoundException() {
        // Arrange
        String username = "unknown";
        when(repo.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            service.loadUserByUsername(username);
        });

        verify(repo, times(1)).findByUsername(username);
    }
}
