package com.carmeet.ms_competition_reg.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class EventoCoreClient {

    private final WebClient webClient;

    public EventoCoreClient(
            WebClient.Builder builder,
            @Value("${services.event-core.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public void validarEvento(Long eventoId, String bearerToken) {
        if (eventoId == null) return;
        webClient.get()
                .uri("/api/v1/eventos/" + eventoId)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
