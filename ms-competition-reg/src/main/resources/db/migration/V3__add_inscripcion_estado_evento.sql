-- V3: Añade campos 'estado' y 'evento_id' a la tabla inscripcion
ALTER TABLE inscripcion
    ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    ADD COLUMN evento_id BIGINT;
