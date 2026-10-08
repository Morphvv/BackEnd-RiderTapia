# ms-pagos — Rider Tapia Biker

Microservicio REST de **pagos** de Rider Tapia Biker (épica Gestion de Pagos, EP-13). Implementa la parte de pagos del diagrama de clases de Pedidos y Pagos: el pago como clase abstracta con sus tres formas (WebPay, transferencia y pago presencial) y el cierre de caja diario. Las respuestas siguen el formato HATEOAS (HAL).

Un pedido puede tener varios pagos (por ejemplo, un WebPay rechazado y luego uno aprobado).

## Tecnologías

| Tecnología | Versión |
|---|---|
| Java | 25 |
| Spring Boot | 4.0.6 (webmvc, data-jpa, hateoas) |
| springdoc-openapi (Swagger UI) | 3.0.3 |
| MySQL (mysql-connector-j) | runtime |
| H2 | runtime y tests |
| Lombok | gestionado por Spring Boot |
| RestTemplate | cliente HTTP hacia ms-pedidos |
| JUnit 5 + Mockito + MockMvc | tests |
| JaCoCo | 0.8.14 |

## Del diagrama al código

| Clase del diagrama | En el código |
|---|---|
| Pago (abstracta) | Clase abstracta JPA con herencia `JOINED`: tabla `pagos` con id de pedido, monto, fecha y estado |
| PagoWebPay, PagoTransferencia, PagoPresencial | Subclases de Pago, cada una con su tabla, repository, service, controller y tests |
| CierreCaja | Entidad JPA con repository, service, controller y tests |
| EstadoPago, MedioPago | Enums de Java guardados como texto |
| Pedido | Está en `ms-pedidos`: el pago guarda `idPedido` y se valida por HTTP |
| Empleado | Pertenece a otro microservicio: CierreCaja guarda solo `idEmpleado` |

## Rol en la arquitectura

**Consume**:

- `ms-pedidos` (puerto 8086): antes de guardar cualquier pago llama a `GET /api/v1/pedidos/{idPedido}`. Si el pedido no existe, no se guarda y responde 404. La URL se configura en `ms.pedidos.url`.

**Es consumido por**:

- Frontend tienda: registra el resultado del pago WebPay y sube el comprobante de transferencia.
- Panel administrador / vendedor: registra pagos presenciales, aprueba o rechaza transferencias, marca reembolsos y hace el cierre de caja.
- `ms-reportes` (futuro): ventas por medio de pago.

Para registrar pagos en local, `ms-pedidos` debe estar levantado.

## Configuración

| Propiedad | Valor |
|---|---|
| Puerto | `8087` |
| Base de datos | `ridertapia_pagos` (MySQL, se crea sola con `createDatabaseIfNotExist=true`) |
| Usuario / clave MySQL | `root` / vacío (ajustar en `application.properties`) |
| URL ms-pedidos | `ms.pedidos.url=http://localhost:8086/api/v1/pedidos` |
| Swagger UI | http://localhost:8087/doc/swagger-ui.html |
| OpenAPI JSON | http://localhost:8087/api-docs |

Ejecutar:

```bash
./mvnw spring-boot:run
```

## Endpoints

Cada clase tiene su propio controller con el mismo patrón: listar, obtener por id, una búsqueda adicional, crear, actualizar y eliminar.

### PagoWebPay — `/api/v1/pagos-webpay`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/pagos-webpay` | Lista todos | 200 |
| GET | `/api/v1/pagos-webpay/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/pagos-webpay/idPedido/{idPedido}` | Lista los que tienen ese idPedido | 200 |
| POST | `/api/v1/pagos-webpay` | Crea | 201 |
| PUT | `/api/v1/pagos-webpay/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/pagos-webpay/{id}` | Elimina | 204 |

