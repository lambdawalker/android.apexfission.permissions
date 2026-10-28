# Declaraciones de la API pública

Usa esta página como mapa rápido de las API invocables de la biblioteca. Los nombres pertenecen a `com.apexfission.android.permission`; importa desde el subpaquete indicado en cada encabezado. Los argumentos predeterminados y el comportamiento del ciclo de vida se explican en la [integración con Compose](compose.md), la [integración solo con código](code-only.md) y las [recetas de plataforma](platform-recipes.md). Las declaraciones siguientes omiten KDoc y anotaciones cuando no afectan a la sintaxis de llamada. `defaultPermissionOverview()` es **internal**: para obtenerla, omite `overview`; no se puede importar la función de fábrica.

## `requester`: control de acceso Compose

```kotlin
@Composable
fun HandlePermissions(
    permissions: List<PermissionDescription>,
    onBack: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier,
    overview: PermissionOverview = /* library default */,
    onPermissionsResult: (Map<String, Boolean>) -> Unit = {},
    displayMode: PermissionDisplayMode = PermissionDisplayMode.All,
    overviewMode: PermissionOverviewMode = PermissionOverviewMode.Automatic,
    autoAdvance: Boolean = false,
    recoveryContent: @Composable (List<String>) -> Unit = { DefaultPermissionRecovery(it) },
    settingsActionLabel: String? = null,
    onOpenSettings: (() -> Unit)? = null,
    content: @Composable (PermissionGrants) -> Unit,
)
```

`HandlePermissions` gestiona el lanzador de solicitudes por lotes de Android y condiciona `content` a los permisos **obligatorios**. `onPermissionsResult` puede contener concesiones parciales; usa el `PermissionGrants` actual en `content`. `onBack` y `onNotNow` son callbacks de navegación de la aplicación. Solo `overviewMode` controla la visibilidad: `Automatic` cuando hay más de un permiso visible, `Show` siempre y `Hide` nunca. La vista general predeterminada es una página genérica con icono de candado. Las solicitudes empiezan con el botón, no durante la composición. La aplicación declara cada permiso en su manifiesto. El acceso especial de aplicaciones y las solicitudes por etapas necesitan flujos separados de la aplicación.

```kotlin
class PermissionGrants internal constructor(/* library-owned snapshot */) {
    val statusByPermission: Map<String, PermissionStatus>
    val missingRequired: Set<String>
    val missingOptional: Set<String>
    val canProceed: Boolean
    fun isGranted(permission: String): Boolean
}

@Composable fun DefaultPermissionRecovery(permissions: List<String>)
```

La nota de recuperación expresa incertidumbre deliberadamente: `PermanentlyDenied` se infiere a partir del historial local de solicitudes y de la señal de justificación de Android. Proporciona `recoveryContent`, `settingsActionLabel` y `onOpenSettings` cuando la función necesite su propia explicación y ruta hacia Ajustes.

## `ui`: páginas y carrusel

```kotlin
class PermissionDescription(
    val permission: String,
    val label: String = /* permission visual default */,
    val icon: ImageVector = /* permission visual default */,
    val autoAdvanceDelayMillis: Long = 6_000L,
    val required: Boolean = true,
    val page: @Composable () -> Unit,
)

class PermissionOverview(
    val autoAdvanceDelayMillis: Long = 6_000L,
    val page: @Composable () -> Unit,
)

enum class PermissionDisplayMode { All, MissingOnly }
enum class PermissionOverviewMode { Automatic, Show, Hide }

data class PermissionFeature(val icon: ImageVector, val title: String, val subtitle: String)

@Composable fun DefaultPermissionPage(
    label: String,
    heroImage: ImageVector = Icons.Default.Lock,
    title: String? = null,
    body: String? = null,
    features: List<PermissionFeature> = emptyList(),
)
@Composable fun DefaultPermissionPage(
    label: String, heroImage: Bitmap, title: String? = null, body: String? = null,
    features: List<PermissionFeature> = emptyList(),
)
@Composable fun DefaultPermissionPage(
    label: String, @DrawableRes heroImage: Int, title: String? = null, body: String? = null,
    features: List<PermissionFeature> = emptyList(),
)
@Composable fun DefaultPermissionPage(
    label: String, heroImage: Drawable, title: String? = null, body: String? = null,
    features: List<PermissionFeature> = emptyList(),
)

@Composable fun DefaultDescription(
    label: String,
    icon: ImageVector? = null,
    title: String? = null,
    body: String? = null,
    features: List<PermissionFeature> = emptyList(),
)

data class PermissionVisual(val label: String, val icon: ImageVector)
object PermissionVisualDefaults {
    fun forPermission(permission: String): PermissionVisual
}
```

