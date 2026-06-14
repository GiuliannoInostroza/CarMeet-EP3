-- V3: Añade campos 'capacidad' y 'ocupacion' a la tabla zona
ALTER TABLE zona
    ADD COLUMN capacidad INT NOT NULL DEFAULT 0,
    ADD COLUMN ocupacion INT NOT NULL DEFAULT 0;
