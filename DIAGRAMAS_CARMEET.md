# 📊 CarMeet — Diagramas de Arquitectura y Diseño

> Diagramas para uso en diapositivas y documentación técnica.

---

## 1. Arquitectura de Microservicios — Diagrama C4 / Componentes

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': {'primaryColor': '#1e3a5f', 'primaryTextColor': '#e2e8f0', 'primaryBorderColor': '#3b82f6', 'lineColor': '#94a3b8', 'secondaryColor': '#0f172a', 'tertiaryColor': '#1e293b', 'background': '#0f172a', 'mainBkg': '#1e3a5f', 'nodeBorder': '#3b82f6', 'clusterBkg': '#1e293b', 'titleColor': '#f1f5f9', 'edgeLabelBackground': '#1e293b', 'fontFamily': 'Segoe UI, sans-serif'}}}%%
graph TD
    CLIENT(["👤 Cliente / Admin"])

    subgraph AUTH ["🔐 Capa de Autenticación"]
        A["ms-auth-user\n🔑 :8090\n/api/v1/auth"]
    end

    subgraph CORE ["🏎️ Servicios Core (sin WebClient)"]
        B["ms-event-core\n📅 :8091\n/api/v1/eventos"]
        C["ms-vehicle-registry\n🚗 :8092\n/api/v1/vehiculos"]
        D["ms-venue-capacity\n🏟️ :8096\n/api/v1/recintos"]
        E["ms-payment-mock\n💳 :8097\n/api/v1/pagos"]
        F["ms-notification-log\n🔔 :8099\n/api/v1/notificaciones"]
    end

    subgraph BUSSINESS ["⚙️ Servicios de Negocio (con WebClient)"]
        G["ms-ticketing\n🎫 :8093\n/api/v1/tickets"]
        H["ms-competition-reg\n🏆 :8094\n/api/v1/inscripciones"]
        I["ms-live-scoreboard\n📊 :8095\n/api/v1/puntuaciones"]
        J["ms-analytics-report\n📈 :8098\n/api/v1/reportes"]
    end

    CLIENT -->|"JWT"| A
    CLIENT -->|"Bearer token"| G
    CLIENT -->|"Bearer token"| H
    CLIENT -->|"Bearer token"| I
    CLIENT -->|"Bearer token"| J

    G -->|"valida evento"| B
    G -->|"valida aforo"| D
    G -->|"procesa pago"| E
    G -->|"envía notif."| F

    H -->|"valida evento"| B
    H -->|"valida vehículo"| C
    H -->|"envía notif."| F

    I -->|"valida inscripción"| H

    J -->|"datos evento"| B
    J -->|"conteo tickets"| G
    J -->|"conteo inscritos"| H

    style AUTH fill:#1a2744,stroke:#6366f1,color:#e2e8f0
    style CORE fill:#1a3a2a,stroke:#22c55e,color:#e2e8f0
    style BUSSINESS fill:#3a1a1a,stroke:#f97316,color:#e2e8f0
    style A fill:#312e81,stroke:#6366f1,color:#e2e8f0
    style B fill:#14532d,stroke:#22c55e,color:#e2e8f0
    style C fill:#14532d,stroke:#22c55e,color:#e2e8f0
    style D fill:#14532d,stroke:#22c55e,color:#e2e8f0
    style E fill:#14532d,stroke:#22c55e,color:#e2e8f0
    style F fill:#14532d,stroke:#22c55e,color:#e2e8f0
    style G fill:#7c2d12,stroke:#f97316,color:#e2e8f0
    style H fill:#7c2d12,stroke:#f97316,color:#e2e8f0
    style I fill:#7c2d12,stroke:#f97316,color:#e2e8f0
    style J fill:#7c2d12,stroke:#f97316,color:#e2e8f0
    style CLIENT fill:#1e1b4b,stroke:#a78bfa,color:#e2e8f0
