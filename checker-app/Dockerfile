FROM maven:3.9.2-eclipse-temurin-17-focal AS builder

WORKDIR /build

COPY . .
RUN mvn clean package

FROM eclipse-temurin:17

WORKDIR /app

COPY --from=builder /build/target/*.jar .
COPY --from=builder /build/target/checker .

ENTRYPOINT ["/app/checker"]