### PagoTransferencia — `/api/v1/pagos-transferencia`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/pagos-transferencia` | Lista todos | 200 |
| GET | `/api/v1/pagos-transferencia/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/pagos-transferencia/idPedido/{idPedido}` | Lista los que tienen ese idPedido | 200 |
| POST | `/api/v1/pagos-transferencia` | Crea | 201 |
| PUT | `/api/v1/pagos-transferencia/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/pagos-transferencia/{id}` | Elimina | 204 |

### PagoPresencial — `/api/v1/pagos-presenciales`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/pagos-presenciales` | Lista todos | 200 |
| GET | `/api/v1/pagos-presenciales/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/pagos-presenciales/idPedido/{idPedido}` | Lista los que tienen ese idPedido | 200 |
| POST | `/api/v1/pagos-presenciales` | Crea | 201 |
| PUT | `/api/v1/pagos-presenciales/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/pagos-presenciales/{id}` | Elimina | 204 |

### CierreCaja — `/api/v1/cierres-caja`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/cierres-caja` | Lista todos | 200 |
| GET | `/api/v1/cierres-caja/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/cierres-caja/idEmpleado/{idEmpleado}` | Lista los que tienen ese idEmpleado | 200 |
| POST | `/api/v1/cierres-caja` | Crea | 201 |
| PUT | `/api/v1/cierres-caja/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/cierres-caja/{id}` | Elimina | 204 |

### Ejemplo: PagoWebPay

Entrada (POST / PUT). Las relaciones se envían solo con su id:

```json
{
  "idPedido": 5,
  "monto": 899990,
  "fecha": "2026-10-06T10:35:00",
  "estado": "APROBADO",
  "token": "01ab7c9e2f",
  "codigoAutorizacion": "AUT-123456",
  "ultimosDigitosTarjeta": "4417"
}
```

Respuesta HATEOAS (las relaciones vuelven completas):

```json
{
  "idPago": 1,
  "idPedido": 5,
  "monto": 899990,
  "fecha": "2026-10-06T10:35:00",
  "estado": "APROBADO",
  "token": "01ab7c9e2f",
  "codigoAutorizacion": "AUT-123456",
  "ultimosDigitosTarjeta": "4417",
  "_links": {
    "self": {
      "href": "http://localhost:8087/api/v1/pagos-webpay/1"
    },
    "pagosWebpay": {
      "href": "http://localhost:8087/api/v1/pagos-webpay"
    }
  }
}
```

### Ejemplo: PagoPresencial

Entrada (POST / PUT). Las relaciones se envían solo con su id:

```json
{
  "idPedido": 5,
  "monto": 899990,
  "fecha": "2026-10-06T10:35:00",
  "estado": "APROBADO",
  "medioPago": "EFECTIVO"
}
```

Respuesta HATEOAS (las relaciones vuelven completas):

```json
{
  "idPago": 1,
  "idPedido": 5,
  "monto": 899990,
  "fecha": "2026-10-06T10:35:00",
  "estado": "APROBADO",
  "medioPago": "EFECTIVO",
  "_links": {
    "self": {
      "href": "http://localhost:8087/api/v1/pagos-presenciales/1"
    },
    "pagosPresenciales": {
      "href": "http://localhost:8087/api/v1/pagos-presenciales"
    }
  }
}
```

### Ejemplo de listado HATEOAS (PagoWebPay)

```json
{
  "_embedded": {
    "pagoWebPayList": [
      {
        "idPago": 1,
        "idPedido": 5,
        "monto": 899990,
        "fecha": "2026-10-06T10:35:00",
        "estado": "APROBADO",
        "token": "01ab7c9e2f",
        "codigoAutorizacion": "AUT-123456",
        "ultimosDigitosTarjeta": "4417",
        "_links": {
          "self": {
            "href": "http://localhost:8087/api/v1/pagos-webpay/1"
          }
        }
      }
    ]
  },
  "_links": {
    "self": {
      "href": "http://localhost:8087/api/v1/pagos-webpay"
    }
  }
}
```

## Modelo de datos

