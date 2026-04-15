package com.example.otel.api;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkController {

    private static final Logger log = LoggerFactory.getLogger(WorkController.class);
    private final Tracer tracer = GlobalOpenTelemetry.getTracer("com.example.otel.api", "1.0.0");

    @GetMapping(path = "/work", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> work(@RequestParam(name = "user", defaultValue = "api") String user) throws InterruptedException {
        Span span = tracer.spanBuilder("custom.business.work")
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();

        try (Scope ignored = span.makeCurrent()) {
            int delayMs = ThreadLocalRandom.current().nextInt(100, 300);
            span.setAttribute("app.user", user);
            span.setAttribute("app.delay.ms", delayMs);
            Thread.sleep(delayMs);
            log.info("api-app handled request user={} delayMs={}", user, delayMs);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("app", "api-app");
            payload.put("instrumentation", "opentelemetry-api");
            payload.put("user", user);
            payload.put("delayMs", delayMs);
            payload.put("timestamp", Instant.now().toString());
            return payload;
        } catch (InterruptedException ex) {
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR);
            Thread.currentThread().interrupt();
            throw ex;
        } finally {
            span.end();
        }
    }

    @GetMapping(path = "/error", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> error() {
        Span span = tracer.spanBuilder("custom.business.error")
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();
        try (Scope ignored = span.makeCurrent()) {
            log.error("api-app synthetic error path triggered");
            throw new IllegalStateException("synthetic failure from api-app");
        } catch (RuntimeException ex) {
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR);
            throw ex;
        } finally {
            span.end();
        }
    }
}
