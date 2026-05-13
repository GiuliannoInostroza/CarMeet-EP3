CREATE TABLE vehiculo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    marca VARCHAR(255) NOT NULL,
    modelo VARCHAR(255) NOT NULL,
    anio INT NOT NULL
);

CREATE TABLE mantenimiento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehiculo_id BIGINT,
    descripcion VARCHAR(255) NOT NULL,
    FOREIGN KEY (vehiculo_id) REFERENCES vehiculo(id) ON DELETE CASCADE
);
