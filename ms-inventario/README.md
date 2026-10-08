# ms-inventario — Rider Tapia Biker

Microservicio REST de **inventario** de Rider Tapia Biker (épica Inventario). Implementa las clases del diagrama de clases de Inventario: el stock de cada variante de producto, los movimientos de inventario (con ingresos de mercancía y ajustes como subclases), las alertas de stock bajo, la toma de inventario con su detalle de conteo, y la carga masiva desde Excel con sus errores. Las respuestas siguen el formato HATEOAS (HAL).

Hay **un ItemInventario por variante** (`idVariante` es único). Los datos del producto y la variante (nombre, SKU, talla, color) viven en `ms-productos`; aquí solo está el stock.

## Tecnologías

| Tecnología | Versión |
|---|---|
| Java | 25 |
| Spring Boot | 4.0.6 (webmvc, data-jpa, hateoas) |
| springdoc-openapi (Swagger UI) | 3.0.3 |
| MySQL (mysql-connector-j) | runtime |
| H2 | runtime y tests |
| Lombok | gestionado por Spring Boot |
| RestTemplate | cliente HTTP hacia ms-productos |
| JUnit 5 + Mockito + MockMvc | tests |
| JaCoCo | 0.8.14 |

## Del diagrama al código

| Clase del diagrama | En el código |
|---|---|
| ItemInventario, AlertaStock, TomaInventario, DetalleConteo, CargaMasiva, ErrorCargaMasiva | Entidades JPA, cada una con repository, service, controller y tests |
| MovimientoInventario | Entidad JPA con herencia `JOINED`: guarda las ventas, reservas, usos en taller y devoluciones |
| IngresoMercancia, AjusteInventario | Subclases de MovimientoInventario, cada una con su tabla, repository, service, controller y tests |
| TipoMovimiento, MotivoAjuste, EstadoToma | Enums de Java guardados como texto |
| EstadoStock | Enum calculado: `ItemInventario.getEstadoStock()` devuelve CON_STOCK, BAJO_MINIMO o SIN_STOCK y sale en el JSON junto con `stockTotal` |
| GestorInventario | No guarda datos: sus búsquedas quedan en los services (por variante, movimientos por ítem, etc.) |
| Producto, VarianteProducto | Están en `ms-productos`: ItemInventario guarda `idVariante` y se valida por HTTP |
| Proveedor, Empleado, Pedido, OrdenTrabajo | Pertenecen a otros microservicios: se guarda solo su id (`idProveedores`, `idProveedor`, `idEmpleado`, `idPedido`, `idOrdenTrabajo`) |

Relaciones dentro del microservicio:

- MovimientoInventario (y sus subclases), AlertaStock y DetalleConteo → ItemInventario.
- DetalleConteo → TomaInventario, y AjusteInventario → TomaInventario (opcional, el ajuste que genera un conteo).
- ErrorCargaMasiva → CargaMasiva.
- ItemInventario guarda la lista de proveedores que lo abastecen (`idProveedores`).

## Rol en la arquitectura

**Consume**:

- `ms-productos` (puerto 8085): antes de crear un ItemInventario llama a `GET /api/v1/variantes/{idVariante}`. Si la variante no existe, no se guarda y responde 404. La URL se configura en `ms.productos.variantes-url`.

**Es consumido por**:

- Frontend tienda: muestra "sin stock" cuando el disponible llega a 0.
- Panel administrador / vendedor: ingresos, ajustes, tomas de inventario, alertas y carga masiva.
- `ms-carrito` y `ms-pedidos`: consultan el stock de una variante con `GET /api/v1/items-inventario/idVariante/{idVariante}`.

Para crear ítems en local, `ms-productos` debe estar levantado.

## Configuración

