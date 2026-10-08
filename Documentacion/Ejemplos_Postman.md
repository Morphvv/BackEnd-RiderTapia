# Ejemplos para Postman

Base URL: `http://localhost:8080` (o `http://localhost:8090`). Body: **raw → JSON**.

- Fechas: `"2026-10-07T10:00:00"` o `"2026-10-07"`.
- Relaciones: se envía el id del recurso relacionado, por ejemplo `"producto": { "idProducto": 1 }`.
- Los ids de ejemplo (`1`) corresponden al id devuelto por el POST.

## ms-usuario (8081)

### Usuarios

**POST** `/api/v1/usuarios` — Crear usuario

```json
{
  "rutUsuario": "12345678-5",
  "emailCliente": "cliente123456@correo.cl",
  "password": "clave12345",
  "rol": "CLIENTE"
}
```

**POST** `/api/v1/usuarios/login` — Iniciar sesión

```json
{
  "emailCliente": "cliente123456@correo.cl",
  "password": "clave12345"
}
```

**POST** `/api/v1/usuarios/recuperar-password` — Recuperar contraseña

```json
{
  "emailCliente": "cliente123456@correo.cl",
  "nuevaPassword": "nuevaClave123"
}
```

**PUT** `/api/v1/usuarios/12345678-5` — Actualizar

```json
{
  "emailCliente": "cliente123456@correo.cl",
  "password": "nuevaClave123",
  "rol": "CLIENTE"
}
```

Sin cuerpo:

- `GET /api/v1/usuarios` — Listar
- `GET /api/v1/usuarios/12345678-5` — Buscar por RUT
- `GET /api/v1/usuarios/buscar?email=cliente123456@correo.cl` — Buscar por email

## ms-cliente (8082)

### Clientes

**POST** `/api/v1/clientes` — Crear cliente (el RUT debe existir en ms-usuario)

```json
{
  "rutUsuario": "12345678-5",
  "nombre": "Matias",
  "apellido": "Dominguez",
  "telefono": "+56912345678",
  "direccion": "Calle Falsa 123",
  "comuna": "Concepcion"
}
```

**PUT** `/api/v1/clientes/12345678-5` — Actualizar

```json
{
  "nombre": "Matias",
  "apellido": "Dominguez",
  "telefono": "+56987654321",
  "direccion": "Av. Colon 456",
  "comuna": "Talcahuano"
}
```

Sin cuerpo:

- `GET /api/v1/clientes` — Listar
- `GET /api/v1/clientes/12345678-5` — Buscar por RUT
- `GET /api/v1/clientes/buscar?nombre=matias` — Buscar por nombre

## ms-productos (8085)

### Marcas

**POST** `/api/v1/marcas` — Crear

```json
{
  "nombre": "Trek 123456",
  "activa": true
}
```

**PUT** `/api/v1/marcas/1` — Actualizar

```json
{
  "nombre": "Trek 123456",
  "activa": true
}
```

Sin cuerpo:

- `GET /api/v1/marcas` — Listar
- `GET /api/v1/marcas/1` — Buscar por id
- `GET /api/v1/marcas/nombre/Trek 123456` — Buscar por nombre

### Categorías

**POST** `/api/v1/categorias` — Crear

```json
{
  "nombre": "MTB 123456",
  "descripcion": "Bicicletas de montaña",
  "activa": true,
  "categoriaPadre": null
}
```

**PUT** `/api/v1/categorias/1` — Actualizar

```json
{
  "nombre": "MTB 123456",
  "descripcion": "Bicicletas de montaña aro 27.5 y 29",
  "activa": true,
  "categoriaPadre": null
}
```

Sin cuerpo:

- `GET /api/v1/categorias` — Listar
- `GET /api/v1/categorias/1` — Buscar por id
- `GET /api/v1/categorias/nombre/MTB 123456` — Buscar por nombre

### Compatibilidades

**POST** `/api/v1/compatibilidades` — Crear

```json
{
  "tipoBicicleta": "MTB",
  "aro": "29",
  "velocidades": "12"
}
```

**PUT** `/api/v1/compatibilidades/1` — Actualizar

```json
{
  "tipoBicicleta": "MTB",
  "aro": "29",
  "velocidades": "12"
}
```

Sin cuerpo:

- `GET /api/v1/compatibilidades` — Listar
- `GET /api/v1/compatibilidades/1` — Buscar por id
- `GET /api/v1/compatibilidades/tipoBicicleta/MTB` — Buscar por tipo

### Productos

**POST** `/api/v1/productos` — Crear

