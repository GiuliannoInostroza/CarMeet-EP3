# CarMeet — Lógica de Negocio Completa

> Sistema de gestión de eventos automotrices compuesto por **10 microservicios** independientes.
> Comunicación inter-servicio **exclusivamente mediante WebClient** (ningún microservicio accede a la BD de otro).

---

## 1. Diagrama de Relaciones entre Microservicios (WebClient)

```
                            ┌─────────────────┐
                            │  ms-auth-user   │
                            │   puerto: 8090  │
                            │  /api/v1/auth   │
                            └────────┬────────┘
                                     │ JWT emitido
                     ┌───────────────┼───────────────────────────────────┐
                     │               │                                   │
                     ▼               ▼                                   ▼
         ┌──────────────────┐ ┌──────────────┐              ┌──────────────────┐
         │  ms-event-core   │ │ms-venue-cap  │              │ms-vehicle-reg    │
         │  puerto: 8091    │ │puerto: 8096  │              │puerto: 8092      │
         │/api/v1/eventos   │ │/api/v1/      │              │/api/v1/vehiculos │
         └──────┬───────────┘ │recintos      │              └──────┬───────────┘
                │             └──────┬───────┘                     │
      valida    │                    │ valida                       │ valida
      evento    │             disponib.                             │ vehículo
                │                   │                              │
                ▼                   ▼                              ▼
         ┌─────────────────────────────────────────────────────────────────┐
         │                    ms-ticketing  (puerto: 8093)                 │
         │                    /api/v1/tickets                              │
         └──────────────┬──────────────────────────────────────────────────┘
                        │                    │
              pagar      │                   │ confirmar
              ticket     ▼                   ▼
              ┌─────────────────┐   ┌──────────────────────┐
              │ ms-payment-mock │   │ ms-notification-log  │
              │ puerto: 8097    │   │ puerto: 8099          │
              │/api/v1/pagos    │   │/api/v1/notificaciones│
              └─────────────────┘   └──────────────────────┘
                                             ▲
                                             │ notifica
                              ┌──────────────┤
                              │              │
                     ┌────────────────┐      │
                     │ms-competition  │──────┘
                     │-reg            │
                     │puerto: 8094    │
                     │/api/v1/        │
                     │inscripciones   │
                     └───────┬────────┘
                             │ valida inscripción
                             ▼
                   ┌──────────────────────┐
                   │  ms-live-scoreboard  │
                   │  puerto: 8095        │
                   │  /api/v1/puntuaciones│
                   └──────────────────────┘

              ┌─────────────────────────────────────────────────────────┐
              │              ms-analytics-report (puerto: 8098)         │
              │              /api/v1/reportes                            │
              │   Llama a: ms-event-core, ms-ticketing, ms-competition  │
              └─────────────────────────────────────────────────────────┘
```

---

## 2. Diagrama Entidad-Relación (por microservicio)

### ms-auth-user (BD: auth_db)

```
┌─────────────────────────┐       ┌──────────────────────────────┐
│         usuario         │       │        refresh_token          │
├─────────────────────────┤       ├──────────────────────────────┤
│ PK  id         BIGINT   │       │ PK  id           BIGINT       │
│     username   VARCHAR  │◄──────│     username     VARCHAR      │
│     password   VARCHAR  │       │     token        VARCHAR      │
│     role       VARCHAR  │       │     expiry_date  DATETIME     │
└─────────────────────────┘       └──────────────────────────────┘
  Roles: ROLE_USER | ROLE_ADMIN
```

---

### ms-event-core (BD: event_db)

```
┌────────────────────────────┐       ┌──────────────────────────────────┐
│           evento           │       │           patrocinador            │
├────────────────────────────┤       ├──────────────────────────────────┤
│ PK  id          BIGINT     │◄──────│ PK  id          BIGINT           │
│     nombre      VARCHAR    │  1:N  │     nombre      VARCHAR          │
│     fecha       VARCHAR    │       │     nivel       VARCHAR          │
│     ubicacion   VARCHAR    │       │     FK evento_id BIGINT          │
└────────────────────────────┘       └──────────────────────────────────┘
  nivel: ORO | PLATA | BRONCE
```

---

### ms-venue-capacity (BD: venue_db)

```
┌────────────────────────────────┐     ┌────────────────────────────────┐
│            recinto             │     │              zona              │
├────────────────────────────────┤     ├────────────────────────────────┤
│ PK  id               BIGINT    │◄────│ PK  id            BIGINT       │
│     nombre           VARCHAR   │ 1:N │     nombre        VARCHAR      │
│     capacidad_maxima INT       │     │     capacidad     INT  [nuevo] │
│     ocupacion_actual INT       │     │     ocupacion     INT  [nuevo] │
└────────────────────────────────┘     │     FK recinto_id BIGINT       │
                                       └────────────────────────────────┘
```

