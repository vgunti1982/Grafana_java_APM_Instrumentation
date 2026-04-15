# Java OpenTelemetry Lab

This workspace gives you three different Java instrumentation patterns and a local Grafana stack:

- `agent-app`: zero-code auto instrumentation using the Grafana OpenTelemetry Java agent JAR.
- `api-app`: manual instrumentation with the OpenTelemetry API, with exporting handled by an attached Java agent.
- `sdk-app`: manual instrumentation with the OpenTelemetry SDK and OTLP exporter configured in application code.

## Stack

- Tempo for traces on `4317/4318`
- Loki for logs on `3100`
- Promtail tails `./logs/*.log` into Loki
- Prometheus scrapes `/actuator/prometheus`
- Grafana runs on `http://localhost:3000` with `admin/admin`

## Start the observability stack

```bash
mkdir -p logs
docker compose -f observability/docker-compose.yml up -d
```

## Build the apps

`mvn` is not required locally. The included `./mvnw` script runs Maven inside Docker.

```bash
chmod +x mvnw
./mvnw -q -DskipTests package
```

## Download the Grafana Java agent

Set the agent version you want, then download the JAR into the workspace root.

```bash
export GRAFANA_OTEL_AGENT_VERSION=2.13.0
curl -L -o otel-javaagent.jar \
  "https://github.com/grafana/grafana-opentelemetry-java/releases/download/v${GRAFANA_OTEL_AGENT_VERSION}/grafana-opentelemetry-java.jar"
```

## Run the three apps

### 1. Grafana OTEL JAR

```bash
mkdir -p logs
java \
  -javaagent:otel-javaagent.jar \
  -Dotel.service.name=agent-app \
  -Dotel.exporter.otlp.protocol=grpc \
  -Dotel.exporter.otlp.endpoint=http://localhost:4317 \
  -Dotel.metrics.exporter=none \
  -Dotel.logs.exporter=none \
  -jar agent-app/target/agent-app-1.0.0-SNAPSHOT.jar
```

### 2. OpenTelemetry API

The app creates custom spans via `GlobalOpenTelemetry`. The agent supplies the SDK and exporter.

```bash
java \
  -javaagent:otel-javaagent.jar \
  -Dotel.service.name=api-app \
  -Dotel.exporter.otlp.protocol=grpc \
  -Dotel.exporter.otlp.endpoint=http://localhost:4317 \
  -Dotel.metrics.exporter=none \
  -Dotel.logs.exporter=none \
  -jar api-app/target/api-app-1.0.0-SNAPSHOT.jar
```

### 3. OpenTelemetry SDK

The app creates and exports spans directly in code.

```bash
java -jar sdk-app/target/sdk-app-1.0.0-SNAPSHOT.jar
```

## Generate telemetry

```bash
curl "http://localhost:8081/work?user=alice"
curl "http://localhost:8082/work?user=bob"
curl "http://localhost:8083/work?user=carol"
curl "http://localhost:8081/error"
curl "http://localhost:8082/error"
curl "http://localhost:8083/error"
```

## What to check

- Traces: Grafana Explore -> Tempo -> query `service.name="agent-app"` / `api-app` / `sdk-app`
- Logs: Grafana Explore -> Loki -> query `{job="java-apps"} |= "handled request"`
- Metrics: Grafana Explore -> Prometheus -> query `http_server_requests_seconds_count`