```json
{
  "nombre": "Trek Marlin 7",
  "descripcion": "Bicicleta MTB aro 29, 12 velocidades",
  "precioNormal": 899990,
  "estado": "PUBLICADO",
  "destacado": true,
  "ordenDestacado": 1,
  "limitePorCompra": 2,
  "fechaCreacion": "2026-10-07T10:00:00",
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

**PUT** `/api/v1/productos/1` — Actualizar

```json
{
  "nombre": "Trek Marlin 7",
  "descripcion": "Bicicleta MTB aro 29, 12 velocidades",
  "precioNormal": 849990,
  "estado": "PUBLICADO",
  "destacado": true,
  "ordenDestacado": 1,
  "limitePorCompra": 2,
  "fechaCreacion": "2026-10-07T10:00:00",
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

Sin cuerpo:

- `GET /api/v1/productos` — Listar
- `GET /api/v1/productos/1` — Buscar por id
- `GET /api/v1/productos/categoria/1` — Por categoría

### Variantes

**POST** `/api/v1/variantes` — Crear

```json
{
  "sku": "TRK-M7-123456",
  "talla": "M",
  "color": "Negro",
  "aro": "29",
  "producto": {
    "idProducto": 1
  }
}
```

**PUT** `/api/v1/variantes/1` — Actualizar

```json
{
  "sku": "TRK-M7-123456",
  "talla": "M",
  "color": "Azul",
  "aro": "29",
  "producto": {
    "idProducto": 1
  }
}
```

Sin cuerpo:

- `GET /api/v1/variantes` — Listar
- `GET /api/v1/variantes/1` — Buscar por id
- `GET /api/v1/variantes/sku/TRK-M7-123456` — Buscar por SKU

### Imágenes

**POST** `/api/v1/imagenes` — Crear

```json
{
  "url": "https://ridertapia.cl/img/marlin7-1.jpg",
  "orden": 1,
  "esPrincipal": true,
  "producto": {
    "idProducto": 1
  }
}
```

**PUT** `/api/v1/imagenes/1` — Actualizar

```json
{
  "url": "https://ridertapia.cl/img/marlin7-1.jpg",
  "orden": 1,
  "esPrincipal": true,
  "producto": {
    "idProducto": 1
  }
}
```

Sin cuerpo:

- `GET /api/v1/imagenes` — Listar
- `GET /api/v1/imagenes/1` — Buscar por id
- `GET /api/v1/imagenes/producto/1` — Por producto

### Especificaciones

**POST** `/api/v1/especificaciones` — Crear

```json
{
  "nombre": "Cuadro",
  "valor": "Aluminio Alpha Silver",
  "producto": {
    "idProducto": 1
  }
}
```

**PUT** `/api/v1/especificaciones/1` — Actualizar

```json
{
  "nombre": "Cuadro",
  "valor": "Aluminio Alpha Silver",
  "producto": {
    "idProducto": 1
  }
}
```

Sin cuerpo:

- `GET /api/v1/especificaciones` — Listar
- `GET /api/v1/especificaciones/1` — Buscar por id
- `GET /api/v1/especificaciones/producto/1` — Por producto

### Ofertas

**POST** `/api/v1/ofertas` — Crear

```json
{
  "precioOferta": 799990,
  "fechaInicio": "2026-10-07",
  "fechaTermino": "2026-10-31",
  "producto": {
    "idProducto": 1
  }
}
```

**PUT** `/api/v1/ofertas/1` — Actualizar

```json
{
  "precioOferta": 799990,
  "fechaInicio": "2026-10-07",
  "fechaTermino": "2026-10-31",
  "producto": {
    "idProducto": 1
  }
}
```

Sin cuerpo:

- `GET /api/v1/ofertas` — Listar
- `GET /api/v1/ofertas/1` — Buscar por id
- `GET /api/v1/ofertas/producto/1` — Por producto

### Reseñas de producto

**POST** `/api/v1/resenas` — Crear

```json
{
  "idCliente": 1,
  "calificacion": 5,
  "comentario": "Excelente bicicleta",
  "fecha": "2026-10-07T10:00:00",
  "visible": true,
  "respuestaTienda": null,
  "producto": {
    "idProducto": 1
  }
}
```

**PUT** `/api/v1/resenas/1` — Actualizar

```json
{
  "idCliente": 1,
  "calificacion": 5,
  "comentario": "Excelente bicicleta",
  "fecha": "2026-10-07T10:00:00",
  "visible": true,
  "respuestaTienda": "¡Gracias por tu compra!",
  "producto": {
    "idProducto": 1
  }
}
```

Sin cuerpo:

- `GET /api/v1/resenas` — Listar
- `GET /api/v1/resenas/1` — Buscar por id
- `GET /api/v1/resenas/producto/1` — Por producto

### Avisos de stock

**POST** `/api/v1/avisos-stock` — Crear

```json
{
  "correo": "cliente123456@correo.cl",
  "fechaSolicitud": "2026-10-07T10:00:00",
  "notificado": false,
  "producto": {
    "idProducto": 1
  }
}
```

**PUT** `/api/v1/avisos-stock/1` — Actualizar

```json
{
  "correo": "cliente123456@correo.cl",
  "fechaSolicitud": "2026-10-07T10:00:00",
  "notificado": false,
  "producto": {
    "idProducto": 1
  }
}
```

Sin cuerpo:

- `GET /api/v1/avisos-stock` — Listar
- `GET /api/v1/avisos-stock/1` — Buscar por id
- `GET /api/v1/avisos-stock/producto/1` — Por producto

### Cupones

**POST** `/api/v1/cupones` — Crear

```json
{
  "codigo": "RIDER123456",
  "tipoDescuento": "PORCENTAJE",
  "valor": 10,
  "fechaInicio": "2026-10-01",
  "fechaTermino": "2026-12-31",
  "limiteUsos": 100,
  "usosActuales": 0,
  "activo": true,
  "categorias": [
    {
      "idCategoria": 1
    }
  ]
}
```

**PUT** `/api/v1/cupones/1` — Actualizar

```json
{
  "codigo": "RIDER123456",
  "tipoDescuento": "PORCENTAJE",
  "valor": 10,
  "fechaInicio": "2026-10-01",
  "fechaTermino": "2026-12-31",
  "limiteUsos": 100,
  "usosActuales": 0,
  "activo": true,
  "categorias": [
    {
      "idCategoria": 1
    }
  ]
}
```

Sin cuerpo:

- `GET /api/v1/cupones` — Listar
- `GET /api/v1/cupones/1` — Buscar por id
- `GET /api/v1/cupones/codigo/RIDER123456` — Buscar por código

## ms-inventario (8088)

### Ítems de inventario

**POST** `/api/v1/items-inventario` — Crear

```json
{
  "idVariante": 1,
  "stockDisponible": 10,
  "stockReservado": 0,
  "stockMinimo": 2,
  "costoPromedio": 600000,
  "fechaActualizacion": "2026-10-07T10:00:00",
  "idProveedores": [
    1
  ]
}
```

**PUT** `/api/v1/items-inventario/1` — Actualizar

```json
{
  "idVariante": 1,
  "stockDisponible": 12,
  "stockReservado": 0,
  "stockMinimo": 2,
  "costoPromedio": 600000,
  "fechaActualizacion": "2026-10-07T10:00:00",
  "idProveedores": [
    1
  ]
}
```

Sin cuerpo:

- `GET /api/v1/items-inventario` — Listar
- `GET /api/v1/items-inventario/1` — Buscar por id
- `GET /api/v1/items-inventario/idVariante/1` — Por variante

### Movimientos

**POST** `/api/v1/movimientos` — Crear

```json
{
  "tipo": "VENTA_TIENDA",
  "cantidad": 1,
  "stockAnterior": 10,
  "stockResultante": 9,
  "fecha": "2026-10-07T10:00:00",
  "observacion": "Venta en tienda",
  "idEmpleado": 1,
  "idPedido": null,
  "idOrdenTrabajo": null,
  "itemInventario": {
    "idItemInventario": 1
  }
}
```

**PUT** `/api/v1/movimientos/1` — Actualizar

```json
{
  "tipo": "VENTA_TIENDA",
  "cantidad": 1,
  "stockAnterior": 10,
  "stockResultante": 9,
  "fecha": "2026-10-07T10:00:00",
  "observacion": "Venta en tienda",
  "idEmpleado": 1,
  "idPedido": null,
  "idOrdenTrabajo": null,
  "itemInventario": {
    "idItemInventario": 1
  }
}
```

Sin cuerpo:

- `GET /api/v1/movimientos` — Listar
- `GET /api/v1/movimientos/1` — Buscar por id
- `GET /api/v1/movimientos/itemInventario/1` — Por ítem

### Ingresos de mercancía

**POST** `/api/v1/ingresos` — Crear

```json
{
  "tipo": "INGRESO",
  "cantidad": 5,
  "stockAnterior": 9,
  "stockResultante": 14,
  "fecha": "2026-10-07T10:00:00",
  "observacion": "Ingreso de proveedor",
  "idEmpleado": 1,
  "idPedido": null,
  "idOrdenTrabajo": null,
  "itemInventario": {
    "idItemInventario": 1
  },
  "costoUnitario": 600000,
  "numeroDocumento": "FAC-12345",
  "idProveedor": 1
}
```

**PUT** `/api/v1/ingresos/1` — Actualizar

```json
{
  "tipo": "INGRESO",
  "cantidad": 5,
  "stockAnterior": 9,
  "stockResultante": 14,
  "fecha": "2026-10-07T10:00:00",
  "observacion": "Ingreso de proveedor",
  "idEmpleado": 1,
  "idPedido": null,
  "idOrdenTrabajo": null,
  "itemInventario": {
    "idItemInventario": 1
  },
  "costoUnitario": 600000,
  "numeroDocumento": "FAC-12345",
  "idProveedor": 1
}
```

Sin cuerpo:

- `GET /api/v1/ingresos` — Listar
- `GET /api/v1/ingresos/1` — Buscar por id
- `GET /api/v1/ingresos/itemInventario/1` — Por ítem

### Tomas de inventario

**POST** `/api/v1/tomas-inventario` — Crear

```json
{
  "fechaInicio": "2026-10-07T10:00:00",
  "fechaCierre": null,
  "estado": "EN_CURSO",
  "idEmpleado": 1
}
```

**PUT** `/api/v1/tomas-inventario/1` — Actualizar

```json
{
  "fechaInicio": "2026-10-07T10:00:00",
  "fechaCierre": "2026-10-07T18:00:00",
  "estado": "CONFIRMADA",
  "idEmpleado": 1
}
```

Sin cuerpo:

- `GET /api/v1/tomas-inventario` — Listar
- `GET /api/v1/tomas-inventario/1` — Buscar por id
- `GET /api/v1/tomas-inventario/idEmpleado/1` — Por empleado

### Ajustes

**POST** `/api/v1/ajustes` — Crear

```json
{
  "tipo": "AJUSTE",
  "cantidad": 1,
  "stockAnterior": 14,
  "stockResultante": 13,
  "fecha": "2026-10-07T10:00:00",
  "observacion": "Diferencia en conteo físico",
  "idEmpleado": 1,
  "idPedido": null,
  "idOrdenTrabajo": null,
  "itemInventario": {
    "idItemInventario": 1
  },
  "motivo": "CONTEO_FISICO",
  "tomaInventario": {
    "idTomaInventario": 1
  }
}
```

**PUT** `/api/v1/ajustes/1` — Actualizar

```json
{
  "tipo": "AJUSTE",
  "cantidad": 1,
  "stockAnterior": 14,
  "stockResultante": 13,
  "fecha": "2026-10-07T10:00:00",
  "observacion": "Diferencia en conteo físico",
  "idEmpleado": 1,
  "idPedido": null,
  "idOrdenTrabajo": null,
  "itemInventario": {
    "idItemInventario": 1
  },
  "motivo": "CONTEO_FISICO",
  "tomaInventario": {
    "idTomaInventario": 1
  }
}
```

Sin cuerpo:

- `GET /api/v1/ajustes` — Listar
- `GET /api/v1/ajustes/1` — Buscar por id
- `GET /api/v1/ajustes/itemInventario/1` — Por ítem

### Detalles de conteo

**POST** `/api/v1/detalles-conteo` — Crear

```json
{
  "cantidadSistema": 14,
  "cantidadContada": 13,
  "diferencia": -1,
  "tomaInventario": {
    "idTomaInventario": 1
  },
  "itemInventario": {
    "idItemInventario": 1
  }
}
```

**PUT** `/api/v1/detalles-conteo/1` — Actualizar

```json
{
  "cantidadSistema": 14,
  "cantidadContada": 13,
  "diferencia": -1,
  "tomaInventario": {
    "idTomaInventario": 1
  },
  "itemInventario": {
    "idItemInventario": 1
  }
}
```

Sin cuerpo:

- `GET /api/v1/detalles-conteo` — Listar
- `GET /api/v1/detalles-conteo/1` — Buscar por id
- `GET /api/v1/detalles-conteo/tomaInventario/1` — Por toma

### Alertas de stock

**POST** `/api/v1/alertas-stock` — Crear

```json
{
  "fechaGeneracion": "2026-10-07T10:00:00",
  "stockAlMomento": 1,
  "atendida": false,
  "itemInventario": {
    "idItemInventario": 1
  }
}
```

**PUT** `/api/v1/alertas-stock/1` — Actualizar

```json
{
  "fechaGeneracion": "2026-10-07T10:00:00",
  "stockAlMomento": 1,
  "atendida": true,
  "itemInventario": {
    "idItemInventario": 1
  }
}
```

Sin cuerpo:

- `GET /api/v1/alertas-stock` — Listar
- `GET /api/v1/alertas-stock/1` — Buscar por id
- `GET /api/v1/alertas-stock/itemInventario/1` — Por ítem

### Cargas masivas

**POST** `/api/v1/cargas-masivas` — Crear

```json
{
  "nombreArchivo": "productos_octubre.csv",
  "fechaCarga": "2026-10-07T10:00:00",
  "filasProcesadas": 120,
  "filasConError": 2,
  "idEmpleado": 1
}
```

**PUT** `/api/v1/cargas-masivas/1` — Actualizar

```json
{
  "nombreArchivo": "productos_octubre.csv",
  "fechaCarga": "2026-10-07T10:00:00",
  "filasProcesadas": 120,
  "filasConError": 2,
  "idEmpleado": 1
}
```

Sin cuerpo:

- `GET /api/v1/cargas-masivas` — Listar
- `GET /api/v1/cargas-masivas/1` — Buscar por id
- `GET /api/v1/cargas-masivas/idEmpleado/1` — Por empleado

### Errores de carga

**POST** `/api/v1/errores-carga` — Crear

```json
{
  "fila": 15,
  "mensaje": "SKU duplicado",
  "cargaMasiva": {
    "idCargaMasiva": 1
  }
}
```

**PUT** `/api/v1/errores-carga/1` — Actualizar

```json
{
  "fila": 15,
  "mensaje": "SKU duplicado",
  "cargaMasiva": {
    "idCargaMasiva": 1
  }
}
```

Sin cuerpo:

- `GET /api/v1/errores-carga` — Listar
- `GET /api/v1/errores-carga/1` — Buscar por id
- `GET /api/v1/errores-carga/cargaMasiva/1` — Por carga

## ms-pedidos (8086)

### Datos de visitante

**POST** `/api/v1/visitantes` — Crear

```json
{
  "nombre": "Ana Soto",
  "correo": "ana123456@correo.cl",
  "telefono": "+56911112222"
}
```

**PUT** `/api/v1/visitantes/1` — Actualizar

```json
{
  "nombre": "Ana Soto",
  "correo": "ana123456@correo.cl",
  "telefono": "+56911112222"
}
```

Sin cuerpo:

- `GET /api/v1/visitantes` — Listar
- `GET /api/v1/visitantes/1` — Buscar por id
- `GET /api/v1/visitantes/correo/ana123456@correo.cl` — Por correo

### Pedidos

**POST** `/api/v1/pedidos` — Crear

```json
{
  "numero": "PED-123456",
  "fechaCreacion": "2026-10-07T10:00:00",
  "fechaExpiracion": "2026-10-08T10:00:00",
  "estado": "PENDIENTE_PAGO",
  "canal": "WEB",
  "subtotal": 899990,
  "descuento": 0,
  "total": 899990,
  "motivoAnulacion": null,
  "rutCliente": "12345678-5",
  "idCupon": null,
  "idEmpleado": null,
  "datosVisitante": null
}
```

**PUT** `/api/v1/pedidos/1` — Actualizar

```json
{
  "numero": "PED-123456",
  "fechaCreacion": "2026-10-07T10:00:00",
  "fechaExpiracion": "2026-10-08T10:00:00",
  "estado": "PAGADO",
  "canal": "WEB",
  "subtotal": 899990,
  "descuento": 0,
  "total": 899990,
  "motivoAnulacion": null,
  "rutCliente": "12345678-5",
  "idCupon": null,
  "idEmpleado": null,
  "datosVisitante": null
}
```

Sin cuerpo:

- `GET /api/v1/pedidos` — Listar
- `GET /api/v1/pedidos/1` — Buscar por id
- `GET /api/v1/pedidos/numero/PED-123456` — Buscar por número

### Detalles de pedido

**POST** `/api/v1/detalles` — Crear

```json
{
  "idVariante": 1,
  "cantidad": 1,
  "precioUnitario": 899990,
  "subtotal": 899990,
  "pedido": {
    "idPedido": 1
  }
}
```

**PUT** `/api/v1/detalles/1` — Actualizar

```json
{
  "idVariante": 1,
  "cantidad": 1,
  "precioUnitario": 899990,
  "subtotal": 899990,
  "pedido": {
    "idPedido": 1
  }
}
```

Sin cuerpo:

- `GET /api/v1/detalles` — Listar
- `GET /api/v1/detalles/1` — Buscar por id
- `GET /api/v1/detalles/pedido/1` — Por pedido

### Historial de estados

**POST** `/api/v1/historial-estados` — Crear

```json
{
  "estadoAnterior": "PENDIENTE_PAGO",
  "estadoNuevo": "PAGADO",
  "fechaCambio": "2026-10-07T10:00:00",
  "idEmpleado": 1,
  "pedido": {
    "idPedido": 1
  }
}
```

**PUT** `/api/v1/historial-estados/1` — Actualizar

```json
{
  "estadoAnterior": "PENDIENTE_PAGO",
  "estadoNuevo": "PAGADO",
  "fechaCambio": "2026-10-07T10:00:00",
  "idEmpleado": 1,
  "pedido": {
    "idPedido": 1
  }
}
```

Sin cuerpo:

- `GET /api/v1/historial-estados` — Listar
- `GET /api/v1/historial-estados/1` — Buscar por id
- `GET /api/v1/historial-estados/pedido/1` — Por pedido

### Notificaciones

**POST** `/api/v1/notificaciones` — Crear

```json
{
  "tipo": "CONFIRMACION_COMPRA",
  "destinatario": "cliente123456@correo.cl",
  "fechaEnvio": "2026-10-07T10:00:00",
  "enviada": true,
  "pedido": {
    "idPedido": 1
  }
}
```

**PUT** `/api/v1/notificaciones/1` — Actualizar

```json
{
  "tipo": "CONFIRMACION_COMPRA",
  "destinatario": "cliente123456@correo.cl",
  "fechaEnvio": "2026-10-07T10:00:00",
  "enviada": true,
  "pedido": {
    "idPedido": 1
  }
}
```

Sin cuerpo:

- `GET /api/v1/notificaciones` — Listar
- `GET /api/v1/notificaciones/1` — Buscar por id
- `GET /api/v1/notificaciones/pedido/1` — Por pedido

### Boletas

**POST** `/api/v1/boletas` — Crear

```json
{
  "folio": 1001,
  "fechaEmision": "2026-10-07",
  "montoNeto": 756294,
  "iva": 143696,
  "total": 899990,
  "pdfUrl": "https://docs.ridertapia.cl/1001.pdf",
  "pedido": {
    "idPedido": 1
  }
}
```

**PUT** `/api/v1/boletas/1` — Actualizar

```json
{
  "folio": 1001,
  "fechaEmision": "2026-10-07",
  "montoNeto": 756294,
  "iva": 143696,
  "total": 899990,
  "pdfUrl": "https://docs.ridertapia.cl/1001.pdf",
  "pedido": {
    "idPedido": 1
  }
}
```

Sin cuerpo:

- `GET /api/v1/boletas` — Listar
- `GET /api/v1/boletas/1` — Buscar por id
- `GET /api/v1/boletas/pedido/1` — Por pedido

### Facturas

**POST** `/api/v1/facturas` — Crear

```json
{
  "folio": 2001,
  "fechaEmision": "2026-10-07",
  "montoNeto": 756294,
  "iva": 143696,
  "total": 899990,
  "pdfUrl": "https://docs.ridertapia.cl/2001.pdf",
  "pedido": {
    "idPedido": 1
  },
  "rutEmpresa": "76123456-7",
  "razonSocial": "Ciclismo Sur SpA",
  "giro": "Venta de artículos deportivos",
  "direccionEmpresa": "Barros Arana 123, Concepción"
}
```

**PUT** `/api/v1/facturas/1` — Actualizar

```json
{
  "folio": 2001,
  "fechaEmision": "2026-10-07",
  "montoNeto": 756294,
  "iva": 143696,
  "total": 899990,
  "pdfUrl": "https://docs.ridertapia.cl/2001.pdf",
  "pedido": {
    "idPedido": 1
  },
  "rutEmpresa": "76123456-7",
  "razonSocial": "Ciclismo Sur SpA",
  "giro": "Venta de artículos deportivos",
  "direccionEmpresa": "Barros Arana 123, Concepción"
}
```

Sin cuerpo:

- `GET /api/v1/facturas` — Listar
- `GET /api/v1/facturas/1` — Buscar por id
- `GET /api/v1/facturas/pedido/1` — Por pedido

### Pedido para factura (lo usa ms-reportes)

**POST** `/api/v1/pedidos` — Crear pedido pagado

```json
{
  "numero": "PED-F-123456",
  "fechaCreacion": "2026-10-07T10:00:00",
  "fechaExpiracion": "2026-10-08T10:00:00",
  "estado": "PAGADO",
  "canal": "TIENDA",
  "subtotal": 79990,
  "descuento": 0,
  "total": 79990,
  "motivoAnulacion": null,
  "rutCliente": "12345678-5",
  "idCupon": null,
  "idEmpleado": 1,
  "datosVisitante": null
}
```

**POST** `/api/v1/detalles` — Agregar su detalle

```json
{
  "idVariante": 1,
  "cantidad": 1,
  "precioUnitario": 79990,
  "subtotal": 79990,
  "pedido": {
    "idPedido": 1
  }
}
```

## ms-pagos (8087)

### Pagos WebPay

**POST** `/api/v1/pagos-webpay` — Crear

```json
{
  "idPedido": 1,
  "monto": 899990,
  "fecha": "2026-10-07T10:00:00",
  "estado": "APROBADO",
  "token": "01ab23cd45ef67",
  "codigoAutorizacion": "123456",
  "ultimosDigitosTarjeta": "4321"
}
```

**PUT** `/api/v1/pagos-webpay/1` — Actualizar

```json
{
  "idPedido": 1,
  "monto": 899990,
  "fecha": "2026-10-07T10:00:00",
  "estado": "APROBADO",
  "token": "01ab23cd45ef67",
  "codigoAutorizacion": "123456",
  "ultimosDigitosTarjeta": "4321"
}
```

Sin cuerpo:

- `GET /api/v1/pagos-webpay` — Listar
- `GET /api/v1/pagos-webpay/1` — Buscar por id
- `GET /api/v1/pagos-webpay/idPedido/1` — Por pedido

### Pagos por transferencia

**POST** `/api/v1/pagos-transferencia` — Crear

```json
{
  "idPedido": 1,
  "monto": 899990,
  "fecha": "2026-10-07T10:00:00",
  "estado": "PENDIENTE",
  "comprobanteUrl": "https://ridertapia.cl/comprobantes/1.jpg",
  "fechaLimite": "2026-10-08T10:00:00",
  "observacion": "Transferencia BancoEstado"
}
```

**PUT** `/api/v1/pagos-transferencia/1` — Actualizar

```json
{
  "idPedido": 1,
  "monto": 899990,
  "fecha": "2026-10-07T10:00:00",
  "estado": "APROBADO",
  "comprobanteUrl": "https://ridertapia.cl/comprobantes/1.jpg",
  "fechaLimite": "2026-10-08T10:00:00",
  "observacion": "Transferencia BancoEstado"
}
```

Sin cuerpo:

- `GET /api/v1/pagos-transferencia` — Listar
- `GET /api/v1/pagos-transferencia/1` — Buscar por id
- `GET /api/v1/pagos-transferencia/idPedido/1` — Por pedido

### Pagos presenciales

**POST** `/api/v1/pagos-presenciales` — Crear

```json
{
  "idPedido": 1,
  "monto": 899990,
  "fecha": "2026-10-07T10:00:00",
  "estado": "APROBADO",
  "medioPago": "DEBITO"
}
```

**PUT** `/api/v1/pagos-presenciales/1` — Actualizar

```json
{
  "idPedido": 1,
  "monto": 899990,
  "fecha": "2026-10-07T10:00:00",
  "estado": "APROBADO",
  "medioPago": "DEBITO"
}
```

Sin cuerpo:

- `GET /api/v1/pagos-presenciales` — Listar
- `GET /api/v1/pagos-presenciales/1` — Buscar por id
- `GET /api/v1/pagos-presenciales/idPedido/1` — Por pedido

### Cierres de caja

**POST** `/api/v1/cierres-caja` — Crear

```json
{
  "fecha": "2026-10-07",
  "totalEfectivo": 150000,
  "totalDebito": 899990,
  "totalCredito": 0,
  "totalTransferencia": 0,
  "efectivoContado": 150000,
  "diferencia": 0,
  "idEmpleado": 1
}
```

**PUT** `/api/v1/cierres-caja/1` — Actualizar

```json
{
  "fecha": "2026-10-07",
  "totalEfectivo": 150000,
  "totalDebito": 899990,
  "totalCredito": 0,
  "totalTransferencia": 0,
  "efectivoContado": 150000,
  "diferencia": 0,
  "idEmpleado": 1
}
```

Sin cuerpo:

- `GET /api/v1/cierres-caja` — Listar
- `GET /api/v1/cierres-caja/1` — Buscar por id
- `GET /api/v1/cierres-caja/idEmpleado/1` — Por empleado

## ms-carrito (8089)

### Carritos

**POST** `/api/v1/carritos` — Crear

```json
{
  "rutCliente": "12345678-5",
  "tokenSesion": "tok-123456",
  "fechaCreacion": "2026-10-07T10:00:00",
  "fechaActualizacion": "2026-10-07T10:00:00",
  "estado": "ACTIVO",
  "subtotal": 0,
  "descuento": 0,
  "total": 0,
  "idCupon": null
}
```

**PUT** `/api/v1/carritos/1` — Actualizar

```json
{
  "rutCliente": "12345678-5",
  "tokenSesion": "tok-123456",
  "fechaCreacion": "2026-10-07T10:00:00",
  "fechaActualizacion": "2026-10-07T10:00:00",
  "estado": "ACTIVO",
  "subtotal": 0,
  "descuento": 0,
  "total": 0,
  "idCupon": null
}
```

Sin cuerpo:

- `GET /api/v1/carritos` — Listar
- `GET /api/v1/carritos/1` — Buscar por id
- `GET /api/v1/carritos/rutCliente/12345678-5` — Por RUT de cliente
- `GET /api/v1/carritos/1/resumen` — Resumen
- `GET /api/v1/carritos/1/avisos` — Avisos antes de pagar
- `GET /api/v1/carritos/1/cupon/RIDER123456` — Validar cupón

### Ítems de carrito

**POST** `/api/v1/items-carrito` — Crear

```json
{
  "idVariante": 1,
  "cantidad": 1,
  "precioUnitario": 899990,
  "fechaAgregado": "2026-10-07T10:00:00",
  "carrito": {
    "idCarrito": 1
  }
}
```

**PUT** `/api/v1/items-carrito/1` — Actualizar

```json
{
  "idVariante": 1,
  "cantidad": 2,
  "precioUnitario": 899990,
  "fechaAgregado": "2026-10-07T10:00:00",
  "carrito": {
    "idCarrito": 1
  }
}
```

Sin cuerpo:

- `GET /api/v1/items-carrito` — Listar
- `GET /api/v1/items-carrito/1` — Buscar por id
- `GET /api/v1/items-carrito/carrito/1` — Por carrito

## ms-soporte (8083)

### Contactos

**POST** `/api/v1/contactos` — Enviar contacto

```json
{
  "rutUsuario": "12345678-5",
  "nombre": "Pedro Suárez",
  "emailContacto": "pedro@correo.cl",
  "asunto": "Consulta por mi pedido",
  "mensaje": "Hola, quiero saber el estado de mi pedido"
}
```

**PUT** `/api/v1/contactos/1/responder` — Responder

```json
{
  "respuesta": "Tu pedido ya está listo para retiro en tienda"
}
```

Sin cuerpo:

- `GET /api/v1/contactos` — Listar (filtro opcional ?estado=ABIERTO)
- `GET /api/v1/contactos/1` — Buscar por id

### Devoluciones

**POST** `/api/v1/devoluciones` — Solicitar devolución

```json
{
  "rutUsuario": "12345678-5",
  "idPedido": 1,
  "idProducto": 1,
  "motivo": "El producto llegó con la llanta dañada"
}
```

**PUT** `/api/v1/devoluciones/1/resolver` — Resolver (APROBADA, RECHAZADA o COMPLETADA)

```json
{
  "estado": "APROBADA",
  "respuesta": "Puedes traer el producto a la tienda"
}
```

Sin cuerpo:

- `GET /api/v1/devoluciones` — Listar (filtro opcional ?estado=SOLICITADA)
- `GET /api/v1/devoluciones/1` — Buscar por id
- `GET /api/v1/devoluciones/usuario/12345678-5` — Por usuario

### Garantías

**POST** `/api/v1/garantias` — Registrar garantía

```json
{
  "rutUsuario": "12345678-5",
  "idPedido": 1,
  "idProducto": 1,
  "mesesCobertura": 12
}
```

Sin cuerpo:

- `GET /api/v1/garantias` — Listar
- `GET /api/v1/garantias/1` — Buscar por id
- `GET /api/v1/garantias/usuario/12345678-5` — Por usuario
- `GET /api/v1/garantias/1/vigencia` — Vigencia

### Reseñas de soporte

**POST** `/api/v1/soporte/resenas` — Crear reseña

```json
{
  "rutUsuario": "12345678-5",
  "idProducto": 1,
  "calificacion": 5,
  "comentario": "Excelente atención"
}
```

Sin cuerpo:

- `GET /api/v1/soporte/resenas` — Listar
- `GET /api/v1/soporte/resenas/1` — Buscar por id
- `GET /api/v1/soporte/resenas/producto/1` — Por producto
- `GET /api/v1/soporte/resenas/producto/1/resumen` — Resumen del producto
- `GET /api/v1/soporte/resenas/usuario/12345678-5` — Por usuario
- `PATCH /api/v1/soporte/resenas/1/ocultar` — Ocultar (sin cuerpo)

### Seguimientos

**POST** `/api/v1/seguimientos` — Crear seguimiento (LLAMADA, CORREO o ENCUESTA)

```json
{
  "idPedido": 1,
  "rutUsuario": "12345678-5",
  "tipo": "LLAMADA",
  "observacion": "Llamar en horario de la tarde"
}
```

**PUT** `/api/v1/seguimientos/1/estado` — Cambiar estado (PENDIENTE, REALIZADO o CANCELADO)

```json
{
  "estado": "REALIZADO",
  "observacion": "Cliente conforme"
}
```

Sin cuerpo:

- `GET /api/v1/seguimientos` — Listar
- `GET /api/v1/seguimientos/1` — Buscar por id
- `GET /api/v1/seguimientos/pedido/1` — Por pedido

## ms-reportes (8084)

### Documentos de venta

**POST** `/api/v1/documentos/boleta` — Emitir boleta (el pedido debe estar PAGADO o posterior)

```json
{
  "idPedido": 1
}
```

**POST** `/api/v1/documentos/factura` — Emitir factura (un pedido admite un solo documento)

```json
{
  "idPedido": 1,
  "rutEmpresa": "76543210-3",
  "razonSocial": "Ciclos del Sur SpA",
  "giro": "Venta de bicicletas y accesorios",
  "direccionEmpresa": "Av. Colón 1234, Talcahuano"
}
```

Sin cuerpo:

- `GET /api/v1/documentos` — Listar (filtro opcional ?tipo=BOLETA)
- `GET /api/v1/documentos/1` — Buscar por id
- `GET /api/v1/documentos/pedido/1` — Por pedido
- `GET /api/v1/documentos/buscar?tipo=FACTURA&numero=1` — Buscar por tipo y número
- `GET /api/v1/documentos/cliente/12345678-5` — Por cliente

### Reportes

Sin cuerpo:

- `POST /api/v1/reportes/diario?fecha=2026-10-07&top=10&generadoPor=admin` — Generar reporte diario (sin cuerpo)
- `POST /api/v1/reportes/semanal?fecha=2026-10-07` — Generar reporte semanal (sin cuerpo)
- `POST /api/v1/reportes/mensual?fecha=2026-10-07` — Generar reporte mensual (sin cuerpo)
- `GET /api/v1/reportes` — Listar (filtro opcional ?tipo=DIARIO)
- `GET /api/v1/reportes/1` — Buscar por id
- `GET /api/v1/reportes/1/mas-vendidos` — Más vendidos

## Eliminar

`DELETE /api/v1/<recurso>/{id}` responde 204. Orden para eliminar sin conflictos:

- `DELETE /api/v1/items-carrito/1`
- `DELETE /api/v1/carritos/1`
- `DELETE /api/v1/cierres-caja/1`
- `DELETE /api/v1/pagos-presenciales/1`
- `DELETE /api/v1/pagos-transferencia/1`
- `DELETE /api/v1/pagos-webpay/1`
- `DELETE /api/v1/facturas/1`
- `DELETE /api/v1/boletas/1`
- `DELETE /api/v1/notificaciones/1`
- `DELETE /api/v1/historial-estados/1`
- `DELETE /api/v1/detalles/1`
- `DELETE /api/v1/detalles/1`
- `DELETE /api/v1/pedidos/1`
- `DELETE /api/v1/pedidos/1`
- `DELETE /api/v1/visitantes/1`
- `DELETE /api/v1/errores-carga/1`
- `DELETE /api/v1/cargas-masivas/1`
- `DELETE /api/v1/alertas-stock/1`
- `DELETE /api/v1/detalles-conteo/1`
- `DELETE /api/v1/ajustes/1`
- `DELETE /api/v1/tomas-inventario/1`
- `DELETE /api/v1/ingresos/1`
- `DELETE /api/v1/movimientos/1`
- `DELETE /api/v1/items-inventario/1`
- `DELETE /api/v1/cupones/1`
- `DELETE /api/v1/avisos-stock/1`
- `DELETE /api/v1/resenas/1`
- `DELETE /api/v1/ofertas/1`
- `DELETE /api/v1/especificaciones/1`
- `DELETE /api/v1/imagenes/1`
- `DELETE /api/v1/variantes/1`
- `DELETE /api/v1/productos/1`
- `DELETE /api/v1/compatibilidades/1`
- `DELETE /api/v1/categorias/1`
- `DELETE /api/v1/marcas/1`
