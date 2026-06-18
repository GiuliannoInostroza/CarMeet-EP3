@echo off
echo ========================================
echo  CarMeet - Compilar todos los JARs
echo ========================================
echo.

echo [1/12] Compilando ms-eureka...
cd ms-eureka
call .\mvnw.cmd clean package -DskipTests
if %errorlevel% neq 0 ( echo ERROR en ms-eureka & pause & exit /b %errorlevel% )
cd ..

echo [2/12] Compilando ms-gateway...
cd ms-gateway
call .\mvnw.cmd clean package -DskipTests
if %errorlevel% neq 0 ( echo ERROR en ms-gateway & pause & exit /b %errorlevel% )
cd ..

echo [3/12] Compilando ms-auth-user...
cd ms-auth-user
call .\mvnw.cmd clean package -DskipTests
if %errorlevel% neq 0 ( echo ERROR en ms-auth-user & pause & exit /b %errorlevel% )
cd ..

echo [4/12] Compilando ms-event-core...
cd ms-event-core
call .\mvnw.cmd clean package -DskipTests
if %errorlevel% neq 0 ( echo ERROR en ms-event-core & pause & exit /b %errorlevel% )
cd ..

echo [5/12] Compilando ms-vehicle-registry...
cd ms-vehicle-registry
call .\mvnw.cmd clean package -DskipTests
if %errorlevel% neq 0 ( echo ERROR en ms-vehicle-registry & pause & exit /b %errorlevel% )
cd ..

echo [6/12] Compilando ms-ticketing...
cd ms-ticketing
call .\mvnw.cmd clean package -DskipTests
if %errorlevel% neq 0 ( echo ERROR en ms-ticketing & pause & exit /b %errorlevel% )
cd ..

echo [7/12] Compilando ms-competition-reg...
cd ms-competition-reg
call .\mvnw.cmd clean package -DskipTests
if %errorlevel% neq 0 ( echo ERROR en ms-competition-reg & pause & exit /b %errorlevel% )
cd ..

echo [8/12] Compilando ms-live-scoreboard...
cd ms-live-scoreboard
call .\mvnw.cmd clean package -DskipTests
if %errorlevel% neq 0 ( echo ERROR en ms-live-scoreboard & pause & exit /b %errorlevel% )
cd ..

echo [9/12] Compilando ms-venue-capacity...
cd ms-venue-capacity
call .\mvnw.cmd clean package -DskipTests
if %errorlevel% neq 0 ( echo ERROR en ms-venue-capacity & pause & exit /b %errorlevel% )
cd ..

echo [10/12] Compilando ms-payment-mock...
cd ms-payment-mock
call .\mvnw.cmd clean package -DskipTests
if %errorlevel% neq 0 ( echo ERROR en ms-payment-mock & pause & exit /b %errorlevel% )
cd ..

echo [11/12] Compilando ms-analytics-report...
cd ms-analytics-report
call .\mvnw.cmd clean package -DskipTests
if %errorlevel% neq 0 ( echo ERROR en ms-analytics-report & pause & exit /b %errorlevel% )
cd ..

echo [12/12] Compilando ms-notification-log...
cd ms-notification-log
call .\mvnw.cmd clean package -DskipTests
if %errorlevel% neq 0 ( echo ERROR en ms-notification-log & pause & exit /b %errorlevel% )
cd ..

echo.
echo ========================================
echo  Todos los JARs compilados exitosamente
echo ========================================
echo.
echo  Ahora ejecuta: docker compose up --build
echo.
pause
