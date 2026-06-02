# CarMeet — Guía de Pruebas Postman

> Orden de arranque obligatorio: levanta los 10 servicios antes de probar.
> Todo endpoint (salvo login/register/refresh) requiere el header `Authorization: Bearer <token>`.

---

## Puertos de referencia

| Microservicio | Puerto |
|---|---|
| ms-auth-user | 8090 |
| ms-event-core | 8091 |
| ms-vehicle-registry | 8092 |
| ms-ticketing | 8093 |
| ms-competition-reg | 8094 |
| ms-live-scoreboard | 8095 |
| ms-venue-capacity | 8096 |
| ms-payment-mock | 8097 |
| ms-analytics-report | 8098 |
| ms-notification-log | 8099 |

---

## PASO 1 — Autenticación (ms-auth-user :8090)

> Siempre empieza aquí. Sin JWT no puedes probar nada más.

### Registrar usuario

```
POST http://localhost:8090/api/v1/auth/register
Content-Type: application/json
```
```json
{ "username": "testuser", "password": "123456", "role": "ROLE_USER" }
```
**201** → devuelve `accessToken` y `refreshToken`
**400** → username ya existe

---

### Registrar admin

```
POST http://localhost:8090/api/v1/auth/register
Content-Type: application/json
```
```json
{ "username": "admin", "password": "admin123", "role": "ROLE_ADMIN" }
```
**201** → guarda el `accessToken` como variable `{{token}}` en Postman

---

### Login

```
POST http://localhost:8090/api/v1/auth/login
Content-Type: application/json
```
```json
{ "username": "admin", "password": "admin123" }
```
**200** → `{ accessToken, refreshToken }`
**401** → credenciales incorrectas

---

### Renovar token

```
POST http://localhost:8090/api/v1/auth/refresh
Content-Type: application/json
```
```json
{ "refreshToken": "<tu_refresh_token>" }
```
**200** → nuevo `accessToken`
**401** → refresh token expirado o inválido

---

### Ver usuario actual

```
GET http://localhost:8090/api/v1/auth/me
Authorization: Bearer {{token}}
```
**200** → datos del usuario autenticado
**401** → token inválido o ausente

---

### Listar usuarios (solo ADMIN)

```
GET http://localhost:8090/api/v1/auth/usuarios
Authorization: Bearer {{token}}
```
**200** → lista de usuarios
**403** → no eres ADMIN

---

### Obtener usuario por username

```
GET http://localhost:8090/api/v1/auth/usuarios/testuser
Authorization: Bearer {{token}}
```
**200** → datos del usuario
**404** → username no existe

---

### Promover a ADMIN

```
PUT http://localhost:8090/api/v1/auth/promote/testuser
Authorization: Bearer {{token}}
```
**200** → usuario promovido
**403** → no eres ADMIN

---

### Degradar a USER

```
PUT http://localhost:8090/api/v1/auth/demote/testuser
Authorization: Bearer {{token}}
```
**200** → usuario degradado

---

## PASO 2 — Eventos (ms-event-core :8091)

> Crea un evento primero. Los demás servicios lo necesitan para validar.

### Crear evento

```
POST http://localhost:8091/api/v1/eventos
Authorization: Bearer {{token}}
Content-Type: application/json
```
```json
{
  "nombre": "CarMeet Verano 2026",
  "fecha": "2026-08-15",
  "ubicacion": "Autodromo Nacional",
  "patrocinadores": [
    { "nombre": "Shell", "nivel": "ORO" },
    { "nombre": "Castrol", "nivel": "PLATA" }
  ]
}
```
**201** → evento creado con `id` (guarda ese id, lo usarás en todo)
**401** → sin token

> `nivel` válido: `ORO`, `PLATA`, `BRONCE`

---

### Listar eventos

```
GET http://localhost:8091/api/v1/eventos
Authorization: Bearer {{token}}
```
**200** → lista de eventos

---

### Obtener evento por ID

```
GET http://localhost:8091/api/v1/eventos/1
Authorization: Bearer {{token}}
```
**200** → evento con patrocinadores
**404** → evento no existe

---

### Actualizar evento

```
PUT http://localhost:8091/api/v1/eventos/1
Authorization: Bearer {{token}}
Content-Type: application/json
```
```json
{
  "nombre": "CarMeet Verano 2026 - Edición Especial",
  "fecha": "2026-08-20",
  "ubicacion": "Autodromo Nacional",
  "patrocinadores": []
}
```
**200** → evento actualizado

