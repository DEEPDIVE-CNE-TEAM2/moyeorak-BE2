FROM amazoncorretto:17-alpine
WORKDIR /moyeorak-BE
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY build/libs/moyeorak-0.0.1-SNAPSHOT.jar moyeorak.jar
RUN chown appuser:appgroup moyeorak.jar
USER appuser
ENTRYPOINT ["java", "-jar", "moyeorak.jar"]

