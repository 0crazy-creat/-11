@echo off
chcp 65001 >nul
REM Phase 1 快速启动脚本 (Windows)
REM 一键完成数据库迁移、后端编译、服务启动

echo =========================================
echo Phase 1 快速启动
echo =========================================
echo.

set PROJECT_ROOT=%~dp0
cd /d "%PROJECT_ROOT%"

REM 检查依赖
echo 检查依赖...
where docker >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo 错误: 未安装 Docker
    pause
    exit /b 1
)
echo √ 依赖检查完成
echo.

REM 步骤 1: 数据库迁移
echo 步骤 1/4: 数据库迁移
set /p MYSQL_PASSWORD=请输入 MySQL root 密码: 

echo 执行迁移脚本...
for %%f in (deploy\sql\18_abac_policy.sql deploy\sql\19_team_invite_code.sql deploy\sql\20_anti_cheat_deep.sql deploy\sql\21_awd_traffic_capture.sql) do (
    echo   执行 %%~nxf...
    docker exec -i mysql mysql -uroot -p%MYSQL_PASSWORD% csp_platform < "%%f"
)
echo √ 数据库迁移完成
echo.

REM 步骤 2: 后端编译
echo 步骤 2/4: 后端编译
cd backend
echo 使用阿里云镜像编译...
docker run --rm -v "%CD%:/app" -v "%CD%\settings.xml:/root/.m2/settings.xml" -w /app maven:3.9-eclipse-temurin-21 mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo 编译失败
    cd ..
    pause
    exit /b 1
)
cd ..
echo √ 后端编译完成
echo.

REM 步骤 3: 启动后端服务
echo 步骤 3/4: 启动后端服务
cd deploy
docker-compose up -d backend
cd ..
echo 等待服务启动...
timeout /t 10 /nobreak >nul

REM 检查服务状态
curl -s http://localhost:8080/actuator/health | findstr "UP" >nul
if %ERRORLEVEL% NEQ 0 (
    echo ✗ 后端服务启动失败，请检查日志
    pause
    exit /b 1
)
echo √ 后端服务启动成功
echo.

REM 步骤 4: 前端构建（可选）
echo 步骤 4/4: 前端构建
set /p BUILD_FRONTEND=是否构建前端？(y/n): 
if /i "%BUILD_FRONTEND%"=="y" (
    cd frontend
    call npm install
    call npm run build
    cd ..
    echo √ 前端构建完成
    
    set /p DEPLOY_NGINX=是否部署到 nginx？(y/n): 
    if /i "%DEPLOY_NGINX%"=="y" (
        xcopy /E /Y /I frontend\dist\* C:\nginx\html\
        nginx -s reload
        echo √ 前端部署完成
    )
) else (
    echo 跳过前端构建
)
echo.

REM 完成
echo =========================================
echo √ Phase 1 部署完成！
echo =========================================
echo.
echo 访问地址：
echo   前端: http://localhost
echo   后端 API: http://localhost:8080
echo   API 文档: http://localhost:8080/swagger-ui.html
echo.
echo 下一步：
echo   1. 运行 API 测试: deploy\test_phase1_api.sh
echo   2. 或使用 Postman: deploy\postman_collection_phase1.json
echo.
pause