| Propiedad | Valor |
|---|---|
| Puerto | `8088` |
| Base de datos | `ridertapia_inventario` (MySQL, se crea sola con `createDatabaseIfNotExist=true`) |
| Usuario / clave MySQL | `root` / vacío (ajustar en `application.properties`) |
| URL variantes | `ms.productos.variantes-url=http://localhost:8085/api/v1/variantes` |
| Swagger UI | http://localhost:8088/doc/swagger-ui.html |
| OpenAPI JSON | http://localhost:8088/api-docs |

Ejecutar:

```bash
./mvnw spring-boot:run
```

Al crear o actualizar, las relaciones se envían solo con su id (por ejemplo `"itemInventario": {"idItemInventario": 1}`). Si no existe responde 404. Al eliminar un registro que otro está usando (por ejemplo, un ítem con movimientos) responde 409. Una variante con dos ítems de inventario también responde 409.

## Endpoints

Cada clase tiene su propio controller con el mismo patrón: listar, obtener por id, una búsqueda adicional, crear, actualizar y eliminar.

### ItemInventario — `/api/v1/items-inventario`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/items-inventario` | Lista todos | 200 |
| GET | `/api/v1/items-inventario/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/items-inventario/idVariante/{idVariante}` | Busca por idVariante | 200 / 404 |
| POST | `/api/v1/items-inventario` | Crea | 201 / 409 si el valor único ya existe |
| PUT | `/api/v1/items-inventario/{id}` | Actualiza | 200 / 404 / 409 si el valor único ya existe |
| DELETE | `/api/v1/items-inventario/{id}` | Elimina | 204 / 409 si está en uso |

### TomaInventario — `/api/v1/tomas-inventario`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/tomas-inventario` | Lista todos | 200 |
| GET | `/api/v1/tomas-inventario/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/tomas-inventario/idEmpleado/{idEmpleado}` | Lista los que tienen ese idEmpleado | 200 |
| POST | `/api/v1/tomas-inventario` | Crea | 201 |
| PUT | `/api/v1/tomas-inventario/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/tomas-inventario/{id}` | Elimina | 204 / 409 si está en uso |

### MovimientoInventario — `/api/v1/movimientos`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/movimientos` | Lista todos | 200 |
| GET | `/api/v1/movimientos/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/movimientos/itemInventario/{idItemInventario}` | Lista los de un itemInventario | 200 |
| POST | `/api/v1/movimientos` | Crea | 201 / 404 si una relación no existe |
| PUT | `/api/v1/movimientos/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/movimientos/{id}` | Elimina | 204 |

### IngresoMercancia — `/api/v1/ingresos`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/ingresos` | Lista todos | 200 |
| GET | `/api/v1/ingresos/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/ingresos/itemInventario/{idItemInventario}` | Lista los de un itemInventario | 200 |
| POST | `/api/v1/ingresos` | Crea | 201 / 404 si una relación no existe |
| PUT | `/api/v1/ingresos/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/ingresos/{id}` | Elimina | 204 |

### AjusteInventario — `/api/v1/ajustes`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/ajustes` | Lista todos | 200 |
| GET | `/api/v1/ajustes/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/ajustes/itemInventario/{idItemInventario}` | Lista los de un itemInventario | 200 |
| POST | `/api/v1/ajustes` | Crea | 201 / 404 si una relación no existe |
| PUT | `/api/v1/ajustes/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/ajustes/{id}` | Elimina | 204 |

### AlertaStock — `/api/v1/alertas-stock`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/alertas-stock` | Lista todos | 200 |
| GET | `/api/v1/alertas-stock/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/alertas-stock/itemInventario/{idItemInventario}` | Lista los de un itemInventario | 200 |
| POST | `/api/v1/alertas-stock` | Crea | 201 / 404 si una relación no existe |
| PUT | `/api/v1/alertas-stock/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/alertas-stock/{id}` | Elimina | 204 |

