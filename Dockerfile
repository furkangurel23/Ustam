# === Build stage: Maven ile jar üret ===
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Bağımlılık cache'i için önce pom.xml
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Sonra kaynak kodları
COPY src ./src
RUN mvn -q -DskipTests package

# === Run stage: minimal JRE ===
FROM eclipse-temurin:21-jre
WORKDIR /app

# (Opsiyonel) non-root kullanıcı
RUN useradd -ms /bin/sh appuser
USER appuser

# Üretilen jar'ı kopyala
COPY --from=build /workspace/target/*.jar app.jar

# Port
EXPOSE 8080

# JVM bayrakları için JAVA_OPTS kullan
ENV JAVA_OPTS=""

# Uygulamayı başlat
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]

#Multistage: Build aşamasındaki ağır imaj (Maven) çalıştırma imajına taşınmıyor → final imaj küçük ve hızlı.
#dependency:go-offline: Cache etkisiyle incremental build’ler hızlanır.
#Temurin 21: Spring Boot 3.x + Kotlin için uygun LTS JDK.
#User non-root: Güvenlik.