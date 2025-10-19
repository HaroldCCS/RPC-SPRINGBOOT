# Sistema RPC con gRPC y Spring Boot

Sistema de comunicación RPC usando gRPC en Spring Boot con 3 proyectos modulares que se comunican entre sí.

## Estructura del Proyecto

### 1. rpc.library (Librería Compartida)
- Contiene las definiciones protobuf (.proto) del servicio gRPC
- Define los DTOs Java para FormRequest y FormResponse
- Genera automáticamente el código gRPC
- **Puerto:** N/A (es una librería)

### 2. rpc.server (Servidor gRPC)
- Implementa el servicio gRPC definido en la librería
- Recibe y procesa formularios vía gRPC
- Valida datos y simula guardado en memoria
- **Puerto:** 9090 (gRPC)

### 3. rpc.client (Cliente gRPC)
- Expone API REST para recibir formularios
- Transforma peticiones REST a gRPC
- Se comunica con el servidor mediante gRPC
- **Puerto:** 8080 (HTTP)

## Flujo de Comunicación

```
Usuario → REST (POST /api/form) → Cliente (puerto 8080)
                                      ↓
                                   gRPC call
                                      ↓
                           Servidor gRPC (puerto 9090)
                                      ↓
                              Procesa y valida
                                      ↓
                             Respuesta gRPC
                                      ↓
                           Cliente (puerto 8080)
                                      ↓
                           Respuesta REST (JSON)
```

## Compilación

### Opción 1: Compilar todos los proyectos
```bash
# Compilar e instalar la librería
cd rpc.library
./mvnw clean install -DskipTests

# Compilar el servidor
cd ../rpc.server
./mvnw clean package -DskipTests

# Compilar el cliente
cd ../rpc.client
./mvnw clean package -DskipTests
```

### Opción 2: Compilar solo lo necesario
```bash
# Si solo cambias la librería, recompila todo
cd rpc.library && ./mvnw clean install -DskipTests
cd ../rpc.server && ./mvnw clean package -DskipTests
cd ../rpc.client && ./mvnw clean package -DskipTests

# Si solo cambias el servidor o cliente, no necesitas recompilar la librería
```

## Ejecución

### 1. Iniciar el Servidor gRPC (Terminal 1)
```bash
cd rpc.server
./mvnw spring-boot:run
```

El servidor iniciará en el puerto **9090** y mostrará:
```
gRPC Server started, listening on port 9090
```

### 2. Iniciar el Cliente REST (Terminal 2)
```bash
cd rpc.client
./mvnw spring-boot:run
```

El cliente iniciará en el puerto **8080** y mostrará:
```
Started Application in X.XXX seconds
```

## Pruebas

### Health Check del Cliente
```bash
curl http://localhost:8080/api/health
```

Respuesta esperada:
```
Cliente gRPC funcionando correctamente
```

### Enviar Formulario Válido
```bash
curl -X POST http://localhost:8080/api/form \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellido": "Pérez",
    "edad": 30,
    "email": "juan.perez@example.com"
  }'
```

Respuesta esperada:
```json
{
  "status": "OK",
  "message": "Formulario recibido y procesado exitosamente. ID: 1",
  "timestamp": 1698765432000
}
```

### Casos de Error

#### Nombre vacío
```bash
curl -X POST http://localhost:8080/api/form \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "",
    "apellido": "Pérez",
    "edad": 30,
    "email": "juan.perez@example.com"
  }'
```

Respuesta:
```json
{
  "status": "ERROR",
  "message": "El nombre es requerido",
  "timestamp": 1698765432000
}
```

#### Email inválido
```bash
curl -X POST http://localhost:8080/api/form \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellido": "Pérez",
    "edad": 30,
    "email": "emailinvalido"
  }'
```

Respuesta:
```json
{
  "status": "ERROR",
  "message": "El email no es válido",
  "timestamp": 1698765432000
}
```

#### Edad fuera de rango
```bash
curl -X POST http://localhost:8080/api/form \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellido": "Pérez",
    "edad": 200,
    "email": "juan.perez@example.com"
  }'
```

Respuesta:
```json
{
  "status": "ERROR",
  "message": "La edad debe estar entre 1 y 150",
  "timestamp": 1698765432000
}
```

## Tecnologías Utilizadas

- **Java 21**
- **Spring Boot 3.5.6**
- **gRPC 1.74.0**
- **Spring gRPC 0.11.0**
- **Protocol Buffers 4.31.1**
- **Lombok** - Para reducir código boilerplate
- **Maven** - Para gestión de dependencias

## Estructura de Archivos Importantes

```
rpc/
├── rpc.library/
│   ├── src/main/proto/form_service.proto      # Definiciones protobuf
│   ├── src/main/java/com/rpc/library/dto/     # DTOs Java
│   └── pom.xml
├── rpc.server/
│   ├── src/main/java/com/rpc/server/service/  # Implementación gRPC
│   ├── src/main/resources/application.properties
│   └── pom.xml
├── rpc.client/
│   ├── src/main/java/com/rpc/client/controller/  # API REST
│   ├── src/main/java/com/rpc/client/grpc/        # Cliente gRPC
│   ├── src/main/resources/application.properties
│   └── pom.xml
└── README.md
```

## Validaciones Implementadas

El servidor valida:
- ✅ Nombre no vacío
- ✅ Apellido no vacío
- ✅ Edad entre 1 y 150 años
- ✅ Email contiene @

## Logs

Ambos servicios generan logs informativos:

**Servidor:**
```
INFO  Recibiendo formulario: nombre=Juan, apellido=Pérez, edad=30, email=juan.perez@example.com
INFO  Formulario guardado exitosamente con ID: 1
```

**Cliente:**
```
INFO  Recibiendo solicitud REST para formulario: FormRequestDto(nombre=Juan, apellido=Pérez, edad=30, email=juan.perez@example.com)
INFO  Enviando formulario via gRPC: FormRequestDto(nombre=Juan, apellido=Pérez, edad=30, email=juan.perez@example.com)
INFO  Respuesta recibida: status=OK, message=Formulario recibido y procesado exitosamente. ID: 1
```

## Troubleshooting

### El cliente no puede conectarse al servidor
- Asegúrate de que el servidor esté corriendo en el puerto 9090
- Verifica que no haya firewall bloqueando el puerto
- Revisa los logs del servidor para errores

### Error de compilación en rpc.server o rpc.client
- Asegúrate de haber compilado e instalado rpc.library primero
- Ejecuta `./mvnw clean install` en rpc.library

### Puerto ya en uso
- Si el puerto 8080 o 9090 está en uso, puedes cambiarlos en los archivos application.properties respectivos

## Arquitectura

Este sistema implementa:
- **Patrón Cliente-Servidor** con gRPC
- **Separación de responsabilidades** (API Gateway pattern)
- **Comunicación sincrónica** mediante RPC
- **Validación de datos** en el servidor
- **Manejo centralizado de errores**

## Autores

Sistema desarrollado como ejemplo de comunicación RPC con gRPC y Spring Boot.

