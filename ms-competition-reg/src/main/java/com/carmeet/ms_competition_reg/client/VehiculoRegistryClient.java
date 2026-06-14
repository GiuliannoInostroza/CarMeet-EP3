package com.carmeet.ms_competition_reg.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class VehiculoRegistryClient {

    private final WebClient webClient;

    public VehiculoRegistryClient(
            WebClient.Builder builder,
            @Value("${services.vehicle-registry.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public void validarVehiculo(Long vehiculoId, String bearerToken) {
        webClient.get()
                .uri("/api/v1/vehiculos/" + vehiculoId)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
