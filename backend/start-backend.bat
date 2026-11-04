@echo off
docker rm csp-backend -f 2>nul
timeout /t 2 /nobreak >nul
docker run -d ^
  --name csp-backend ^
  -p 8080:8080 ^
  --add-host=host.docker.internal:host-gateway ^
  -e "SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/csp?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false" ^
  -e "SPRING_DATASOURCE_USERNAME=root" ^
  -e "SPRING_DATASOURCE_PASSWORD=csp123456" ^
  -e "SPRING_DATA_REDIS_HOST=host.docker.internal" ^
  -e "SPRING_DATA_REDIS_PORT=6379" ^
  -e "SPRING_DATA_MONGODB_URI=mongodb://host.docker.internal:27017/csp" ^
  csp-backend:latest
