# Rider Tapia Biker – Backend

9 microservicios Spring Boot y un API Gateway.

## Requisitos

- JDK 25 y JDK 21 ([adoptium.net](https://adoptium.net))
- MySQL en el puerto 3306 (por ejemplo, XAMPP)

Las bases de datos se crean solas al iniciar. Usuario por defecto: `root` sin contraseña.

## Ejecutar

| Acción | Archivo |
|---|---|
| Iniciar todo | `iniciar-todo.bat` |
| Verificar estado | `verificar-todo.bat` |
| Detener todo | `detener-todo.bat` |

Si el usuario `root` de MySQL tiene contraseña:

```powershell
.\iniciar-todo.ps1 -DbPassword tuClave
```

El gateway usa el puerto 8080. Si está ocupado, usa el 8090.

## Puertos

| Servicio | Puerto | Java |
|---|---|---|
| ms-gateway | 8080 / 8090 | 25 |
| ms-usuario | 8081 | 21 |
| ms-cliente | 8082 | 21 |
| ms-soporte | 8083 | 21 |
| ms-reportes | 8084 | 21 |
| ms-productos | 8085 | 25 |
| ms-pedidos | 8086 | 25 |
| ms-pagos | 8087 | 25 |
| ms-inventario | 8088 | 25 |
| ms-carrito | 8089 | 25 |

## Postman

1. Importar `Documentacion/RiderTapia.postman_collection.json`.
2. Ajustar la variable `gateway` al puerto del gateway.
3. Run collection.

## Documentación

- `Documentacion/Puertos_y_Rutas.md`: rutas, bases de datos y comunicación entre servicios.
- `Documentacion/Ejemplos_Postman.md`: cada endpoint con su JSON.
