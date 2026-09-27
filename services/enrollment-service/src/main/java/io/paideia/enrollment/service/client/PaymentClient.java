package io.paideia.enrollment.service.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import io.paideia.enrollment.service.client.dto.PaymentClientRequestDTO;
import io.paideia.enrollment.service.client.dto.PaymentClientResponseDTO;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentClient {

    private static final String PAYMENT_SIMULATION_HEADER = "X-Payment-Simulation";

    private final RestClient paymentServiceClient;

    public PaymentClientResponseDTO process(PaymentClientRequestDTO request, String paymentSimulation) {
        RestClient.RequestBodySpec spec = paymentServiceClient.post()
                .uri("/payments");

        if (paymentSimulation != null && !paymentSimulation.isBlank()) {
            spec.header(PAYMENT_SIMULATION_HEADER, paymentSimulation);
        }

        return spec.body(request)
                .retrieve()
                .body(PaymentClientResponseDTO.class);
    }
}