Cada `PermissionDescription` y cada `PermissionOverview` creada por la aplicación requiere un composable de **página completa**. La tira de iconos, los botones, el progreso y el texto de recuperación de la biblioteca están fuera de la página. `DefaultPermissionPage` es opcional: dibuja una imagen principal centrada y texto explicativo. `heroImage` acepta un `ImageVector` (candado de forma predeterminada), un `Bitmap` de Android, un `Drawable` o un ID de recurso drawable como `R.drawable.permission_hero`. La imagen es decorativa; el texto transmite su significado a los servicios de accesibilidad. Mantén válido el bitmap proporcionado mientras su página esté compuesta. El `icon` del permiso es un metadato separado para la tira de iconos. Compón tu propia `page` para usar otra disposición. `label` e `icon` alimentan la tira de iconos, con valores predeterminados para permisos Android comunes; sustitúyelos para adaptar el texto a la función o localizarlo. `DefaultDescription` es la parte textual de la página integrada. `PermissionFeature` añade allí filas de ventajas. `All` conserva las páginas de permisos concedidos y añade marcas verdes; `MissingOnly` las oculta, pero no cambia el lote ni las comprobaciones de permisos.

```kotlin
@Composable fun PermissionBundleScreen(
    permissions: List<PermissionDescription>,
    statuses: List<PermissionStatus>,
    onBack: () -> Unit,
    onNotNow: () -> Unit,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    overview: PermissionOverview = /* library default */,
    onComplete: () -> Unit = {},
    initialPage: Int = 0,
    overviewMode: PermissionOverviewMode = PermissionOverviewMode.Automatic,
    autoAdvance: Boolean = false,
    onUserInteraction: () -> Unit = {},
    initialAutoAdvanceStopped: Boolean = false,
    onAutoAdvanceResume: () -> Unit = {},
    recoveryContent: @Composable (List<String>) -> Unit = { DefaultPermissionRecovery(it) },
    settingsActionLabel: String? = null,
)

enum class ReadingPace(val wordsPerMinute: Int) {
    Slow(120), Normal(180), Fast(230)
}
fun estimateReadingDelayMillis(text: String, pace: ReadingPace = ReadingPace.Slow): Long
```

`PermissionBundleScreen` solo renderiza estados e invoca acciones de la aplicación; úsala cuando la aplicación gestione el lanzador. El orden de los estados debe coincidir con el de los permisos. La función auxiliar de lectura cuenta palabras separadas por espacios en blanco, añade dos segundos y limita el resultado a 4–60 segundos. No puede inspeccionar contenido Compose arbitrario. Pasa el mismo **texto visible** a la función y a la página:

```kotlin
val title = "Record narration"
val body = "Add a voice note to your scan."
PermissionDescription(
    permission = Manifest.permission.RECORD_AUDIO,
    required = false,
    autoAdvanceDelayMillis = estimateReadingDelayMillis("$title $body", ReadingPace.Slow),
) { NarrationPermissionPage(title, body) }
```