### DetalleConteo — `/api/v1/detalles-conteo`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/detalles-conteo` | Lista todos | 200 |
| GET | `/api/v1/detalles-conteo/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/detalles-conteo/tomaInventario/{idTomaInventario}` | Lista los de una tomaInventario | 200 |
| POST | `/api/v1/detalles-conteo` | Crea | 201 / 404 si una relación no existe |
| PUT | `/api/v1/detalles-conteo/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/detalles-conteo/{id}` | Elimina | 204 |

### CargaMasiva — `/api/v1/cargas-masivas`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/cargas-masivas` | Lista todos | 200 |
| GET | `/api/v1/cargas-masivas/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/cargas-masivas/idEmpleado/{idEmpleado}` | Lista los que tienen ese idEmpleado | 200 |
| POST | `/api/v1/cargas-masivas` | Crea | 201 |
| PUT | `/api/v1/cargas-masivas/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/cargas-masivas/{id}` | Elimina | 204 / 409 si está en uso |

### ErrorCargaMasiva — `/api/v1/errores-carga`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/errores-carga` | Lista todos | 200 |
| GET | `/api/v1/errores-carga/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/errores-carga/cargaMasiva/{idCargaMasiva}` | Lista los de una cargaMasiva | 200 |
| POST | `/api/v1/errores-carga` | Crea | 201 / 404 si una relación no existe |
| PUT | `/api/v1/errores-carga/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/errores-carga/{id}` | Elimina | 204 |

### Ejemplo: ItemInventario

Entrada (POST / PUT). Las relaciones se envían solo con su id:

```json
{
  "idVariante": 10,
  "stockDisponible": 15,
  "stockReservado": 2,
  "stockMinimo": 3,
  "costoPromedio": 650000,
  "fechaActualizacion": "2026-10-06T09:00:00",
  "idProveedores": [
    1,
    2
  ]
}
```

Respuesta HATEOAS (las relaciones vuelven completas):

```json
{
  "idItemInventario": 1,
  "idVariante": 10,
  "stockDisponible": 15,
  "stockReservado": 2,
  "stockMinimo": 3,
  "costoPromedio": 650000,
  "fechaActualizacion": "2026-10-06T09:00:00",
  "idProveedores": [
    1,
    2
  ],
  "_links": {
    "self": {
      "href": "http://localhost:8088/api/v1/items-inventario/1"
    },
    "itemsInventario": {
      "href": "http://localhost:8088/api/v1/items-inventario"
    }
  }
}
```

### Ejemplo: IngresoMercancia

Entrada (POST / PUT). Las relaciones se envían solo con su id:

```json
{
  "tipo": "INGRESO",
  "cantidad": 10,
  "stockAnterior": 5,
  "stockResultante": 15,
  "fecha": "2026-10-06T09:00:00",
  "observacion": "Recepción de factura",
  "idEmpleado": 1,
  "idPedido": 5,
  "idOrdenTrabajo": 3,
  "itemInventario": {
    "idItemInventario": 1
  },
  "costoUnitario": 640000,
  "numeroDocumento": "FAC-5501",
  "idProveedor": 1
}
```

Respuesta HATEOAS (las relaciones vuelven completas):

```json
{
  "idMovimientoInventario": 1,
  "tipo": "INGRESO",
  "cantidad": 10,
  "stockAnterior": 5,
  "stockResultante": 15,
  "fecha": "2026-10-06T09:00:00",
  "observacion": "Recepción de factura",
  "idEmpleado": 1,
  "idPedido": 5,
  "idOrdenTrabajo": 3,
  "itemInventario": {
    "idItemInventario": 1,
    "idVariante": 10,
    "stockDisponible": 15,
    "stockReservado": 2,
    "stockMinimo": 3,
    "costoPromedio": 650000,
    "fechaActualizacion": "2026-10-06T09:00:00",
    "idProveedores": [
      1,
      2
    ]
  },
  "costoUnitario": 640000,
  "numeroDocumento": "FAC-5501",
  "idProveedor": 1,
  "_links": {
    "self": {
      "href": "http://localhost:8088/api/v1/ingresos/1"
    },
    "ingresos": {
      "href": "http://localhost:8088/api/v1/ingresos"
    }
  }
}
```

