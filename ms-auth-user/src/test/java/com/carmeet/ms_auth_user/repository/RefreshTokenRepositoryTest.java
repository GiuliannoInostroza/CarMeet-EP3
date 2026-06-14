package com.carmeet.ms_auth_user.repository;

import com.carmeet.ms_auth_user.model.RefreshToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository repository;

    @Test
    void debeGuardarRefreshToken() {
        RefreshToken token = new RefreshToken();
        token.setToken("sample_token_uuid");
        token.setUsername("testuser");
        token.setExpiryDate(new Date());

        RefreshToken guardado = repository.save(token);
        assertNotNull(guardado.getId());
        assertEquals("sample_token_uuid", guardado.getToken());
    }

    @Test
    void debeBuscarRefreshTokenPorId() {
        RefreshToken token = new RefreshToken();
        token.setToken("another_token");
        token.setUsername("john");
        token.setExpiryDate(new Date());

        RefreshToken guardado = repository.save(token);
        Optional<RefreshToken> resultado = repository.findById(guardado.getId());
        assertTrue(resultado.isPresent());
        assertEquals("another_token", resultado.get().getToken());
    }

    @Test
    void debeListarRefreshTokens() {
        RefreshToken t1 = new RefreshToken();
        t1.setToken("token1");
        repository.save(t1);

        RefreshToken t2 = new RefreshToken();
        t2.setToken("token2");
        repository.save(t2);

        List<RefreshToken> resultado = repository.findAll();
        assertFalse(resultado.isEmpty());
        assertTrue(resultado.size() >= 2);
    }

    @Test
    void debeEliminarRefreshToken() {
        RefreshToken token = new RefreshToken();
        token.setToken("delete_me_token");
        RefreshToken guardado = repository.save(token);

        repository.deleteById(guardado.getId());
        Optional<RefreshToken> resultado = repository.findById(guardado.getId());
        assertFalse(resultado.isPresent());
    }

    @Test
    void debeBuscarPorToken() {
        RefreshToken token = new RefreshToken();
        token.setToken("find_me");
        repository.save(token);

        Optional<RefreshToken> resultado = repository.findByToken("find_me");
        assertTrue(resultado.isPresent());
        assertEquals("find_me", resultado.get().getToken());
    }
}
