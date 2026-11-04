docker context use default
docker rm csp-backend -f 2>$null
Start-Sleep -Seconds 2
docker run -d --name csp-backend -p 8080:8080 --add-host=host.docker.internal:host-gateway --env-file "D:/tmp/网安平台项目/旧项目源码/网安平台项目/backend/.env.backend" csp-backend:latest
