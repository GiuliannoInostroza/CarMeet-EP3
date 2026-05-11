CREATE TABLE usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE TABLE refresh_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    username VARCHAR(50) NOT NULL,
    expiry_date DATETIME NOT NULL
);

INSERT INTO usuario (username, password, role) VALUES ('admin', '$2a$10$Dow1s7x1uJk7kP8Gk9kF9e9Zz9mZ3z7fZ9yZQzQ9xZQzQ9xZQzQ9', 'ROLE_ADMIN');
