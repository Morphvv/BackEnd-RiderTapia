# ms-productos — Rider Tapia Biker

Microservicio REST del **catálogo de productos** de Rider Tapia Biker (épica Catálogo de Productos). Implementa las clases del diagrama de clases de Catálogo: productos con su categoría y marca, variantes (talla, color, aro y SKU), imágenes, especificaciones técnicas, compatibilidades, ofertas, reseñas, avisos de stock y cupones de descuento. Las respuestas siguen el formato HATEOAS (HAL).

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
| Producto, Categoria, Marca, VarianteProducto, ImagenProducto, EspecificacionTecnica, Compatibilidad, Oferta, Resena, AvisoStock, Cupon | Entidades JPA, cada una con repository, service, controller y tests |
| EstadoPublicacion, TipoDescuento | Enums de Java guardados como texto |
| Catalogo | No guarda datos: sus búsquedas quedan en los services (por ejemplo, productos por categoría) |
| Cliente | Pertenece a otro microservicio: Resena guarda solo `idCliente` |

Relaciones dentro del microservicio:

- Producto → Categoria y Producto → Marca (muchos a uno).
- Categoria → Categoria padre (subcategorías, opcional).
- VarianteProducto, ImagenProducto, EspecificacionTecnica, Oferta, Resena y AvisoStock → Producto (muchos a uno).
- Producto ↔ Compatibilidad y Cupon ↔ Categoria (muchos a muchos).

El **stock no vive aquí**: está en `ms-inventario`, asociado a cada variante.

## Rol en la arquitectura

`ms-productos` **no consume** otros microservicios. Es **consumido por**:

- Frontend tienda y panel administrador: catálogo, fichas de producto, reseñas, ofertas y mantención del catálogo.
- `ms-inventario`: el stock se lleva por variante (`/api/v1/variantes`).
- `ms-carrito`: precio del producto y validación de cupones (`/api/v1/cupones/codigo/{codigo}`).
- `ms-pedidos`: datos del producto y la variante para el detalle del pedido.

## Configuración

| Propiedad | Valor |
|---|---|
| Puerto | `8085` |
| Base de datos | `ridertapia_productos` (MySQL, se crea sola con `createDatabaseIfNotExist=true`) |
| Usuario / clave MySQL | `root` / vacío (ajustar en `application.properties`) |
| Swagger UI | http://localhost:8085/doc/swagger-ui.html |
| OpenAPI JSON | http://localhost:8085/api-docs |

Ejecutar:

```bash
./mvnw spring-boot:run
```

Al crear o actualizar, las relaciones se envían solo con su id (por ejemplo `"categoria": {"idCategoria": 1}`). El service busca el registro real; si no existe responde 404. Al eliminar un registro que otro está usando (por ejemplo, una categoría con productos) responde 409.

## Endpoints

Cada clase tiene su propio controller con el mismo patrón: listar, obtener por id, una búsqueda adicional, crear, actualizar y eliminar.

### Categoria — `/api/v1/categorias`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/categorias` | Lista todos | 200 |
| GET | `/api/v1/categorias/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/categorias/nombre/{nombre}` | Busca por nombre | 200 / 404 |
| POST | `/api/v1/categorias` | Crea | 201 / 404 si una relación no existe / 409 si el valor único ya existe |
| PUT | `/api/v1/categorias/{id}` | Actualiza | 200 / 404 / 409 si el valor único ya existe |
| DELETE | `/api/v1/categorias/{id}` | Elimina | 204 / 409 si está en uso |

### Marca — `/api/v1/marcas`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/marcas` | Lista todos | 200 |
| GET | `/api/v1/marcas/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/marcas/nombre/{nombre}` | Busca por nombre | 200 / 404 |
| POST | `/api/v1/marcas` | Crea | 201 / 409 si el valor único ya existe |
| PUT | `/api/v1/marcas/{id}` | Actualiza | 200 / 404 / 409 si el valor único ya existe |
| DELETE | `/api/v1/marcas/{id}` | Elimina | 204 / 409 si está en uso |

### Compatibilidad — `/api/v1/compatibilidades`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/compatibilidades` | Lista todos | 200 |
| GET | `/api/v1/compatibilidades/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/compatibilidades/tipoBicicleta/{tipoBicicleta}` | Lista los que tienen ese tipoBicicleta | 200 |
| POST | `/api/v1/compatibilidades` | Crea | 201 |
| PUT | `/api/v1/compatibilidades/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/compatibilidades/{id}` | Elimina | 204 / 409 si está en uso |

### Producto — `/api/v1/productos`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/productos` | Lista todos | 200 |
| GET | `/api/v1/productos/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/productos/categoria/{idCategoria}` | Lista los de una categoria | 200 |
| POST | `/api/v1/productos` | Crea | 201 / 404 si una relación no existe |
| PUT | `/api/v1/productos/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/productos/{id}` | Elimina | 204 / 409 si está en uso |

