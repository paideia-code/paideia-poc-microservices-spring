package io.paideia.enrollment.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient courseServiceClient(
            @Value("${clients.course-service.url}") String courseServiceUrl) {
        return RestClient.builder()
                .baseUrl(courseServiceUrl)
                .build();
    }

    @Bean
    public RestClient paymentServiceClient(
            @Value("${clients.payment-service.url}") String paymentServiceUrl) {
        return RestClient.builder()
                .baseUrl(paymentServiceUrl)
                .build();
    }

    @Bean
    public RestClient notificationServiceClient(
            @Value("${clients.notification-service.url}") String notificationServiceUrl) {
        return RestClient.builder()
                .baseUrl(notificationServiceUrl)
                .build();
    }
}
