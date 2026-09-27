package io.paideia.user.service.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequestDTO(
                @NotBlank @Size(max = 200) String displayName) {
}