```

---

## 2. Diagrama Entidad-Relación Global (Lógico)

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': {'primaryColor': '#1e3a5f', 'primaryTextColor': '#e2e8f0', 'lineColor': '#94a3b8', 'background': '#0f172a', 'fontFamily': 'Segoe UI, sans-serif'}}}%%
erDiagram
    USUARIO {
        bigint id PK
        varchar username
        varchar password
        varchar role
    }
    REFRESH_TOKEN {
        bigint id PK
        varchar username FK
        varchar token
        datetime expiry_date
    }
    EVENTO {
        bigint id PK
        varchar nombre
        varchar fecha
        varchar ubicacion
    }
    PATROCINADOR {
        bigint id PK
        varchar nombre
        varchar nivel
        bigint evento_id FK
    }
    RECINTO {
        bigint id PK
        varchar nombre
        int capacidad_maxima
        int ocupacion_actual
    }
    ZONA {
        bigint id PK
        varchar nombre
        int capacidad
        int ocupacion
        bigint recinto_id FK
    }
    VEHICULO {
        bigint id PK
        varchar marca
        varchar modelo
        int anio
    }
    MANTENIMIENTO {
        bigint id PK
        varchar descripcion
        bigint vehiculo_id FK
    }
    TICKET {
        bigint id PK
        bigint evento_id
        double precio
        varchar categoria
        varchar estado
        varchar username
    }
    BENEFICIO {
        bigint id PK
        varchar nombre
        varchar descripcion
        bigint ticket_id FK
    }
    INSCRIPCION {
        bigint id PK
        bigint vehiculo_id
        bigint evento_id
        varchar participante
        varchar categoria
        varchar username
        varchar estado
    }
    REQUISITO {
        bigint id PK
        varchar nombre
        varchar descripcion
        bigint inscripcion_id FK
    }
    PUNTUACION {
        bigint id PK
        bigint inscripcion_id
        bigint evento_id
        int puntos
    }
    DETALLE_PUNTUACION {
        bigint id PK
        varchar categoria
        int puntos_asignados
        varchar descripcion
        bigint puntuacion_id FK
    }
    PAGO {
        bigint id PK
        bigint ticket_id
        double monto
        varchar metodo_pago
    }
    TRANSACCION_LOG {
        bigint id PK
        varchar estado
        datetime fecha
        bigint pago_id FK
    }
    NOTIFICACION {
        bigint id PK
        varchar destinatario
        text mensaje
        boolean leida
    }
    ADJUNTO {
        bigint id PK
        varchar nombre_archivo
        varchar url
        varchar ruta_archivo
        bigint notificacion_id FK
    }
    REPORTE {
        bigint id PK
        int total_eventos
        varchar fecha_generacion
        bigint evento_id
        int total_tickets
        int total_inscripciones
    }
    METRICA {
        bigint id PK
        varchar nombre
        double valor
        bigint reporte_id FK
    }

    USUARIO ||--o{ REFRESH_TOKEN : "tiene"
    EVENTO ||--o{ PATROCINADOR : "tiene"
    RECINTO ||--o{ ZONA : "tiene"
    VEHICULO ||--o{ MANTENIMIENTO : "tiene"
    TICKET ||--o{ BENEFICIO : "tiene"
    INSCRIPCION ||--o{ REQUISITO : "tiene"
    PUNTUACION ||--o{ DETALLE_PUNTUACION : "tiene"
    PAGO ||--o{ TRANSACCION_LOG : "genera"
    NOTIFICACION ||--o{ ADJUNTO : "tiene"
    REPORTE ||--o{ METRICA : "tiene"
```

---

