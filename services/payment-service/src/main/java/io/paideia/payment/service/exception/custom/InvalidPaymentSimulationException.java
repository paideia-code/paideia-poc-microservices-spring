package io.paideia.payment.service.exception.custom;

public class InvalidPaymentSimulationException extends RuntimeException {

    public InvalidPaymentSimulationException(String paymentSimulation) {
        super("Unsupported payment simulation: " + paymentSimulation);
    }
}