---

### Eliminar evento

```
DELETE http://localhost:8091/api/v1/eventos/1
Authorization: Bearer {{token}}
```
**200** → eliminado
**404** → no existe

---

### Eventos futuros

```
GET http://localhost:8091/api/v1/eventos/proximos
Authorization: Bearer {{token}}
```
**200** → lista de eventos con fecha mayor a hoy

---

### Buscar por nombre

```
GET http://localhost:8091/api/v1/eventos/buscar?nombre=verano
Authorization: Bearer {{token}}
```
**200** → eventos que coincidan (búsqueda parcial)

---

### Patrocinadores de un evento

```
GET http://localhost:8091/api/v1/eventos/1/patrocinadores
Authorization: Bearer {{token}}
```
**200** → lista de patrocinadores
**404** → evento no existe

---

## PASO 3 — Vehículos (ms-vehicle-registry :8092)

> Crea al menos un vehículo. Lo necesitas para las inscripciones.

### Crear vehículo

```
POST http://localhost:8092/api/v1/vehiculos
Authorization: Bearer {{token}}
Content-Type: application/json
```
```json
{
  "marca": "Toyota",
  "modelo": "Supra",
  "anio": 2022,
  "mantenimientos": [
    { "descripcion": "Cambio de aceite y filtros" }
  ]
}
```
**201** → vehículo creado con `id`

---

### Listar / Obtener / Actualizar / Eliminar

```
GET    http://localhost:8092/api/v1/vehiculos
GET    http://localhost:8092/api/v1/vehiculos/1
PUT    http://localhost:8092/api/v1/vehiculos/1   (mismo body que crear)
DELETE http://localhost:8092/api/v1/vehiculos/1
Authorization: Bearer {{token}}
```
**200/201** → OK
**404** → no existe

---

### Agregar mantenimiento

```
POST http://localhost:8092/api/v1/vehiculos/1/mantenimientos
Authorization: Bearer {{token}}
Content-Type: application/json
```
```json
{ "descripcion": "Revisión de frenos y suspensión" }
```
**201** → mantenimiento agregado sin borrar los anteriores
**404** → vehículo no existe

---

### Historial de mantenimientos

```
GET http://localhost:8092/api/v1/vehiculos/1/mantenimientos
Authorization: Bearer {{token}}
```
**200** → lista de mantenimientos

---

### Buscar por modelo

```
GET http://localhost:8092/api/v1/vehiculos/buscar?placa=Supra
Authorization: Bearer {{token}}
```
**200** → vehículos que coincidan

---

## PASO 4 — Recintos (ms-venue-capacity :8096)

### Crear recinto

```
POST http://localhost:8096/api/v1/recintos
Authorization: Bearer {{token}}
Content-Type: application/json
```
```json
{
  "nombre": "Sector A",
  "capacidadMaxima": 500,
  "ocupacionActual": 0,
  "zonas": [
    { "nombre": "Tribuna Norte", "capacidad": 200, "ocupacion": 0 },
    { "nombre": "Pit Lane", "capacidad": 50, "ocupacion": 0 }
  ]
}
```
**201** → recinto creado con `id`

---

### Listar / Obtener / Actualizar / Eliminar

```
GET    http://localhost:8096/api/v1/recintos
GET    http://localhost:8096/api/v1/recintos/1
PUT    http://localhost:8096/api/v1/recintos/1
DELETE http://localhost:8096/api/v1/recintos/1
Authorization: Bearer {{token}}
```

---

### Zonas del recinto

```
GET http://localhost:8096/api/v1/recintos/1/zonas
Authorization: Bearer {{token}}
```
**200** → lista de zonas con capacidad y ocupación

---

### Disponibilidad

```
GET http://localhost:8096/api/v1/recintos/1/disponibilidad
Authorization: Bearer {{token}}
```
**200** → `{ disponible, capacidadMaxima, ocupacionActual, plazasLibres }`

---

### Registrar ingreso / egreso

```
POST http://localhost:8096/api/v1/recintos/1/registrar-ingreso
POST http://localhost:8096/api/v1/recintos/1/registrar-egreso
Authorization: Bearer {{token}}
```
**200** → ocupación actualizada
**400** → recinto lleno (ingreso) o ya vacío (egreso)

---

