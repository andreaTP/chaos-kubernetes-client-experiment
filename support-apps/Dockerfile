FROM maven:3.9.2-eclipse-temurin-17-focal AS builder
ARG APP_NAME
ENV APP_NAME=${APP_NAME}
ARG HTTP_CLIENT

WORKDIR /build

COPY . .
RUN mvn clean package -U -P${APP_NAME} -P${HTTP_CLIENT}

FROM eclipse-temurin:17

WORKDIR /app

COPY --from=builder /build/target/*.jar /app/
COPY --from=builder /build/target/app /app/

ENTRYPOINT [ "/app/app" ]
