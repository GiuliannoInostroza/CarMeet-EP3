package com.carmeet.ms_ticketing.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class PagoMockClient {

    private final WebClient webClient;

    public PagoMockClient(
            WebClient.Builder builder,
            @Value("${services.payment-mock.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    // envia solicitud de pago al mock, retorna el resultado
    public Map<String, Object> procesarPago(Long ticketId, Double monto, String metodoPago, String bearerToken) {
        Map<String, Object> body = Map.of(
                "ticketId", ticketId,
                "monto", monto,
                "metodoPago", metodoPago != null ? metodoPago : "TARJETA"
        );
        return webClient.post()
                .uri("/api/v1/pagos/procesar")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }
}