## PASO 5 — Tickets (ms-ticketing :8093)

> Al crear un ticket, el servicio valida el evento en ms-event-core via WebClient.

### Crear ticket

```
POST http://localhost:8093/api/v1/tickets
Authorization: Bearer {{token}}
Content-Type: application/json
```
```json
{
  "eventoId": 1,
  "precio": 50000,
  "categoria": "VIP",
  "username": "testuser",
  "beneficios": [
    {
      "nombre": "Pase a Pits",
      "descripcion": "Acceso al área de pits durante la competencia"
    }
  ]
}
```
**201** → ticket creado con `estado: PENDIENTE`
**500** → ms-event-core no responde o el evento no existe

> `categoria` válida: `VIP`, `GENERAL`, `PALCO`

---

### Listar / Obtener / Actualizar / Eliminar

```
GET    http://localhost:8093/api/v1/tickets
GET    http://localhost:8093/api/v1/tickets/1
PUT    http://localhost:8093/api/v1/tickets/1
DELETE http://localhost:8093/api/v1/tickets/1
Authorization: Bearer {{token}}
```

---

### Tickets por evento

```
GET http://localhost:8093/api/v1/tickets/evento/1
Authorization: Bearer {{token}}
```
**200** → todos los tickets del evento

---

### Tickets por usuario

```
GET http://localhost:8093/api/v1/tickets/usuario/testuser
Authorization: Bearer {{token}}
```
**200** → todos los tickets del usuario

---

### Cancelar ticket

```
PATCH http://localhost:8093/api/v1/tickets/1/cancelar
Authorization: Bearer {{token}}
```
**200** → `estado: CANCELADO`
**500** → ticket no está en PENDIENTE

---

### Pagar ticket ⚡ inter-servicio

> Llama a ms-payment-mock → si aprobado, actualiza estado → notifica a ms-notification-log.
> El mock tiene 80% de aprobación. Si falla, intenta de nuevo.

```
PATCH http://localhost:8093/api/v1/tickets/1/pagar
Authorization: Bearer {{token}}
Content-Type: application/json
```
```json
{ "metodoPago": "TARJETA" }
```
**200** → `estado: PAGADO`
**500** → pago rechazado (intenta de nuevo) o ticket no está en PENDIENTE

> `metodoPago` válido: `TARJETA`, `EFECTIVO`, `TRANSFERENCIA`

---

## PASO 6 — Inscripciones (ms-competition-reg :8094)

> Al crear valida el evento. Al aprobar valida el vehículo.

### Crear inscripción

```
POST http://localhost:8094/api/v1/inscripciones
Authorization: Bearer {{token}}
Content-Type: application/json
```
```json
{
  "vehiculoId": 1,
  "eventoId": 1,
  "participante": "Juan Pérez",
  "categoria": "Drift",
  "username": "testuser",
  "requisitos": [
    {
      "nombre": "Licencia de conducir",
      "descripcion": "Vigente y en buen estado"
    }
  ]
}
```
**201** → inscripción con `estado: PENDIENTE`
**500** → evento no existe en ms-event-core

---

### Listar / Obtener / Actualizar / Eliminar

```
GET    http://localhost:8094/api/v1/inscripciones
GET    http://localhost:8094/api/v1/inscripciones/1
PUT    http://localhost:8094/api/v1/inscripciones/1
DELETE http://localhost:8094/api/v1/inscripciones/1
Authorization: Bearer {{token}}
```

---

### Por evento / por vehículo

```
GET http://localhost:8094/api/v1/inscripciones/evento/1
GET http://localhost:8094/api/v1/inscripciones/vehiculo/1
Authorization: Bearer {{token}}
```
**200** → lista filtrada

---

### Aprobar inscripción ⚡ inter-servicio

> Valida vehículo en ms-vehicle-registry + notifica en ms-notification-log.

```
PATCH http://localhost:8094/api/v1/inscripciones/1/aprobar
Authorization: Bearer {{token_admin}}
```
**200** → `estado: APROBADA`
**403** → no eres ADMIN
**500** → no está en PENDIENTE o vehículo no existe

---

### Rechazar inscripción ⚡ inter-servicio

> Notifica al usuario en ms-notification-log.

```
PATCH http://localhost:8094/api/v1/inscripciones/1/rechazar
Authorization: Bearer {{token_admin}}
```
**200** → `estado: RECHAZADA`
**403** → no eres ADMIN
**500** → no está en PENDIENTE

