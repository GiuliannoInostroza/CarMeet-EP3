package com.carmeet.ms_auth_user.repository;

import com.carmeet.ms_auth_user.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository repository;

    @Test
    void debeGuardarUsuario() {
        Usuario u = new Usuario();
        u.setUsername("testuser");
        u.setPassword("password123");
        u.setRole("ROLE_USER");
        
        Usuario guardado = repository.save(u);
        assertNotNull(guardado.getId());
        assertEquals("testuser", guardado.getUsername());
    }

    @Test
    void debeBuscarUsuarioPorId() {
        Usuario u = new Usuario();
        u.setUsername("john_doe");
        u.setPassword("securepass");
        u.setRole("ROLE_USER");
        
        Usuario guardado = repository.save(u);
        Optional<Usuario> resultado = repository.findById(guardado.getId());
        assertTrue(resultado.isPresent());
        assertEquals("john_doe", resultado.get().getUsername());
    }

    @Test
    void debeListarUsuarios() {
        Usuario u1 = new Usuario();
        u1.setUsername("user1");
        repository.save(u1);

        Usuario u2 = new Usuario();
        u2.setUsername("user2");
        repository.save(u2);

        List<Usuario> resultado = repository.findAll();
        assertFalse(resultado.isEmpty());
        assertTrue(resultado.size() >= 2);
    }

    @Test
    void debeEliminarUsuario() {
        Usuario u = new Usuario();
        u.setUsername("delete_me");
        Usuario guardado = repository.save(u);
        
        repository.deleteById(guardado.getId());
        Optional<Usuario> resultado = repository.findById(guardado.getId());
        assertFalse(resultado.isPresent());
    }

    @Test
    void debeBuscarPorUsername() {
        Usuario u = new Usuario();
        u.setUsername("special_user");
        repository.save(u);

        Optional<Usuario> resultado = repository.findByUsername("special_user");
        assertTrue(resultado.isPresent());
        assertEquals("special_user", resultado.get().getUsername());
    }
}
