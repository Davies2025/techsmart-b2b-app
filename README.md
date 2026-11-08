<div align="center">
  <img width="400" alt="techsmart-app" src="https://github.com/user-attachments/assets/92046601-fce2-4ed7-867a-4aa09a20ef89" />
</div>
 <h1>Techsmart App</h1>

Aplicación móvil desarrollada para el negocio Techsmart, enfocada en la consulta de productos tecnológicos y suministros informáticos. Su propósito es facilitar a los clientes el acceso al catálogo de productos y a la información de los artículos disponibles.

## 🛠️ Tecnologías Utilizadas

- **Entorno de desarrollo:** Android Studio.
- **Lenguaje de programación:** Kotlin.
- **Base de datos y servicios:** Firebase.
- **Diseño de interfaces:** Jetpack Compose.
- **Prototipado y diseño inicial:** Stitch.

## 🚀 Instalación y Configuración

### Requisitos

- **Android Studio** en una versión reciente (el proyecto usa Android Gradle Plugin 9.5.1; si Android Studio pide actualizarse para abrirlo, hay que actualizarlo).
- **Git** y acceso a este repositorio (es privado).
- Un emulador Android o un teléfono con Android 7.0 (API 24) o superior.

### Pasos

1. Clonar el repositorio:

   ```bash
   git clone https://github.com/Davies2025/techsmart-b2b-app.git
   ```

2. En Android Studio: **File → Open** y seleccionar la carpeta clonada.
3. Esperar a que termine la sincronización de Gradle (la primera vez descarga dependencias y puede tardar varios minutos).
4. Firebase ya está configurado: el archivo `app/google-services.json` viene en el repositorio, no hay que agregar nada.
5. Crear o elegir un emulador en **Device Manager** (o conectar un teléfono con depuración USB) y pulsar **Run ▶**.

> Conviene que el emulador o teléfono tenga la zona horaria de El Salvador: las fechas de cotizaciones y reclamos se guardan como texto con la fecha local del dispositivo.

### Cuentas de usuario

- El inicio de sesión usa **Firebase Authentication (correo y contraseña)**. La app y la web ([CarlosRuizHub/techsmart-b2b](https://github.com/CarlosRuizHub/techsmart-b2b)) comparten el mismo proyecto de Firebase, así que **una cuenta creada en una funciona en la otra**.
- Las cuentas antiguas de la web (las que guardaban un hash de la contraseña) ya no sirven: hay que registrarse de nuevo.
- Todos usamos la **misma base de datos**: lo que se cree al probar es real. Usen cuentas de prueba y borren desde la consola lo que no sirva.

### Cómo trabajar en equipo

1. Antes de empezar, actualizar `main`:

   ```bash
   git checkout main
   git pull
   ```

2. Crear una rama por tarea:

   ```bash
   git checkout -b feature/nombre-de-la-tarea
   ```

3. Hacer commits en esa rama, subirla (`git push -u origin feature/nombre-de-la-tarea`) y abrir un **Pull Request** hacia `main` para que el equipo lo revise antes de unirlo. No subir cambios directo a `main`.

📌 Información del Proyecto - 
Desarrollado como parte del segundo laboratorio de la asignatura Tecnologías de la Información.
