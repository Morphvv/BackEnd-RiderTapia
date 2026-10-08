# ms-pedidos — Rider Tapia Biker

Microservicio REST de **gestión de pedidos** de Rider Tapia Biker (épica Gestion de Pedidos, EP-4). Implementa las clases del diagrama de clases de Pedidos: el pedido con su detalle, el historial de cambios de estado, los datos del visitante que compra sin cuenta, los documentos tributarios (boleta y factura) y las notificaciones al cliente. Las respuestas siguen el formato HATEOAS (HAL).

Rider Tapia trabaja solo con **retiro en tienda**, así que no hay despacho: el ciclo del pedido termina en `RETIRADO`. Los pagos viven en `ms-pagos`.

## Tecnologías

| Tecnología | Versión |
|---|---|
| Java | 25 |
| Spring Boot | 4.0.6 (webmvc, data-jpa, hateoas) |
| springdoc-openapi (Swagger UI) | 3.0.3 |
| MySQL (mysql-connector-j) | runtime |
| H2 | runtime y tests |
| Lombok | gestionado por Spring Boot |
| JUnit 5 + Mockito + MockMvc | tests |
| JaCoCo | 0.8.14 |

## Del diagrama al código

| Clase del diagrama | En el código |
|---|---|
| Pedido, DetallePedido, HistorialEstadoPedido, DatosVisitante, NotificacionPedido | Entidades JPA, cada una con repository, service, controller y tests |
| DocumentoTributario (abstracta) | Clase abstracta JPA con herencia `JOINED`: tabla `documentos_tributarios` con los datos comunes |
| Boleta, Factura | Subclases de DocumentoTributario, cada una con su tabla, repository, service, controller y tests |
| EstadoPedido, CanalVenta | Enums de Java guardados como texto |
| GestorPedidos | No guarda datos: sus búsquedas quedan en los services (por número, detalle por pedido, etc.) |
| Cliente, Empleado, Cupon, VarianteProducto, Carrito | Pertenecen a otros microservicios: se guarda solo su id o RUT (`rutCliente`, `idEmpleado`, `idCupon`, `idVariante`) |
| Pago, CierreCaja | Están en `ms-pagos` (épica Gestion de Pagos) |
| DireccionDespacho, MetodoEntrega | Se quitaron porque la tienda no hace despacho |

Relaciones dentro del microservicio:

- Pedido → DatosVisitante (opcional, para compras sin cuenta).
- DetallePedido, HistorialEstadoPedido, Boleta, Factura y NotificacionPedido → Pedido (muchos a uno).

## Rol en la arquitectura

**Consume**:

- `ms-cliente` (puerto 8082): `GET /api/v1/clientes/{rut}`. Al crear un pedido con `rutCliente`, valida que el cliente exista (si no, 404). Los pedidos de visitantes (sin RUT) no se validan. La URL se configura en `ms.clientes.url`.

**Es consumido por**:

- Frontend tienda: el checkout crea el pedido y su detalle; el cliente consulta su historial.
- Panel administrador / vendedor: lista pedidos, cambia estados (queda en el historial), emite boleta o factura.
- `ms-pagos` (puerto 8087): valida que el pedido exista antes de registrar un pago (`GET /api/v1/pedidos/{id}`).
- `ms-reportes` (puerto 8084): lee pedidos y detalles para los reportes de venta y para emitir boletas y facturas.

## Configuración

| Propiedad | Valor |
|---|---|
| Puerto | `8086` |
| Base de datos | `ridertapia_pedidos` (MySQL, se crea sola con `createDatabaseIfNotExist=true`) |
| Usuario / clave MySQL | `root` / vacío (ajustar en `application.properties`) |
| Swagger UI | http://localhost:8086/doc/swagger-ui.html |
| OpenAPI JSON | http://localhost:8086/api-docs |

Ejecutar:

```bash
./mvnw spring-boot:run
```

Al crear o actualizar, las relaciones se envían solo con su id (por ejemplo `"pedido": {"idPedido": 1}`). El service busca el registro real; si no existe responde 404. Al eliminar un registro que otro está usando (por ejemplo, un pedido con detalle) responde 409. Un número de pedido repetido también responde 409.

## Endpoints

Cada clase tiene su propio controller con el mismo patrón: listar, obtener por id, una búsqueda adicional, crear, actualizar y eliminar.

