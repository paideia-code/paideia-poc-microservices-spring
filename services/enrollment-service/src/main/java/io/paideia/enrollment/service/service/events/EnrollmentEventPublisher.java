package io.paideia.enrollment.service.service.events;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class EnrollmentEventPublisher {

    private final KafkaTemplate<String, EnrollmentCreatedEvent> kafkaTemplate;
    private static final String TOPIC = "enrollment.events";

    public void publish(EnrollmentCreatedEvent event) {
        kafkaTemplate.send(TOPIC, event.enrollmentId().toString(), event);
    }
    
}
