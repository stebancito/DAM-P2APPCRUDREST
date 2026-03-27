<div align="center">

# INSTITUTO POLITÉCNICO NACIONAL
## "Escuela Superior de Cómputo"

<br>

### DESARROLLO DE APLICACIONES 
### MOVILES NATIVAS 
<br>

## PRÁCTICA 2: IAplicación móvil básica para operaciones CRUD con un servicio REST

<br>
<br>

**ALUMNOS:**                   
**RÍOS GÓMEZ JUAN ESTEBAN - 2023630116**   
**ROJAS BARRÓN ISAAC - 2023630463**   

<br>

**PROFESOR:**
**GABRIEL HURTADO AVILÉS**

<br>

**GRUPO:**
**7CV4**

<br>

**FECHA DE ENTREGA:**
**26 de marzo de 2026**

</div>

---

## Introducción

En esta práctica se desarrolló una aplicación móvil nativa para Android capaz de realizar operaciones CRUD (Crear, Leer, Actualizar y Borrar) consumiendo un servicio REST. 

La lógica del proyecto se divide en dos partes principales:
1.  **El Backend:** Se construyó una API REST utilizando Node.js y Express, conectada a una base de datos relacional PostgreSQL. Todo el entorno backend se encuentra dockerizado para facilitar su despliegue y portabilidad. Para garantizar la seguridad del sistema, las contraseñas de los usuarios se encriptan en la base de datos usando `bcrypt`, y las sesiones se manejan mediante JSON Web Tokens (JWT) generados al iniciar sesión y verificados mediante un *middleware* en cada operación CRUD.
2.  **El Frontend (App Móvil):** La interfaz de usuario fue diseñada utilizando el framework moderno **Jetpack Compose**, adoptando los lineamientos responsivos de **Material Design 3**. Se implementó un sistema de navegación mediante `Navigation Compose` para alternar entre las pantallas de Login, Registro y el gestor de tareas. Las comunicaciones de red hacia el backend se manejan de forma asíncrona utilizando **Corrutinas** de Kotlin y la librería **Retrofit**, enviando el Token JWT en las cabeceras de autorización (`Authorization: Bearer <token>`) para consumir los *endpoints* protegidos.

---

## Desarrollo

A continuación se presentan las capturas de pantalla que evidencian el flujo de la aplicación, el consumo de la API REST y las validaciones del sistema manejadas mediante notificaciones `Toast`.

### 1. Sistema de Autenticación y Manejo de Errores

El flujo comienza en el punto de entrada de la aplicación, donde el usuario puede iniciar sesión o navegar a la vista de creación de cuenta.

**Pantalla de Inicio de Sesión y Registro:**
<br>
<img src="screenshots/inicio-sesion.png" width="300"> 
<img src="screenshots/crear-cuenta.png" width="300">

*Figura 1: Pantalla principal de Login (izquierda) y formulario de Registro (derecha), ambos implementados con diseño responsivo y componentes de Material Design 3.*

**Validaciones en el Registro:**
<br>
<img src="screenshots/noti-registrobueno.png" width="300"> 
<img src="screenshots/registro-malo.png" width="300">

*Figura 2: Manejo de respuestas HTTP del registro. Si el servidor responde con un 201, se notifica el éxito (izquierda). Si devuelve un error 400 (ej. usuario ya existente), Retrofit atrapa la excepción y notifica al usuario sin crashear la app (derecha).*

**Validaciones en el Login:**
<br>
<img src="screenshots/ingresar.png" width="300"> 
<img src="screenshots/pass-incorrecta.png" width="300">

*Figura 3: Proceso de login. El usuario ingresa sus datos (izquierda). Si los datos no coinciden, el backend rechaza la petición con un error 401 y la app muestra el mensaje correspondiente de "Contraseña incorrecta" (derecha).*

---

### 2. Operaciones CRUD (Gestión de Tareas)

Una vez que el usuario se autentica con éxito, la aplicación almacena el Token JWT en memoria e ingresa a la vista principal de la gestión de datos.

**Leer (Read) - Lista de Tareas:**
<br>
<img src="screenshots/task-list.png" width="300">
*Figura 4: Ejecución exitosa de la petición GET. Muestra las tareas que le pertenecen únicamente al usuario autenticado. Se renderizan mediante una `LazyColumn` y componentes tipo `Card`.*

**Crear (Create):**
<br>
<img src="screenshots/nueva-tarea.png" width="300">

*Figura 5: Al pulsar el Floating Action Button (botón "+"), se despliega un `AlertDialog` para enviar una petición POST al servidor y crear un nuevo registro.*

**Actualizar (Update):**
<br>
<img src="screenshots/task-edit.png" width="300">

*Figura 6: Al seleccionar el icono de lápiz sobre una tarjeta existente, el mismo `AlertDialog` se recicla, precargando los datos actuales. Al guardar, se dispara una petición PUT con el ID de la tarea.*

**Borrar (Delete):**
<br>
<img src="screenshots/task-delete.png" width="300">

*Figura 7: Al presionar el icono de la papelera roja, el cliente envía la petición DELETE. Si la base de datos elimina el registro correctamente, se recibe un código 200 OK, se avisa mediante un Toast en la parte inferior y la lista se refresca automáticamente.*

---
### Comunicación Frontend - Backend (API REST)

