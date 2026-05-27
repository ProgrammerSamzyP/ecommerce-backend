FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy Maven wrapper and POM first (for dependency caching)
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline

# Copy source code and build
COPY src ./src
RUN ./mvnw package -DskipTests

# Rename the built JAR to a fixed name
RUN cp target/*.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]