## 3. Mapa de Comunicación WebClient (Flujos de Llamadas)

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': {'primaryColor': '#1e3a5f', 'primaryTextColor': '#e2e8f0', 'lineColor': '#94a3b8', 'background': '#0f172a', 'fontFamily': 'Segoe UI, sans-serif'}}}%%
flowchart LR
    T["🎫 ms-ticketing\n:8093"]
    C["🏆 ms-competition-reg\n:8094"]
    S["📊 ms-scoreboard\n:8095"]
    R["📈 ms-analytics\n:8098"]

    EC["📅 ms-event-core\n:8091"]
    VR["🚗 ms-vehicle-reg\n:8092"]
    VC["🏟️ ms-venue-cap\n:8096"]
    PM["💳 ms-payment\n:8097"]
    NL["🔔 ms-notif-log\n:8099"]

    T -->|"GET /eventos/id\nvalida evento"| EC
    T -->|"GET /recintos/id/disponibilidad\nvalida aforo"| VC
    T -->|"POST /pagos/procesar\nprocesa pago"| PM
    T -->|"POST /notificaciones/enviar\nconfirma pago"| NL

    C -->|"GET /eventos/id\nvalida evento"| EC
    C -->|"GET /vehiculos/id\nvalida vehículo"| VR
    C -->|"POST /notificaciones/enviar\naprob./rechazo"| NL

    S -->|"GET /inscripciones/id\nvalida inscripción"| C

    R -->|"GET /eventos/id\ndatos evento"| EC
    R -->|"GET /tickets/evento/id\nconteo tickets"| T
    R -->|"GET /inscripciones/evento/id\nconteo inscritos"| C

    style T fill:#7c2d12,stroke:#f97316,color:#fef3c7
    style C fill:#7c2d12,stroke:#f97316,color:#fef3c7
    style S fill:#7c2d12,stroke:#f97316,color:#fef3c7
    style R fill:#7c2d12,stroke:#f97316,color:#fef3c7
    style EC fill:#14532d,stroke:#4ade80,color:#dcfce7
    style VR fill:#14532d,stroke:#4ade80,color:#dcfce7
    style VC fill:#14532d,stroke:#4ade80,color:#dcfce7
    style PM fill:#14532d,stroke:#4ade80,color:#dcfce7
    style NL fill:#14532d,stroke:#4ade80,color:#dcfce7
```

---

## 4. Flujo de Negocio — Compra de Ticket

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': {'primaryColor': '#1e3a5f', 'primaryTextColor': '#e2e8f0', 'lineColor': '#94a3b8', 'background': '#0f172a', 'fontFamily': 'Segoe UI, sans-serif'}}}%%
sequenceDiagram
    actor Cliente
    participant TK as ms-ticketing :8093
    participant EC as ms-event-core :8091
    participant PM as ms-payment-mock :8097
    participant NL as ms-notification-log :8099

    Cliente->>+TK: POST /api/v1/tickets
    TK->>+EC: GET /api/v1/eventos/{id}
    EC-->>-TK: 200 Evento válido
    TK-->>-Cliente: 201 Ticket creado (estado=PENDIENTE)

    Cliente->>+TK: PATCH /api/v1/tickets/{id}/pagar
    TK->>TK: Valida estado=PENDIENTE
    TK->>+PM: POST /api/v1/pagos/procesar
    alt Pago APROBADO
        PM-->>-TK: 200 APROBADO
        TK->>TK: Cambia estado → PAGADO
        TK->>NL: POST /api/v1/notificaciones/enviar
        Note over NL: "Tu pago fue aprobado"
        TK-->>-Cliente: 200 Ticket pagado
    else Pago RECHAZADO
        PM-->>TK: 200 RECHAZADO
        TK-->>Cliente: 402 Payment Required
    end
```

---

## 5. Flujo de Negocio — Inscripción a Competencia

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': {'primaryColor': '#1e3a5f', 'primaryTextColor': '#e2e8f0', 'lineColor': '#94a3b8', 'background': '#0f172a', 'fontFamily': 'Segoe UI, sans-serif'}}}%%
sequenceDiagram
    actor Cliente
    actor Admin
    participant CR as ms-competition-reg :8094
    participant EC as ms-event-core :8091
    participant VR as ms-vehicle-registry :8092
    participant NL as ms-notification-log :8099

    Cliente->>+CR: POST /api/v1/inscripciones
    CR->>+EC: GET /api/v1/eventos/{id}
    EC-->>-CR: 200 Evento válido
    CR-->>-Cliente: 201 Inscripción creada (estado=PENDIENTE)

    Admin->>+CR: PATCH /api/v1/inscripciones/{id}/aprobar
    CR->>CR: Valida estado=PENDIENTE
    CR->>+VR: GET /api/v1/vehiculos/{id}
    VR-->>-CR: 200 Vehículo válido
    CR->>CR: Cambia estado → APROBADA
    CR->>NL: POST /api/v1/notificaciones/enviar
    Note over NL: "Inscripción aprobada"
    CR-->>-Admin: 200 Inscripción aprobada
