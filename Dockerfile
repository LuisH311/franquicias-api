FROM maven:3.9.11-eclipse-temurin-17 AS compilacion

WORKDIR /app

COPY pom.xml ./
COPY franquicias/ ./franquicias/
COPY application.properties schema.sql ./

# Compila y empaqueta la aplicación sin ejecutar pruebas.
RUN mvn -B clean package -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=compilacion /app/target/franquicias-1.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

