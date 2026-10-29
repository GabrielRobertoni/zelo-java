# Etapa 1: compila o projeto com Maven e JDK 17
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# Etapa 2: imagem final apenas com o JRE e o jar
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --no-create-home zelo
COPY --from=build /app/target/zelo-java.jar app.jar
USER zelo
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
