package io.paideia.enrollment.service.client;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import io.paideia.enrollment.service.client.dto.CourseClientDTO;
import io.paideia.enrollment.service.exception.custom.CourseNotAvailableException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CourseClient {

    private final RestClient courseServiceClient;

    public CourseClientDTO findById(UUID courseId) {
        try {
            return courseServiceClient.get()
                    .uri("/courses/{id}", courseId)
                    .retrieve()
                    .body(CourseClientDTO.class);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new CourseNotAvailableException(courseId, "Course not found");
            }
            throw ex;
        }
    }
}
