# Puertos, bases de datos y rutas

## Servicios

| Servicio | Puerto | Base de datos |
|---|---|---|
| ms-gateway | 8080 / 8090 | — |
| ms-usuario | 8081 | `ridertapia_usuarios` |
| ms-cliente | 8082 | `ridertapia_clientes` |
| ms-soporte | 8083 | `ridertapia_soporte` |
| ms-reportes | 8084 | `ridertapia_reportes` |
| ms-productos | 8085 | `ridertapia_productos` |
| ms-pedidos | 8086 | `ridertapia_pedidos` |
| ms-pagos | 8087 | `ridertapia_pagos` |
| ms-inventario | 8088 | `ridertapia_inventario` |
| ms-carrito | 8089 | `ridertapia_carrito` |

Credenciales de MySQL: variables `DB_USER` (por defecto `root`) y `DB_PASSWORD` (por defecto vacía).

## Rutas del gateway

| Servicio | Rutas (`/api/v1/...`) |
|---|---|
| ms-usuario | `usuarios/**` |
| ms-cliente | `clientes/**` |
| ms-soporte | `garantias/**`, `devoluciones/**`, `contactos/**`, `seguimientos/**`, `soporte/**` |
| ms-reportes | `reportes/**`, `documentos/**` |
| ms-productos | `productos/**`, `categorias/**`, `marcas/**`, `variantes/**`, `imagenes/**`, `especificaciones/**`, `compatibilidades/**`, `ofertas/**`, `resenas/**`, `avisos-stock/**`, `cupones/**` |
| ms-pedidos | `pedidos/**`, `detalles/**`, `historial-estados/**`, `boletas/**`, `facturas/**`, `notificaciones/**`, `visitantes/**` |
| ms-pagos | `pagos-webpay/**`, `pagos-transferencia/**`, `pagos-presenciales/**`, `cierres-caja/**` |
| ms-inventario | `items-inventario/**`, `movimientos/**`, `ingresos/**`, `ajustes/**`, `alertas-stock/**`, `tomas-inventario/**`, `detalles-conteo/**`, `cargas-masivas/**`, `errores-carga/**` |
| ms-carrito | `carritos/**`, `items-carrito/**` |

CORS habilitado para `http://localhost:5500` y `http://127.0.0.1:5500`.

## Comunicación entre servicios

| Origen | Destino | Endpoint | Propiedad |
|---|---|---|---|
| ms-cliente | ms-usuario | `GET /api/v1/usuarios/{rut}` | `usuario.service.url` |
| ms-soporte | ms-usuario | `GET /api/v1/usuarios/{rut}` | `usuario.service.url` |
| ms-reportes | ms-pedidos | `GET /api/v1/pedidos`, `GET /api/v1/detalles` | `pedidos.service.url` |
| ms-inventario | ms-productos | `GET /api/v1/variantes/{id}` | `ms.productos.variantes-url` |
| ms-pagos | ms-pedidos | `GET /api/v1/pedidos/{id}` | `ms.pedidos.url` |
| ms-pedidos | ms-cliente | `GET /api/v1/clientes/{rut}` | `ms.clientes.url` |
| ms-carrito | ms-productos | `GET /api/v1/variantes/{id}`, `GET /api/v1/cupones/codigo/{codigo}` | `ms.productos.variantes-url`, `ms.productos.cupones-url` |
| ms-carrito | ms-inventario | `GET /api/v1/items-inventario/idVariante/{id}` | `ms.inventario.url` |
| ms-carrito | ms-cliente | `GET /api/v1/clientes/{rut}` | `ms.clientes.url` |

## Orden de inicio

1. ms-usuario
2. ms-cliente
3. ms-productos
4. ms-inventario
5. ms-pedidos
6. ms-pagos
7. ms-carrito
8. ms-soporte
9. ms-reportes
10. ms-gateway