### DatosVisitante — `/api/v1/visitantes`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/visitantes` | Lista todos | 200 |
| GET | `/api/v1/visitantes/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/visitantes/correo/{correo}` | Lista los que tienen ese correo | 200 |
| POST | `/api/v1/visitantes` | Crea | 201 |
| PUT | `/api/v1/visitantes/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/visitantes/{id}` | Elimina | 204 / 409 si está en uso |

### Pedido — `/api/v1/pedidos`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/pedidos` | Lista todos | 200 |
| GET | `/api/v1/pedidos/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/pedidos/numero/{numero}` | Busca por numero | 200 / 404 |
| POST | `/api/v1/pedidos` | Crea | 201 / 404 si una relación no existe / 409 si el valor único ya existe |
| PUT | `/api/v1/pedidos/{id}` | Actualiza | 200 / 404 / 409 si el valor único ya existe |
| DELETE | `/api/v1/pedidos/{id}` | Elimina | 204 / 409 si está en uso |

### DetallePedido — `/api/v1/detalles`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/detalles` | Lista todos | 200 |
| GET | `/api/v1/detalles/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/detalles/pedido/{idPedido}` | Lista los de un pedido | 200 |
| POST | `/api/v1/detalles` | Crea | 201 / 404 si una relación no existe |
| PUT | `/api/v1/detalles/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/detalles/{id}` | Elimina | 204 |

### HistorialEstadoPedido — `/api/v1/historial-estados`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/historial-estados` | Lista todos | 200 |
| GET | `/api/v1/historial-estados/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/historial-estados/pedido/{idPedido}` | Lista los de un pedido | 200 |
| POST | `/api/v1/historial-estados` | Crea | 201 / 404 si una relación no existe |
| PUT | `/api/v1/historial-estados/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/historial-estados/{id}` | Elimina | 204 |

### Boleta — `/api/v1/boletas`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/boletas` | Lista todos | 200 |
| GET | `/api/v1/boletas/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/boletas/pedido/{idPedido}` | Lista los de un pedido | 200 |
| POST | `/api/v1/boletas` | Crea | 201 / 404 si una relación no existe |
| PUT | `/api/v1/boletas/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/boletas/{id}` | Elimina | 204 |

### Factura — `/api/v1/facturas`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/facturas` | Lista todos | 200 |
| GET | `/api/v1/facturas/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/facturas/pedido/{idPedido}` | Lista los de un pedido | 200 |
| POST | `/api/v1/facturas` | Crea | 201 / 404 si una relación no existe |
| PUT | `/api/v1/facturas/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/facturas/{id}` | Elimina | 204 |

### NotificacionPedido — `/api/v1/notificaciones`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/notificaciones` | Lista todos | 200 |
| GET | `/api/v1/notificaciones/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/notificaciones/pedido/{idPedido}` | Lista los de un pedido | 200 |
| POST | `/api/v1/notificaciones` | Crea | 201 / 404 si una relación no existe |
| PUT | `/api/v1/notificaciones/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/notificaciones/{id}` | Elimina | 204 |

### Ejemplo: Pedido

Entrada (POST / PUT). Las relaciones se envían solo con su id:

```json
{
  "numero": "PED-0001",
  "fechaCreacion": "2026-10-06T10:00:00",
  "fechaExpiracion": "2026-10-07T10:00:00",
  "estado": "PENDIENTE_PAGO",
  "canal": "WEB",
  "subtotal": 899990,
  "descuento": 0,
  "total": 899990,
  "motivoAnulacion": "No aplica",
  "rutCliente": "12345678-5",
  "idCupon": 2,
  "idEmpleado": 1,
  "datosVisitante": null
}
```

Respuesta HATEOAS (las relaciones vuelven completas):

```json
{
  "idPedido": 1,
  "numero": "PED-0001",
  "fechaCreacion": "2026-10-06T10:00:00",
  "fechaExpiracion": "2026-10-07T10:00:00",
  "estado": "PENDIENTE_PAGO",
  "canal": "WEB",
  "subtotal": 899990,
  "descuento": 0,
  "total": 899990,
  "motivoAnulacion": "No aplica",
  "rutCliente": "12345678-5",
  "idCupon": 2,
  "idEmpleado": 1,
  "datosVisitante": null,
  "_links": {
    "self": {
      "href": "http://localhost:8086/api/v1/pedidos/1"
    },
    "pedidos": {
      "href": "http://localhost:8086/api/v1/pedidos"
    }
  }
}
```

