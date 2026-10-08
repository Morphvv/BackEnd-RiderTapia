# ms-carrito — Rider Tapia Biker

Microservicio REST del **carrito de compra** de Rider Tapia Biker (épica Carrito de compra). Implementa las clases del diagrama de clases de Carrito: el carrito con sus totales y estado, los ítems que contiene, y las clases de respuesta para el resumen del encabezado, los avisos de cambios de stock y la validación de cupones. Las respuestas siguen el formato HATEOAS (HAL).

Como el carrito queda guardado en base de datos, se mantiene aunque el cliente cierre el navegador (HU-24). Un visitante sin cuenta se identifica con `tokenSesion`.

## Tecnologías

| Tecnología | Versión |
|---|---|
| Java | 25 |
| Spring Boot | 4.0.6 (webmvc, data-jpa, hateoas) |
| springdoc-openapi (Swagger UI) | 3.0.3 |
| MySQL (mysql-connector-j) | runtime |
| H2 | runtime y tests |
| Lombok | gestionado por Spring Boot |
| RestTemplate | cliente HTTP hacia ms-productos y ms-inventario |
| JUnit 5 + Mockito + MockMvc | tests |
| JaCoCo | 0.8.14 |

## Del diagrama al código

| Clase del diagrama | En el código |
|---|---|
| Carrito, ItemCarrito | Entidades JPA, cada una con repository, service, controller y tests |
| ResumenCarrito, AvisoCambio, ResultadoValidacionCupon | Clases de respuesta en el paquete `dto` (no son tablas, se calculan al momento) |
| EstadoCarrito, TipoAviso | Enums de Java guardados como texto |
| GestorCarrito | No guarda datos: su lógica queda en los services (validar stock, carritos por cliente) |
| Cupon, Producto, VarianteProducto | Están en `ms-productos`: el carrito guarda `idCupon` y el ítem guarda `idVariante` |
| ItemInventario | Está en `ms-inventario`: el stock se consulta por HTTP |
| Cliente, Pedido | Pertenecen a otros microservicios: el carrito guarda solo el RUT del cliente (`rutCliente`) |

Relaciones dentro del microservicio: ItemCarrito → Carrito (muchos a uno).

## Rol en la arquitectura

**Consume**:

- `ms-productos` (puerto 8085):
  - `GET /api/v1/variantes/{id}`: al agregar un ítem valida que la variante exista (si no, 404).
  - `GET /api/v1/cupones/codigo/{codigo}`: para validar un cupón contra el carrito.
- `ms-inventario` (puerto 8088): `GET /api/v1/items-inventario/idVariante/{idVariante}`. Al agregar o cambiar la cantidad de un ítem, si no hay stock suficiente responde **409 Conflict** (HU-19 y HU-20). También se usa para los avisos de stock.
- `ms-cliente` (puerto 8082): `GET /api/v1/clientes/{rut}`. Al crear un carrito con `rutCliente`, valida que el cliente exista (si no, 404). Los carritos anónimos (sin RUT) no se validan.

**Es consumido por**:

- Frontend tienda: agrega y quita productos, muestra el carrito, el mini carrito del encabezado y los avisos antes de pagar.
- `ms-pedidos`: el pedido se crea a partir del carrito.

Para agregar ítems en local, `ms-productos` y `ms-inventario` deben estar levantados.

## Configuración

| Propiedad | Valor |
|---|---|
| Puerto | `8089` |
| Base de datos | `ridertapia_carrito` (MySQL, se crea sola con `createDatabaseIfNotExist=true`) |
| Usuario / clave MySQL | `root` / vacío (ajustar en `application.properties`) |
| URL variantes | `ms.productos.variantes-url=http://localhost:8085/api/v1/variantes` |
| URL cupones | `ms.productos.cupones-url=http://localhost:8085/api/v1/cupones` |
| URL inventario | `ms.inventario.url=http://localhost:8088/api/v1/items-inventario` |
| URL clientes | `ms.clientes.url=http://localhost:8082/api/v1/clientes` |
| Swagger UI | http://localhost:8089/doc/swagger-ui.html |
| OpenAPI JSON | http://localhost:8089/api-docs |

Ejecutar:

```bash
./mvnw spring-boot:run
```

## Endpoints

Cada clase tiene su propio controller con el mismo patrón: listar, obtener por id, una búsqueda adicional, crear, actualizar y eliminar.

### Carrito — `/api/v1/carritos`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/carritos` | Lista todos | 200 |
| GET | `/api/v1/carritos/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/carritos/rutCliente/{rutCliente}` | Lista los carritos de ese RUT | 200 |
| POST | `/api/v1/carritos` | Crea | 201 |
| PUT | `/api/v1/carritos/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/carritos/{id}` | Elimina | 204 / 409 si está en uso |

