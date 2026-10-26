CREATE TABLE IF NOT EXISTS franquicias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS sucursales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    franquicia_id BIGINT NOT NULL,
    FOREIGN KEY (franquicia_id) REFERENCES franquicias(id)
);

CREATE TABLE IF NOT EXISTS productos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    stock INT NOT NULL CHECK (stock >= 0),
    sucursal_id BIGINT NOT NULL,
    FOREIGN KEY (sucursal_id) REFERENCES sucursales(id)
);
