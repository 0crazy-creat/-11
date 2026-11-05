# ============================================================
# CSP 后端镜像（自包含多阶段构建）
# 直接 `docker compose build` 即可，无需先在宿主机 mvn package
# ============================================================

# ---------- 阶段1：Maven 构建 ----------
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# 先拷贝 pom 利用缓存（可选优化）
COPY pom.xml .
COPY settings.xml .
COPY settings-aliyun.xml .

# 拷贝源码
COPY . .

# 构建 platform 可执行 jar（跳过测试）
RUN mvn clean package -DskipTests -pl csp-platform -am \
    -s settings.xml || \
    mvn clean package -DskipTests -pl csp-platform -am \
    -s settings-aliyun.xml

# ---------- 阶段2：运行时镜像 ----------
FROM eclipse-temurin:21-jre-alpine

# 安装 Docker CLI（容器内管理靶场容器）
RUN apk add --no-cache docker-cli

WORKDIR /app

COPY --from=builder /build/csp-platform/target/csp-platform.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m"
ENV DOCKER_HOST=unix:///var/run/docker.sock

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]