### ItemCarrito — `/api/v1/items-carrito`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/items-carrito` | Lista todos | 200 |
| GET | `/api/v1/items-carrito/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/items-carrito/carrito/{idCarrito}` | Lista los de un carrito | 200 |
| POST | `/api/v1/items-carrito` | Agrega un producto al carrito | 201 / 404 si la variante o el carrito no existe / 409 sin stock |
| PUT | `/api/v1/items-carrito/{id}` | Cambia la cantidad | 200 / 404 / 409 sin stock |
| DELETE | `/api/v1/items-carrito/{id}` | Elimina | 204 |

### Ejemplo: Carrito

Entrada (POST / PUT). Las relaciones se envían solo con su id:

```json
{
  "rutCliente": "12345678-5",
  "tokenSesion": "tok-abc123",
  "fechaCreacion": "2026-10-06T11:00:00",
  "fechaActualizacion": "2026-10-06T11:30:00",
  "estado": "ACTIVO",
  "subtotal": 899990,
  "descuento": 0,
  "total": 899990,
  "idCupon": 2
}
```

Respuesta HATEOAS (las relaciones vuelven completas):

```json
{
  "idCarrito": 1,
  "rutCliente": "12345678-5",
  "tokenSesion": "tok-abc123",
  "fechaCreacion": "2026-10-06T11:00:00",
  "fechaActualizacion": "2026-10-06T11:30:00",
  "estado": "ACTIVO",
  "subtotal": 899990,
  "descuento": 0,
  "total": 899990,
  "idCupon": 2,
  "_links": {
    "self": {
      "href": "http://localhost:8089/api/v1/carritos/1"
    },
    "carritos": {
      "href": "http://localhost:8089/api/v1/carritos"
    }
  }
}
```

### Ejemplo: ItemCarrito

Entrada (POST / PUT). Las relaciones se envían solo con su id:

```json
{
  "idVariante": 10,
  "cantidad": 1,
  "precioUnitario": 899990,
  "fechaAgregado": "2026-10-06T11:00:00",
  "carrito": {
    "idCarrito": 1
  }
}
```

Respuesta HATEOAS (las relaciones vuelven completas):

```json
{
  "idItemCarrito": 1,
  "idVariante": 10,
  "cantidad": 1,
  "precioUnitario": 899990,
  "fechaAgregado": "2026-10-06T11:00:00",
  "carrito": {
    "idCarrito": 1,
    "rutCliente": "12345678-5",
    "tokenSesion": "tok-abc123",
    "fechaCreacion": "2026-10-06T11:00:00",
    "fechaActualizacion": "2026-10-06T11:30:00",
    "estado": "ACTIVO",
    "subtotal": 899990,
    "descuento": 0,
    "total": 899990,
    "idCupon": 2
  },
  "_links": {
    "self": {
      "href": "http://localhost:8089/api/v1/items-carrito/1"
    },
    "itemsCarrito": {
      "href": "http://localhost:8089/api/v1/items-carrito"
    }
  }
}
```

### Ejemplo de listado HATEOAS (Carrito)

```json
{
  "_embedded": {
    "carritoList": [
      {
        "idCarrito": 1,
        "rutCliente": "12345678-5",
        "tokenSesion": "tok-abc123",
        "fechaCreacion": "2026-10-06T11:00:00",
        "fechaActualizacion": "2026-10-06T11:30:00",
        "estado": "ACTIVO",
        "subtotal": 899990,
        "descuento": 0,
        "total": 899990,
        "idCupon": 2,
        "_links": {
          "self": {
            "href": "http://localhost:8089/api/v1/carritos/1"
          }
        }
      }
    ]
  },
  "_links": {
    "self": {
      "href": "http://localhost:8089/api/v1/carritos"
    }
  }
}
```

## Modelo de datos

### Carrito (tabla `carritos`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idCarrito | Long | id_carrito | PK, autoincremental |
| rutCliente | String(12) | rut_cliente | opcional (si viene, debe existir en ms-cliente) |
| tokenSesion | String | token_sesion | opcional, máx. 100 |
| fechaCreacion | LocalDateTime | fecha_creacion | NOT NULL |
| fechaActualizacion | LocalDateTime | fecha_actualizacion | NOT NULL |
| estado | EstadoCarrito (enum) | estado | NOT NULL: `ACTIVO`, `CONVERTIDO_EN_PEDIDO`, `ABANDONADO` |
| subtotal | Integer | subtotal | NOT NULL |
| descuento | Integer | descuento | NOT NULL |
| total | Integer | total | NOT NULL |
| idCupon | Long | id_cupon | opcional |

