# Migración y ejemplos obsoletos

Lee [IMPORT.md](../../IMPORT.md) para conocer la versión publicada actual. No se ha establecido una correspondencia histórica entre versiones y commits para los cambios siguientes; no deduzcas una primera versión Maven precisa a partir del orden de los commits. Los commits siguientes documentan cambios del código fuente, no afirmaciones sobre lanzamientos.

| Patrón anterior | Sustitución actual | Cambio del código fuente |
| --- | --- | --- |
| `CameraPermissionState` y API de compatibilidad exclusiva para cámara | `HandlePermissions` con una descripción de cámara | `254432f` eliminó la compatibilidad de cámara |
| `HandlePermissionsIndividually` | Flujo por lotes unificado `HandlePermissions` | `1758b37` |
| Página alternativa generada por la descripción | Lambda obligatoria `PermissionDescription.page` | `6330918` |
| `HandlePermissionBundle` / contenedor separado `permissionContent` | Colocar la interfaz en la página de cada descripción | `d58bcf7` |
| Construir `PermissionOverview()` vacío | Proporcionar su página; omitir el argumento overview del control para obtener el valor genérico predeterminado | `d58bcf7`, `e5bb5d4` |
| `DefaultPermissionPage(icon = ...)` | `DefaultPermissionPage(heroImage = ...)` | `ae7ef5a` añade sobrecargas de imagen |
| Importaciones del paquete raíz | Importar desde `requester`, `ui`, `recipe` | `01be774` |
| Manual de consumo en la guía raíz / `agents/` | [docs/agents/index.md](index.md); AGENTS raíz es solo para mantenedores | Consolidación actual de la documentación |

No generes código nuevo con las API eliminadas exclusivas para cámara o de flujo individual, con `permissionContent` ni con descripciones sin página. Los valores actuales del enum son `PermissionDisplayMode.All` y `MissingOnly`; la función actual de lectura es `estimateReadingDelayMillis`, no `computeReadingText`. No sustituyas nombres por conjeturas basadas en ejemplos antiguos.

```kotlin
PermissionDescription(Manifest.permission.CAMERA) {
    DefaultPermissionPage(label = "Camera", heroImage = Icons.Default.PhotoCamera)
}
```

El fragmento requiere importaciones de Android Manifest, iconos Material de Compose y tipos de `ui`; consulta el [inicio rápido completo](quickstart.md) para una integración con todas las importaciones. Omitir overview aporta una página genérica solo cuando las reglas de visibilidad lo permiten; no elimina el requisito de una página al construir explícitamente `PermissionOverview`.

Confirma las coordenadas del artefacto en la [configuración de publicación](../../permission/build.gradle.kts). El espacio de nombres `com.apexfission.android.permission` no es el grupo Maven. El repositorio no garantiza que las coordenadas antiguas y la documentación anterior sean alias válidos.
