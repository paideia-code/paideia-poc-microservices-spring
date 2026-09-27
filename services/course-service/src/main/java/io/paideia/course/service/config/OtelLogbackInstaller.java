package io.paideia.course.service.config;

import org.springframework.stereotype.Component;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class OtelLogbackInstaller {

    private final OpenTelemetry openTelemetry;

    @PostConstruct
    void install() {
        OpenTelemetryAppender.install(openTelemetry);
    }
}