### ItemCarrito (tabla `items_carrito`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idItemCarrito | Long | id_item_carrito | PK, autoincremental |
| idVariante | Long | id_variante | NOT NULL |
| cantidad | Integer | cantidad | NOT NULL |
| precioUnitario | Integer | precio_unitario | NOT NULL |
| fechaAgregado | LocalDateTime | fecha_agregado | NOT NULL |
| carrito | Carrito | id_carrito | FK a Carrito, NOT NULL |

## Operaciones del carrito

Además del CRUD, `CarritoController` tiene tres endpoints que implementan métodos del diagrama:

| Método | Ruta | Método del diagrama | Respuesta |
|---|---|---|---|
| GET | `/api/v1/carritos/{id}/resumen` | `getResumen()` | `ResumenCarrito` con cantidad de productos y total (HU-25). 200 / 404 |
| GET | `/api/v1/carritos/{id}/avisos` | `revalidar()` | Lista de `AvisoCambio` para los ítems sin stock o con stock insuficiente (HU-26). 200 / 404 |
| GET | `/api/v1/carritos/{id}/cupon/{codigo}` | `aplicarCupon()` | `ResultadoValidacionCupon` con si es válido, el motivo y el monto a descontar (HU-22). 200 / 404 |

Ejemplo de resumen:

```json
{
  "cantidadItems": 3,
  "total": 979980,
  "_links": {
    "self": { "href": "http://localhost:8089/api/v1/carritos/1/resumen" },
    "carrito": { "href": "http://localhost:8089/api/v1/carritos/1" }
  }
}
```

Ejemplo de validación de cupón:

```json
{
  "valido": false,
  "mensaje": "El cupón está vencido",
  "montoDescuento": 0,
  "_links": {
    "self": { "href": "http://localhost:8089/api/v1/carritos/1/cupon/RIDER10" },
    "carrito": { "href": "http://localhost:8089/api/v1/carritos/1" }
  }
}
```

La validación del cupón revisa que exista, esté activo, no esté vencido y tenga usos disponibles. Calcula el descuento por porcentaje o monto fijo sobre el subtotal. Solo valida: no guarda el cupón en el carrito; para aplicarlo se actualiza el carrito con `idCupon`, `descuento` y `total`.

## Tests

Los tests usan el perfil `test` (`src/test/resources/application-test.properties`) con H2 en memoria y **no necesitan MySQL, ms-productos ni ms-inventario**: el `RestTemplate` se reemplaza por un mock (`@Mock` en los tests de service y `@MockitoBean` en los de integración).

```bash
./mvnw test
```

Reporte de cobertura JaCoCo: `target/site/jacoco/index.html`

| Test | Tipo | Qué cubre |
|---|---|---|
| `CarritoServiceTest`, `ItemCarritoServiceTest` | Unitario (Mockito) | Todos los métodos de los services, casos no existentes, variante inexistente y stock insuficiente |
| `CarritoControllerTest`, `ItemCarritoControllerTest` | `@WebMvcTest` | Códigos HTTP (200, 201, 204, 404, 409), campos y enlaces HATEOAS |
| `CarritoControllerIT`, `ItemCarritoControllerIT` | Integración (`@SpringBootTest` + H2) | Crear + listar + búsqueda, variante inexistente, sin stock, actualizar, eliminar + 404 |
| `CarritoOperacionesServiceTest` | Unitario (Mockito) | Resumen, avisos de stock y validación de cupón (válido, monto fijo, inexistente, inactivo, vencido, sin usos) |
| `CarritoOperacionesControllerTest` | `@WebMvcTest` | Endpoints de resumen, avisos y cupón |

Además, `CarritoApplicationTests` prueba la carga del contexto, y `LimpiezaBaseDatos` vacía las tablas antes de cada test de integración en el orden correcto.

## Estructura del proyecto

```
ms-carrito/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/ridertapia/carrito/
    │   │   ├── CarritoApplication.java
    │   │   ├── controller/   (CarritoController, ItemCarritoController)
    │   │   ├── dto/          (ResumenCarrito, AvisoCambio, ResultadoValidacionCupon)
    │   │   ├── model/        (Carrito, ItemCarrito, EstadoCarrito, TipoAviso)
    │   │   ├── repository/   (CarritoRepository, ItemCarritoRepository)
    │   │   └── service/      (CarritoService, ItemCarritoService)
    │   └── resources/application.properties
    └── test/
        ├── java/com/ridertapia/carrito/
        │   ├── CarritoApplicationTests.java
        │   ├── LimpiezaBaseDatos.java
        │   ├── controller/   (ControllerTest, ControllerIT y CarritoOperacionesControllerTest)
        │   └── service/      (ServiceTest y CarritoOperacionesServiceTest)
        └── resources/application-test.properties
```
