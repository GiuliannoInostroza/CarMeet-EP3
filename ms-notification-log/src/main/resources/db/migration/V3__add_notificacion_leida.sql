-- V3: Añade campo 'leida' a la tabla notificacion
ALTER TABLE notificacion
    ADD COLUMN leida BOOLEAN NOT NULL DEFAULT FALSE;
