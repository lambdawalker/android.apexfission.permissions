# Integración con Compose

Usa `HandlePermissions` para una pantalla explicativa y contenido protegido. Importa los tipos públicos de los subpaquetes `requester`, `ui` y `recipe` de `com.apexfission.android.permission`. Las solicitudes empiezan solo tras una pulsación del usuario. Android puede mostrar varios diálogos o conceder solo parte del lote.

## Configuración

Sigue [IMPORT.md](../../IMPORT.md) para la dependencia publicada actual y la configuración de repositorios. En esta copia del código fuente usa `implementation(project(":permission"))`. La aplicación requiere minSdk 24 y debe declarar cada permiso solicitado en su propio manifiesto:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

La implementación actual del conjunto no añade por sí sola márgenes para las barras del sistema. La aplicación debe aplicar los márgenes adecuados exactamente una vez para su disposición. Inspecciona la demostración y tu contenedor de borde a borde en lugar de asumir que el control de acceso siempre incluye o excluye el relleno de Scaffold.

## Condicionar el acceso a una función

Dentro de `setContent` de una Activity (añade las importaciones habituales de Compose y Android):

```kotlin
HandlePermissions(
    permissions = listOf(
        PermissionDescription(
            permission = Manifest.permission.CAMERA,
        ) {
            DefaultPermissionPage(
                label = "Camera",
                heroImage = Icons.Default.PhotoCamera,
                title = "Scan a document",
                body = "Allow camera access when you start scanning.",
            )
        },
        PermissionDescription(
            permission = Manifest.permission.RECORD_AUDIO,
        ) { DefaultPermissionPage("Microphone", Icons.Default.Mic) },
    ),
    overview = PermissionOverview(page = {
        DefaultPermissionPage(
            label = "Access",
            heroImage = Icons.Default.Lock,
            title = "Prepare your scan",
            body = "Review the permissions needed for this feature.",
        )
    }),
    displayMode = PermissionDisplayMode.MissingOnly, // default: All
    overviewMode = PermissionOverviewMode.Automatic, // Show or Hide
    onBack = { finish() },
    onNotNow = { finish() },
) {
    ScanFeature() // composed when every required permission is granted
}
```

La lambda final `PermissionDescription { ... }` es la **página completa obligatoria**, con imagen principal y texto. `PermissionOverview(page = { ... })` proporciona contenido para la página inicial opcional. `DefaultPermissionPage` es una función auxiliar lista para usar dentro de la lambda obligatoria. La tira de iconos usa `PermissionDescription.label` como nombre de accesibilidad. La biblioteca proporciona un icono y una etiqueta para permisos Android comunes mediante `PermissionVisualDefaults`; las cadenas no reconocidas usan un candado y un nombre legible derivado de la cadena. Sustituye `label` o `icon` para usar un texto específico de la función, otro idioma o un aspecto personalizado. `PermissionVisualDefaults.forPermission(permission)` expone el mismo par si tu página lo necesita. El carrusel gestiona el botón de solicitud por lotes y su texto.

