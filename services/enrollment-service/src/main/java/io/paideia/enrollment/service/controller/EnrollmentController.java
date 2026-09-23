package io.paideia.enrollment.service.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.paideia.enrollment.service.controller.dto.EnrollmentAccessResponseDTO;
import io.paideia.enrollment.service.controller.dto.EnrollmentRequestDTO;
import io.paideia.enrollment.service.controller.dto.EnrollmentResponseDTO;
import io.paideia.enrollment.service.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private static final String PAYMENT_SIMULATION_HEADER = "X-Payment-Simulation";

    private final EnrollmentService enrollmentService;

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(enrollmentService.findById(id));
    }

    @GetMapping("/access")
    public ResponseEntity<EnrollmentAccessResponseDTO> access(@AuthenticationPrincipal Jwt jwt, @RequestParam UUID courseId) {
        return ResponseEntity.ok(enrollmentService.access(UUID.fromString(jwt.getSubject()), courseId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<EnrollmentResponseDTO>> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(enrollmentService.me(UUID.fromString(jwt.getSubject())));
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponseDTO> enroll(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = PAYMENT_SIMULATION_HEADER, required = false) String paymentSimulation,
            @Valid @RequestBody EnrollmentRequestDTO dto) {

        EnrollmentResponseDTO response = enrollmentService.enroll(UUID.fromString(jwt.getSubject()), paymentSimulation, dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }
}