---

### ms-vehicle-registry (BD: vehicle_db)

```
┌────────────────────────────┐     ┌────────────────────────────────────┐
│          vehiculo          │     │           mantenimiento             │
├────────────────────────────┤     ├────────────────────────────────────┤
│ PK  id       BIGINT        │◄────│ PK  id           BIGINT            │
│     marca    VARCHAR       │ 1:N │     descripcion  VARCHAR           │
│     modelo   VARCHAR       │     │     FK vehiculo_id BIGINT          │
│     anio     INT           │     └────────────────────────────────────┘
└────────────────────────────┘
```

---

### ms-ticketing (BD: ticket_db)

```
┌──────────────────────────────────┐     ┌──────────────────────────────────┐
│             ticket               │     │            beneficio              │
├──────────────────────────────────┤     ├──────────────────────────────────┤
│ PK  id          BIGINT           │◄────│ PK  id           BIGINT          │
│     evento_id   BIGINT  [externo]│ 1:N │     nombre       VARCHAR         │
│     precio      DOUBLE           │     │     descripcion  VARCHAR         │
│     categoria   VARCHAR          │     │     FK ticket_id BIGINT          │
│     estado      VARCHAR          │     └──────────────────────────────────┘
│     username    VARCHAR          │
└──────────────────────────────────┘
  categoria: VIP | GENERAL | PALCO
  estado: PENDIENTE | PAGADO | CANCELADO
  evento_id → ms-event-core (WebClient)
```

---

### ms-competition-reg (BD: competition_db)

```
┌──────────────────────────────────────┐     ┌──────────────────────────────────┐
│              inscripcion             │     │             requisito             │
├──────────────────────────────────────┤     ├──────────────────────────────────┤
│ PK  id           BIGINT              │◄────│ PK  id             BIGINT        │
│     vehiculo_id  BIGINT  [externo]   │ 1:N │     nombre         VARCHAR       │
│     evento_id    BIGINT  [externo]   │     │     descripcion     VARCHAR       │
│     participante VARCHAR             │     │     FK inscripcion_id BIGINT      │
│     categoria    VARCHAR             │     └──────────────────────────────────┘
│     username     VARCHAR             │
│     estado       VARCHAR  [nuevo]    │
└──────────────────────────────────────┘
  estado: PENDIENTE | APROBADA | RECHAZADA
  vehiculo_id → ms-vehicle-registry (WebClient)
  evento_id   → ms-event-core (WebClient)
```

---

### ms-live-scoreboard (BD: scoreboard_db)

```
┌────────────────────────────────────┐     ┌──────────────────────────────────────┐
│            puntuacion              │     │          detalle_puntuacion           │
├────────────────────────────────────┤     ├──────────────────────────────────────┤
│ PK  id              BIGINT         │◄────│ PK  id               BIGINT          │
│     inscripcion_id  BIGINT [ext]   │ 1:N │     categoria        VARCHAR         │
│     evento_id       BIGINT [ext]   │     │     puntos_asignados INT             │
│     puntos          INT            │     │     descripcion       VARCHAR         │
└────────────────────────────────────┘     │     FK puntuacion_id  BIGINT         │
                                           └──────────────────────────────────────┘
  inscripcion_id → ms-competition-reg (WebClient)
  evento_id      → ms-event-core (WebClient, informativo)
```

---

### ms-payment-mock (BD: payment_db)

```
┌──────────────────────────────────┐     ┌──────────────────────────────────────┐
│               pago               │     │           transaccion_log             │
├──────────────────────────────────┤     ├──────────────────────────────────────┤
│ PK  id          BIGINT           │◄────│ PK  id      BIGINT                   │
│     ticket_id   BIGINT  [externo]│ 1:N │     estado  VARCHAR                  │
│     monto       DOUBLE           │     │     fecha   DATETIME                 │
│     metodo_pago VARCHAR          │     │     FK pago_id BIGINT                │
└──────────────────────────────────┘     └──────────────────────────────────────┘
  ticket_id → ms-ticketing (referencia informativa)
  estado TransaccionLog: APROBADO | RECHAZADO | PENDIENTE
```

---

### ms-notification-log (BD: notification_db)

