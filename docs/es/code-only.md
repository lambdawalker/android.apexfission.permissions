# Integración solo con código

Para ubicación por etapas, notificaciones o medios seleccionados por el usuario, consulta las [recetas adaptadas a la plataforma](platform-recipes.md) antes de usar un lote genérico de permisos.

Usa estas API cuando la aplicación gestione la interfaz. Declara cada permiso en su manifiesto. Ambas vías se orientan a permisos ordinarios en tiempo de ejecución; el acceso especial de aplicaciones y las solicitudes de plataforma por etapas necesitan otro flujo.

## Lanzador gestionado por la biblioteca

Construye `PermissionRequester` como propiedad de la Activity **antes de STARTED** y llámalo desde una acción del usuario:

```kotlin
class ScannerActivity : ComponentActivity() {
    private val permissions = PermissionRequester(this)

    private fun onScanClicked() = with(permissions) {
        requestPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        ) {
            startScan() // all granted, possibly synchronously
        } onDenied { missing: List<String> ->
            showDeniedState(missing)
        }
    }

    private fun startScan() { /* protected work */ }
    private fun showDeniedState(missing: List<String>) { /* host UI or settings */ }
}
```

`requestPermissions(...) { onGranted }` crea una llamada pendiente; la función infija `onDenied` **adjunta el callback y la inicia**. El solicitante comprueba los permisos actuales, solicita solo los permisos pendientes distintos y vuelve a comprobar tras el regreso de Android. Solo puede haber una solicitud pendiente. Nunca reutilices una llamada pendiente. Los callbacks viven en memoria; tras recrear la Activity o finalizar el proceso, vuelve a comprobar en la siguiente acción del usuario.

## Lanzador gestionado por la aplicación

`Context.runIfPermissionsGranted` es síncrono, con sobrecargas vararg y `List<String>`. Nunca presenta un diálogo ni conserva la acción. Ejemplo dentro de una Activity:

```kotlin
private val permissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
) { grants ->
    if (grants.isNotEmpty() && grants.values.all { it }) {
        onFeatureClicked() // recheck current grants
    } else {
        showDeniedState()
    }
}

private fun onFeatureClicked() {
    runIfPermissionsGranted(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
    ) {
        startFeature()
    } otherwise { missing ->
        permissionLauncher.launch(missing.toTypedArray())
    }
}
```

`otherwise` recibe los nombres distintos que faltan en **esa comprobación**. Gestiona la denegación por separado; no reintentes desde un resultado denegado o el diálogo puede reabrirse repetidamente. Si los permisos ya están concedidos, la acción se ejecuta de forma síncrona. La comprobación funciona con cualquier `Context` válido, mientras que la solicitud necesita un lanzador de resultados de Activity, Fragment o Compose registrado en el momento apropiado del ciclo de vida.

Para una interfaz gestionada por la biblioteca, consulta la [integración con Compose](compose.md).
