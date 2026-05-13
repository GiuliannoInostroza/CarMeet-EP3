CREATE TABLE notificacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    destinatario VARCHAR(255) NOT NULL,
    mensaje TEXT NOT NULL
);

CREATE TABLE adjunto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notificacion_id BIGINT,
    ruta_archivo VARCHAR(255) NOT NULL,
    FOREIGN KEY (notificacion_id) REFERENCES notificacion(id) ON DELETE CASCADE
);
