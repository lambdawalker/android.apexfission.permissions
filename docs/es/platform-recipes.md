# Recetas de solicitudes adaptadas a la plataforma

Estas recetas complementan el carrusel genérico de permisos en tiempo de ejecución. Declara los permisos en el manifiesto de la aplicación y llama a una receta cuando el usuario acceda a la función. Cada llamada devuelve la **siguiente** acción según los permisos actuales; vuelve a llamarla tras un resultado o al regresar de Ajustes. Ninguna receta inicia un diálogo durante la composición.

## Ubicación en primer plano

Usa `PermissionRecipes.foregroundLocation(context, LocationAccuracy.Approximate)` para una función que pueda usar ubicación aproximada. Para ubicación precisa, solicita `ACCESS_COARSE_LOCATION` y `ACCESS_FINE_LOCATION` juntos. Declara ambos en el manifiesto cuando se necesite acceso preciso. El usuario todavía puede conceder solo acceso aproximado; inspecciona `PermissionRecipes.grantedLocationAccuracy(context)` y adapta la función adecuadamente.

```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<!-- Only for a feature that truly needs background location: -->
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
```



```kotlin
// Inside a ComponentActivity method; permissionRequester is an Activity property.
val activity = this
when (val step = PermissionRecipes.foregroundLocation(this, LocationAccuracy.Precise)) {
    is PermissionRecipeStep.RequestRuntime -> with(permissionRequester) {
        requestPermissions(*step.permissions.toTypedArray()) {
            // Recheck accuracy before using location.
            useLocation(PermissionRecipes.grantedLocationAccuracy(activity))
        } onDenied { missing -> showLocationExplanation(missing) }
    }
    PermissionRecipeStep.Ready -> useLocation(PermissionRecipes.grantedLocationAccuracy(activity))
    PermissionRecipeStep.OpenAppSettings -> openPermissionRecipeSettings()
    PermissionRecipeStep.SystemControlledPrompt -> Unit
}
```

`PermissionRequester` es una propiedad de la Activity registrada antes de STARTED. Solicita después de una acción del usuario. `useLocation` y `showLocationExplanation` del ejemplo son funciones de la aplicación.

## Ubicación en segundo plano

Inicia este flujo solo cuando el usuario acceda a una función que realmente necesite acceso en segundo plano. Declara `ACCESS_BACKGROUND_LOCATION` además de los permisos de primer plano. `PermissionRecipes.backgroundLocation(context, accuracy)` devuelve primero un paso de permisos en tiempo de ejecución de primer plano cuando hace falta. Vuelve a llamarla tras concederse ese acceso, como **acción separada del usuario** con su propia explicación. En Android 10 devuelve una solicitud en tiempo de ejecución solo para segundo plano; en Android 11+ devuelve `OpenAppSettings`. Presenta una pantalla informativa que explique por qué hace falta el acceso en segundo plano, proporcione la etiqueta localizada de la opción de segundo plano de la plataforma cuando esté disponible y permita rechazarlo. Llama a `openPermissionRecipeSettings()` solo cuando el usuario elija continuar. Vuelve a comprobar la receta al regresar.

Nunca incluyas ubicación en primer y segundo plano en el mismo lote `RequestMultiplePermissions`.

## Notificaciones

Declara `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />` en el manifiesto de la aplicación. `PermissionRecipes.notifications(context)` devuelve `RequestRuntime(POST_NOTIFICATIONS)` en Android 13+ cuando la aplicación tiene API 33+ como destino, `Ready` cuando las notificaciones están activadas u `OpenAppSettings` si están desactivadas pese a tener permiso. En Android 13 con un destino anterior, `SystemControlledPrompt` significa que Android controla cuándo aparece el diálogo; actualiza el destino de la aplicación a 33+ para solicitarlo en una acción predecible del usuario. En versiones anteriores de Android no hay diálogo de notificaciones en tiempo de ejecución; las notificaciones desactivadas requieren Ajustes. Una denegación en tiempo de ejecución puede requerir otra explicación o una ruta hacia Ajustes; no repitas automáticamente la solicitud.

## Fotos y vídeos

`rememberVisualMediaPicker` usa el selector de fotos de AndroidX para medios seleccionados por el usuario. Es un flujo de selección, por lo que **no** pertenece al lote de permisos en tiempo de ejecución:

```kotlin
val picker = rememberVisualMediaPicker { uri ->
    if (uri != null) processSelectedPhoto(uri)
}
Button(onClick = { picker.launch(VisualMediaSelection.Image) }) { Text("Choose photo") }
```

El selector también puede iniciarse con `Video` o `ImageOrVideo` y devuelve `null` al cancelarse. No requiere permisos de medios en tiempo de ejecución. Si la aplicación gestiona una galería que necesita consultar toda la biblioteca de medios, maneja el acceso a fotos seleccionadas de Android 14 (`READ_MEDIA_VISUAL_USER_SELECTED`) y la nueva selección como un flujo separado de la aplicación. No interpretes una denegación de `READ_MEDIA_IMAGES` como prueba de que no se puede acceder a ninguna foto.

## Verificación

Prueba dispositivos o emuladores con API 28, 29, 30/31, 33 y 34+. Comprueba la ubicación aproximada, el regreso de Ajustes para segundo plano, la denegación de notificaciones, la cancelación del selector de fotos y los cambios de permisos mientras la aplicación está ausente. `PermissionRecipesTest` cubre las transiciones puras de las recetas.

Guías de Android: [ubicación](https://developer.android.com/develop/sensors-and-location/location/permissions/runtime), [ubicación en segundo plano](https://developer.android.com/develop/sensors-and-location/location/permissions/background), [notificaciones](https://developer.android.com/develop/ui/compose/notifications/notification-permission), [selector de fotos](https://developer.android.com/training/data-storage/shared/photo-picker), [acceso a fotos seleccionadas](https://developer.android.com/about/versions/14/changes/partial-photo-video-access).
