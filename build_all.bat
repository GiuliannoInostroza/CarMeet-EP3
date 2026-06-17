@echo off
echo =======================================================
echo Construyendo todos los microservicios (saltando tests)...
echo =======================================================

for /d %%i in (ms-*) do (
    echo.
    echo --- Empaquetando %%i ---
    cd %%i
    call mvnw.cmd clean package -DskipTests
    cd ..
)

echo.
echo =======================================================
echo ¡Todos los microservicios han sido empaquetados!
echo =======================================================
