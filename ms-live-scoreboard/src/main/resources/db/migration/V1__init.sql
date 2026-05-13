CREATE TABLE puntuacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inscripcion_id BIGINT NOT NULL,
    puntos INT NOT NULL
);

CREATE TABLE detalle_puntuacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    puntuacion_id BIGINT,
    descripcion VARCHAR(255) NOT NULL,
    FOREIGN KEY (puntuacion_id) REFERENCES puntuacion(id) ON DELETE CASCADE
);
