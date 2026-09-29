# TP-IYCS-GIT

Trabajo práctico de Ingeniería y Calidad de Software sobre Sistemas de Gestión de la Configuración.

El proyecto consiste en una API de gestión hotelera desarrollada con Java y Spring Boot. Incluye operaciones relacionadas con conserjes, huéspedes, habitaciones, reservas, estadías y facturación.

El equipo utiliza Git para versionar el código, GitHub para colaborar y Gitflow para organizar las ramas y las versiones.

## Integrantes

- [@brunogriffa1](https://github.com/brunogriffa1)
- [@NachoMaldo02](https://github.com/NachoMaldo02)
- [@FacuPerron](https://github.com/FacuPerron)
- [@ValeTessini](https://github.com/ValeTessini)

## Tecnologías

- Java 21.
- Spring Boot y Spring Data JPA.
- Maven, mediante el Maven Wrapper incluido en el repositorio.
- MySQL para ejecutar la aplicación.
- H2 en memoria para las pruebas.
- GitHub Actions para integración continua.

## Configuración y ejecución

### Requisitos

- JDK 21.
- Una instancia de MySQL disponible con una base de datos llamada `hotel_premier`.

### Configuración local

Desde la raíz del proyecto, copiar el archivo de ejemplo si todavía no existe una configuración local.

En Linux o macOS:

```bash
cp application-local.properties.example application-local.properties
```

En Windows:

```powershell
Copy-Item application-local.properties.example application-local.properties
```

Completar en `application-local.properties` la URL de la base, el usuario y la contraseña de MySQL.

Este archivo está excluido mediante `.gitignore`, porque sus valores dependen del entorno de cada integrante. La plantilla `.example` sí se versiona y contiene valores de ejemplo.

La configuración compartida permanece en:

```text
src/main/resources/application.properties
```

Ese archivo carga la configuración local mediante `spring.config.import`.

### Ejecutar la aplicación

Desde la raíz del proyecto:

```bash
bash ./mvnw spring-boot:run
```

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Si se ejecuta desde un IDE, configurar la raíz del proyecto como directorio de trabajo.

El puerto configurado es 8080.

### Ejecutar las pruebas

```bash
bash ./mvnw -B clean verify
```

En Windows:

```powershell
.\mvnw.cmd -B clean verify
```

Los tests utilizan su propia configuración y una base H2 en memoria, por lo que no requieren una conexión con MySQL.

## Endpoints principales

| Recurso | Ruta base | Operaciones |
|---|---|---|
| Conserjes | `/api/conserjes` | Alta, login, consulta, cantidad registrada y baja. |
| Huéspedes | `/api/huespedes` | Alta, búsqueda, consulta por documento, modificación y baja. |
| Habitaciones | `/api/habitaciones` | Listado, alta y consulta de estado. |
| Reservas | `/api/reservas` | Alta, búsqueda, listado y cancelación. |
| Estadías | `/api/estadias` | Check-in, consulta de ocupantes de una habitación e historial de un huésped. |
| Facturación | `/api/facturacion` | Validación del responsable de pago, ítems a facturar y generación de facturas. |

## Organización del trabajo

Seguimos Gitflow:

| Rama | Propósito |
|---|---|
| `main` | Mantener las versiones publicadas y estables. |
| `develop` | Integrar los cambios para las próximas versiones. |
| `feature/*` | Desarrollar funcionalidades desde `develop` e integrarlas nuevamente allí. |
| `release/*` | Preparar una versión desde `develop` e integrarla en `main` y `develop`. |
| `hotfix/*` | Corregir errores de una versión publicada, partiendo de `main` e integrando la corrección en `main` y `develop`. |

GitHub Actions compila el proyecto y ejecuta las verificaciones en los Pull Requests y en los push a `main` y `develop`.

### Versiones

Las versiones publicadas se identifican mediante etiquetas de Git y siguen el versionado semántico `MAYOR.MENOR.PARCHE`: se incrementa MAYOR ante cambios incompatibles, MENOR ante funcionalidades nuevas compatibles y PARCHE ante correcciones de errores.

1. **v1.0.0**: primera versión publicada.
2. **v1.0.1**: corrección que responde 409 al intentar eliminar un conserje con datos asociados.
3. **v1.1.0**: nuevo endpoint que informa la cantidad de conserjes registrados.

## 8.a. ¿Cómo podemos documentar con Git y qué incluiríamos en el README?

Git permite versionar documentación junto con el código. De esta manera, cada versión del proyecto conserva las instrucciones y explicaciones que le corresponden.

En este README documentamos, o debemos mantener actualizados, los siguientes aspectos:

| Información | Fundamento |
|---|---|
| Objetivo y alcance del sistema | Permite entender qué problema resuelve y qué funcionalidades ofrece. |
| Integrantes | Indica a quién consultar sobre el proyecto. |
| Tecnologías y requisitos | Indica qué necesita una persona para trabajar con el proyecto. |
| Configuración y ejecución | Facilita reproducir el entorno sin depender de explicaciones individuales. |
| Ejecución de pruebas | Permite verificar los cambios antes de integrarlos. |
| Endpoints principales | Sirve como guía rápida para usar la API. |
| Organización de ramas, versiones y contribuciones | Define cómo colaborar y preparar versiones de manera consistente. |
| Cambios que afectan el uso o la instalación | Evita que las instrucciones queden desactualizadas respecto del software. |

El README debe mantenerse dentro del repositorio y actualizarse mediante commits cuando cambien las instrucciones o el comportamiento que describe.

El historial de Git permite conocer qué se modificó, quién lo hizo y cuándo. Por ejemplo:

```bash
git log -- README.md
```

Además del README, documentamos el trabajo mediante mensajes de commit claros, descripciones de Pull Requests, comentarios de revisión y etiquetas de versión.

La documentación no debe contener contraseñas ni valores personales de configuración. Para explicar esos parámetros utilizamos archivos de ejemplo.

## 8.b. ¿Qué información pediríamos en un PR externo y cómo nos ayuda GitHub?

Antes de integrar una contribución externa, necesitamos comprender su objetivo, alcance y forma de verificación.

Pediríamos la siguiente información:

| Información solicitada | Fundamento |
|---|---|
| Título descriptivo y resumen | Permiten identificar rápidamente qué propone el cambio. |
| Problema o necesidad que resuelve | Ayuda a evaluar si el cambio responde a una necesidad del proyecto. |
| Issue relacionado, si existe | Vincula la implementación con la solicitud o el error que la originó. |
| Descripción de los cambios principales | Orienta al revisor sobre los componentes afectados. |
| Pasos para comprobar el comportamiento | Permiten reproducir y evaluar el resultado. |
| Pruebas realizadas y resultados | Aportan evidencia de que el cambio funciona y no rompe verificaciones existentes. |
| Impacto en configuración, dependencias o datos | Permite anticipar ajustes necesarios para integrar y ejecutar la modificación. |
| Documentación actualizada, cuando corresponda | Mantiene las instrucciones alineadas con el código. |

No todos los cambios necesitan el mismo nivel de detalle. Por ejemplo, una modificación de documentación puede indicar que no afecta la ejecución, mientras que un cambio en la base de datos debe explicar su impacto.

### Herramientas que ofrece GitHub

#### Plantillas de Pull Request

GitHub permite definir una plantilla en:

```text
.github/pull_request_template.md
```

La plantilla propone una estructura al crear un PR y ayuda a que los colaboradores incluyan la información necesaria. Por sí sola no valida que las respuestas estén completas o sean correctas.

Una estructura que proponemos utilizar es:

```markdown
## Descripción
¿Qué cambia y por qué?

## Issue relacionado
Enlace, si corresponde.

## Cambios principales
¿Qué componentes se modificaron?

## Verificación
¿Qué pruebas se ejecutaron y cuáles fueron los resultados?
¿Cómo puede reproducirlo el revisor?

## Impacto
¿Requiere cambios de configuración, dependencias o datos?

## Documentación
¿Se actualizaron las instrucciones correspondientes?
```

#### Revisión de código

Los Pull Requests permiten comparar archivos, dejar comentarios y sugerencias sobre líneas concretas, aprobar cambios o solicitar correcciones. La conversación queda asociada a la contribución y permite comprender las decisiones tomadas.

#### CODEOWNERS

El archivo `.github/CODEOWNERS` identifica responsables de revisión según los archivos modificados. Facilita que cada cambio sea revisado por integrantes familiarizados con esa parte del sistema.

#### Integración continua

GitHub Actions ejecuta la compilación y las pruebas configuradas. Sus resultados aportan evidencia automática para la revisión, aunque no reemplazan la evaluación del equipo.

#### Protección de ramas

GitHub permite configurar reglas para exigir aprobaciones o verificaciones exitosas antes del merge. Estas exigencias deben habilitarse expresamente; crear un archivo CODEOWNERS no las activa por sí solo.

Estas herramientas combinan una explicación del cambio, revisión humana y verificaciones automáticas para decidir si una contribución puede integrarse.