### Ejemplo de listado HATEOAS (ItemInventario)

```json
{
  "_embedded": {
    "itemInventarioList": [
      {
        "idItemInventario": 1,
        "idVariante": 10,
        "stockDisponible": 15,
        "stockReservado": 2,
        "stockMinimo": 3,
        "costoPromedio": 650000,
        "fechaActualizacion": "2026-10-06T09:00:00",
        "idProveedores": [
          1,
          2
        ],
        "_links": {
          "self": {
            "href": "http://localhost:8088/api/v1/items-inventario/1"
          }
        }
      }
    ]
  },
  "_links": {
    "self": {
      "href": "http://localhost:8088/api/v1/items-inventario"
    }
  }
}
```

## Modelo de datos

### ItemInventario (tabla `items_inventario`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idItemInventario | Long | id_item_inventario | PK, autoincremental |
| idVariante | Long | id_variante | NOT NULL, único |
| stockDisponible | Integer | stock_disponible | NOT NULL |
| stockReservado | Integer | stock_reservado | NOT NULL |
| stockMinimo | Integer | stock_minimo | NOT NULL |
| costoPromedio | Integer | costo_promedio | NOT NULL |
| fechaActualizacion | LocalDateTime | fecha_actualizacion | NOT NULL |
| idProveedores | List<Long> | tabla `items_inventario_proveedores` | lista de ids de otro microservicio |

### TomaInventario (tabla `tomas_inventario`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idTomaInventario | Long | id_toma_inventario | PK, autoincremental |
| fechaInicio | LocalDateTime | fecha_inicio | NOT NULL |
| fechaCierre | LocalDateTime | fecha_cierre | opcional |
| estado | EstadoToma (enum) | estado | NOT NULL: `EN_CURSO`, `CONFIRMADA` |
| idEmpleado | Long | id_empleado | NOT NULL |

### MovimientoInventario — clase padre de IngresoMercancia y AjusteInventario (tabla `movimientos_inventario`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idMovimientoInventario | Long | id_movimiento_inventario | PK, autoincremental |
| tipo | TipoMovimiento (enum) | tipo | NOT NULL: `INGRESO`, `VENTA_WEB`, `VENTA_TIENDA`, `USO_TALLER`, `AJUSTE`, `DEVOLUCION_CLIENTE`, `DEVOLUCION_PROVEEDOR`, `RESERVA`, `LIBERACION_RESERVA` |
| cantidad | Integer | cantidad | NOT NULL |
| stockAnterior | Integer | stock_anterior | NOT NULL |
| stockResultante | Integer | stock_resultante | NOT NULL |
| fecha | LocalDateTime | fecha | NOT NULL |
| observacion | String | observacion | opcional, máx. 255 |
| idEmpleado | Long | id_empleado | NOT NULL |
| idPedido | Long | id_pedido | opcional |
| idOrdenTrabajo | Long | id_orden_trabajo | opcional |
| itemInventario | ItemInventario | id_item_inventario | FK a ItemInventario, NOT NULL |

### IngresoMercancia — hereda de MovimientoInventario (tabla `ingresos_mercancia`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idMovimientoInventario | Long | id_movimiento_inventario | PK y FK a MovimientoInventario |
| costoUnitario | Integer | costo_unitario | NOT NULL |
| numeroDocumento | String | numero_documento | NOT NULL, máx. 30 |
| idProveedor | Long | id_proveedor | NOT NULL |

### AjusteInventario — hereda de MovimientoInventario (tabla `ajustes_inventario`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idMovimientoInventario | Long | id_movimiento_inventario | PK y FK a MovimientoInventario |
| motivo | MotivoAjuste (enum) | motivo | NOT NULL: `MERMA`, `PERDIDA`, `ERROR_CONTEO`, `CONTEO_FISICO` |
| tomaInventario | TomaInventario | id_toma_inventario | FK a TomaInventario, opcional |