### Ejemplo: DetallePedido

Entrada (POST / PUT). Las relaciones se envían solo con su id:

```json
{
  "idVariante": 10,
  "cantidad": 1,
  "precioUnitario": 899990,
  "subtotal": 899990,
  "pedido": {
    "idPedido": 1
  }
}
```

Respuesta HATEOAS (las relaciones vuelven completas):

```json
{
  "idDetallePedido": 1,
  "idVariante": 10,
  "cantidad": 1,
  "precioUnitario": 899990,
  "subtotal": 899990,
  "pedido": {
    "idPedido": 1,
    "numero": "PED-0001",
    "fechaCreacion": "2026-10-06T10:00:00",
    "fechaExpiracion": "2026-10-07T10:00:00",
    "estado": "PENDIENTE_PAGO",
    "canal": "WEB",
    "subtotal": 899990,
    "descuento": 0,
    "total": 899990,
    "motivoAnulacion": "No aplica",
    "rutCliente": "12345678-5",
    "idCupon": 2,
    "idEmpleado": 1,
    "datosVisitante": null
  },
  "_links": {
    "self": {
      "href": "http://localhost:8086/api/v1/detalles/1"
    },
    "detalles": {
      "href": "http://localhost:8086/api/v1/detalles"
    }
  }
}
```

### Ejemplo de listado HATEOAS (Pedido)

```json
{
  "_embedded": {
    "pedidoList": [
      {
        "idPedido": 1,
        "numero": "PED-0001",
        "fechaCreacion": "2026-10-06T10:00:00",
        "fechaExpiracion": "2026-10-07T10:00:00",
        "estado": "PENDIENTE_PAGO",
        "canal": "WEB",
        "subtotal": 899990,
        "descuento": 0,
        "total": 899990,
        "motivoAnulacion": "No aplica",
        "rutCliente": "12345678-5",
        "idCupon": 2,
        "idEmpleado": 1,
        "datosVisitante": null,
        "_links": {
          "self": {
            "href": "http://localhost:8086/api/v1/pedidos/1"
          }
        }
      }
    ]
  },
  "_links": {
    "self": {
      "href": "http://localhost:8086/api/v1/pedidos"
    }
  }
}
```

## Modelo de datos

### DatosVisitante (tabla `datos_visitantes`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idDatosVisitante | Long | id_datos_visitante | PK, autoincremental |
| nombre | String | nombre | NOT NULL, máx. 100 |
| correo | String | correo | NOT NULL, máx. 100 |
| telefono | String | telefono | opcional, máx. 20 |

### Pedido (tabla `pedidos`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idPedido | Long | id_pedido | PK, autoincremental |
| numero | String | numero | NOT NULL, único, máx. 20 |
| fechaCreacion | LocalDateTime | fecha_creacion | NOT NULL |
| fechaExpiracion | LocalDateTime | fecha_expiracion | opcional |
| estado | EstadoPedido (enum) | estado | NOT NULL: `PENDIENTE_PAGO`, `PAGADO`, `EN_PREPARACION`, `LISTO_PARA_RETIRO`, `RETIRADO`, `CANCELADO`, `ANULADO` |
| canal | CanalVenta (enum) | canal | NOT NULL: `WEB`, `TIENDA` |
| subtotal | Integer | subtotal | NOT NULL |
| descuento | Integer | descuento | NOT NULL |
| total | Integer | total | NOT NULL |
| motivoAnulacion | String | motivo_anulacion | opcional, máx. 255 |
| rutCliente | String(12) | rut_cliente | opcional (si viene, debe existir en ms-cliente) |
| idCupon | Long | id_cupon | opcional |
| idEmpleado | Long | id_empleado | opcional |
| datosVisitante | DatosVisitante | id_datos_visitante | FK a DatosVisitante, opcional |

### DetallePedido (tabla `detalles_pedido`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idDetallePedido | Long | id_detalle_pedido | PK, autoincremental |
| idVariante | Long | id_variante | NOT NULL |
| cantidad | Integer | cantidad | NOT NULL |
| precioUnitario | Integer | precio_unitario | NOT NULL |
| subtotal | Integer | subtotal | NOT NULL |
| pedido | Pedido | id_pedido | FK a Pedido, NOT NULL |

