# Similar Products API

Este proyecto es una aplicación Spring Boot que expone una API REST para obtener productos similares a un producto dado. Utiliza `RestTemplate` para realizar llamadas a servicios mock y `Resilience4j` para implementar un Circuit Breaker para mejorar la resiliencia.

## Requisitos Previos

- Java 11 o superior
- Maven
- Docker
- Seguir instrucciones del proyecto :  [backendDevTest](https://github.com/dalogax/backendDevTest?tab=readme-ov-file "backendDevTest")

## Configuración del Proyecto
### 1. Clonar el Repositorio
```sh
git clone <URL_DEL_REPOSITORIO>
cd similar-products-api
```
### 2. Configurar el Archivo application.properties
Asegúrate de que el archivo src/main/resources/application.properties tenga las siguientes configuraciones:
```sh
server.port=5000
# Configuración de Resilience4j para el Circuit Breaker
resilience4j.circuitbreaker.instances.similarProducts.register-health-indicator=true
resilience4j.circuitbreaker.instances.similarProducts.sliding-window-size=10
resilience4j.circuitbreaker.instances.similarProducts.minimum-number-of-calls=5
resilience4j.circuitbreaker.instances.similarProducts.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.similarProducts.wait-duration-in-open-state=10000
```
### 3. Construir y Ejecutar la Aplicación
```sh
./mvnw clean install
./mvnw spring-boot:run
```
La aplicación estará disponible en http://localhost:5000

## Endpoints de la API
### Obtener Productos Similares
Endpoint: `GET /product/{productId}/similar`

Descripción: Este endpoint recibe un `productId` como parámetro y devuelve una lista de productos similares.

**Ejemplo de Solicitud:**
```sh
http://localhost:5000/product/1/similar
```

**Respuesta de Ejemplo:**
```json
[
    {
        "id": "2",
        "name": "Product 2",
        "price": 29.99,
        "availability": true
    },
    {
        "id": "3",
        "name": "Product 3",
        "price": 49.99,
        "availability": false
    }
]
```
## Dependencias Principales
- Spring Boot Starter Web: Para crear la API REST.
- Spring Boot DevTools: Para mejorar la experiencia de desarrollo.
- Resilience4j Spring Boot 2: Para implementar el Circuit Breaker.

## Contribuir
Si deseas contribuir a este proyecto, por favor sigue los siguientes pasos:

1. Haz un fork del proyecto.
2. Crea una nueva rama (git checkout -b feature/nueva-funcionalidad).
3. Realiza tus cambios y haz commit (git commit -am 'Añadir nueva funcionalidad').
4. Haz push a la rama (git push origin feature/nueva-funcionalidad).
5. Abre un Pull Request.
