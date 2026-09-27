package io.paideia.content.service.client;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import io.paideia.content.service.client.dto.EnrollmentAccessResponseDTO;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EnrollmentAccessClient {

    private final RestClient enrollmentServiceClient;

    public boolean canAccessContent(UUID courseId) {
        EnrollmentAccessResponseDTO response = enrollmentServiceClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/enrollments/access")
                        .queryParam("courseId", courseId)
                        .build())
                .retrieve()
                .body(EnrollmentAccessResponseDTO.class);
        return response != null && response.accessAllowed();
    }
}
