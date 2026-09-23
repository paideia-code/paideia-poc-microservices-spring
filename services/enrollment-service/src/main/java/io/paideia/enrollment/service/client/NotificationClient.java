package io.paideia.enrollment.service.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import io.paideia.enrollment.service.client.dto.NotificationClientRequestDTO;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NotificationClient {

    private final RestClient notificationServiceClient;

    public NotificationClient(@Qualifier("notificationServiceClient") RestClient notificationServiceClient) {
        this.notificationServiceClient = notificationServiceClient;
    }

    public void notify(NotificationClientRequestDTO request) {
        try {
            notificationServiceClient.post()
                    .uri("/notifications")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("event=enrollment.notification.sent type={}", request.type());
        } catch (RestClientException ex) {
            log.warn("event=enrollment.notification.failed type={} cause={}", request.type(), ex.toString(), ex);
        }
    }
}