`NarrationPermissionPage` es un **composable de demostración de `:app`**, no una API de la biblioteca. Su tarjeta de consola de grabación, forma de onda y texto alineado a la izquierda muestran cuándo resulta útil una página personalizada. Consulta el [código completo de la demostración](../../app/src/main/java/com/apexfission/android/permissions/demo/PermissionCarouselDemo.kt) y la [guía de selección de página](compose.md#choose-a-page-layout). Usa una demora explícita para páginas con muchas imágenes o idiomas sin separación de palabras mediante espacios en blanco.

## `requester`: solicitudes y comprobaciones solo con código

```kotlin
class PermissionRequester(activity: ComponentActivity) {
    fun requestPermissions(vararg permissions: String, onGranted: () -> Unit): PendingPermissionRequest
}
class PendingPermissionRequest internal constructor(/* library-owned */) {
    infix fun onDenied(handler: (List<String>) -> Unit)
}

fun Context.runIfPermissionsGranted(
    vararg permissions: String, onGranted: () -> Unit,
): PermissionCheckResult
fun Context.runIfPermissionsGranted(
    permissions: List<String>, onGranted: () -> Unit,
): PermissionCheckResult
class PermissionCheckResult internal constructor(/* library-owned */) {
    val missing: List<String>
    infix fun otherwise(onMissing: (List<String>) -> Unit): PermissionCheckResult
}
```

Construye `PermissionRequester` como propiedad de la Activity antes de STARTED. `requestPermissions(...) { ... } onDenied { missing -> ... }` se inicia en `onDenied`; puede haber una solicitud pendiente. `runIfPermissionsGranted(...) { ... } otherwise { missing -> ... }` es una comprobación síncrona; la aplicación gestiona cualquier lanzador y debe volver a comprobar tras recibir un resultado. Consulta las [llamadas listas para copiar y las reglas del ciclo de vida](code-only.md).

## `recipe`: acceso por etapas y selección de medios

```kotlin
enum class PermissionStatus { Granted, NotRequested, RationaleRequired, PermanentlyDenied }
enum class LocationAccuracy { Approximate, Precise }
sealed interface PermissionRecipeStep {
    data object Ready : PermissionRecipeStep
    data class RequestRuntime(val permissions: List<String>) : PermissionRecipeStep
    data object OpenAppSettings : PermissionRecipeStep
    data object SystemControlledPrompt : PermissionRecipeStep
}
object PermissionRecipes {
    fun foregroundLocation(context: Context, accuracy: LocationAccuracy): PermissionRecipeStep
    fun backgroundLocation(context: Context, accuracy: LocationAccuracy): PermissionRecipeStep
    fun notifications(context: Context): PermissionRecipeStep
    fun grantedLocationAccuracy(context: Context): LocationAccuracy?
}
fun Context.openPermissionRecipeSettings()

enum class VisualMediaSelection { Image, Video, ImageOrVideo }
class VisualMediaPicker internal constructor(/* library-owned */) {
    fun launch(selection: VisualMediaSelection = VisualMediaSelection.ImageOrVideo)
}
@Composable fun rememberVisualMediaPicker(onResult: (Uri?) -> Unit): VisualMediaPicker
```

Las recetas devuelven la **siguiente acción de la aplicación**; vuelve a llamarlas tras una concesión o el regreso de Ajustes. La ubicación en primer y segundo plano se solicita en etapas separadas. El selector de fotos no necesita un permiso amplio de medios para los elementos seleccionados por el usuario. Consulta la [guía según la versión](platform-recipes.md).

## Validación y contratos observables

| API | Precondiciones, errores y consecuencias |
| --- | --- |
| `HandlePermissions` | Rechaza listas vacías, nombres duplicados o en blanco con `IllegalArgumentException`. Solo los permisos obligatorios condicionan el contenido; si todos son opcionales, continúa de inmediato. `onPermissionsResult` observa callbacks del lote, no cambios en Ajustes. |
| `PermissionDescription`, `PermissionOverview` | Rechazan demoras no positivas. Cada constructor explícito requiere contenido de página. La validación del nombre de la descripción se realiza en el control de acceso, no en su constructor. |
| `PermissionBundleScreen` | Requiere nombres únicos en una lista no vacía, igual cantidad de estados y una página inicial válida. Usa todos los estados proporcionados para decidir entre completar, solicitar o abrir Ajustes; los indicadores required solo etiquetan páginas. No hay `onComplete` automático: lo invoca el botón del estado completado. `initialPage` es una inicialización, no una propiedad de selección controlada. |
| `PermissionGrants` | La biblioteca construye la instantánea; los nombres desconocidos devuelven false. No la almacenes como autoridad permanente. |
| `PermissionRequester` / `PendingPermissionRequest` | La llamada terminal rechaza listas vacías o nombres en blanco y elimina duplicados; las solicitudes superpuestas o la repetición de la llamada terminal lanzan `IllegalStateException`. Las excepciones del lanzador se propagan tras limpiar el estado pendiente. Sin cancelación ni cola. |
| `runIfPermissionsGranted` / `PermissionCheckResult` | Una entrada vacía o con nombres en blanco lanza `IllegalArgumentException`; se eliminan duplicados. La acción se ejecuta en línea solo si no falta ningún nombre. Cada llamada repetida a `otherwise` invoca el manejador con la misma instantánea de permisos pendientes. |
| `PermissionRecipes` | Cálculo síncrono del siguiente paso; sin iniciar solicitudes, mantener historial de denegaciones ni suscribirse a cambios. Volver a evaluar tras cambios externos. |
| `openPermissionRecipeSettings` | Abre los detalles de la aplicación en Ajustes con NEW_TASK. Sin resultado ni garantía de éxito; los fallos de inicio de la plataforma se propagan. |
| `VisualMediaPicker.launch` | Selector AndroidX de un solo elemento. ImageOrVideo de forma predeterminada; la cancelación devuelve null. La aplicación gestiona el acceso al URI y su persistencia. |
| `DefaultDescription` | Si title/body son null, usa valores alternativos de recursos de cadenas; un icono proporcionado sustituye el de cada fila de ventajas. No dibuja una imagen principal. |
| `PermissionVisualDefaults.forPermission` | Los nombres conocidos reciben metadatos; los desconocidos reciben un sufijo legible y un candado. No es una API de validación. |

Todos los tipos públicos previstos para uso externo aparecen arriba; la biblioteca devuelve o crea las clases con constructores internos. `defaultPermissionOverview`, `PermissionRequestCoordinator`, las funciones auxiliares de decisión de estados y recetas, y los composables privados de renderizado no son API para consumidores. `NarrationPermissionPage` y las demás funciones de demostración no están en la biblioteca publicada. Accompanist es una dependencia de compilación exportada, no un sustituto de la API documentada de esta biblioteca.

Consulta [conceptos](concepts.md) para los contratos de hilos, ciclo de vida, propiedad de recursos, reentrada y eliminación, y [limitaciones](limitations.md) para los límites de seguridad y plataforma. Los callbacks de interfaz se ejecutan en la ruta de eventos de la interfaz; no trasladan el trabajo lento a otro hilo. La pantalla independiente no tiene parámetro `displayMode` de permisos: filtra conjuntamente tus propias listas de estados y descripciones.
