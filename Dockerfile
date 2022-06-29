FROM openjdk:17-alpine as builder

WORKDIR app
COPY . .
RUN ./gradlew assemble

FROM openjdk:17-alpine

WORKDIR app
COPY --from=builder /app/build/libs/candlesticks-1.1.1.jar .
EXPOSE 9000
CMD ["java", "-jar", "/app/candlesticks-1.1.1.jar"]