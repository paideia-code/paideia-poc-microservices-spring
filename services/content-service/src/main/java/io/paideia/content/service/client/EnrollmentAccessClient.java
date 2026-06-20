package io.paideia.content.service.client;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import io.paideia.content.service.client.dto.EnrollmentAccessResponseDTO;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EnrollmentAccessClient {

    private static final String STUDENT_ID_HEADER = "X-Student-Id";

    private final RestClient enrollmentServiceClient;

    public boolean canAccessContent(UUID studentId, UUID courseId) {
        EnrollmentAccessResponseDTO response = enrollmentServiceClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/enrollments/access")
                        .queryParam("courseId", courseId)
                        .build())
                .header(STUDENT_ID_HEADER, studentId.toString())
                .retrieve()
                .body(EnrollmentAccessResponseDTO.class);
        return response != null && response.accessAllowed();
    }
}
