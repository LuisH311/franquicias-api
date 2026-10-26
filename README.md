# Franquicias API

API REST desarrollada con **Java 17, Spring Boot y MySQL 8.4**, utilizando `JdbcTemplate` para las consultas SQL y Docker Compose para ejecutar la aplicación y la base de datos.

## Ejecución

Requisitos: Docker y Docker Compose.

```bash
docker compose up --build -d
docker compose logs -f api
```

Cuando aparezca `Started Main`, la API estará disponible en `http://localhost:8080`.

## Endpoints

| Método | Endpoint | Funcionalidad |
|---|---|---|
| POST | `/franquicias` | Crear franquicia |
| POST | `/franquicias/{id}/sucursales` | Crear sucursal |
| POST | `/sucursales/{id}/productos` | Crear producto |
| DELETE | `/sucursales/{sucursalId}/productos/{productoId}` | Eliminar producto |
| PUT | `/productos/{id}/stock` | Actualizar stock |
| GET | `/franquicias/{id}/mayor-stock` | Consultar el producto con mayor stock por sucursal |
| PUT | `/franquicias/{id}` | Actualizar nombre de franquicia |
| PUT | `/sucursales/{id}` | Actualizar nombre de sucursal |
| PUT | `/productos/{id}` | Actualizar nombre de producto |

## Reglas principales

- Los nombres son obligatorios y admiten hasta 100 caracteres.
- El stock debe ser un entero mayor o igual a cero.
- Los recursos inexistentes responden con `404 Not Found`.
- La consulta de mayor stock devuelve todos los productos empatados y omite las sucursales sin productos.

## Pruebas y persistencia

- `ejemplos.http`: peticiones para probar los endpoints.
- `schema.sql`: estructura de las tablas.
- `consultas.sql`: ejemplos de consultas SQL manuales.

Los datos se almacenan en MySQL y se conservan en el volumen de Docker al detener los servicios.



