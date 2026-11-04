FROM eclipse-temurin:21-jre-alpine

# 安装 Docker CLI（用于容器内管理 Docker）
RUN apk add --no-cache docker-cli

WORKDIR /app

COPY csp-platform/target/csp-platform.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m"
ENV DOCKER_HOST=unix:///var/run/docker.sock

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