```
┌──────────────────────────────────┐     ┌──────────────────────────────────────┐
│           notificacion           │     │              adjunto                 │
├──────────────────────────────────┤     ├──────────────────────────────────────┤
│ PK  id           BIGINT          │◄────│ PK  id              BIGINT          │
│     destinatario VARCHAR         │ 1:N │     nombre_archivo  VARCHAR         │
│     mensaje      TEXT            │     │     url             VARCHAR         │
│     leida        BOOLEAN [nuevo] │     │     ruta_archivo    VARCHAR         │
└──────────────────────────────────┘     │     FK notificacion_id BIGINT       │
                                         └──────────────────────────────────────┘
```

---

### ms-analytics-report (BD: analytics_db)

```
┌────────────────────────────────────┐     ┌──────────────────────────────────────┐
│              reporte               │     │               metrica                │
├────────────────────────────────────┤     ├──────────────────────────────────────┤
│ PK  id               BIGINT        │◄────│ PK  id         BIGINT               │
│     total_eventos    INT           │ 1:N │     nombre     VARCHAR              │
│     fecha_generacion VARCHAR       │     │     valor      DOUBLE               │
│     evento_id        BIGINT [ext]  │     │     FK reporte_id BIGINT            │
└────────────────────────────────────┘     └──────────────────────────────────────┘
  evento_id → ms-event-core (WebClient, informativo)
  Genera métricas consultando ms-ticketing y ms-competition-reg vía WebClient
```

---

## 3. Tabla de WebClient Calls (Relaciones entre Microservicios)

| Origen | Destino | Endpoint | Trigger |
|--------|---------|----------|---------|
| ms-ticketing | ms-event-core | `GET /api/v1/eventos/{id}` | Al crear ticket — valida que el evento exista |
| ms-ticketing | ms-venue-capacity | `GET /api/v1/recintos/{id}/disponibilidad` | Al crear ticket — valida disponibilidad de recinto |
| ms-ticketing | ms-payment-mock | `POST /api/v1/pagos/procesar` | Al ejecutar `PATCH /tickets/{id}/pagar` |
| ms-ticketing | ms-notification-log | `POST /api/v1/notificaciones/enviar` | Al confirmar pago exitoso |
| ms-competition-reg | ms-vehicle-registry | `GET /api/v1/vehiculos/{id}` | Al aprobar inscripción — valida que el vehículo exista |
| ms-competition-reg | ms-event-core | `GET /api/v1/eventos/{id}` | Al crear inscripción — valida que el evento exista |
| ms-competition-reg | ms-notification-log | `POST /api/v1/notificaciones/enviar` | Al aprobar o rechazar inscripción |
| ms-live-scoreboard | ms-competition-reg | `GET /api/v1/inscripciones/{id}` | Al registrar puntuación — valida que la inscripción exista |
| ms-analytics-report | ms-event-core | `GET /api/v1/eventos/{id}` | Al generar reporte de un evento |
| ms-analytics-report | ms-ticketing | `GET /api/v1/tickets/evento/{eventoId}` | Para contar tickets vendidos |
| ms-analytics-report | ms-competition-reg | `GET /api/v1/inscripciones/evento/{eventoId}` | Para contar inscriptos |

> **Todos los WebClient calls propagan el header `Authorization: Bearer <token>` de la petición entrante.**

---

## 4. Lógica de Negocio por Microservicio

### 4.1 ms-auth-user — Autenticación y Autorización

**Propósito**: Punto de entrada de todos los usuarios. Emite y valida JWT.

**Modelos**: `Usuario`, `RefreshToken`

**Flujo de negocio**:
1. `POST /api/v1/auth/register` → Crea usuario con `ROLE_USER`, emite `accessToken` + `refreshToken`
2. `POST /api/v1/auth/login` → Valida credenciales, emite tokens
3. `POST /api/v1/auth/refresh` → Renueva `accessToken` usando `refreshToken` válido
4. `GET /api/v1/auth/me` → Devuelve datos del usuario autenticado
5. `PUT /api/v1/auth/promote/{username}` → Solo ADMIN: promueve usuario a ADMIN
6. `PUT /api/v1/auth/demote/{username}` → Solo ADMIN: degrada usuario a USER
7. `GET /api/v1/auth/usuarios` → Solo ADMIN: lista todos los usuarios
8. `GET /api/v1/auth/usuarios/{username}` → Obtiene usuario por username

**Reglas**:
- `accessToken` expira en 1 hora
- `refreshToken` expira en 24 horas
- Los demás microservicios validan el JWT con la misma `jwt.secret`

---

### 4.2 ms-event-core — Gestión de Eventos

**Propósito**: CRUD de eventos automotrices con sus patrocinadores.

**Modelos**: `Evento`, `Patrocinador`

