CREATE TABLE reporte (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    total_eventos INT NOT NULL,
    fecha_generacion VARCHAR(255) NOT NULL
);

CREATE TABLE metrica (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reporte_id BIGINT,
    nombre VARCHAR(255) NOT NULL,
    FOREIGN KEY (reporte_id) REFERENCES reporte(id) ON DELETE CASCADE
);

