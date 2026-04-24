FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

ENV MAVEN_OPTS="-Xmx512m -XX:+UseSerialGC"

COPY demo/pom.xml .
COPY demo/.mvn/ .mvn/
COPY demo/mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY demo/src/ src/
RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /www/algo-mind/uploads/avatars /www/algo-mind/uploads/chat-files && \
    chown -R appuser:appgroup /www/algo-mind

USER appuser

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