**Flujo de negocio**:
1. CRUD completo de `Evento` con sus `Patrocinador` embebidos
2. `GET /api/v1/eventos/{id}/patrocinadores` → Lista patrocinadores de un evento
3. `GET /api/v1/eventos/proximos` → Filtra eventos cuya fecha es futura
4. `GET /api/v1/eventos/buscar?nombre=` → Búsqueda por nombre (like)

**Reglas**:
- `Patrocinador.nivel` puede ser: `ORO`, `PLATA`, `BRONCE`
- Un evento puede tener 0 o más patrocinadores
- El campo `fecha` es String (formato: `yyyy-MM-dd`)
- Bug corregido: `toEntity()` ahora asigna `nivel` al patrocinador

---

### 4.3 ms-venue-capacity — Gestión de Recintos

**Propósito**: Control de aforo y zonas de recintos.

**Modelos**: `Recinto`, `Zona`

**Flujo de negocio**:
1. CRUD completo de `Recinto` con sus `Zona` embebidas
2. `GET /api/v1/recintos/{id}/zonas` → Lista zonas de un recinto
3. `GET /api/v1/recintos/{id}/disponibilidad` → Devuelve `{ disponible, capacidadMaxima, ocupacionActual, plazasLibres }`
4. `POST /api/v1/recintos/{id}/registrar-ingreso` → Incrementa `ocupacionActual` si hay cupo
5. `POST /api/v1/recintos/{id}/registrar-egreso` → Decrementa `ocupacionActual`

**Reglas**:
- `ocupacionActual` nunca puede superar `capacidadMaxima` → 400 si lleno
- `ocupacionActual` nunca puede ser menor que 0
- `Zona` tiene campos `capacidad` y `ocupacion` propios (nuevos)
- Este servicio es consultado por `ms-ticketing` vía WebClient al crear tickets

---

### 4.4 ms-vehicle-registry — Registro de Vehículos

**Propósito**: CRUD de vehículos con historial de mantenimientos.

**Modelos**: `Vehiculo`, `Mantenimiento`

**Flujo de negocio**:
1. CRUD completo de `Vehiculo` con `Mantenimiento` embebidos
2. `GET /api/v1/vehiculos/{id}/mantenimientos` → Historial de mantenimientos
3. `GET /api/v1/vehiculos/buscar?placa=` → Búsqueda por placa (o modelo)
4. `POST /api/v1/vehiculos/{id}/mantenimientos` → Agrega mantenimiento sin reemplazar otros

**Reglas**:
- Un vehículo puede tener múltiples registros de mantenimiento
- `ms-competition-reg` consulta este servicio al aprobar inscripciones

---

### 4.5 ms-ticketing — Gestión de Tickets

**Propósito**: Venta y gestión del ciclo de vida de tickets de eventos.

**Modelos**: `Ticket`, `Beneficio`

**Flujo de negocio**:
1. `POST /api/v1/tickets` → Crea ticket:
   - Llama a `ms-event-core` para validar que el evento exista
   - Estado inicial: `PENDIENTE`
2. `GET /api/v1/tickets/evento/{eventoId}` → Lista tickets de un evento
3. `GET /api/v1/tickets/usuario/{username}` → Lista tickets de un usuario
4. `PATCH /api/v1/tickets/{id}/cancelar` → Cambia estado a `CANCELADO`
5. `PATCH /api/v1/tickets/{id}/pagar`:
   - Llama a `ms-payment-mock` con `POST /api/v1/pagos/procesar`
   - Si pago APROBADO → cambia estado a `PAGADO`
   - Si pago RECHAZADO → lanza error 402
   - Llama a `ms-notification-log` para notificar al usuario

**Reglas**:
- `categoria`: `VIP`, `GENERAL`, `PALCO`
- `estado`: `PENDIENTE`, `PAGADO`, `CANCELADO`
- Solo tickets en estado `PENDIENTE` pueden ser pagados
- Solo tickets en estado `PENDIENTE` pueden ser cancelados
- Los beneficios son extras del ticket (ej: Pase a Pits, Merchandising)

---

### 4.6 ms-competition-reg — Registro de Competencias

**Propósito**: Inscripción de participantes/vehículos a competencias.

**Modelos**: `Inscripcion`, `Requisito`

**Flujo de negocio**:
1. `POST /api/v1/inscripciones` → Crea inscripción:
   - Llama a `ms-event-core` para validar que el evento exista
   - Estado inicial: `PENDIENTE`
2. `GET /api/v1/inscripciones/evento/{eventoId}` → Inscripciones de un evento
3. `GET /api/v1/inscripciones/vehiculo/{vehiculoId}` → Inscripciones de un vehículo
4. `PATCH /api/v1/inscripciones/{id}/aprobar`:
   - Llama a `ms-vehicle-registry` para validar que el vehículo exista
   - Cambia estado a `APROBADA`
   - Notifica al participante vía `ms-notification-log`