### HistorialEstadoPedido (tabla `historial_estados_pedido`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idHistorialEstadoPedido | Long | id_historial_estado_pedido | PK, autoincremental |
| estadoAnterior | EstadoPedido (enum) | estado_anterior | NOT NULL: `PENDIENTE_PAGO`, `PAGADO`, `EN_PREPARACION`, `LISTO_PARA_RETIRO`, `RETIRADO`, `CANCELADO`, `ANULADO` |
| estadoNuevo | EstadoPedido (enum) | estado_nuevo | NOT NULL: `PENDIENTE_PAGO`, `PAGADO`, `EN_PREPARACION`, `LISTO_PARA_RETIRO`, `RETIRADO`, `CANCELADO`, `ANULADO` |
| fechaCambio | LocalDateTime | fecha_cambio | NOT NULL |
| idEmpleado | Long | id_empleado | opcional |
| pedido | Pedido | id_pedido | FK a Pedido, NOT NULL |

### DocumentoTributario — clase abstracta (tabla `documentos_tributarios`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idDocumentoTributario | Long | id_documento_tributario | PK, autoincremental |
| folio | Integer | folio | NOT NULL |
| fechaEmision | LocalDate | fecha_emision | NOT NULL |
| montoNeto | Integer | monto_neto | NOT NULL |
| iva | Integer | iva | NOT NULL |
| total | Integer | total | NOT NULL |
| pdfUrl | String | pdf_url | opcional, máx. 255 |
| pedido | Pedido | id_pedido | FK a Pedido, NOT NULL |

### Boleta — hereda de DocumentoTributario (tabla `boletas`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idDocumentoTributario | Long | id_documento_tributario | PK y FK a DocumentoTributario |

### Factura — hereda de DocumentoTributario (tabla `facturas`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idDocumentoTributario | Long | id_documento_tributario | PK y FK a DocumentoTributario |
| rutEmpresa | String | rut_empresa | NOT NULL, máx. 12 |
| razonSocial | String | razon_social | NOT NULL, máx. 150 |
| giro | String | giro | NOT NULL, máx. 150 |
| direccionEmpresa | String | direccion_empresa | NOT NULL, máx. 255 |

### NotificacionPedido (tabla `notificaciones_pedido`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idNotificacionPedido | Long | id_notificacion_pedido | PK, autoincremental |
| tipo | String | tipo | NOT NULL, máx. 30 |
| destinatario | String | destinatario | NOT NULL, máx. 100 |
| fechaEnvio | LocalDateTime | fecha_envio | NOT NULL |
| enviada | Boolean | enviada | NOT NULL |
| pedido | Pedido | id_pedido | FK a Pedido, NOT NULL |

## Tests

Los tests usan el perfil `test` (`src/test/resources/application-test.properties`) con H2 en memoria, así que **no necesitan MySQL**.

```bash
./mvnw test
```

Reporte de cobertura JaCoCo: `target/site/jacoco/index.html`

Por cada clase hay tres archivos de test:

| Test | Tipo | Qué cubre |
|---|---|---|
| `<Clase>ServiceTest` | Unitario (Mockito) | Todos los métodos del service, casos no existentes y relaciones inexistentes |
| `<Clase>ControllerTest` | `@WebMvcTest` | Códigos HTTP, campos, enlaces HATEOAS, 404 y 409 |
| `<Clase>ControllerIT` | Integración (`@SpringBootTest` + H2) | Crear + listar + búsqueda, actualizar, eliminar + 404 |

Además, `PedidoApplicationTests` prueba la carga del contexto, y `LimpiezaBaseDatos` vacía las tablas antes de cada test de integración en el orden correcto, primero los registros que dependen de otros.

## Estructura del proyecto

```
ms-pedidos/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/ridertapia/pedidos/
    │   │   ├── PedidoApplication.java
    │   │   ├── controller/   (7 controllers, uno por clase concreta)
    │   │   ├── model/        (8 entidades: 7 concretas + 1 abstracta, y 2 enums)
    │   │   ├── repository/   (7 repositories)
    │   │   └── service/      (7 services)
    │   └── resources/application.properties
    └── test/
        ├── java/com/ridertapia/pedidos/
        │   ├── PedidoApplicationTests.java
        │   ├── LimpiezaBaseDatos.java
        │   ├── controller/   (<Clase>ControllerTest y <Clase>ControllerIT)
        │   └── service/      (<Clase>ServiceTest)
        └── resources/application-test.properties
```
