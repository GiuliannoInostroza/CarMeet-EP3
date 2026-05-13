CREATE TABLE inscripcion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehiculo_id BIGINT NOT NULL,
    categoria VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL
);

CREATE TABLE requisito (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inscripcion_id BIGINT,
    descripcion VARCHAR(255) NOT NULL,
    FOREIGN KEY (inscripcion_id) REFERENCES inscripcion(id) ON DELETE CASCADE
);