```

---

## 6. Flujo de Negocio — Generación de Reporte Analytics

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': {'primaryColor': '#1e3a5f', 'primaryTextColor': '#e2e8f0', 'lineColor': '#94a3b8', 'background': '#0f172a', 'fontFamily': 'Segoe UI, sans-serif'}}}%%
sequenceDiagram
    actor Admin
    participant AR as ms-analytics-report :8098
    participant EC as ms-event-core :8091
    participant TK as ms-ticketing :8093
    participant CR as ms-competition-reg :8094

    Admin->>+AR: POST /api/v1/reportes/generar/{eventoId}
    AR->>+EC: GET /api/v1/eventos/{eventoId}
    EC-->>-AR: nombre, fecha, ubicación
    AR->>+TK: GET /api/v1/tickets/evento/{eventoId}
    TK-->>-AR: Lista de tickets → totalTickets, ingresos
    AR->>+CR: GET /api/v1/inscripciones/evento/{eventoId}
    CR-->>-AR: Lista de inscripciones → totalInscritos
    AR->>AR: Crea Reporte + Métricas
    Note over AR: Total Tickets Vendidos: N\nInscritos en Competencia: N\nIngresos Estimados: $X
    AR-->>-Admin: 201 Reporte generado
```

---

## 7. Ciclos de Vida de Entidades (State Machine)

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': {'primaryColor': '#1e3a5f', 'primaryTextColor': '#e2e8f0', 'lineColor': '#94a3b8', 'background': '#0f172a', 'fontFamily': 'Segoe UI, sans-serif'}}}%%
stateDiagram-v2
    direction LR

    state "🎫 TICKET" as ticket_group {
        [*] --> PENDIENTE_T: POST /tickets
        PENDIENTE_T --> PAGADO: PATCH /pagar\n[pago aprobado]
        PENDIENTE_T --> CANCELADO: PATCH /cancelar
        PAGADO --> [*]
        CANCELADO --> [*]
        PENDIENTE_T: PENDIENTE
        PAGADO: PAGADO
        CANCELADO: CANCELADO
    }

    state "🏆 INSCRIPCIÓN" as inscripcion_group {
        [*] --> PENDIENTE_I: POST /inscripciones
        PENDIENTE_I --> APROBADA: PATCH /aprobar\n[vehículo válido]
        PENDIENTE_I --> RECHAZADA: PATCH /rechazar
        APROBADA --> [*]
        RECHAZADA --> [*]
        PENDIENTE_I: PENDIENTE
        APROBADA: APROBADA
        RECHAZADA: RECHAZADA
    }

    state "💳 TRANSACCIÓN" as transaccion_group {
        [*] --> PENDIENTE_P: POST /pagos/procesar
        PENDIENTE_P --> APROBADO: mock 80%
        PENDIENTE_P --> RECHAZADO: mock 20%
        APROBADO --> [*]
        RECHAZADO --> [*]
        PENDIENTE_P: PENDIENTE
        APROBADO: APROBADO
        RECHAZADO: RECHAZADO
    }
```

---

## 8. Mapa de Puertos y Bases de Datos

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': {'primaryColor': '#1e3a5f', 'primaryTextColor': '#e2e8f0', 'lineColor': '#94a3b8', 'background': '#0f172a', 'fontFamily': 'Segoe UI, sans-serif'}}}%%
graph LR
    subgraph MYSQL ["🗄️ MySQL :3306"]
        DB1[("auth_db")]
        DB2[("event_db")]
        DB3[("vehicle_db")]
        DB4[("venue_db")]
        DB5[("ticket_db")]
        DB6[("competition_db")]
        DB7[("scoreboard_db")]
        DB8[("payment_db")]
        DB9[("notification_db")]
        DB10[("analytics_db")]
    end

    subgraph SERVICES ["🚀 Microservicios"]
        S1["ms-auth-user\n:8090"] --- DB1
        S2["ms-event-core\n:8091"] --- DB2
        S3["ms-vehicle-registry\n:8092"] --- DB3
        S4["ms-venue-capacity\n:8096"] --- DB4
        S5["ms-ticketing\n:8093"] --- DB5
        S6["ms-competition-reg\n:8094"] --- DB6
        S7["ms-live-scoreboard\n:8095"] --- DB7
        S8["ms-payment-mock\n:8097"] --- DB8
        S9["ms-notification-log\n:8099"] --- DB9
        S10["ms-analytics-report\n:8098"] --- DB10
    end

    style MYSQL fill:#1e1b4b,stroke:#6366f1,color:#e2e8f0
    style SERVICES fill:#0f2a1e,stroke:#22c55e,color:#e2e8f0
    style S1 fill:#312e81,stroke:#6366f1,color:#e2e8f0
    style S2 fill:#14532d,stroke:#22c55e,color:#e2e8f0
    style S3 fill:#14532d,stroke:#22c55e,color:#e2e8f0
    style S4 fill:#14532d,stroke:#22c55e,color:#e2e8f0
    style S5 fill:#7c2d12,stroke:#f97316,color:#e2e8f0
    style S6 fill:#7c2d12,stroke:#f97316,color:#e2e8f0
    style S7 fill:#7c2d12,stroke:#f97316,color:#e2e8f0
    style S8 fill:#14532d,stroke:#22c55e,color:#e2e8f0
    style S9 fill:#14532d,stroke:#22c55e,color:#e2e8f0
    style S10 fill:#7c2d12,stroke:#f97316,color:#e2e8f0
```

