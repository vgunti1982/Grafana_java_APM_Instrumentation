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

## Quick Start

### Option 1: Run in Docker Containers (Recommended)

**Prerequisites:** Docker and Docker Compose

Build and run all three apps with Docker Compose:

```bash
docker compose -f docker-compose.apps.yml up -d
```

This runs:
- `agent-app` on `http://localhost:8081`
- `api-app` on `http://localhost:8082`
- `sdk-app` on `http://localhost:8083`

Stop the containers:

```bash
docker compose -f docker-compose.apps.yml down
```

### Option 2: Run Locally (Non-Docker)

**Prerequisites:**
- Java 21 (JDK)
- Maven 3.9+ OR use the included Maven wrapper

#### Step 1: Build All Apps

```bash
# Make the wrapper executable (Linux/Mac)
chmod +x mvnw

# Build all modules
./mvnw clean package -DskipTests
```

Or with system Maven:
```bash
mvn clean package -DskipTests
```

#### Step 2: Run Each App in Separate Terminals

**Terminal 1 - Agent App (port 8081):**
```bash
java -jar agent-app/target/agent-app-1.0.0-SNAPSHOT.jar
```

**Terminal 2 - API App (port 8082):**
```bash
java -jar api-app/target/api-app-1.0.0-SNAPSHOT.jar
```

**Terminal 3 - SDK App (port 8083):**
```bash
java -jar sdk-app/target/sdk-app-1.0.0-SNAPSHOT.jar
```

Or use Maven directly to run (requires separate terminals):

**Terminal 1:**
```bash
./mvnw -pl agent-app spring-boot:run
```

**Terminal 2:**
```bash
./mvnw -pl api-app spring-boot:run
```

**Terminal 3:**
```bash
./mvnw -pl sdk-app spring-boot:run
```

### Optional: Setup Grafana Instrumentation Agent

To enable auto-instrumentation with Grafana OTEL agent on the `agent-app`:

```bash
export GRAFANA_OTEL_AGENT_VERSION=2.13.0
curl -L -o otel-javaagent.jar \
  "https://github.com/grafana/grafana-opentelemetry-java/releases/download/v${GRAFANA_OTEL_AGENT_VERSION}/grafana-opentelemetry-java.jar"

# Run agent-app with the OTEL agent
java \
  -javaagent:otel-javaagent.jar \
  -Dotel.service.name=agent-app \
  -jar agent-app/target/agent-app-1.0.0-SNAPSHOT.jar
```

## Detailed Running Instructions

### Docker Setup

#### Single Command Start

Start all three containerized apps with auto-networking:

```bash
docker compose -f docker-compose.apps.yml up -d
```

View logs:
```bash
docker compose -f docker-compose.apps.yml logs -f
```

Stop and clean up:
```bash
docker compose -f docker-compose.apps.yml down
```

#### Build Individual Images

```bash
# Build agent-app
docker build -t agent-app:latest -f agent-app/Dockerfile .

# Build api-app
docker build -t api-app:latest -f api-app/Dockerfile .

# Build sdk-app
docker build -t sdk-app:latest -f sdk-app/Dockerfile .
```

Run individual containers:
```bash
docker run -p 8081:8081 --name agent-app agent-app:latest
docker run -p 8082:8082 --name api-app api-app:latest
docker run -p 8083:8083 --name sdk-app sdk-app:latest
```

### Non-Docker Local Setup

#### Prerequisites

- **Java 21 JDK**: [Download](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html) or use:
  ```bash
  # Ubuntu/Debian
  sudo apt-get install openjdk-21-jdk

  # Mac with Homebrew
  brew install openjdk@21
  ```

- **Maven 3.9+** (optional - included wrapper works fine):
  ```bash
  # Ubuntu/Debian
  sudo apt-get install maven

  # Mac with Homebrew
  brew install maven
  ```

#### Build Process

```bash
# Make wrapper executable (once)
chmod +x mvnw

# Build all modules
./mvnw clean package -DskipTests
```

**Output:** JARs are created in:
- `agent-app/target/agent-app-1.0.0-SNAPSHOT.jar`
- `api-app/target/api-app-1.0.0-SNAPSHOT.jar`
- `sdk-app/target/sdk-app-1.0.0-SNAPSHOT.jar`

#### Running Apps Locally

**Method 1: Direct JAR Execution (Simplest)**

In three separate terminal windows:

```bash
# Terminal 1
java -jar agent-app/target/agent-app-1.0.0-SNAPSHOT.jar
```

```bash
# Terminal 2
java -jar api-app/target/api-app-1.0.0-SNAPSHOT.jar
```

```bash
# Terminal 3
java -jar sdk-app/target/sdk-app-1.0.0-SNAPSHOT.jar
```

**Method 2: Maven Spring Boot Plugin**

In three separate terminal windows:

```bash
# Terminal 1
./mvnw -pl agent-app spring-boot:run
```

```bash
# Terminal 2
./mvnw -pl api-app spring-boot:run
```

```bash
# Terminal 3
./mvnw -pl sdk-app spring-boot:run
```

**Method 3: IDE Integration**

Run directly from your IDE (IntelliJ, VS Code) by:
1. Opening the project
2. Right-clicking on each `*Application.java` class
3. Selecting "Run"

#### Verify Apps Are Running

Check each endpoint (should return HTTP 200):

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

Or visit in browser:
- http://localhost:8081/actuator/health
- http://localhost:8082/actuator/health
- http://localhost:8083/actuator/health

## Generate telemetry

```bash
curl "http://localhost:8081/work?user=alice"
curl "http://localhost:8082/work?user=bob"
curl "http://localhost:8083/work?user=carol"
curl "http://localhost:8081/error"
curl "http://localhost:8082/error"
curl "http://localhost:8083/error"
```

## Docker Reference

### Advanced: Container with Full Observability Stack

To run apps with the full observability stack (Grafana, Prometheus, Loki, Tempo):

```bash
# Start observability stack
docker compose -f observability/docker-compose.yml up -d

# Start Java apps in containers
docker compose -f docker-compose.apps.yml up -d
```

The apps will automatically connect to the local observability services.

Check Grafana at `http://localhost:3000` (admin/admin).

### Container Networking

Apps automatically join the `java-apps` network and can communicate using container names:
- `http://agent-app:8081`
- `http://api-app:8082`
- `http://sdk-app:8083`

### Container Logs

```bash
# All app logs
docker compose -f docker-compose.apps.yml logs -f

# Specific app
docker compose -f docker-compose.apps.yml logs -f agent-app

# Follow new logs
docker compose -f docker-compose.apps.yml logs -f --tail=50
```

### Container Resource Management

Modify resource limits in `docker-compose.apps.yml`:

```yaml
services:
  agent-app:
    # ... existing config ...
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 512M
        reservations:
          cpus: '0.5'
          memory: 256M
```

## What to check

- Traces: Grafana Explore -> Tempo -> query `service.name="agent-app"` / `api-app` / `sdk-app`
- Logs: Grafana Explore -> Loki -> query `{job="java-apps"} |= "handled request"`
- Metrics: Grafana Explore -> Prometheus -> query `http_server_requests_seconds_count`