La imagen principal de `DefaultPermissionPage` es independiente de la tira de iconos. Usa `heroImage = Icons.Default.PhotoCamera` para un vector, `heroImage = R.drawable.scan_hero` para un recurso drawable de mapa de bits o vectorial, o pasa una instancia Android de `Bitmap` o `Drawable`. Los bitmaps y drawables se ajustan al marco de la imagen principal; conserva válido el bitmap que gestionas hasta que la página salga de la composición. `PermissionDescription.icon` sigue controlando el icono pequeño de la tira. Consulta [todas las sobrecargas](api.md#ui-pages-and-carousel).

## Elegir la disposición de una página

| Uso | Contenido de la página |
| --- | --- |
| `DefaultPermissionPage` | Imagen principal centrada de tipo vector, bitmap o drawable, encabezado, descripción y filas opcionales `PermissionFeature`. Proporciónala dentro de `PermissionDescription { ... }` cuando esta disposición sea adecuada. |
| Composable de la aplicación | Tu propia disposición de página completa. Úsala cuando la función necesite una jerarquía visual distinta; la biblioteca sigue dibujando la tira de iconos, el temporizador segmentado, el botón de solicitud y la interfaz de recuperación. |

`NarrationPermissionPage` de la demostración es un **composable de la aplicación, no parte de la biblioteca**. Su consola de grabación, forma de onda y texto alineado a la izquierda son deliberadamente distintos de la página predeterminada centrada. Pásalo a la misma API `PermissionDescription`:

```kotlin
val title = "Record narration"
val body = "Allow microphone access to add narration when you record a scan."
PermissionDescription(
    permission = Manifest.permission.RECORD_AUDIO,
    required = false,
    autoAdvanceDelayMillis = estimateReadingDelayMillis("$title $body", ReadingPace.Slow),
) { NarrationPermissionPage(title, body) }
```

Por ejemplo, una implementación compacta de la aplicación puede disponer de forma distinta un motivo de grabación y el texto:

```kotlin
@Composable
private fun NarrationPermissionPage(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(28.dp))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text("VOICE NOTES / OPTIONAL", style = MaterialTheme.typography.labelLarge)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Mic, contentDescription = null)
                listOf(12, 30, 48, 24, 40, 16).forEach { height ->
                    Box(Modifier.width(5.dp).height(height.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp)))
                }
            }
            Text("ADD YOUR VOICE TO A SCAN", style = MaterialTheme.typography.labelMedium)
        }
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(body, style = MaterialTheme.typography.bodyLarge)
    }
}
```

Añade las importaciones estándar de Compose para disposición, formas, Material, iconos y `dp`. La [demostración ejecutable y su vista previa](../../app/src/main/java/com/apexfission/android/permissions/demo/PermissionCarouselDemo.kt) usan una versión más completa; el inicio rápido completo en [quickstart.md](quickstart.md) proporciona la configuración de la aplicación. Para las firmas y los valores predeterminados de todos los tipos públicos, consulta las [declaraciones de la API pública](api.md).

Usa una lista no vacía con nombres de permisos Android únicos y no vacíos. La solicitud por lotes se ejecuta solo mientras falta acceso obligatorio. Si ya están concedidos todos los permisos obligatorios, `content` se muestra de inmediato, incluso cuando falta un permiso opcional.

## Permisos obligatorios y opcionales

`PermissionDescription` es obligatoria de forma predeterminada. Establece `required = false` para una capacidad de la que la función pueda prescindir. El lote inicial incluye permisos opcionales pendientes siempre que aparezca la pantalla explicativa por faltar un permiso obligatorio; una denegación opcional no bloquea el contenido protegido. La interfaz marca las páginas opcionales. En la lambda `content`, inspecciona la instantánea actual, por ejemplo `{ grants -> ScanFeature(narrationEnabled = grants.isGranted(Manifest.permission.RECORD_AUDIO)) }`. También expone `missingRequired`, `missingOptional`, `statusByPermission` y `canProceed`. Si ya existen los permisos obligatorios, el contenido aparece de inmediato y el acceso opcional no se solicita automáticamente; solicítalo por separado cuando vaya a usarse. Una lista con todos los permisos opcionales muestra contenido de inmediato. Consulta la [Activity de ejemplo](../../app/src/main/java/com/apexfission/android/permissions/MainActivity.kt).

## Página de vista general

Omitir `overview` proporciona una `DefaultPermissionPage` genérica con icono de candado titulada “Before you continue.” Una `PermissionOverview` creada por la aplicación debe tener una página completa. Establece `overviewMode = PermissionOverviewMode.Hide` para empezar en el primer permiso. `overviewMode` se aplica tanto a `HandlePermissions` como a `PermissionBundleScreen`, que solo renderiza:

| `PermissionOverviewMode` | Comportamiento | Vista previa |
| --- | --- | --- |
| `Automatic` (predeterminado) | Muestra la vista general genérica o personalizada cuando hay más de un permiso visible. El filtro `MissingOnly` puede cambiar esa cantidad. | [Vista general del conjunto](../screenshots/bundle-overview.png) |
| `Show` | Incluye la vista general incluso con un solo permiso. | [Vista general de un permiso](../screenshots/single-permission-overview.png) |
| `Hide` | Empieza en el primer permiso incluso cuando hay varios. | [Varios permisos sin vista general](../screenshots/multiple-without-overview.png) |

Para mostrar una introducción personalizada antes de un único permiso, pasa `overviewMode = PermissionOverviewMode.Show` y `overview = PermissionOverview { IntroPage() }`. `Show` también funciona con la vista general predeterminada. Para omitir la introducción de un conjunto, pasa `overviewMode = PermissionOverviewMode.Hide`. Cambiar el modo no cambia qué permisos solicita el botón.

## Carrusel temporizado

`HandlePermissions(autoAdvance = true)` recorre en bucle las páginas visibles y vuelve a la primera cuando termina el temporizador de la última. La barra de progreso segmentada situada encima del encabezado tiene un segmento del mismo ancho por cada página visible, incluida la vista general cuando se muestra. Los segmentos anteriores están llenos, el actual se llena durante su tiempo de lectura y los futuros están vacíos; todos se llenan al terminar la última página y se reinician al volver al principio. El botón junto al contador de páginas pausa o reanuda el avance. La interacción del usuario pausa el temporizador y atenúa la barra hasta que el lector lo reanuda; la página conserva su tiempo de lectura restante. Una sola página visible permanece inmóvil. El valor predeterminado es `false`. La aplicación proporciona `autoAdvanceDelayMillis` en cada `PermissionDescription` y, si existe, en `PermissionOverview` (seis segundos de forma predeterminada). Usa `estimateReadingDelayMillis("$title $body", ReadingPace.Slow)` para obtener una demora a partir del mismo texto que se muestra en una página personalizada; consulta la [Activity de demostración](../../app/src/main/java/com/apexfission/android/permissions/MainActivity.kt). La biblioteca no puede contar el texto de un composable arbitrario. `ReadingPace.Slow`, `Normal` y `Fast` usan 120, 180 y 230 palabras por minuto. Para páginas con muchas imágenes o idiomas sin separadores de palabras, establece la demora directamente. El avance automático se pausa cuando la aplicación no está reanudada y se desactiva para la exploración táctil.

Consulta la [vista previa del progreso segmentado](../screenshots/segmented-reading-progress.png) para ver su posición encima del encabezado.

`PermissionDisplayMode.All` incluye las páginas de permisos concedidos con marcas verdes. `MissingOnly` oculta esas páginas e iconos. Ambos evalúan la **lista original completa** y solicitan juntos los permisos pendientes mientras se muestra la explicación. Solo las entradas obligatorias condicionan el contenido protegido. Tras una concesión parcial que deja acceso obligatorio pendiente, la explicación permanece; cuando se infiere que ninguno de los permisos restantes puede mostrar un diálogo, la acción ofrece los ajustes de la aplicación.

`PermissionStatus.PermanentlyDenied` es solo una inferencia basada en el historial de solicitudes y la señal de justificación. La nota predeterminada indica que Android **podría** omitir el diálogo; nunca presentes este estado como una certeza. `HandlePermissions` acepta `recoveryContent = { inferredPermissionNames -> ... }`, `settingsActionLabel` y `onOpenSettings = { ... }`. El contenido aparece cuando se infiere que al menos un permiso está bloqueado, aunque el lote aún pueda solicitar otro permiso pendiente. El callback sustituye la apertura predeterminada de los detalles de la aplicación y debe proporcionar una ruta hacia Ajustes iniciada por el usuario. `PermissionBundleScreen` acepta las opciones de contenido y etiqueta; su `onOpenSettings` obligatorio controla la acción. Consulta el [inicio rápido completo](quickstart.md) y el [carrusel ejecutable](../../app/src/main/java/com/apexfission/android/permissions/demo/PermissionCarouselDemo.kt).

Consulta las vistas previas de [permiso seleccionado](../screenshots/permission-detail.png), [recuperación mediante Ajustes](../screenshots/settings-recovery.png) y [tira de iconos desbordada](../screenshots/overflowing-icons.png) para los demás estados principales de la interfaz. La [galería para lectores](https://lambdawalker.github.io/android.apexfission.permissions/gallery/) incluye las siete imágenes revisadas. Son vistas previas estáticas de Compose, no diálogos de permisos del sistema.

## Otros puntos de entrada de la interfaz

- `HandlePermissions(onPermissionsResult = { results -> ... })` observa el resultado del lote. No lo trates como atómico.
- Solo para cámara, usa `HandlePermissions` con una `PermissionDescription(Manifest.permission.CAMERA) { CameraExplanation() }`; Automatic oculta la vista general salvo que establezcas `overviewMode = Show`.
- Para una página explicativa personalizada con el botón por lotes integrado, proporciona `PermissionDescription(Manifest.permission.CAMERA) { CameraExplanation() }`.
- `PermissionBundleScreen` renderiza estados y acciones gestionados por la aplicación sin iniciar solicitudes de Android.

Consulta [README.md](../../README.md) y la [Activity de ejemplo](../../app/src/main/java/com/apexfission/android/permissions/MainActivity.kt).