### VarianteProducto — `/api/v1/variantes`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/variantes` | Lista todos | 200 |
| GET | `/api/v1/variantes/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/variantes/sku/{sku}` | Busca por sku | 200 / 404 |
| POST | `/api/v1/variantes` | Crea | 201 / 404 si una relación no existe / 409 si el valor único ya existe |
| PUT | `/api/v1/variantes/{id}` | Actualiza | 200 / 404 / 409 si el valor único ya existe |
| DELETE | `/api/v1/variantes/{id}` | Elimina | 204 |

### ImagenProducto — `/api/v1/imagenes`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/imagenes` | Lista todos | 200 |
| GET | `/api/v1/imagenes/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/imagenes/producto/{idProducto}` | Lista los de un producto | 200 |
| POST | `/api/v1/imagenes` | Crea | 201 / 404 si una relación no existe |
| PUT | `/api/v1/imagenes/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/imagenes/{id}` | Elimina | 204 |

### EspecificacionTecnica — `/api/v1/especificaciones`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/especificaciones` | Lista todos | 200 |
| GET | `/api/v1/especificaciones/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/especificaciones/producto/{idProducto}` | Lista los de un producto | 200 |
| POST | `/api/v1/especificaciones` | Crea | 201 / 404 si una relación no existe |
| PUT | `/api/v1/especificaciones/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/especificaciones/{id}` | Elimina | 204 |

### Oferta — `/api/v1/ofertas`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/ofertas` | Lista todos | 200 |
| GET | `/api/v1/ofertas/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/ofertas/producto/{idProducto}` | Lista los de un producto | 200 |
| POST | `/api/v1/ofertas` | Crea | 201 / 404 si una relación no existe |
| PUT | `/api/v1/ofertas/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/ofertas/{id}` | Elimina | 204 |

### Resena — `/api/v1/resenas`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/resenas` | Lista todos | 200 |
| GET | `/api/v1/resenas/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/resenas/producto/{idProducto}` | Lista los de un producto | 200 |
| POST | `/api/v1/resenas` | Crea | 201 / 404 si una relación no existe |
| PUT | `/api/v1/resenas/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/resenas/{id}` | Elimina | 204 |

### AvisoStock — `/api/v1/avisos-stock`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/avisos-stock` | Lista todos | 200 |
| GET | `/api/v1/avisos-stock/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/avisos-stock/producto/{idProducto}` | Lista los de un producto | 200 |
| POST | `/api/v1/avisos-stock` | Crea | 201 / 404 si una relación no existe |
| PUT | `/api/v1/avisos-stock/{id}` | Actualiza | 200 / 404 |
| DELETE | `/api/v1/avisos-stock/{id}` | Elimina | 204 |

### Cupon — `/api/v1/cupones`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/v1/cupones` | Lista todos | 200 |
| GET | `/api/v1/cupones/{id}` | Obtiene por id | 200 / 404 |
| GET | `/api/v1/cupones/codigo/{codigo}` | Busca por codigo | 200 / 404 |
| POST | `/api/v1/cupones` | Crea | 201 / 404 si una relación no existe / 409 si el valor único ya existe |
| PUT | `/api/v1/cupones/{id}` | Actualiza | 200 / 404 / 409 si el valor único ya existe |
| DELETE | `/api/v1/cupones/{id}` | Elimina | 204 |

### Ejemplo: Producto

Entrada (POST / PUT). Las relaciones se envían solo con su id:

```json
{
  "nombre": "Bicicleta MTB Aro 29",
  "descripcion": "Cuadro de aluminio, 12 velocidades",
  "precioNormal": 899990,
  "estado": "PUBLICADO",
  "destacado": true,
  "ordenDestacado": 1,
  "limitePorCompra": 2,
  "fechaCreacion": "2026-10-06T10:00:00",
  "categoria": {
    "idCategoria": 1
  },
  "marca": {
    "idMarca": 1
  },
  "compatibilidades": [
    {
      "idCompatibilidad": 1
    }
  ]
}
```

Respuesta HATEOAS (las relaciones vuelven completas):

```json
{
  "idProducto": 1,
  "nombre": "Bicicleta MTB Aro 29",
  "descripcion": "Cuadro de aluminio, 12 velocidades",
  "precioNormal": 899990,
  "estado": "PUBLICADO",
  "destacado": true,
  "ordenDestacado": 1,
  "limitePorCompra": 2,
  "fechaCreacion": "2026-10-06T10:00:00",
  "categoria": {
    "idCategoria": 1,
    "nombre": "Bicicletas",
    "descripcion": "Bicicletas de todo tipo",
    "activa": true,
    "categoriaPadre": null
  },
  "marca": {
    "idMarca": 1,
    "nombre": "Trek",
    "activa": true
  },
  "compatibilidades": [
    {
      "idCompatibilidad": 1,
      "tipoBicicleta": "MTB",
      "aro": "29",
      "velocidades": "12"
    }
  ],
  "_links": {
    "self": {
      "href": "http://localhost:8085/api/v1/productos/1"
    },
    "productos": {
      "href": "http://localhost:8085/api/v1/productos"
    }
  }
}
```

