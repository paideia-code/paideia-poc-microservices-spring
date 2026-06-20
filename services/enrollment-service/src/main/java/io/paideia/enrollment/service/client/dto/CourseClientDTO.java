package io.paideia.enrollment.service.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CourseClientDTO(
        UUID id,
        String title,
        BigDecimal price,
        String status
) {}
