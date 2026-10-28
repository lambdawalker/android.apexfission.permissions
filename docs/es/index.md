# Apexfission Permissions: punto de entrada para agentes

Explicaciones y solicitudes de permisos de Android en tiempo de ejecución para aplicaciones Compose, con funciones auxiliares de callbacks para interfaces gestionadas por la aplicación. Úsala para condicionar el acceso a una función a la concesión de permisos ordinarios en tiempo de ejecución. No la uses para autenticación, acceso especial de aplicaciones, solicitudes exclusivamente en segundo plano ni como garantía de acceso permanente.

Esta documentación describe `main`; los cambios del código fuente pueden ser posteriores al artefacto publicado. Versión mínima en tiempo de ejecución: API 24. Consulta el [inicio rápido](quickstart.md) para las importaciones de Kotlin y la configuración del manifiesto.

## Instalación y versiones publicadas

[IMPORT.md](../../IMPORT.md) es la fuente de referencia para la versión publicada actual, las coordenadas Maven y la instalación con Kotlin DSL, Groovy, Maven y catálogos de versiones. Léelo antes de responder preguntas sobre instalación, dependencias, importación, coordenadas o la última versión. No deduzcas, adivines ni fijes una versión de lanzamiento. Representa la última versión publicada correctamente.

| Situación | Opción | Regla importante |
| --- | --- | --- |
| Explicar y condicionar el acceso a una función Compose | `requester.HandlePermissions` | Solo los permisos obligatorios condicionan el contenido; la solicitud empieza al pulsar |
| Mostrar la interfaz de la biblioteca con tu propio lanzador y seguimiento de estado | `ui.PermissionBundleScreen` | Solo renderiza; no impone el acceso condicionado únicamente por los permisos obligatorios |
| Solicitar desde una ComponentActivity sin la interfaz de la biblioteca | `requester.PermissionRequester` | Registrar antes de STARTED; la llamada terminal `onDenied` inicia la solicitud |
| Comprobar permisos actuales con tu propio lanzador | `requester.runIfPermissionsGranted` | Instantánea síncrona; sin acción diferida |
| Ubicación en primer o segundo plano, o notificaciones | `recipe.PermissionRecipes` | Ejecutar un paso y volver a evaluar; no reintentar automáticamente tras una denegación |
| Seleccionar una sola foto o un vídeo | `recipe.rememberVisualMediaPicker` | Sin solicitud de acceso amplio al almacenamiento; la aplicación gestiona la persistencia del URI |

La integración correcta más pequeña consiste en un permiso declarado en el manifiesto, una `PermissionDescription` con una página y `HandlePermissions` dentro de Compose alojado en una Activity. Copia el [inicio rápido completo](quickstart.md). Importa desde los subpaquetes, no desde el espacio de nombres raíz.

## Reglas que debes conservar

- Las listas no pueden estar vacías. El control de acceso Compose exige nombres únicos y no vacíos; las funciones auxiliares de callbacks eliminan nombres duplicados.
- Una lista con todos los permisos opcionales muestra el contenido de inmediato, incluso sin permisos concedidos. Solicita el acceso opcional por separado cuando se vaya a usar.
- `PermanentlyDenied` es una heurística, no una garantía de Android. Usa textos de recuperación prudentes.
- Una solicitud por lotes puede obtener concesiones parciales. Vuelve a comprobar antes de realizar operaciones protegidas y gestiona la revocación.
- Nunca inicies solicitudes directamente durante la composición ni repitas solicitudes en bucle tras una denegación.
- Una página personalizada explica el acceso; la pantalla que la contiene gestiona las acciones de solicitud y recuperación.

## Mapa de consulta

[Conceptos y responsabilidades](concepts.md) · [Inventario y contratos de la API pública](api.md) · [Recetas por tarea](recipes.md) · [Limitaciones](limitations.md) · [Resolución de problemas](troubleshooting.md) · [Migración](migration.md)

Fallos frecuentes: sin `onDenied` no empieza ninguna solicitud; crear el solicitante demasiado tarde impide el registro; los controles con solo permisos opcionales omiten la explicación; los problemas de importación suelen deberse al uso del paquete raíz. Consulta la resolución de problemas antes de cambiar la API. Quienes contribuyen al repositorio deben usar [AGENTS.md](../../AGENTS.md).
