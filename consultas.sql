-- Estas consultas se ejecutan dentro de la base franquicias.
-- Las tablas se crean automáticamente desde schema.sql al arrancar la API.

USE franquicias;

INSERT INTO franquicias (id, nombre) VALUES (1, 'Caribe');
INSERT INTO sucursales (id, nombre, franquicia_id) VALUES (1, 'Norte', 1), (2, 'Centro', 1);
INSERT INTO productos (id, nombre, stock, sucursal_id) VALUES
    (1, 'Cafe', 10, 1),
    (2, 'Pan', 25, 1),
    (3, 'Agua', 8, 2),
    (4, 'Jugo', 4, 2);

-- CONSULTAS
SELECT * FROM franquicias;
SELECT * FROM sucursales WHERE franquicia_id = 1;
SELECT * FROM productos WHERE sucursal_id = 1;

-- Mayor stock de cada sucursal de la franquicia.
SELECT s.id AS sucursalId, s.nombre AS sucursal,
       p.id AS productoId, p.nombre AS producto, p.stock
FROM sucursales s
JOIN productos p ON p.sucursal_id = s.id
WHERE s.franquicia_id = 1
  AND p.stock = (
      SELECT MAX(p2.stock)
      FROM productos p2
      WHERE p2.sucursal_id = s.id
  )
ORDER BY s.id, p.id;

-- Establecer nuevo stock.
UPDATE productos SET stock = 30 WHERE id = 1;

-- Eliminar un producto de una sucursal concreta.
DELETE FROM productos WHERE id = 2 AND sucursal_id = 1;
