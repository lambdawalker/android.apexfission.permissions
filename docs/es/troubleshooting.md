# Resolución de problemas

| Síntoma | Diagnóstico o causa probable | Solución correcta; qué evitar |
| --- | --- | --- |
| No hay solicitud tras `requestPermissions` | No se completó el constructor de la llamada pendiente | Añadir `onDenied` exactamente una vez; no almacenar ni reutilizar llamadas pendientes |
| Excepción de registro | El solicitante se construyó después de que la Activity alcanzara STARTED | Construirlo como propiedad de la Activity, no dentro de un manejador de pulsaciones |
| “request already in progress” | Segunda llamada terminal mientras hay una solicitud pendiente | Desactivar acciones duplicadas; no crear solicitantes adicionales para eludir la coordinación |
| “already started” | Se reutilizó el mismo objeto pendiente | Construir una nueva llamada para una acción posterior del usuario |
| La página opcional nunca aparece | Ya están concedidos los permisos obligatorios o todos son opcionales | Solicitar el acceso opcional cuando vaya a usarse; no hacerlo obligatorio solo para forzar la presentación inicial |
| Excepción por lista vacía, duplicados o nombres en blanco | Configuración inválida del control de acceso | Proporcionar nombres únicos y no vacíos en una lista no vacía; las solicitudes auxiliares eliminan duplicados, pero rechazan entradas vacías o en blanco |
| No aparece el diálogo o se repite la denegación | Falta la declaración en el manifiesto, el sistema no admite el permiso, hay historial de denegación o una política | Inspeccionar el manifiesto combinado de la aplicación y el permiso o la justificación actuales; ofrecer recuperación prudente, nunca repetir en bucle automáticamente |
| Tras volver de Ajustes la función sigue sin estar disponible | No se activó el acceso o el estado gestionado por la aplicación está desactualizado | Volver a comprobar el permiso o la receta al reanudar; abrir Ajustes no implica éxito |
| Callback perdido tras rotar | Los callbacks pertenecían a la Activity destruida | Restaurar la intención y el estado de la aplicación y volver a comprobar en la siguiente acción del usuario; no conservar la Activity antigua |
| Importación sin resolver | Se usó el espacio de nombres raíz en vez del subpaquete, o una API obsoleta | Usar el [inventario de API](api.md) y la [migración](migration.md) |
| Dependencia Maven sin resolver | Coordenadas o versión incorrectas, o falta el repositorio | Copiar las coordenadas y la versión publicada de [IMPORT.md](../../IMPORT.md), incluida la escritura exacta del grupo, y habilitar Maven Central |
| Margen del sistema inesperado | Se combinaron incorrectamente el relleno de la aplicación y el del contenedor | Inspeccionar la disposición real y aplicar los márgenes del sistema exactamente una vez; el código actual del conjunto no llama a `systemBarsPadding` |
| Fallos de Bitmap o imágenes desactualizadas | La aplicación recicló o modificó una imagen mientras estaba compuesta | Mantener las imágenes válidas y estables hasta que ningún consumidor las use; la biblioteca no asume su propiedad |
| Permiso aproximado tras solicitar ubicación precisa | El usuario eligió acceso aproximado | Comprobar la precisión real y adaptar la función o explicarlo; nunca afirmar que existe acceso preciso a partir de una concesión aproximada |
| La receta de notificaciones sigue solicitando | La receta no tiene estado de historial de denegaciones | Gestionar la denegación en la interfaz de la aplicación y permitir una ruta deliberada hacia Ajustes |
| La compilación falla antes de compilar el código | SDK o herramientas no disponibles | Seguir [AGENTS.md](../../AGENTS.md); distinguir el bytecode Java 17 del daemon de Gradle con Java 25 |

Incorrecto: llamar a `picker.launch()` o a una llamada terminal de solicitud directamente en el cuerpo de un composable. Correcto: registrar o recordar durante la composición e invocar desde `Button(onClick = { ... })`.

Incorrecto: ejecutar operaciones de cámara dentro de `content` como una llamada ordinaria en cada recomposición. Correcto: usar API de recursos o efectos vinculados al ciclo de vida y liberar los recursos de la aplicación al eliminar la composición.

Antes de distribuir una integración, prueba la primera concesión, la concesión parcial, la denegación, el regreso de Ajustes, la revocación, la recreación y la denegación opcional. Para medios, prueba la cancelación y la duración del URI. Consulta las [demostraciones compiladas](recipes.md) y la [verificación del repositorio](../../AGENTS.md#verification).
