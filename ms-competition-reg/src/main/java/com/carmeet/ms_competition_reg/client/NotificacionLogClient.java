package com.carmeet.ms_competition_reg.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class NotificacionLogClient {

    private final WebClient webClient;

    public NotificacionLogClient(
            WebClient.Builder builder,
            @Value("${services.notification-log.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public void enviar(String destinatario, String mensaje, String bearerToken) {
        Map<String, String> body = Map.of("destinatario", destinatario, "mensaje", mensaje);
        webClient.post()
                .uri("/api/v1/notificaciones/enviar")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .subscribe(r -> {}, err -> {});
    }
}