5. `PATCH /api/v1/inscripciones/{id}/rechazar`:
   - Cambia estado a `RECHAZADA`
   - Notifica al participante vía `ms-notification-log`

**Reglas**:
- `estado`: `PENDIENTE`, `APROBADA`, `RECHAZADA`
- Solo inscripciones en `PENDIENTE` pueden ser aprobadas o rechazadas
- Los requisitos son condiciones que el participante debe cumplir

---

### 4.7 ms-live-scoreboard — Tabla de Puntuaciones

**Propósito**: Registro y consulta de puntuaciones en tiempo real.

**Modelos**: `Puntuacion`, `DetallePuntuacion`

**Flujo de negocio**:
1. `POST /api/v1/puntuaciones` → Registra puntuación:
   - Llama a `ms-competition-reg` para validar que la inscripción exista
2. `GET /api/v1/puntuaciones/evento/{eventoId}` → Ranking de un evento (ordenado desc)
3. `GET /api/v1/puntuaciones/inscripcion/{inscripcionId}` → Puntuación de una inscripción
4. `GET /api/v1/puntuaciones/ranking` → Top 10 puntajes globales

**Reglas**:
- `DetallePuntuacion.categoria` ejemplos: `Drift`, `Velocidad`, `Presentacion`
- El ranking se ordena por `puntos` descendente
- Un `inscripcionId` puede tener múltiples registros (sumables o separados)

---

### 4.8 ms-payment-mock — Pagos (Mock)

**Propósito**: Simulación de procesamiento de pagos.

**Modelos**: `Pago`, `TransaccionLog`

**Flujo de negocio**:
1. `POST /api/v1/pagos` → Crea pago manual (CRUD)
2. `POST /api/v1/pagos/procesar` → Procesa pago mock:
   - Simula aprobación/rechazo (ej: 80% aprobado, 20% rechazado)
   - Guarda el resultado en `TransaccionLog`
3. `GET /api/v1/pagos/ticket/{ticketId}` → Pago asociado a un ticket
4. `GET /api/v1/pagos/{id}/logs` → Logs de transacción de un pago

**Reglas**:
- `metodo_pago`: `TARJETA`, `EFECTIVO`, `TRANSFERENCIA`
- `TransaccionLog.estado`: `APROBADO`, `RECHAZADO`, `PENDIENTE`
- Bug corregido: `actualizar()` ahora actualiza `metodoPago`

---

### 4.9 ms-notification-log — Notificaciones

**Propósito**: Registro y envío simulado de notificaciones a usuarios.

**Modelos**: `Notificacion`, `Adjunto`

**Flujo de negocio**:
1. `POST /api/v1/notificaciones` → Crea notificación (CRUD)
2. `POST /api/v1/notificaciones/enviar` → Endpoint de envío:
   - Crea la notificación
   - Simula envío (log en consola)
3. `GET /api/v1/notificaciones/destinatario/{username}` → Notificaciones de un usuario
4. `PATCH /api/v1/notificaciones/{id}/marcar-leida` → Marca como leída

**Reglas**:
- `leida` es un campo booleano nuevo (default: `false`)
- Los adjuntos contienen archivos opcionales (nombre, URL, ruta)
- Es llamado por `ms-ticketing` y `ms-competition-reg` vía WebClient

---

### 4.10 ms-analytics-report — Reportes y Analíticas

**Propósito**: Generación de reportes con métricas de eventos.

**Modelos**: `Reporte`, `Metrica`

**Flujo de negocio**:
1. `POST /api/v1/reportes/generar/{eventoId}` → Genera reporte automático:
   - Llama a `ms-event-core` para obtener datos del evento
   - Llama a `ms-ticketing` para contar tickets vendidos
   - Llama a `ms-competition-reg` para contar inscriptos
   - Crea métricas: `Total Tickets`, `Total Inscriptos`, `Ingresos Estimados`
2. `GET /api/v1/reportes/metricas/resumen` → Resumen de todas las métricas

**Reglas**:
- Bug corregido: `WebClientConfig` ahora tiene `@Configuration` y `@Bean WebClient.Builder`
- Métricas calculadas de forma automática a partir de datos de otros servicios

---

## 5. Endpoints Completos por Microservicio

