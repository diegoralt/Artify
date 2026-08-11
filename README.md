# Artify

App Android para explorar el catálogo musical de [Discogs](https://www.discogs.com): buscar artistas, ver su ficha y navegar por sus lanzamientos. Funciona sin conexión apoyándose en la caché local.

Construida en Kotlin con Jetpack Compose sobre Clean Architecture y MVVM.

| | |
|---|---|
| **Lenguaje** | Kotlin |
| **UI** | Jetpack Compose + Navigation |
| **Arquitectura** | Clean Architecture (data / domain / presentation) + MVVM |
| **Inyección de dependencias** | Hilt |
| **Red y persistencia** | Retrofit · Room · Coroutines |
| **Imágenes** | Coil |
| **Calidad** | Lint · Detekt · pruebas unitarias en GitHub Actions |
| **API** | Discogs |

---

## Pantallas

| Pantalla | Qué hace |
|---|---|
| Splash | Carga inicial y navegación al buscador |
| Búsqueda | Busca artistas en el catálogo de Discogs |
| Detalle de artista | Ficha del artista y su lista de lanzamientos |
| Detalle de álbum | Información del lanzamiento seleccionado |

---

## Arquitectura

El código está dividido en tres capas con responsabilidades separadas:

```
domain/         Entidades, interfaces de repositorio y casos de uso.
                Sin dependencias de Android ni de librerías externas.

data/           Implementación de los repositorios, cliente Retrofit,
                DAOs de Room y mapeo entre modelos de red y de dominio.

presentation/   ViewModels y estado de UI.

ui/             Pantallas y componentes en Jetpack Compose.
```

La dirección de las dependencias siempre apunta hacia `domain`: la capa de datos y la de presentación conocen al dominio, y el dominio no conoce a ninguna de las dos. Eso permite cambiar la fuente de datos o la UI sin tocar las reglas de negocio.

La navegación entre pantallas está declarada en un solo lugar, de modo que el flujo completo de la app se puede leer de un vistazo.

**Hilt** se encarga de construir y compartir las dependencias, como el cliente de red o la base de datos, para que cada pantalla no tenga que resolverlas por su cuenta.

---

## Modo offline

Todos los flujos de repositorio tienen fallback por caché. Cuando una petición a Discogs falla por falta de red, el repositorio devuelve lo que haya en Room en vez de propagar el error a la UI.

El comportamiento es por flujo, no global: cada repositorio decide qué hacer cuando no hay datos en caché para responder, de modo que la app distingue entre "estoy sin conexión pero tengo esto guardado" y "no hay nada que mostrar".

---

## Calidad y CI

Cada Pull Request contra `master` dispara un workflow de GitHub Actions que compila, analiza y prueba:

```
./gradlew assembleDebug     Build
./gradlew lint              Lint de Android
./gradlew detekt            Análisis estático de Kotlin
./gradlew test              Pruebas unitarias
```

### Lint

Herramienta oficial de Android. Detecta uso incorrecto de APIs, recursos sin usar y posibles errores en tiempo de ejecución. El reporte queda en `app/build/reports/lint-results-debug.html`. Los problemas marcados como **Error** rompen el build; los **Warning** son informativos.

### Detekt

Análisis estático específico para Kotlin: complejidad excesiva, nombres poco claros, clases con demasiadas responsabilidades y malas prácticas. La configuración vive en `config/detekt/detekt.yml` con reglas de estilo, complejidad, naming, corrutinas y manejo de excepciones. El reporte queda en `app/build/reports/detekt/detekt.html`.

El proyecto usa un archivo baseline (`detekt-baseline.xml`) para que solo los problemas **nuevos** rompan el build.

### Pruebas

Pruebas unitarias sobre repositorios y casos de uso, incluyendo los escenarios de caché vacía y de fallo de red que sostienen el modo offline.

---

## Cómo ejecutarlo

| Requisito | Versión |
|---|---|
| Android Studio | Ladybug 2024.2 o superior |
| Android SDK | API 36 (se descarga sola al abrir el proyecto) |
| JDK | Java 11 (incluido con Android Studio) |
| Cuenta de Discogs | Gratuita, necesaria para generar el token |

### 1. Clonar

```bash
git clone https://github.com/diegoralt/Artify.git
```

### 2. Configurar el token de Discogs

La app necesita un token personal para consumir la API.

1. Entra a **discogs.com → Settings → Developers → Generate new token** y cópialo.
2. Crea un archivo `local.properties` en la raíz del proyecto, junto a `build.gradle.kts`.
3. Agrega esta línea:

```properties
DISCOGS_TOKEN=pega_tu_token_aqui
```

> `local.properties` está en `.gitignore` y no se sube al repositorio.

### 3. Abrir y correr

1. **File → Open** y selecciona la carpeta del proyecto.
2. Espera a que Gradle sincronice y descargue las dependencias (unos dos minutos la primera vez).
3. Elige un emulador o un dispositivo físico con **Depuración USB** activada.
4. Presiona **▶ Run** (`Shift + F10`).

---

## Sobre el proyecto

Artify nació como ejercicio técnico y se siguió desarrollando después. El proceso incluyó analizar la documentación de la API de Discogs, separar requerimientos funcionales de los no funcionales y de lo que quedaba fuera de alcance, prototipar la interfaz antes de escribir código, y trabajar cada pantalla en su propia rama con Pull Request.

Buena parte del código se generó con Claude Code sobre especificaciones y criterios de aceptación definidos previamente, con revisión y ajuste manual de los resultados.