---

## 9. Arquitectura de Seguridad JWT

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': {'primaryColor': '#1e3a5f', 'primaryTextColor': '#e2e8f0', 'lineColor': '#94a3b8', 'background': '#0f172a', 'fontFamily': 'Segoe UI, sans-serif'}}}%%
sequenceDiagram
    actor U as Usuario
    participant A as ms-auth-user :8090
    participant X as Cualquier ms-* :809X

    U->>+A: POST /auth/register o /auth/login
    A-->>-U: accessToken (1h) + refreshToken (24h)

    U->>+X: Request + Header "Authorization: Bearer token"
    X->>X: JwtAuthFilter valida firma\ncon jwt.secret compartida
    alt Token válido
        X-->>-U: 200 Respuesta OK
    else Token expirado/inválido
        X-->>U: 401 Unauthorized
    end

    U->>+A: POST /auth/refresh + refreshToken
    A->>A: Valida refreshToken en DB
    A-->>-U: Nuevo accessToken

    Note over A,X: Todos los ms-* comparten el mismo\njwt.secret para validar sin llamar a auth
```

---

## 10. Orden de Arranque Recomendado

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': {'primaryColor': '#1e3a5f', 'primaryTextColor': '#e2e8f0', 'lineColor': '#94a3b8', 'background': '#0f172a', 'fontFamily': 'Segoe UI, sans-serif'}}}%%
graph TD
    A["1️⃣ ms-auth-user\n:8090\n(Autenticación)"]
    B["2️⃣ ms-event-core\n:8091"]
    C["3️⃣ ms-vehicle-registry\n:8092"]
    D["4️⃣ ms-venue-capacity\n:8096"]
    E["5️⃣ ms-payment-mock\n:8097"]
    F["6️⃣ ms-notification-log\n:8099"]
    G["7️⃣ ms-ticketing\n:8093\n(depende de B, D, E, F)"]
    H["8️⃣ ms-competition-reg\n:8094\n(depende de B, C, F)"]
    I["9️⃣ ms-live-scoreboard\n:8095\n(depende de H)"]
    J["🔟 ms-analytics-report\n:8098\n(depende de B, G, H)"]

    A --> B & C & D & E & F
    B & D & E & F --> G
    B & C & F --> H
    H --> I
    B & G & H --> J

    style A fill:#312e81,stroke:#6366f1,color:#e2e8f0
    style B fill:#14532d,stroke:#22c55e,color:#e2e8f0
    style C fill:#14532d,stroke:#22c55e,color:#e2e8f0
    style D fill:#14532d,stroke:#22c55e,color:#e2e8f0
    style E fill:#14532d,stroke:#22c55e,color:#e2e8f0
    style F fill:#14532d,stroke:#22c55e,color:#e2e8f0
    style G fill:#7c2d12,stroke:#f97316,color:#e2e8f0
    style H fill:#7c2d12,stroke:#f97316,color:#e2e8f0
    style I fill:#7c2d12,stroke:#f97316,color:#e2e8f0
    style J fill:#7c2d12,stroke:#f97316,color:#e2e8f0
```
