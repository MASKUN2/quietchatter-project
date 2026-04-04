FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# GitHub Actions 워크플로우에서 빌드한 jar 파일을 복사합니다.
ARG JAR_FILE=build/libs/*-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# DEVELOPMENT.md에 명시된 JVM 튜닝 (SerialGC 및 메모리 제한)
ENV JAVA_OPTS="-XX:+UseSerialGC -Xms256m -Xmx256m -XX:MaxMetaspaceSize=128m -Xss256k"

# 컨테이너 기본 포트
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
