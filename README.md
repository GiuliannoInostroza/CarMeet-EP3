# Proyecto CarMeet - Evaluacion Parcial 3 (EP3)

**Integrantes:**
- Giulianno Inostroza
- Pascal Pacheco

Sistema de microservicios para gestion de eventos automotrices, competencias, registro de vehiculos y venta de tickets. Desarrollado con Spring Boot, arquitectura de microservicios, Eureka, Gateway, JWT, Swagger, HATEOAS y Docker.

## Arquitectura

| Microservicio | Puerto | Base de datos | Descripcion |
|---|---|---|---|
| ms-eureka | 8761 | - | Service Discovery |
| ms-gateway | 8777 | - | API Gateway (rutas lb://) |
| ms-auth-user | 8090 | db_auth | Autenticacion y usuarios JWT |
| ms-event-core | 8091 | db_event | Eventos y patrocinadores |
| ms-vehicle-registry | 8092 | db_vehicle | Vehiculos y mantenimientos |
| ms-ticketing | 8093 | db_ticketing | Tickets y beneficios |
| ms-competition-reg | 8094 | db_competition | Inscripciones a competencias |
| ms-live-scoreboard | 8095 | db_scoreboard | Puntuaciones en vivo |
| ms-venue-capacity | 8096 | db_venue | Recintos y control de aforo |
| ms-payment-mock | 8097 | db_payment | Pasarela de pago mock |
| ms-analytics-report | 8098 | db_analytics | Reportes y metricas |
| ms-notification-log | 8099 | db_notification | Notificaciones y adjuntos |

## Tecnologias
- Spring Boot, Spring Cloud, Eureka, Gateway
- JWT distribuido (mismo secret en todos los servicios)
- Swagger OpenAPI + HATEOAS
- MySQL 8 (1 contenedor por microservicio)
- Docker Compose
- Maven wrapper (mvnw.cmd)

## Requisitos previos
- Docker Desktop instalado y ejecutandose
- Git
- Puerto 8761, 8777, 3310-3319, 8090-8099 libres

## Instrucciones de ejecucion (Docker)

### Paso 1: Compilar los JARs
Cada microservicio debe generar su .jar antes de construir las imagenes Docker:

```batch
build-all.bat
```

Esto ejecuta `mvn clean package -DskipTests` en los 12 servicios.

### Paso 2: Levantar la arquitectura

```batch
docker compose up --build
```

### Paso 3: Verificar Eureka
Abrir en el navegador: http://localhost:8761
Deben aparecer registrados los 10 microservicios + ms-gateway.

### Paso 4: Probar login via Gateway
```
POST http://localhost:8777/api/v1/auth/login
Body: { "username": "admin", "password": "1234" }
```

### Paso 5: Probar endpoint protegido
```
GET http://localhost:8777/api/v1/eventos
Authorization: Bearer ACCESS_TOKEN
```

### Comandos utiles
| Comando | Descripcion |
|---|---|
| `docker compose up --build` | Construye y levanta todo |
| `docker compose down` | Detiene y elimina contenedores (mantiene datos) |
| `docker compose down -v` | Detiene y elimina tambien los volumenes (borra BD) |
| `docker compose restart ms-auth-user` | Reinicia un servicio especifico |
| `docker logs -f ms-auth-user` | Ver logs en vivo de un servicio |

## Swagger UI
Cada microservicio expone su propia documentacion Swagger:

| Servicio | URL Swagger |
|---|---|
| ms-auth-user | http://localhost:8090/swagger-ui.html |
| ms-event-core | http://localhost:8091/swagger-ui.html |
| ms-vehicle-registry | http://localhost:8092/swagger-ui.html |
| ms-ticketing | http://localhost:8093/swagger-ui.html |
| ms-competition-reg | http://localhost:8094/swagger-ui.html |
| ms-live-scoreboard | http://localhost:8095/swagger-ui.html |
| ms-venue-capacity | http://localhost:8096/swagger-ui.html |
| ms-payment-mock | http://localhost:8097/swagger-ui.html |
| ms-analytics-report | http://localhost:8098/swagger-ui.html |
| ms-notification-log | http://localhost:8099/swagger-ui.html |

## Gateway - Rutas disponibles
El Gateway centraliza el consumo via `lb://`  con Eureka:

| Ruta | Servicio destino |
|---|---|
| `/api/v1/auth/**` | ms-auth-user |
| `/api/v1/eventos/**` | ms-event-core |
| `/api/v1/vehiculos/**` | ms-vehicle-registry |
| `/api/v1/tickets/**` | ms-ticketing |
| `/api/v1/inscripciones/**` | ms-competition-reg |
| `/api/v1/puntuaciones/**` | ms-live-scoreboard |
| `/api/v1/recintos/**` | ms-venue-capacity |
| `/api/v1/pagos/**` | ms-payment-mock |
| `/api/v1/reportes/**` | ms-analytics-report |
| `/api/v1/notificaciones/**` | ms-notification-log |

## Pruebas sin Docker (alternativa)
Para ejecutar sin Docker, se necesita MySQL local con las 10 bases de datos creadas. Luego ejecutar cada JAR:

```batch
java -jar ms-eureka\target\ms-eureka-0.0.1-SNAPSHOT.jar
java -jar ms-auth-user\target\ms-auth-user-0.0.1-SNAPSHOT.jar
...
```