---

## PASO 7 — Puntuaciones (ms-live-scoreboard :8095)

> Al registrar valida que la inscripción exista en ms-competition-reg.

### Registrar puntuación

```
POST http://localhost:8095/api/v1/puntuaciones
Authorization: Bearer {{token}}
Content-Type: application/json
```
```json
{
  "inscripcionId": 1,
  "eventoId": 1,
  "puntos": 850,
  "detalles": [
    {
      "categoria": "Drift",
      "puntosAsignados": 450,
      "descripcion": "Excelente control del sobreviraje"
    },
    {
      "categoria": "Presentacion",
      "puntosAsignados": 400,
      "descripcion": "Vehículo impecable"
    }
  ]
}
```
**201** → puntuación registrada
**500** → inscripción no existe en ms-competition-reg

---

### Listar / Obtener / Actualizar / Eliminar

```
GET    http://localhost:8095/api/v1/puntuaciones
GET    http://localhost:8095/api/v1/puntuaciones/1
PUT    http://localhost:8095/api/v1/puntuaciones/1
DELETE http://localhost:8095/api/v1/puntuaciones/1
Authorization: Bearer {{token}}
```

---

### Ranking por evento

```
GET http://localhost:8095/api/v1/puntuaciones/evento/1
Authorization: Bearer {{token}}
```
**200** → puntuaciones del evento ordenadas por puntos DESC

---

### Por inscripción

```
GET http://localhost:8095/api/v1/puntuaciones/inscripcion/1
Authorization: Bearer {{token}}
```
**200** → puntuaciones de esa inscripción

---

### Top 10 global

```
GET http://localhost:8095/api/v1/puntuaciones/ranking
Authorization: Bearer {{token}}
```
**200** → los 10 mejores puntajes del sistema

---

## PASO 8 — Pagos (ms-payment-mock :8097)

> Normalmente es llamado por ms-ticketing automáticamente. Puedes probarlo directo también.

### Procesar pago (mock)

```
POST http://localhost:8097/api/v1/pagos/procesar
Authorization: Bearer {{token}}
Content-Type: application/json
```
```json
{ "ticketId": 1, "monto": 50000, "metodoPago": "TARJETA" }
```
**200** → TransaccionLog `APROBADO` (80% de probabilidad)
**500** → TransaccionLog `RECHAZADO` (20% de probabilidad)

---

### Crear pago (CRUD)

```
POST http://localhost:8097/api/v1/pagos
Authorization: Bearer {{token}}
Content-Type: application/json
```
```json
{ "ticketId": 1, "monto": 50000, "metodoPago": "EFECTIVO", "logs": [] }
```
**201** → pago creado

---

### Listar / Obtener / Actualizar / Eliminar

```
GET    http://localhost:8097/api/v1/pagos
GET    http://localhost:8097/api/v1/pagos/1
PUT    http://localhost:8097/api/v1/pagos/1
DELETE http://localhost:8097/api/v1/pagos/1
Authorization: Bearer {{token}}
```

---

### Pago por ticket

```
GET http://localhost:8097/api/v1/pagos/ticket/1
Authorization: Bearer {{token}}
```
**200** → pago del ticket 1
**404** → no hay pago para ese ticket

---

### Logs de un pago

```
GET http://localhost:8097/api/v1/pagos/1/logs
Authorization: Bearer {{token}}
```
**200** → lista de TransaccionLog

---

## PASO 9 — Notificaciones (ms-notification-log :8099)

> Recibe notificaciones automáticas de ms-ticketing y ms-competition-reg.

### Enviar notificación

```
POST http://localhost:8099/api/v1/notificaciones/enviar
Authorization: Bearer {{token}}
Content-Type: application/json
```
```json
{ "destinatario": "testuser", "mensaje": "Prueba manual de notificación" }
```
**201** → guardada con `leida: false`, log en consola del servicio

---

### Crear / Listar / Obtener / Actualizar / Eliminar

```
POST   http://localhost:8099/api/v1/notificaciones
GET    http://localhost:8099/api/v1/notificaciones
GET    http://localhost:8099/api/v1/notificaciones/1
PUT    http://localhost:8099/api/v1/notificaciones/1
DELETE http://localhost:8099/api/v1/notificaciones/1
Authorization: Bearer {{token}}
```

---

### Por usuario