### AlertaStock (tabla `alertas_stock`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idAlertaStock | Long | id_alerta_stock | PK, autoincremental |
| fechaGeneracion | LocalDateTime | fecha_generacion | NOT NULL |
| stockAlMomento | Integer | stock_al_momento | NOT NULL |
| atendida | Boolean | atendida | NOT NULL |
| itemInventario | ItemInventario | id_item_inventario | FK a ItemInventario, NOT NULL |

### DetalleConteo (tabla `detalles_conteo`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idDetalleConteo | Long | id_detalle_conteo | PK, autoincremental |
| cantidadSistema | Integer | cantidad_sistema | NOT NULL |
| cantidadContada | Integer | cantidad_contada | NOT NULL |
| diferencia | Integer | diferencia | NOT NULL |
| tomaInventario | TomaInventario | id_toma_inventario | FK a TomaInventario, NOT NULL |
| itemInventario | ItemInventario | id_item_inventario | FK a ItemInventario, NOT NULL |

### CargaMasiva (tabla `cargas_masivas`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idCargaMasiva | Long | id_carga_masiva | PK, autoincremental |
| nombreArchivo | String | nombre_archivo | NOT NULL, máx. 150 |
| fechaCarga | LocalDateTime | fecha_carga | NOT NULL |
| filasProcesadas | Integer | filas_procesadas | NOT NULL |
| filasConError | Integer | filas_con_error | NOT NULL |
| idEmpleado | Long | id_empleado | NOT NULL |

### ErrorCargaMasiva (tabla `errores_carga_masiva`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idErrorCargaMasiva | Long | id_error_carga_masiva | PK, autoincremental |
| fila | Integer | fila | NOT NULL |
| mensaje | String | mensaje | NOT NULL, máx. 255 |
| cargaMasiva | CargaMasiva | id_carga_masiva | FK a CargaMasiva, NOT NULL |

## Tests

Los tests usan el perfil `test` (`src/test/resources/application-test.properties`) con H2 en memoria, así que **no necesitan MySQL ni ms-productos**: el `RestTemplate` se reemplaza por un mock (`@Mock` en el test de service y `@MockitoBean` en el de integración).

```bash
./mvnw test
```

Reporte de cobertura JaCoCo: `target/site/jacoco/index.html`

Por cada clase hay tres archivos de test:

| Test | Tipo | Qué cubre |
|---|---|---|
| `<Clase>ServiceTest` | Unitario (Mockito) | Todos los métodos del service, casos no existentes y relaciones inexistentes (en ItemInventario también la variante inexistente y el cálculo del estado de stock) |
| `<Clase>ControllerTest` | `@WebMvcTest` | Códigos HTTP, campos, enlaces HATEOAS, 404 y 409 |
| `<Clase>ControllerIT` | Integración (`@SpringBootTest` + H2) | Crear + listar + búsqueda, actualizar, eliminar + 404 |

Además, `InventarioApplicationTests` prueba la carga del contexto, y `LimpiezaBaseDatos` vacía las tablas antes de cada test de integración en el orden correcto, primero los registros que dependen de otros.

## Estructura del proyecto

```
ms-inventario/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/ridertapia/inventario/
    │   │   ├── InventarioApplication.java
    │   │   ├── controller/   (9 controllers, uno por clase)
    │   │   ├── model/        (9 entidades + 4 enums)
    │   │   ├── repository/   (9 repositories)
    │   │   └── service/      (9 services)
    │   └── resources/application.properties
    └── test/
        ├── java/com/ridertapia/inventario/
        │   ├── InventarioApplicationTests.java
        │   ├── LimpiezaBaseDatos.java
        │   ├── controller/   (<Clase>ControllerTest y <Clase>ControllerIT)
        │   └── service/      (<Clase>ServiceTest)
        └── resources/application-test.properties
```
