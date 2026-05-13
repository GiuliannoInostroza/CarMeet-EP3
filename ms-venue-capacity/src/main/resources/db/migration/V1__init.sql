CREATE TABLE recinto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    capacidad_maxima INT NOT NULL,
    ocupacion_actual INT NOT NULL
);

CREATE TABLE zona (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recinto_id BIGINT,
    nombre VARCHAR(255) NOT NULL,
    FOREIGN KEY (recinto_id) REFERENCES recinto(id) ON DELETE CASCADE
);