```
GET http://localhost:8099/api/v1/notificaciones/destinatario/testuser
Authorization: Bearer {{token}}
```
**200** → todas las notificaciones del usuario

---

### Marcar como leída

```
PATCH http://localhost:8099/api/v1/notificaciones/1/marcar-leida
Authorization: Bearer {{token}}
```
**200** → `leida: true`
**500** → ya estaba marcada como leída

---

## PASO 10 — Reportes (ms-analytics-report :8098)

### Generar reporte automático ⚡ inter-servicio

> Consulta ms-event-core, ms-ticketing y ms-competition-reg para consolidar datos reales.

```
POST http://localhost:8098/api/v1/reportes/generar/1
Authorization: Bearer {{token}}
```
**201** → reporte con métricas `total_tickets` y `total_inscripciones`
**500** → evento 1 no existe en ms-event-core

---

### Resumen de métricas

```
GET http://localhost:8098/api/v1/reportes/metricas/resumen
Authorization: Bearer {{token}}
```
**200** → suma de todas las métricas agrupadas por nombre

---

### Crear / Listar / Obtener / Actualizar / Eliminar

```
POST   http://localhost:8098/api/v1/reportes
GET    http://localhost:8098/api/v1/reportes
GET    http://localhost:8098/api/v1/reportes/1
PUT    http://localhost:8098/api/v1/reportes/1
DELETE http://localhost:8098/api/v1/reportes/1
Authorization: Bearer {{token}}
```

---

## FLUJOS COMPLETOS

### Flujo A — Compra de ticket y pago

```
1. POST :8090/api/v1/auth/login          → obtén token
2. POST :8091/api/v1/eventos             → crea evento (guarda id: 1)
3. POST :8093/api/v1/tickets             → ticket eventoId:1 → estado: PENDIENTE
4. PATCH :8093/api/v1/tickets/1/pagar   → llama a ms-payment-mock
                                          → APROBADO: estado PAGADO
                                          → ms-notification-log recibe notificación
5. GET  :8099/api/v1/notificaciones/destinatario/testuser → ves la notificación
6. GET  :8097/api/v1/pagos/ticket/1      → ves el log del pago
```

---

### Flujo B — Inscripción y aprobación

```
1. POST :8090/api/v1/auth/login          → token admin
2. POST :8091/api/v1/eventos             → evento (id: 1)
3. POST :8092/api/v1/vehiculos           → vehículo (id: 1)
4. POST :8094/api/v1/inscripciones       → inscripción evento 1 + vehiculo 1 → PENDIENTE
5. PATCH :8094/api/v1/inscripciones/1/aprobar
                                         → valida vehículo en ms-vehicle-registry
                                         → estado: APROBADA
                                         → ms-notification-log recibe notificación
6. POST :8095/api/v1/puntuaciones        → puntuación para inscripción 1
                                         → valida inscripción en ms-competition-reg
7. GET  :8095/api/v1/puntuaciones/ranking → top 10 global
```

---

### Flujo C — Reporte de evento

```
1. Completa el flujo A y B primero (necesitas datos reales)
2. POST :8098/api/v1/reportes/generar/1
                                         → consulta ms-ticketing: cuenta tickets
                                         → consulta ms-competition-reg: cuenta inscripciones
                                         → guarda reporte con métricas
3. GET  :8098/api/v1/reportes/metricas/resumen → métricas acumuladas
```

---

## Códigos de respuesta

| Código | Cuándo ocurre |
|---|---|
| **200** | Operación exitosa (GET, PUT, PATCH, DELETE) |
| **201** | Recurso creado (POST) |
| **400** | Datos inválidos, campo faltante, regla de negocio simple |
| **401** | Sin token, token expirado o mal formado |
| **403** | Token válido pero sin permisos de ADMIN |
| **404** | Recurso no encontrado (id inexistente) |
| **500** | Error de negocio (estado inválido, servicio externo caído, pago rechazado) |

---

## Tips para Postman

- Crea una colección `CarMeet` con variable de entorno `token`
- En cada login, copia el `accessToken` al valor de `{{token}}`
- El pago tiene 20% de rechazo — si ves 500 en `/pagar`, es normal, reintenta
- Si un microservicio no está levantado y otro lo llama, recibirás 500 con el mensaje del error
- El orden importa: primero evento y vehículo, luego ticket e inscripción, luego puntuación y reporte
