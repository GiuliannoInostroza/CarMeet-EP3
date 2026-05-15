# Proyecto CarMeet - Evaluación Parcial 2 (EP2)

**Integrantes:**  
- Giulianno Inostroza  
- Pascal Pacheco  

Este repositorio contiene el código de CarMeet, un sistema basado en microservicios para gestionar eventos automotrices, competencias, registro de vehículos y venta de tickets.

El proyecto fue desarrollado para la EP2 utilizando Spring Boot. Aplicamos el patrón de diseño multicapas (Controller-Service-Repository), seguridad mediante tokens JWT y bases de datos independientes para cada servicio.

## Arquitectura
El sistema está dividido en 10 microservicios. Para mantener el bajo acoplamiento, cada uno se conecta a su propia base de datos.

### 1. ms-auth-user (Puerto: 8090 | BD: db_auth)
Maneja el registro, login y la emisión de los tokens JWT para acceder al resto del sistema.

Entidades: Usuario, RefreshToken

### 2. ms-event-core (Puerto: 8091 | BD: db_event)
Es el servicio principal que guarda la información general de los eventos (fechas, lugares) y sus patrocinadores.

Entidades: Evento, Patrocinador

### 3. ms-vehicle-registry (Puerto: 8092 | BD: db_vehicle)
Guarda el registro de los autos que participan y su historial de modificaciones o mantenimientos.

Entidades: Vehiculo, Mantenimiento

### 4. ms-ticketing (Puerto: 8093 | BD: db_ticketing)
Encargado de la emisión de las entradas para los eventos y los beneficios asociados a cada tipo de ticket.

Entidades: Ticket, Beneficio

### 5. ms-competition-reg (Puerto: 8094 | BD: db_competition)
Gestiona las inscripciones de los competidores a las distintas categorías y revisa los requisitos técnicos.

Entidades: Inscripcion, Requisito

### 6. ms-live-scoreboard (Puerto: 8095 | BD: db_scoreboard)
Maneja las puntuaciones de las competencias y el detalle de las calificaciones (estilo, velocidad, etc.).

Entidades: Puntuacion, DetallePuntuacion

### 7. ms-venue-capacity (Puerto: 8096 | BD: db_venue)
Administra los recintos donde se hacen los eventos y el aforo de cada zona interna (cancha, VIP, etc.).

Entidades: Recinto, Zona

### 8. ms-payment-mock (Puerto: 8097 | BD: db_payment)
Simula una pasarela de pago para la compra de tickets e inscripciones, guardando el registro de las transacciones.

Entidades: Pago, TransaccionLog

### 9. ms-analytics-report (Puerto: 8098 | BD: db_analytics)
Genera reportes y guarda métricas de asistencia y rendimiento de los eventos.

Entidades: Reporte, Metrica

### 10. ms-notification-log (Puerto: 8099 | BD: db_notification)
Servicio encargado de registrar y simular el envío de notificaciones (correos o alertas) a los usuarios.

Entidades: Notificacion, Adjunto

## Detalles Técnicos
- Arquitectura: Patrón CSR (Controller, Service, Repository) separando modelos y DTOs.
- Seguridad: Usamos un JwtFilter en 9 de los 10 servicios para denegar peticiones que no traigan un token válido en el header.
- Base de Datos: Relaciones de JPA/Hibernate (@OneToMany, @ManyToOne) manejando cascadas.
- Migraciones: Desactivamos el hibernate.ddl-auto y configuramos Flyway para crear las tablas usando scripts SQL (V1__init.sql).
- Manejo de Errores: Implementamos un @ControllerAdvice global para que todas las excepciones y errores devuelvan un JSON estructurado.
- Validaciones: Uso de dependencias de validación (@Valid, @NotNull, @NotBlank) directamente en los Controllers.

## Instrucciones para levantar el proyecto
Para probar el proyecto en local, sigue estos pasos:

1. Levantar el motor de base de datos MySQL con Laragon y crear las 10 bases de datos vacías con los nombres indicados arriba. Las tablas se crearán solas gracias a Flyway.
2. Ejecutar primero el servicio ms-auth-user.
3. Usar Postman para hacer un POST a /auth/login y copiar el Token JWT que devuelve.
4. Levantar el resto de los microservicios desde el IDE.
5. En Postman, configurar la pestaña de Authorization seleccionando Bearer Token y pegar el token para poder probar los endpoints protegidos.