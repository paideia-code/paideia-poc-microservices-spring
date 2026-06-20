package io.paideia.content.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient enrollmentServiceClient(
            @Value("${clients.enrollment-service.url}") String enrollmentServiceUrl) {
        return RestClient.builder()
                .baseUrl(enrollmentServiceUrl)
                .build();
    }
}
