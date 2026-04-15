package com.example.otel.agent;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkController {

    private static final Logger log = LoggerFactory.getLogger(WorkController.class);

    @GetMapping(path = "/work", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> work(@RequestParam(name = "user", defaultValue = "agent") String user) throws InterruptedException {
        int delayMs = ThreadLocalRandom.current().nextInt(75, 250);
        Thread.sleep(delayMs);

        log.info("agent-app processed request user={} delayMs={}", user, delayMs);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("app", "agent-app");
        payload.put("instrumentation", "grafana-agent-jar");
        payload.put("user", user);
        payload.put("delayMs", delayMs);
        payload.put("timestamp", Instant.now().toString());
        return payload;
    }

    @GetMapping(path = "/error", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> error() {
        log.error("agent-app synthetic error path triggered");
        throw new IllegalStateException("synthetic failure from agent-app");
    }
}