### ms-auth-user (puerto: 8090)
| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| POST | `/api/v1/auth/register` | NO | Registro |
| POST | `/api/v1/auth/login` | NO | Login |
| POST | `/api/v1/auth/refresh` | NO | Renovar token |
| GET | `/api/v1/auth/me` | SÍ | Datos del usuario actual |
| PUT | `/api/v1/auth/promote/{username}` | ADMIN | Promover a admin |
| PUT | `/api/v1/auth/demote/{username}` | ADMIN | Degradar a user |
| GET | `/api/v1/auth/usuarios` | ADMIN | Listar usuarios |
| GET | `/api/v1/auth/usuarios/{username}` | SÍ | Obtener usuario |

### ms-event-core (puerto: 8091)
| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| GET | `/api/v1/eventos` | SÍ | Listar eventos |
| GET | `/api/v1/eventos/{id}` | SÍ | Obtener evento |
| POST | `/api/v1/eventos` | SÍ | Crear evento |
| PUT | `/api/v1/eventos/{id}` | SÍ | Actualizar evento |
| DELETE | `/api/v1/eventos/{id}` | SÍ | Eliminar evento |
| GET | `/api/v1/eventos/{id}/patrocinadores` | SÍ | Patrocinadores del evento |
| GET | `/api/v1/eventos/proximos` | SÍ | Eventos futuros |
| GET | `/api/v1/eventos/buscar?nombre=` | SÍ | Buscar por nombre |

### ms-venue-capacity (puerto: 8096)
| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| GET | `/api/v1/recintos` | SÍ | Listar recintos |
| GET | `/api/v1/recintos/{id}` | SÍ | Obtener recinto |
| POST | `/api/v1/recintos` | SÍ | Crear recinto |
| PUT | `/api/v1/recintos/{id}` | SÍ | Actualizar recinto |
| DELETE | `/api/v1/recintos/{id}` | SÍ | Eliminar recinto |
| GET | `/api/v1/recintos/{id}/zonas` | SÍ | Zonas del recinto |
| GET | `/api/v1/recintos/{id}/disponibilidad` | SÍ | Disponibilidad |
| POST | `/api/v1/recintos/{id}/registrar-ingreso` | SÍ | Registrar ingreso |
| POST | `/api/v1/recintos/{id}/registrar-egreso` | SÍ | Registrar egreso |

### ms-vehicle-registry (puerto: 8092)
| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| GET | `/api/v1/vehiculos` | SÍ | Listar vehículos |
| GET | `/api/v1/vehiculos/{id}` | SÍ | Obtener vehículo |
| POST | `/api/v1/vehiculos` | SÍ | Crear vehículo |
| PUT | `/api/v1/vehiculos/{id}` | SÍ | Actualizar vehículo |
| DELETE | `/api/v1/vehiculos/{id}` | SÍ | Eliminar vehículo |
| GET | `/api/v1/vehiculos/{id}/mantenimientos` | SÍ | Historial mantenimientos |
| GET | `/api/v1/vehiculos/buscar?placa=` | SÍ | Buscar por placa |
| POST | `/api/v1/vehiculos/{id}/mantenimientos` | SÍ | Agregar mantenimiento |

### ms-ticketing (puerto: 8093)
| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| GET | `/api/v1/tickets` | SÍ | Listar tickets |
| GET | `/api/v1/tickets/{id}` | SÍ | Obtener ticket |
| POST | `/api/v1/tickets` | SÍ | Crear ticket |
| PUT | `/api/v1/tickets/{id}` | SÍ | Actualizar ticket |
| DELETE | `/api/v1/tickets/{id}` | SÍ | Eliminar ticket |
| GET | `/api/v1/tickets/evento/{eventoId}` | SÍ | Tickets de un evento |
| GET | `/api/v1/tickets/usuario/{username}` | SÍ | Tickets de un usuario |
| PATCH | `/api/v1/tickets/{id}/cancelar` | SÍ | Cancelar ticket |
| PATCH | `/api/v1/tickets/{id}/pagar` | SÍ | Pagar ticket |

### ms-competition-reg (puerto: 8094)
| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| GET | `/api/v1/inscripciones` | SÍ | Listar inscripciones |
| GET | `/api/v1/inscripciones/{id}` | SÍ | Obtener inscripción |
| POST | `/api/v1/inscripciones` | SÍ | Crear inscripción |
| PUT | `/api/v1/inscripciones/{id}` | SÍ | Actualizar inscripción |
| DELETE | `/api/v1/inscripciones/{id}` | SÍ | Eliminar inscripción |
| GET | `/api/v1/inscripciones/evento/{eventoId}` | SÍ | Por evento |
| GET | `/api/v1/inscripciones/vehiculo/{vehiculoId}` | SÍ | Por vehículo |
| PATCH | `/api/v1/inscripciones/{id}/aprobar` | ADMIN | Aprobar inscripción |
| PATCH | `/api/v1/inscripciones/{id}/rechazar` | ADMIN | Rechazar inscripción |

