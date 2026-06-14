-- V3: Añade campo 'evento_id' a la tabla puntuacion
ALTER TABLE puntuacion
    ADD COLUMN evento_id BIGINT;
