# syntax=docker/dockerfile:1

# Stage 1: Dependencies resolution
FROM registry.access.redhat.com/ubi9-minimal:9.4-1227.1726694542 AS deps

RUN microdnf install -y --nodocs java-21-openjdk-devel maven which && microdnf clean all
ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk
ENV PATH=$JAVA_HOME/bin:$PATH


WORKDIR /build

# Copy Maven configuration and pom.xml
COPY pom.xml pom.xml

# Download dependencies as a separate step to take advantage of Docker's caching
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -DskipTests

# Stage 2: Application packaging
FROM deps AS package

WORKDIR /build

# Copy source code
COPY ./src src/
COPY pom.xml pom.xml

# Build the application
RUN --mount=type=cache,target=/root/.m2 mvn package -DskipTests
RUN mv target/$(mvn help:evaluate \
    -Dexpression=project.artifactId -q -DforceStdout)-$(mvn help:evaluate \
    -Dexpression=project.version -q -DforceStdout).jar target/app.jar

# Stage 3: Extract application layers
FROM package AS extract

WORKDIR /build

# Extract Spring Boot layers for efficient caching
RUN java -Djarmode=layertools -jar target/app.jar extract --destination target/extracted

# Stage 4: Final runtime image
FROM registry.access.redhat.com/ubi9-minimal:9.4-1227.1726694542 AS final

RUN microdnf install -y --nodocs java-21-openjdk-headless which && microdnf clean all

# Create a non-privileged user that the app will run under
ARG UID=10001
RUN groupadd --system --gid ${UID} appuser && \
    useradd --system -M \
    --comment "" \
    --home-dir /nonexistent \
    --shell /sbin/nologin \
    --uid "${UID}" \
    --gid "${UID}" \
    appuser

WORKDIR /app

COPY --from=extract --chown=appuser:appuser /build/target/extracted/dependencies/ ./
COPY --from=extract --chown=appuser:appuser /build/target/extracted/spring-boot-loader/ ./
COPY --from=extract --chown=appuser:appuser /build/target/extracted/snapshot-dependencies/ ./
COPY --from=extract --chown=appuser:appuser /build/target/extracted/application/ ./

RUN mkdir -p /app/logs /app/tmp && \
    chown -R appuser:appuser /app

# Copy and set permissions for healthcheck script (as root)
COPY healthcheck.sh /usr/local/bin/healthcheck.sh
RUN chmod +x /usr/local/bin/healthcheck.sh

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD ["/usr/local/bin/healthcheck.sh"]

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"] 