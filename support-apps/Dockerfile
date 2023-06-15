FROM maven:3.9.2-eclipse-temurin-17-focal AS builder
ARG APP_NAME
ENV APP_NAME=${APP_NAME}

WORKDIR /build

COPY . .
RUN mvn clean package -P${APP_NAME}

FROM eclipse-temurin:17

WORKDIR /app

COPY --from=builder /build/target/*.jar /app/
COPY --from=builder /build/target/app /app/

ENTRYPOINT [ "/app/app" ]
