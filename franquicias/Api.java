package franquicias;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@RestController
public class Api {
    private final JdbcTemplate db;

    public Api(JdbcTemplate db) { this.db = db; }

    // Estos records representan los cuerpos JSON recibidos.
    public record Nombre(@NotBlank @Size(max = 100) String nombre) {}
    public record Producto(@NotBlank @Size(max = 100) String nombre,
                           @NotNull @PositiveOrZero Integer stock) {}
    public record Stock(@NotNull @PositiveOrZero Integer stock) {}

    // Criterio 2: agregar franquicia.
    @PostMapping("/franquicias")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> crearFranquicia(@Valid @RequestBody Nombre dato) {
        long id = insertar("INSERT INTO franquicias(nombre) VALUES (?)", dato.nombre().trim());
        return Map.of("id", id, "nombre", dato.nombre().trim());
    }

    // Criterio 3: agregar sucursal a una franquicia existente.
    @PostMapping("/franquicias/{id}/sucursales")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Map<String, Object> crearSucursal(@PathVariable long id, @Valid @RequestBody Nombre dato) {
        comprobar("SELECT COUNT(*) FROM franquicias WHERE id = ?", id, "Franquicia no encontrada");
        long nueva = insertar("INSERT INTO sucursales(nombre, franquicia_id) VALUES (?, ?)", dato.nombre().trim(), id);
        return Map.of("id", nueva, "nombre", dato.nombre().trim(), "franquiciaId", id);
    }

    // Criterio 4: agregar producto a una sucursal existente.
    @PostMapping("/sucursales/{id}/productos")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Map<String, Object> crearProducto(@PathVariable long id, @Valid @RequestBody Producto dato) {
        comprobar("SELECT COUNT(*) FROM sucursales WHERE id = ?", id, "Sucursal no encontrada");
        long nuevo = insertar("INSERT INTO productos(nombre, stock, sucursal_id) VALUES (?, ?, ?)", dato.nombre().trim(), dato.stock(), id);
        return Map.of("id", nuevo, "nombre", dato.nombre().trim(), "stock", dato.stock(), "sucursalId", id);
    }

    // Criterio 5: eliminar el producto solo de la sucursal indicada.
    @DeleteMapping("/sucursales/{sucursalId}/productos/{productoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarProducto(@PathVariable long sucursalId, @PathVariable long productoId) {
        int filas = db.update("DELETE FROM productos WHERE id = ? AND sucursal_id = ?", productoId, sucursalId);
        if (filas == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado en esa sucursal");
    }

    // Criterio 6: establecer el stock final del producto.
    @PutMapping("/productos/{id}/stock")
    public Map<String, Object> cambiarStock(@PathVariable long id, @Valid @RequestBody Stock dato) {
        int filas = db.update("UPDATE productos SET stock = ? WHERE id = ?", dato.stock(), id);
        if (filas == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        return Map.of("id", id, "stock", dato.stock());
    }

    // Actualizar el nombre de una franquicia.
    @PutMapping("/franquicias/{id}")
    public Map<String, Object> cambiarNombreFranquicia(
            @PathVariable long id,
            @Valid @RequestBody Nombre dato) {

        int filas = db.update(
                "UPDATE franquicias SET nombre = ? WHERE id = ?",
                dato.nombre().trim(), id);

        if (filas == 0) {
            comprobar("SELECT COUNT(*) FROM franquicias WHERE id = ?", id,
                    "Franquicia no encontrada");
        }

        return Map.of("id", id, "nombre", dato.nombre().trim());
    }

    // Actualizar el nombre de una sucursal.
    @PutMapping("/sucursales/{id}")
    public Map<String, Object> cambiarNombreSucursal(
            @PathVariable long id,
            @Valid @RequestBody Nombre dato) {

        int filas = db.update(
                "UPDATE sucursales SET nombre = ? WHERE id = ?",
                dato.nombre().trim(), id);

        if (filas == 0) {
            comprobar("SELECT COUNT(*) FROM sucursales WHERE id = ?", id,
                    "Sucursal no encontrada");
        }

        return Map.of("id", id, "nombre", dato.nombre().trim());
    }

    // Actualizar el nombre de un producto.
    @PutMapping("/productos/{id}")
    public Map<String, Object> cambiarNombreProducto(
            @PathVariable long id,
            @Valid @RequestBody Nombre dato) {

        int filas = db.update(
                "UPDATE productos SET nombre = ? WHERE id = ?",
                dato.nombre().trim(), id);

        if (filas == 0) {
            comprobar("SELECT COUNT(*) FROM productos WHERE id = ?", id,
                    "Producto no encontrado");
        }

        return Map.of("id", id, "nombre", dato.nombre().trim());
    }

    // Criterio 7: máximo de cada sucursal, filtrando por franquicia.
    // Si hay empate se devuelven todos los empatados.
    @GetMapping("/franquicias/{id}/mayor-stock")
    public List<Map<String, Object>> mayorStock(@PathVariable long id) {
        comprobar("SELECT COUNT(*) FROM franquicias WHERE id = ?", id, "Franquicia no encontrada");
        return db.queryForList("""
            SELECT s.id AS sucursalId, s.nombre AS sucursal,
                   p.id AS productoId, p.nombre AS producto, p.stock
            FROM sucursales s
            JOIN productos p ON p.sucursal_id = s.id
            WHERE s.franquicia_id = ?
              AND p.stock = (
                  SELECT MAX(p2.stock)
                  FROM productos p2
                  WHERE p2.sucursal_id = s.id
              )
            ORDER BY s.id, p.id
            """, id);
    }

    // Ejecuta INSERT con parámetros y obtiene el ID autogenerado de MySQL.
    private long insertar(String sql, Object... valores) {
        var clave = new GeneratedKeyHolder();
        db.update(conexion -> {
            var sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < valores.length; i++) sentencia.setObject(i + 1, valores[i]);
            return sentencia;
        }, clave);
        return clave.getKey().longValue();
    }

    private void comprobar(String sql, long id, String mensaje) {
        if (db.queryForObject(sql, Integer.class, id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, mensaje);
        }
    }
}