### Ejemplo: VarianteProducto

Entrada (POST / PUT). Las relaciones se envían solo con su id:

```json
{
  "sku": "BIC-MTB29-M-ROJ",
  "talla": "M",
  "color": "Rojo",
  "aro": "29",
  "producto": {
    "idProducto": 1
  }
}
```

Respuesta HATEOAS (las relaciones vuelven completas):

```json
{
  "idVarianteProducto": 1,
  "sku": "BIC-MTB29-M-ROJ",
  "talla": "M",
  "color": "Rojo",
  "aro": "29",
  "producto": {
    "idProducto": 1,
    "nombre": "Bicicleta MTB Aro 29",
    "descripcion": "Cuadro de aluminio, 12 velocidades",
    "precioNormal": 899990,
    "estado": "PUBLICADO",
    "destacado": true,
    "ordenDestacado": 1,
    "limitePorCompra": 2,
    "fechaCreacion": "2026-10-06T10:00:00",
    "categoria": {
      "idCategoria": 1,
      "nombre": "Bicicletas",
      "descripcion": "Bicicletas de todo tipo",
      "activa": true,
      "categoriaPadre": null
    },
    "marca": {
      "idMarca": 1,
      "nombre": "Trek",
      "activa": true
    },
    "compatibilidades": [
      {
        "idCompatibilidad": 1,
        "tipoBicicleta": "MTB",
        "aro": "29",
        "velocidades": "12"
      }
    ]
  },
  "_links": {
    "self": {
      "href": "http://localhost:8085/api/v1/variantes/1"
    },
    "variantes": {
      "href": "http://localhost:8085/api/v1/variantes"
    }
  }
}
```

### Ejemplo de listado HATEOAS (Producto)

```json
{
  "_embedded": {
    "productoList": [
      {
        "idProducto": 1,
        "nombre": "Bicicleta MTB Aro 29",
        "descripcion": "Cuadro de aluminio, 12 velocidades",
        "precioNormal": 899990,
        "estado": "PUBLICADO",
        "destacado": true,
        "ordenDestacado": 1,
        "limitePorCompra": 2,
        "fechaCreacion": "2026-10-06T10:00:00",
        "categoria": {
          "idCategoria": 1,
          "nombre": "Bicicletas",
          "descripcion": "Bicicletas de todo tipo",
          "activa": true,
          "categoriaPadre": null
        },
        "marca": {
          "idMarca": 1,
          "nombre": "Trek",
          "activa": true
        },
        "compatibilidades": [
          {
            "idCompatibilidad": 1,
            "tipoBicicleta": "MTB",
            "aro": "29",
            "velocidades": "12"
          }
        ],
        "_links": {
          "self": {
            "href": "http://localhost:8085/api/v1/productos/1"
          }
        }
      }
    ]
  },
  "_links": {
    "self": {
      "href": "http://localhost:8085/api/v1/productos"
    }
  }
}
```

## Modelo de datos

### Categoria (tabla `categorias`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idCategoria | Long | id_categoria | PK, autoincremental |
| nombre | String | nombre | NOT NULL, único, máx. 50 |
| descripcion | String | descripcion | opcional, máx. 255 |
| activa | Boolean | activa | NOT NULL |
| categoriaPadre | Categoria | id_categoria_padre | FK a Categoria, opcional |

### Marca (tabla `marcas`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idMarca | Long | id_marca | PK, autoincremental |
| nombre | String | nombre | NOT NULL, único, máx. 50 |
| activa | Boolean | activa | NOT NULL |

### Compatibilidad (tabla `compatibilidades`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idCompatibilidad | Long | id_compatibilidad | PK, autoincremental |
| tipoBicicleta | String | tipo_bicicleta | NOT NULL, máx. 30 |
| aro | String | aro | opcional, máx. 10 |
| velocidades | String | velocidades | opcional, máx. 10 |

