# Conceptos, ciclo de vida y responsabilidades

## Control de acceso frente a renderizador

`HandlePermissions` asocia la lista completa de descripciones a estados de permisos de Accompanist, construye una instantánea `PermissionGrants` y compone el contenido cuando `missingRequired` está vacío. Solo en caso contrario filtra las páginas visibles y renderiza `PermissionBundleScreen`. `MissingOnly` afecta a la presentación, no a la comprobación de permisos ni al conjunto original de la solicitud. Cambiar los nombres visibles o el modo de vista general recrea la pantalla identificada por su clave; el control de acceso conserva su indicador guardable de detención del avance automático.

`PermissionBundleScreen` recibe estados ordenados. Su botón invoca `onComplete` solo si **todos los estados proporcionados** son Granted; en caso contrario, invoca `onRequest` si alguno es NotRequested o RationaleRequired, u `onOpenSettings` en los demás casos. El indicador `required` etiqueta las páginas, pero no cambia esta decisión de acción de la pantalla independiente. No consulta Android ni inicia solicitudes.

## Estado inferido

| Evidencia, por orden de prioridad | Estado |
| --- | --- |
| Permiso concedido actualmente | Granted |
| Android indica que debe mostrarse una justificación | RationaleRequired |
| Solicitud anterior registrada localmente | PermanentlyDenied |
| En cualquier otro caso | NotRequested |

El control de acceso almacena valores booleanos por permiso en las SharedPreferences de la aplicación llamadas `camera_permission_state`. Marca las entradas de permisos pendientes antes de iniciar la solicitud y borra el historial cuando se conceden. Este historial abarca las solicitudes realizadas mediante este control, no todas las que pueda realizar otro componente. Borrar o restaurar los datos de la aplicación, o realizar solicitudes externas, puede afectar a la inferencia. Aquí no existe un indicador definitivo de denegación permanente.

## Responsabilidad y ejecución

| Recurso o tarea | Responsable y duración |
| --- | --- |
| Lanzador de permisos del control de acceso | Ciclo de vida de Compose/Accompanist; no es una operación duradera en segundo plano |
| `PermissionRequester` | Instancia de Activity; nunca conservarlo en un singleton ni en un ViewModel que sobreviva a la Activity |
| Callbacks pendientes del solicitante | En memoria hasta el resultado; no se restauran tras una recreación o la finalización del proceso |
| Instantáneas de permisos y comprobaciones | Valores de un momento concreto; volver a comprobar tras cambios externos y antes de operaciones protegidas |
| Bitmap/Drawable de la página | La aplicación mantiene su validez; la biblioteca no recicla los bitmaps proporcionados; evitar modificaciones concurrentes o compartir un Drawable mutable entre vistas |
| URI del medio seleccionado | La aplicación abre y cierra los flujos y gestiona la persistencia admitida; el selector devuelve un URI o null |
| Tareas de avance automático y desplazamiento | Pertenecen a la composición; al eliminarla se cancelan los efectos y se retira el observador del ciclo de vida |

La interfaz Compose, la creación y ejecución del lanzador y el uso del solicitante corresponden al hilo principal. El solicitante no está sincronizado. Su callback de permisos ya concedidos se ejecuta en línea durante la llamada terminal; los callbacks de resultados de Activity se ejecutan mediante el mecanismo de resultados de Activity en el hilo de la interfaz. `runIfPermissionsGranted` y `otherwise` se ejecutan en línea en el hilo que los invoca, sin cambiar de dispatcher. Los cálculos de las recetas son síncronos y no inician nada. Mantén los callbacks breves; la biblioteca no proporciona colas de trabajo ni mecanismos de contrapresión. Las excepciones de los callbacks se propagan; no existe un callback de error de la biblioteca.

El solicitante libera su espacio de solicitud pendiente antes de entregar un resultado, lo que permite una solicitud posterior, pero no conviertas las denegaciones en reintentos automáticos. Una excepción del lanzador libera ese espacio y se propaga. No existe una API pública de cancelación. Salir de la pantalla no garantiza que se cancele un diálogo pendiente de la plataforma.

El avance automático debe activarse expresamente y solo funciona en estado RESUMED y cuando se observa que la exploración táctil está desactivada. La interacción del usuario mantiene la pausa hasta que se reanude; las páginas sin cambios conservan el progreso transcurrido. Los cambios de página, demora o cantidad recrean el progreso. El indicador de exploración táctil se lee durante la composición, sin un listener específico que lo observe. No prometas una respuesta inmediata a los cambios de accesibilidad sin recomposición.
