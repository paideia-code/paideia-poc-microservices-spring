package io.paideia.enrollment.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient courseServiceClient(@Value("${clients.course-service.url}") String courseServiceUrl, RestClient.Builder restClientBuilder) {
        return restClientBuilder
                .clone()
                .baseUrl(courseServiceUrl)
                .requestInterceptor((request, body, execution) -> {
                    var auth = SecurityContextHolder.getContext().getAuthentication();
                    if (auth instanceof JwtAuthenticationToken jwtAuth) {
                        request.getHeaders().setBearerAuth(jwtAuth.getToken().getTokenValue());
                    }
                    return execution.execute(request, body);
                })
                .build();
    }

    @Bean
    RestClient paymentServiceClient(@Value("${clients.payment-service.url}") String paymentServiceUrl, RestClient.Builder restClientBuilder) {
        return restClientBuilder
                .clone()
                .baseUrl(paymentServiceUrl)
                .requestInterceptor((request, body, execution) -> {
                    var auth = SecurityContextHolder.getContext().getAuthentication();
                    if (auth instanceof JwtAuthenticationToken jwtAuth) {
                        request.getHeaders().setBearerAuth(jwtAuth.getToken().getTokenValue());
                    }
                    return execution.execute(request, body);
                })
                .build();
    }

}
