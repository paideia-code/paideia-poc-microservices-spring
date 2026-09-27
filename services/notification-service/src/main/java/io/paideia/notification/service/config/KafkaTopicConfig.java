package io.paideia.notification.service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration 
public class KafkaTopicConfig {

    public static final String ENROLLMENT_EVENTS_TOPIC = "enrollment.events";
    
    @Bean
    NewTopic enrollmentEventsTopic() {
        return TopicBuilder
                .name(ENROLLMENT_EVENTS_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
    
}