### ms-live-scoreboard (puerto: 8095)
| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| GET | `/api/v1/puntuaciones` | SÍ | Listar puntuaciones |
| GET | `/api/v1/puntuaciones/{id}` | SÍ | Obtener puntuación |
| POST | `/api/v1/puntuaciones` | SÍ | Registrar puntuación |
| PUT | `/api/v1/puntuaciones/{id}` | SÍ | Actualizar puntuación |
| DELETE | `/api/v1/puntuaciones/{id}` | SÍ | Eliminar puntuación |
| GET | `/api/v1/puntuaciones/evento/{eventoId}` | SÍ | Ranking del evento |
| GET | `/api/v1/puntuaciones/inscripcion/{inscripcionId}` | SÍ | Por inscripción |
| GET | `/api/v1/puntuaciones/ranking` | SÍ | Top 10 global |

### ms-payment-mock (puerto: 8097)
| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| GET | `/api/v1/pagos` | SÍ | Listar pagos |
| GET | `/api/v1/pagos/{id}` | SÍ | Obtener pago |
| POST | `/api/v1/pagos` | SÍ | Crear pago |
| PUT | `/api/v1/pagos/{id}` | SÍ | Actualizar pago |
| DELETE | `/api/v1/pagos/{id}` | SÍ | Eliminar pago |
| GET | `/api/v1/pagos/ticket/{ticketId}` | SÍ | Pago de un ticket |
| POST | `/api/v1/pagos/procesar` | SÍ | Procesar pago (mock) |
| GET | `/api/v1/pagos/{id}/logs` | SÍ | Logs de un pago |

### ms-notification-log (puerto: 8099)
| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| GET | `/api/v1/notificaciones` | SÍ | Listar notificaciones |
| GET | `/api/v1/notificaciones/{id}` | SÍ | Obtener notificación |
| POST | `/api/v1/notificaciones` | SÍ | Crear notificación |
| PUT | `/api/v1/notificaciones/{id}` | SÍ | Actualizar notificación |
| DELETE | `/api/v1/notificaciones/{id}` | SÍ | Eliminar notificación |
| GET | `/api/v1/notificaciones/destinatario/{username}` | SÍ | Por usuario |
| POST | `/api/v1/notificaciones/enviar` | SÍ | Enviar notificación |
| PATCH | `/api/v1/notificaciones/{id}/marcar-leida` | SÍ | Marcar como leída |

### ms-analytics-report (puerto: 8098)
| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| GET | `/api/v1/reportes` | SÍ | Listar reportes |
| GET | `/api/v1/reportes/{id}` | SÍ | Obtener reporte |
| POST | `/api/v1/reportes` | SÍ | Crear reporte manual |
| PUT | `/api/v1/reportes/{id}` | SÍ | Actualizar reporte |
| DELETE | `/api/v1/reportes/{id}` | SÍ | Eliminar reporte |
| POST | `/api/v1/reportes/generar/{eventoId}` | ADMIN | Generar reporte automático |
| GET | `/api/v1/reportes/metricas/resumen` | SÍ | Resumen de métricas |

---

## 6. Flujos de Negocio Completos

### Flujo 1: Compra de Ticket

```
Cliente → POST /api/v1/tickets
              │
              ├─ WebClient → ms-event-core: GET /api/v1/eventos/{eventoId}
              │   ✓ Evento existe y es futuro
              │
              ├─ Crea Ticket con estado=PENDIENTE
              │
              └─ Responde 201 con el ticket

Cliente → PATCH /api/v1/tickets/{id}/pagar
              │
              ├─ Valida que estado=PENDIENTE
              │
              ├─ WebClient → ms-payment-mock: POST /api/v1/pagos/procesar
              │   { ticketId, monto, metodoPago }
              │   ─── APROBADO ──────────────────────────────────────┐
              │   ─── RECHAZADO → 402 Payment Required               │
              │                                                       ▼
              ├─ Actualiza ticket estado=PAGADO                     (OK)
              │
              └─ WebClient → ms-notification-log: POST /api/v1/notificaciones/enviar
                  { destinatario: username, mensaje: "Tu pago fue aprobado..." }
```

### Flujo 2: Inscripción a Competencia

