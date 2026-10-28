# Integración mínima con Compose

Usa una aplicación Android con Compose habilitado y minSdk 24 o posterior. Sigue [IMPORT.md](../../IMPORT.md) para la dependencia publicada actual y la configuración de repositorios.

En este repositorio, la demostración usa `implementation(project(":permission"))`. La aplicación debe aportar las dependencias Activity Compose y Material 3; consulta la [configuración de compilación de la demostración](../../app/build.gradle.kts) y el [catálogo de versiones](../../gradle/libs.versions.toml) para conocer la combinación probada. No asumas que todas las dependencias de compilación se exportan como API.

Declara lo siguiente fuera de `<application>` en el manifiesto de la aplicación:

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

Añade esta Activity al manifiesto de la aplicación, adaptando su paquete:

```xml
<activity android:name="com.apexfission.android.permissions.demo.CameraQuickstartActivity" android:exported="false" />
```

El siguiente código fuente completo se extrae de la aplicación de demostración. La aplicación aplica los márgenes de dibujo seguro exactamente una vez:

<!-- quickstart:start -->

```kotlin
package com.apexfission.android.permissions.demo

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.apexfission.android.permission.requester.HandlePermissions
import com.apexfission.android.permission.ui.DefaultPermissionPage
import com.apexfission.android.permission.ui.PermissionDescription

class CameraQuickstartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HandlePermissions(
                    modifier = Modifier.safeDrawingPadding(),
                    permissions = listOf(
                        PermissionDescription(Manifest.permission.CAMERA) {
                            DefaultPermissionPage(
                                label = "Camera",
                                title = "Scan a document",
                                body = "Allow camera access to scan your document.",
                            )
                        },
                    ),
                    onBack = { finish() },
                    onNotNow = { finish() },
                ) { grants ->
                    Text("Camera access: ${grants.isGranted(Manifest.permission.CAMERA)}")
                }
            }
        }
    }
}
```

<!-- quickstart:end -->

Resultado esperado: si falta el permiso de cámara, se muestra una explicación; el botón solicita acceso. Conceder el permiso de cámara compone el texto. Si ya existe acceso, el texto aparece de inmediato. La denegación mantiene el flujo de explicación y recuperación. El ejemplo evita deliberadamente iniciar el hardware de cámara desde el cuerpo de un composable: vincula los recursos con el mecanismo de ciclo de vida de tu biblioteca de cámara.

Este ejemplo se extrae de [CameraQuickstartActivity.kt](../../app/src/main/java/com/apexfission/android/permissions/demo/CameraQuickstartActivity.kt), compilado con `:app:assembleDebug`. Ejecuta `./gradlew :app:installDebug`, abre la aplicación y elige **Open quickstart**. La [demostración del carrusel](../../app/src/main/java/com/apexfission/android/permissions/demo/PermissionCarouselDemo.kt) añade acceso opcional y personalización. Consulta las [recetas](recipes.md) para el acceso opcional, las páginas personalizadas y los permisos por etapas.
