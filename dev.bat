@echo off
chcp 65001 >nul 2>&1
REM ============================================
REM CSP 网安平台 - Windows 快速编译部署工具
REM ============================================
REM 用法:
REM   dev.bat build     - 完整构建前后端并部署
REM   dev.bat backend   - 仅构建后端并部署
REM   dev.bat frontend  - 仅构建前端并部署
REM   dev.bat jar       - 仅编译后端 jar（不部署）
REM   dev.bat deploy    - 仅部署已有 jar 到 Docker
REM   dev.bat hot       - 智能热更新（自动检测变更）
REM   dev.bat logs      - 查看后端日志
REM   dev.bat status    - 查看容器状态
REM   dev.bat restart   - 重启后端容器
REM ============================================

setlocal enabledelayedexpansion

set "ROOT_DIR=%~dp0"
set "BACKEND_DIR=%ROOT_DIR%backend"
set "FRONTEND_DIR=%ROOT_DIR%frontend"
set "DEV_DIR=%ROOT_DIR%dev"
set "M2_REPO=%DEV_DIR%\m2repo"
set "SETTINGS=%DEV_DIR%\settings.xml"
set "DOCKER=C:\Program Files\Docker\Docker\resources\bin\docker.exe"

if "%~1"=="" goto :help
if "%~1"=="build" goto :build
if "%~1"=="backend" goto :backend
if "%~1"=="be" goto :backend
if "%~1"=="frontend" goto :frontend
if "%~1"=="fe" goto :frontend
if "%~1"=="jar" goto :jar
if "%~1"=="deploy" goto :deploy
if "%~1"=="hot" goto :hot
if "%~1"=="logs" goto :logs
if "%~1"=="log" goto :logs
if "%~1"=="status" goto :status
if "%~1"=="st" goto :status
if "%~1"=="restart" goto :restart
if "%~1"=="rs" goto :restart
if "%~1"=="help" goto :help
goto :help

:build
echo.
echo === 完整构建前后端 ===
echo.
call :do_build_backend
call :do_build_frontend
call :do_show_status
goto :eof

:backend
echo.
echo === 构建后端 ===
echo.
call :do_build_backend
goto :eof

:frontend
echo.
echo === 构建前端 ===
echo.
call :do_build_frontend
goto :eof

:jar
echo.
echo === 仅编译后端 JAR ===
echo.
call :do_compile_jar
goto :eof

:deploy
echo.
echo === 部署后端到 Docker ===
echo.
call :do_build_image
call :do_deploy_backend
call :do_wait_backend
goto :eof

:hot
echo.
echo === 快速热更新 ===
echo.
call :do_compile_jar
if errorlevel 1 (
    echo [ERROR] 编译失败
    goto :eof
)
call :do_build_image
call :do_deploy_backend
call :do_wait_backend
echo.
echo [OK] 热更新完成!
goto :eof

:logs
%DOCKER% logs csp-backend --tail 50 -f
goto :eof

:status
echo.
echo === 容器状态 ===
echo.
%DOCKER% ps --filter "name=csp-" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo.
echo   前端:   http://localhost/
echo   后端:   http://localhost:8080/api
echo   Swagger: http://localhost:8080/swagger-ui/index.html
echo.
goto :eof

:restart
%DOCKER% restart csp-backend
call :do_wait_backend
goto :eof

:help
echo.
echo   CSP 网安平台 - 快速开发工具
echo.
echo   用法: dev.bat ^<命令^>
echo.
echo   构建命令:
echo     build         完整构建前后端并部署
echo     backend (be)  仅构建后端并部署
echo     frontend (fe) 仅构建前端并部署
echo     jar           仅编译后端 JAR（不部署）
echo     deploy        仅部署已有 JAR 到 Docker
echo     hot           快速热更新（编译+部署）
echo.
echo   运维命令:
echo     logs (log)    查看后端实时日志
echo     status (st)   查看容器状态和端口
echo     restart (rs)  重启后端容器
echo.
echo   开发流程:
echo     1. 修改代码
echo     2. 运行 dev.bat hot
echo     3. 刷新浏览器验收
echo.
goto :eof

REM ============================================
REM 内部函数
REM ============================================

:do_compile_jar
echo [1/3] 编译后端 Java 代码...
%DOCKER% run --rm ^
    -v "%BACKEND_DIR%:/app" ^
    -v "%M2_REPO%:/root/.m2/repository" ^
    -v "%SETTINGS%:/usr/share/maven/ref/settings.xml" ^
    -w /app ^
    maven:3.9-eclipse-temurin-21 ^
    mvn package -DskipTests -T 1C -q
if errorlevel 1 exit /b 1
echo [OK] JAR 编译完成
goto :eof

:do_build_image
echo [2/3] 构建后端 Docker 镜像...
%DOCKER% build --no-cache -t csp-backend:dev "%BACKEND_DIR%" -q
echo [OK] 镜像构建完成: csp-backend:dev
goto :eof

:do_deploy_backend
echo [3/3] 部署后端容器...
%DOCKER% stop csp-backend >nul 2>&1
%DOCKER% rm csp-backend >nul 2>&1
%DOCKER% run -d ^
    --name csp-backend ^
    --restart unless-stopped ^
    -p 8080:8080 ^
    --add-host host.docker.internal:host-gateway ^
    --env-file "%BACKEND_DIR%\.env.backend" ^
    csp-backend:dev >nul
echo [OK] 后端容器已启动 (port 8080)
goto :eof

:do_wait_backend
echo 等待后端启动...
for /L %%i in (1,1,30) do (
    %DOCKER% logs csp-backend 2>&1 | findstr "Started CspApplication" >nul 2>&1
    if not errorlevel 1 (
        echo [OK] 后端启动完成!
        goto :eof
    )
    timeout /t 1 /nobreak >nul
)
echo [WARN] 后端可能还在启动中，请检查: dev.bat logs
goto :eof

:do_build_backend
call :do_compile_jar
if errorlevel 1 (
    echo [ERROR] 后端编译失败
    exit /b 1
)
call :do_build_image
call :do_deploy_backend
call :do_wait_backend
goto :eof

:do_build_frontend
echo 构建前端...
cd /d "%FRONTEND_DIR%"
if not exist "node_modules" (
    echo 安装前端依赖...
    call npm install
)
call npm run build
echo 部署前端容器...
%DOCKER% restart csp-frontend >nul 2>&1
echo [OK] 前端已部署 (port 80)
cd /d "%ROOT_DIR%"
goto :eof

:do_show_status
echo.
echo === 容器状态 ===
%DOCKER% ps --filter "name=csp-" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo.
echo   前端:   http://localhost/
echo   后端:   http://localhost:8080/api
echo.
goto :eof
