package com.carmeet.ms_analytics_report.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class TicketingClient {

    private final WebClient webClient;

    public TicketingClient(
            WebClient.Builder builder,
            @Value("${services.ticketing.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    // retorna la lista de tickets de un evento extrayendo el campo data del ApiResponse
    public int contarTicketsPorEvento(Long eventoId, String bearerToken) {
        Map<String, Object> response = webClient.get()
                .uri("/api/v1/tickets/evento/" + eventoId)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
        if (response != null && response.get("data") instanceof List) {
            return ((List<?>) response.get("data")).size();
        }
        return 0;
    }
}