La arquitectura del sistema se basa en un modelo Cliente-Servidor. La aplicación móvil (Cliente) consume los servicios de la API construida en Node.js (Servidor) a través de peticiones HTTP. 

La comunicación se gestiona mediante la librería **Retrofit** en Android, la cual mapea las respuestas en formato JSON a objetos de datos nativos de Kotlin (`data classes`). Todas las peticiones de red se realizan de manera asíncrona utilizando **Corrutinas** (`launch`) para no bloquear el hilo principal de la interfaz de usuario.

A continuación, se describen los endpoints expuestos y su lógica de integración:

#### 1. Endpoints de Autenticación (Públicos)
Estos endpoints no requieren autorización previa y son el punto de entrada a la aplicación.

* **`POST /register`**
    * **Lógica Backend:** Recibe un `username` y `password`. Verifica si el usuario existe; si no, encripta la contraseña con `bcrypt` y lo guarda en PostgreSQL.
    * **Lógica Frontend:** La pantalla de registro envía los datos. Si el servidor responde con un código `400` (Usuario existente), la app atrapa la `HttpException` y muestra un `Toast` de advertencia. Si responde con `201`, notifica el éxito y redirige al Login.
* **`POST /login`**
    * **Lógica Backend:** Valida las credenciales contra la base de datos. Si son correctas, firma y devuelve un JSON Web Token (JWT) válido por 1 hora.
    * **Lógica Frontend:** Envía las credenciales. Al recibir un código `200`, la aplicación extrae el JWT del cuerpo de la respuesta y lo almacena en memoria (`Session.token`). Si recibe un `401` o `404`, muestra un error indicando contraseñas incorrectas o usuario no encontrado.

#### 2. Endpoints CRUD de Tareas (Protegidos)
Para acceder a estas rutas, el frontend debe incluir obligatoriamente el token obtenido en el login dentro de las cabeceras de la petición (`Header("Authorization"): Bearer <token>`). El *middleware* del backend verifica este token antes de procesar la solicitud.

* **`GET /tasks` (Leer)**
    * **Interacción:** Al abrir la pantalla principal (`TasksScreen`), la app ejecuta esta petición automáticamente. El backend extrae el ID del usuario desde el JWT y devuelve únicamente las tareas que le pertenecen, garantizando la privacidad de los datos.
* **`POST /tasks` (Crear)**
    * **Interacción:** Disparado desde el botón "Guardar" de un diálogo vacío. El frontend envía un objeto con `title` y `description`. El backend lo inserta asociado al ID del usuario del token y devuelve la tarea creada. La app recarga la lista para reflejar los cambios.
* **`PUT /tasks/{id}` (Actualizar)**
    * **Interacción:** Disparado al guardar cambios sobre una tarea existente. Se envía el ID de la tarea en la URL y los nuevos datos en el cuerpo. El backend verifica que la tarea pertenezca al usuario del token antes de aplicar el `UPDATE`.
* **`DELETE /tasks/{id}` (Borrar)**
    * **Interacción:** Al presionar el ícono de la papelera en una tarjeta de tarea, Retrofit envía el método `DELETE` con el ID correspondiente. Tras una respuesta exitosa (`200 OK`), el frontend elimina la tarea de la vista actualizando el estado de Jetpack Compose.

---
## Conclusiones

El desarrollo de esta práctica representó una excelente oportunidad para integrar el desarrollo nativo moderno en Android con arquitecturas web sólidas. 

**Retos principales:**
* A nivel de frontend, el manejo del estado (`State`) y el enrutamiento visual en Jetpack Compose requirió un control preciso para evitar excepciones de navegación (como `IllegalArgumentException` al manejar rutas anidadas). 
* A nivel de conexión física, fue un reto configurar los permisos de red en Android (`usesCleartextTraffic`) y apuntar a la IP local de la computadora puenteando las interfaces de Docker para probar la app directamente desde un dispositivo físico en lugar del emulador.
* En el manejo de errores, se logró mapear correctamente las respuestas del servidor (`HttpException` y códigos 400/500) para mostrar notificaciones amigables al usuario (`Toast`) sin interrumpir el flujo de la app.

**Logros:**
* Se estructuró de forma exitosa una comunicación robusta y segura mediante Retrofit.
* Se cumplió con la seguridad requerida aislando la información de cada usuario con su propio JWT. 
* Se dominó la creación de interfaces declarativas con Compose, logrando vistas dinámicas y reactivas mucho más limpias en comparación con el antiguo sistema de vistas XML.

---

## Bibliografía

1. Android Developers. (2024). *Jetpack Compose Tutorial*. Google. Recuperado de https://developer.android.com/jetpack/compose/tutorial
2. Android Developers. (2024). *Navigating with Compose*. Google. Recuperado de https://developer.android.com/jetpack/compose/nav-adaptive
3. Documentación Oficial de Retrofit. (n.d.). *A type-safe HTTP client for Android and Java*. Square, Inc. Recuperado de https://square.github.io/retrofit/
4. Express.js. (n.d.). *Express - Infraestructura web rápida, minimalista y flexible para Node.js*. Recuperado de https://expressjs.com/es/
5. Docker Inc. (2024). *Docker Compose Documentation*. Recuperado de https://docs.docker.com/compose/
6. 