```
Cliente → POST /api/v1/inscripciones
              │
              ├─ WebClient → ms-event-core: GET /api/v1/eventos/{eventoId}
              │   ✓ Evento existe
              │
              ├─ Crea Inscripcion con estado=PENDIENTE
              │
              └─ Responde 201

Admin → PATCH /api/v1/inscripciones/{id}/aprobar
              │
              ├─ Valida que estado=PENDIENTE
              │
              ├─ WebClient → ms-vehicle-registry: GET /api/v1/vehiculos/{vehiculoId}
              │   ✓ Vehículo existe
              │
              ├─ Actualiza inscripcion estado=APROBADA
              │
              └─ WebClient → ms-notification-log: POST /api/v1/notificaciones/enviar
                  { destinatario: username, mensaje: "Inscripción aprobada" }
```

### Flujo 3: Generación de Reporte

```
Admin → POST /api/v1/reportes/generar/{eventoId}
              │
              ├─ WebClient → ms-event-core: GET /api/v1/eventos/{eventoId}
              │   → datos del evento (nombre, fecha, ubicacion)
              │
              ├─ WebClient → ms-ticketing: GET /api/v1/tickets/evento/{eventoId}
              │   → lista de tickets → calcula: totalTickets, ingresosEstimados
              │
              ├─ WebClient → ms-competition-reg: GET /api/v1/inscripciones/evento/{eventoId}
              │   → lista de inscripciones → calcula: totalInscriptos
              │
              ├─ Crea Reporte con Metricas:
              │   - "Total Tickets Vendidos" → N
              │   - "Inscriptos en Competencia" → N
              │   - "Ingresos Estimados" → suma de precios
              │
              └─ Responde 201 con reporte generado
```

---

## 7. Cambios de BD Requeridos (Nuevos Scripts SQL)

> **NUNCA se modifican scripts existentes.** Solo se agregan nuevos scripts V3, V4...

### V3__add_inscripcion_estado_evento.sql (ms-competition-reg)
```sql
ALTER TABLE inscripcion
    ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    ADD COLUMN evento_id BIGINT;
```

### V3__add_ticket_event_venue_fields.sql (ms-ticketing)
> Nota: `evento_id` ya existe en el modelo actual. Solo se agrega `recinto_id` si se usa.

### V3__add_puntuacion_evento_id.sql (ms-live-scoreboard)
```sql
ALTER TABLE puntuacion
    ADD COLUMN evento_id BIGINT;
```

### V3__add_zona_capacidad_ocupacion.sql (ms-venue-capacity)
```sql
ALTER TABLE zona
    ADD COLUMN capacidad INT NOT NULL DEFAULT 0,
    ADD COLUMN ocupacion INT NOT NULL DEFAULT 0;
```

### V3__add_notificacion_leida.sql (ms-notification-log)
```sql
ALTER TABLE notificacion
    ADD COLUMN leida BOOLEAN NOT NULL DEFAULT FALSE;
```

### V3__add_reporte_evento_id.sql (ms-analytics-report)
```sql
ALTER TABLE reporte
    ADD COLUMN evento_id BIGINT;
```

---

## 8. Configuración WebClient por Microservicio

| Microservicio | Necesita WebClient | Estado |
|---------------|--------------------|--------|
| ms-auth-user | NO | — |
| ms-event-core | NO | — |
| ms-venue-capacity | NO | — |
| ms-vehicle-registry | NO | — |
| ms-ticketing | **SÍ** | WebClientConfig existente (funcional) |
| ms-competition-reg | **SÍ** | Debe agregarse |
| ms-live-scoreboard | **SÍ** | Debe agregarse |
| ms-payment-mock | NO | — |
| ms-notification-log | NO | — |
| ms-analytics-report | **SÍ** | WebClientConfig vacía — debe corregirse |

### Dependencia pom.xml para WebClient
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

---

## 9. Resumen de Bugs Detectados y Correcciones

| # | Bug | Microservicio | Corrección |
|---|-----|---------------|------------|
| 1 | `toEntity()` no asigna `nivel` a Patrocinador | ms-event-core | Agregar `p.setNivel(pdto.getNivel())` |
| 2 | `actualizar()` no actualiza `metodoPago` en Pago | ms-payment-mock | Agregar `existente.setMetodoPago(datosNuevos.getMetodoPago())` |
| 3 | `WebClientConfig` vacía sin `@Configuration` | ms-analytics-report | Corregir con `@Configuration` + `@Bean WebClient.Builder` |
| 4 | Encoding roto: `invÃ¡lido` en strings | Todos (9) | Reemplazar por `inválido` |
| 5 | Controllers sin versionado `/v1/` | Todos (10) | Agregar `/v1/` en `@RequestMapping` |
| 6 | Sin métodos de negocio propios | Todos | Implementar según lógica de negocio |
| 7 | Sin integración WebClient real | 4 servicios | Implementar calls inter-servicio |
