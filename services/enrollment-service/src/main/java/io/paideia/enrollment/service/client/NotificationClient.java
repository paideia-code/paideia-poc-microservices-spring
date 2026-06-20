package io.paideia.enrollment.service.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import io.paideia.enrollment.service.client.dto.NotificationClientRequestDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NotificationClient {

    private final RestClient notificationServiceClient;

    public NotificationClient(@Qualifier("notificationServiceClient") RestClient notificationServiceClient) {
        this.notificationServiceClient = notificationServiceClient;
    }

    /**
     * Best-effort: si notification-service no está disponible, sólo se registra el warning.
     * En v1 esto demuestra el problema de acoplamiento síncrono y pérdida silenciosa de datos.
     * En v4 (Kafka) este método desaparece y se reemplaza por un evento publicado.
     */
    public void notify(NotificationClientRequestDTO request) {
        notificationServiceClient.post()
                .uri("/notifications")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
