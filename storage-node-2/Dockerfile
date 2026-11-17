# Stage 1: Build file .jar bằng Maven 
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Chạy file .jar bằng JRE nhẹ (Đổi từ temurin:17 sang temurin:21 hoặc cao hơn)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Lấy file .jar đã build từ Stage 1 sang
COPY --from=build /app/target/*.jar app.jar
# Mở port (thường Master Node chạy port 8000, storage-node-1 chạy 8081, v.v...)
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]