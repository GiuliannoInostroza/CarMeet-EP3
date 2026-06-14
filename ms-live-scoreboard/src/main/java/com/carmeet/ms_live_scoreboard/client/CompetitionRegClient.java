package com.carmeet.ms_live_scoreboard.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class CompetitionRegClient {

    private final WebClient webClient;

    public CompetitionRegClient(
            WebClient.Builder builder,
            @Value("${services.competition-reg.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public void validarInscripcion(Long inscripcionId, String bearerToken) {
        if (inscripcionId == null) return;
        webClient.get()
                .uri("/api/v1/inscripciones/" + inscripcionId)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
