# Build:  docker build -t omg-matchmaker server/
# Run:    docker run -p 8080:8080 omg-matchmaker            (in-memory store)
#         docker compose -f server/docker-compose.yml up    (with Postgres, Prometheus, Grafana)

# ---- build stage: Maven + JDK, dependencies cached in their own layer ----
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---- runtime stage: JRE only, non-root ----
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S omg && adduser -S -G omg -u 10001 omg
WORKDIR /app
COPY --from=build /src/target/omg-matchmaking-server.jar app.jar
USER omg
EXPOSE 8080
# Size the heap from the container's memory limit; crash fast (and get restarted) on OOM.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
HEALTHCHECK --interval=10s --timeout=2s --start-period=20s --retries=3 \
  CMD wget -q -O /dev/null http://localhost:8080/health/live || exit 1
# exec so the JVM is PID 1 and receives SIGTERM directly (graceful drain on docker stop).
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