### Pago — clase abstracta (tabla `pagos`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idPago | Long | id_pago | PK, autoincremental |
| idPedido | Long | id_pedido | NOT NULL |
| monto | Integer | monto | NOT NULL |
| fecha | LocalDateTime | fecha | NOT NULL |
| estado | EstadoPago (enum) | estado | NOT NULL: `PENDIENTE`, `APROBADO`, `RECHAZADO`, `REEMBOLSADO` |

### PagoWebPay — hereda de Pago (tabla `pagos_webpay`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idPago | Long | id_pago | PK y FK a Pago |
| token | String | token | NOT NULL, máx. 100 |
| codigoAutorizacion | String | codigo_autorizacion | opcional, máx. 50 |
| ultimosDigitosTarjeta | String | ultimos_digitos_tarjeta | opcional, máx. 4 |

### PagoTransferencia — hereda de Pago (tabla `pagos_transferencia`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idPago | Long | id_pago | PK y FK a Pago |
| comprobanteUrl | String | comprobante_url | opcional, máx. 255 |
| fechaLimite | LocalDateTime | fecha_limite | NOT NULL |
| observacion | String | observacion | opcional, máx. 255 |

### PagoPresencial — hereda de Pago (tabla `pagos_presenciales`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idPago | Long | id_pago | PK y FK a Pago |
| medioPago | MedioPago (enum) | medio_pago | NOT NULL: `EFECTIVO`, `DEBITO`, `CREDITO` |

### CierreCaja (tabla `cierres_caja`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idCierreCaja | Long | id_cierre_caja | PK, autoincremental |
| fecha | LocalDate | fecha | NOT NULL |
| totalEfectivo | Integer | total_efectivo | NOT NULL |
| totalDebito | Integer | total_debito | NOT NULL |
| totalCredito | Integer | total_credito | NOT NULL |
| totalTransferencia | Integer | total_transferencia | NOT NULL |
| efectivoContado | Integer | efectivo_contado | NOT NULL |
| diferencia | Integer | diferencia | NOT NULL |
| idEmpleado | Long | id_empleado | NOT NULL |

## Tests

Los tests usan el perfil `test` (`src/test/resources/application-test.properties`) con H2 en memoria, así que **no necesitan MySQL ni ms-pedidos**: el `RestTemplate` se reemplaza por un mock (`@Mock` en los tests de service y `@MockitoBean` en los de integración).

```bash
./mvnw test
```

Reporte de cobertura JaCoCo: `target/site/jacoco/index.html`

Por cada clase hay tres archivos de test:

| Test | Tipo | Qué cubre |
|---|---|---|
| `<Clase>ServiceTest` | Unitario (Mockito) | Todos los métodos del service, casos no existentes y pedido inexistente |
| `<Clase>ControllerTest` | `@WebMvcTest` | Códigos HTTP, campos, enlaces HATEOAS, 404 y 409 |
| `<Clase>ControllerIT` | Integración (`@SpringBootTest` + H2) | Crear + listar + búsqueda, pedido inexistente, actualizar, eliminar + 404 |

Además, `PagoApplicationTests` prueba la carga del contexto, y `LimpiezaBaseDatos` vacía las tablas antes de cada test de integración en el orden correcto, primero los registros que dependen de otros.

## Estructura del proyecto

```
ms-pagos/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/ridertapia/pagos/
    │   │   ├── PagoApplication.java
    │   │   ├── controller/   (4 controllers, uno por clase concreta)
    │   │   ├── model/        (5 entidades: 4 concretas + 1 abstracta, y 2 enums)
    │   │   ├── repository/   (4 repositories)
    │   │   └── service/      (4 services)
    │   └── resources/application.properties
    └── test/
        ├── java/com/ridertapia/pagos/
        │   ├── PagoApplicationTests.java
        │   ├── LimpiezaBaseDatos.java
        │   ├── controller/   (<Clase>ControllerTest y <Clase>ControllerIT)
        │   └── service/      (<Clase>ServiceTest)
        └── resources/application-test.properties
```
