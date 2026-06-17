# CarMeet - Evaluacion Parcial 3

**Integrantes:**
- Giulianno Inostroza
- Pascal Pacheco

## Descripcion del proyecto

CarMeet es un sistema de microservicios desarrollado en Spring Boot para la gestion de eventos automotrices. La plataforma permite organizar competencias, registrar vehiculos participantes, vender tickets, controlar el aforo de los recintos, simular pagos y generar reportes de asistencia. Cada microservicio es independiente, con su propia base de datos y su propia logica de negocio.

## Microservicios implementados

El proyecto cuenta con 12 microservicios en total (10 de negocio + 2 de infraestructura).

### Infraestructura

| Servicio | Puerto | URL Swagger |
|---|---|---|
| ms-eureka | 8761 | none (es el registro de servicios) |
| ms-gateway | 8777 | none (centraliza las rutas) |

### Negocio

| # | Microservicio | Puerto | Base de datos | Swagger UI | Entidades |
|---|---|---|---|---|---|
| 1 | ms-auth-user | 8090 | db_auth | http://localhost:8090/swagger-ui.html | Usuario, RefreshToken |
| 2 | ms-event-core | 8091 | db_event | http://localhost:8091/swagger-ui.html | Evento, Patrocinador |
| 3 | ms-vehicle-registry | 8092 | db_vehicle | http://localhost:8092/swagger-ui.html | Vehiculo, Mantenimiento |
| 4 | ms-ticketing | 8093 | db_ticketing | http://localhost:8093/swagger-ui.html | Ticket, Beneficio |
| 5 | ms-competition-reg | 8094 | db_competition | http://localhost:8094/swagger-ui.html | Inscripcion, Requisito |
| 6 | ms-live-scoreboard | 8095 | db_scoreboard | http://localhost:8095/swagger-ui.html | Puntuacion, DetallePuntuacion |
| 7 | ms-venue-capacity | 8096 | db_venue | http://localhost:8096/swagger-ui.html | Recinto, Zona |
| 8 | ms-payment-mock | 8097 | db_payment | http://localhost:8097/swagger-ui.html | Pago, TransaccionLog |
| 9 | ms-analytics-report | 8098 | db_analytics | http://localhost:8098/swagger-ui.html | Reporte, Metrica |
| 10 | ms-notification-log | 8099 | db_notification | http://localhost:8099/swagger-ui.html | Notificacion, Adjunto |

## Rutas del API Gateway

El gateway corre en el puerto 8777 y enruta usando el nombre de cada servicio registrado en Eureka (lb://). Estas son las rutas definidas:

| Ruta | Destino | Metodo |
|---|---|---|
| /api/v1/auth/** | lb://ms-auth-user | POST login, register, refresh |
| /api/v1/eventos/** | lb://ms-event-core | CRUD eventos |
| /api/v1/vehiculos/** | lb://ms-vehicle-registry | CRUD vehiculos |
| /api/v1/tickets/** | lb://ms-ticketing | CRUD tickets |
| /api/v1/inscripciones/** | lb://ms-competition-reg | CRUD inscripciones |
| /api/v1/puntuaciones/** | lb://ms-live-scoreboard | CRUD puntuaciones |
| /api/v1/recintos/** | lb://ms-venue-capacity | CRUD recintos |
| /api/v1/pagos/** | lb://ms-payment-mock | CRUD pagos |
| /api/v1/reportes/** | lb://ms-analytics-report | CRUD reportes |
| /api/v1/notificaciones/** | lb://ms-notification-log | CRUD notificaciones |

Ejemplo de uso: en vez de llamar directamente a http://localhost:8090/api/v1/auth/login, se llama a http://localhost:8777/api/v1/auth/login.

## Service Discovery con Eureka

Eureka corre en el puerto 8761. Todos los microservicios se registran ahi con su spring.application.name. El gateway consulta Eureka para resolver las rutas lb://. Para ver los servicios registrados hay que abrir http://localhost:8761 en el navegador.

## Detalles tecnicos

- **Patron CSR**: cada microservicio tiene controller, service, repository, model y dto en paquetes separados.
- **Seguridad**: JWT con JwtFilter en todos los servicios de negocio. El token se genera en ms-auth-user y se valida en los demas. Swagger esta liberado (no pide token para abrir la UI).
- **Swagger**: cada servicio tiene su propia documentacion OpenAPI con el esquema bearerAuth para probar endpoints protegidos.
- **HATEOAS**: los endpoints GET por ID devuelven _links de navegacion (self, all, update, delete).
- **Base de datos**: una base MySQL por microservicio. Las migraciones se manejan con Flyway. Para entornos Docker se usa ademas spring.jpa.hibernate.ddl-auto=update.
- **Comunicacion entre servicios**: algunos microservicios se comunican via WebClient o FeignClient. Por ejemplo, ms-ticketing consulta ms-event-core para validar eventos, ms-competition-reg consulta ms-vehicle-registry para validar autos, ms-analytics-report consume datos de varios servicios.
- **Manejo de errores**: GlobalExceptionHandler con @RestControllerAdvice en todos los servicios. Las respuestas siguen el formato ApiResponse { success, message, data, error }.
- **Pruebas unitarias**: cada servicio de negocio tiene pruebas para service (Mockito), repository (H2 + DataJpaTest) y controller (MockMvc). Se uso JaCoCo para medir cobertura.

## Requisitos para ejecutar

- Java 21 (JDK)
- Maven (o usar los wrappers mvnw.cmd incluidos)
- Docker Desktop

## Ejecucion con Docker

```bash
docker compose up --build
```

Esto construye las imagenes y levanta todos los contenedores: 3 MySQL, 10 servicios de negocio, ms-eureka y ms-gateway. Una vez que esten todos arriba, probar el login:

```bash
curl -X POST http://localhost:8777/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"admin\", \"password\": \"1234\"}"
```

Copiar el accessToken de la respuesta y usarlo como Bearer Token para probar los demas endpoints a traves del Gateway. Por ejemplo:

```bash
curl http://localhost:8777/api/v1/eventos \
  -H "Authorization: Bearer <token>"
```

## Ejecucion sin Docker (solo si no funciona Docker)

Si no se puede usar Docker, se puede ejecutar cada microservicio individualmente desde el IDE siguiendo el orden de arranque que define el docker-compose.yml:
1. MySQL con las 10 bases creadas manualmente
2. ms-eureka
3. ms-auth-user
4. Los 9 servicios de negocio restantes
5. ms-gateway

## Ejecutar pruebas unitarias

```bash
cd ms-auth-user
.\mvnw.cmd clean test
```

Reemplazar ms-auth-user por cualquier otro microservicio. Cada servicio genera su reporte de cobertura en target/site/jacoco/index.html.

## Consideraciones

- Todas las properties sensibles usan variables de entorno con valor por defecto (${VAR:default}).
- El JWT_SECRET debe ser el mismo en todos los servicios de negocio para que el token sea valido.
- Si se usa Docker, las variables DB_HOST apuntan al nombre del contenedor MySQL correspondiente, no a localhost.
