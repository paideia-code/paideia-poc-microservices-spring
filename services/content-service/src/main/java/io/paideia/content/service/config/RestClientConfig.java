package io.paideia.content.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

	@Bean
	public RestClient enrollmentServiceClient(@Value("${clients.enrollment-service.url}") String enrollmentServiceUrl) {
		return RestClient.builder()
				.baseUrl(enrollmentServiceUrl)
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