### Producto (tabla `productos`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idProducto | Long | id_producto | PK, autoincremental |
| nombre | String | nombre | NOT NULL, máx. 100 |
| descripcion | String | descripcion | opcional, máx. 500 |
| precioNormal | Integer | precio_normal | NOT NULL |
| estado | EstadoPublicacion (enum) | estado | NOT NULL: `PUBLICADO`, `OCULTO`, `BORRADOR` |
| destacado | Boolean | destacado | NOT NULL |
| ordenDestacado | Integer | orden_destacado | opcional |
| limitePorCompra | Integer | limite_por_compra | opcional |
| fechaCreacion | LocalDateTime | fecha_creacion | NOT NULL |
| categoria | Categoria | id_categoria | FK a Categoria, NOT NULL |
| marca | Marca | id_marca | FK a Marca, NOT NULL |
| compatibilidades | List<Compatibilidad> | tabla `productos_compatibilidades` | muchos a muchos |

### VarianteProducto (tabla `variantes_producto`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idVarianteProducto | Long | id_variante_producto | PK, autoincremental |
| sku | String | sku | NOT NULL, único, máx. 30 |
| talla | String | talla | opcional, máx. 10 |
| color | String | color | opcional, máx. 30 |
| aro | String | aro | opcional, máx. 10 |
| producto | Producto | id_producto | FK a Producto, NOT NULL |

### ImagenProducto (tabla `imagenes_producto`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idImagenProducto | Long | id_imagen_producto | PK, autoincremental |
| url | String | url | NOT NULL, máx. 255 |
| orden | Integer | orden | NOT NULL |
| esPrincipal | Boolean | es_principal | NOT NULL |
| producto | Producto | id_producto | FK a Producto, NOT NULL |

### EspecificacionTecnica (tabla `especificaciones_tecnicas`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idEspecificacionTecnica | Long | id_especificacion_tecnica | PK, autoincremental |
| nombre | String | nombre | NOT NULL, máx. 50 |
| valor | String | valor | NOT NULL, máx. 100 |
| producto | Producto | id_producto | FK a Producto, NOT NULL |

### Oferta (tabla `ofertas`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idOferta | Long | id_oferta | PK, autoincremental |
| precioOferta | Integer | precio_oferta | NOT NULL |
| fechaInicio | LocalDate | fecha_inicio | NOT NULL |
| fechaTermino | LocalDate | fecha_termino | NOT NULL |
| producto | Producto | id_producto | FK a Producto, NOT NULL |

### Resena (tabla `resenas`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idResena | Long | id_resena | PK, autoincremental |
| idCliente | Long | id_cliente | NOT NULL |
| calificacion | Integer | calificacion | NOT NULL |
| comentario | String | comentario | NOT NULL, máx. 500 |
| fecha | LocalDateTime | fecha | NOT NULL |
| visible | Boolean | visible | NOT NULL |
| respuestaTienda | String | respuesta_tienda | opcional, máx. 500 |
| producto | Producto | id_producto | FK a Producto, NOT NULL |

### AvisoStock (tabla `avisos_stock`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idAvisoStock | Long | id_aviso_stock | PK, autoincremental |
| correo | String | correo | NOT NULL, máx. 100 |
| fechaSolicitud | LocalDateTime | fecha_solicitud | NOT NULL |
| notificado | Boolean | notificado | NOT NULL |
| producto | Producto | id_producto | FK a Producto, NOT NULL |

### Cupon (tabla `cupones`)

| Campo | Tipo | Columna | Restricciones |
|---|---|---|---|
| idCupon | Long | id_cupon | PK, autoincremental |
| codigo | String | codigo | NOT NULL, único, máx. 30 |
| tipoDescuento | TipoDescuento (enum) | tipo_descuento | NOT NULL: `PORCENTAJE`, `MONTO_FIJO` |
| valor | Integer | valor | NOT NULL |
| fechaInicio | LocalDate | fecha_inicio | NOT NULL |
| fechaTermino | LocalDate | fecha_termino | NOT NULL |
| limiteUsos | Integer | limite_usos | NOT NULL |
| usosActuales | Integer | usos_actuales | NOT NULL |
| activo | Boolean | activo | NOT NULL |
| categorias | List<Categoria> | tabla `cupones_categorias` | muchos a muchos |

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

Además, `ProductoApplicationTests` prueba la carga del contexto, y `LimpiezaBaseDatos` vacía las tablas antes de cada test de integración en el orden correcto, primero los registros que dependen de otros.

## Estructura del proyecto

```
ms-productos/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/ridertapia/productos/
    │   │   ├── ProductoApplication.java
    │   │   ├── controller/   (11 controllers, uno por clase)
    │   │   ├── model/        (11 entidades + 2 enums)
    │   │   ├── repository/   (11 repositories)
    │   │   └── service/      (11 services)
    │   └── resources/application.properties
    └── test/
        ├── java/com/ridertapia/productos/
        │   ├── ProductoApplicationTests.java
        │   ├── LimpiezaBaseDatos.java
        │   ├── controller/   (<Clase>ControllerTest y <Clase>ControllerIT)
        │   └── service/      (<Clase>ServiceTest)
        └── resources/application-test.properties
```
