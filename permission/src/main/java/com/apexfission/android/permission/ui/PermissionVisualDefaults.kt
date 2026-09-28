package com.apexfission.android.permission.ui

import android.Manifest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

/** A visual and spoken name for a permission in the library's icon selector. */
data class PermissionVisual(val label: String, val icon: ImageVector)

/**
 * Default metadata for common Android runtime permissions. These names describe access, not
 * the host app's feature. Supply [PermissionDescription.label] and/or [PermissionDescription.icon]
 * when feature-specific wording or another language is needed. Unknown strings use a readable
 * name derived from the last segment and a lock icon.
 *
 * This registry only supplies presentation metadata; it does not determine whether a permission
 * can be requested at runtime on a particular Android version.
 */
object PermissionVisualDefaults {
    private val camera = PermissionVisual("Camera", Icons.Default.PhotoCamera)
    private val microphone = PermissionVisual("Microphone", Icons.Default.Mic)
    private val location = PermissionVisual("Location", Icons.Default.LocationOn)
    private val contacts = PermissionVisual("Contacts", Icons.Default.Contacts)
    private val calendar = PermissionVisual("Calendar", Icons.Default.Event)
    private val photos = PermissionVisual("Photos", Icons.Default.Image)
    private val videos = PermissionVisual("Videos", Icons.Default.Videocam)
    private val audio = PermissionVisual("Audio files", Icons.Default.MusicNote)
    private val bluetooth = PermissionVisual("Nearby Bluetooth devices", Icons.Default.Bluetooth)
    private val phone = PermissionVisual("Phone", Icons.Default.Phone)
    private val messages = PermissionVisual("Messages", Icons.Default.Sms)

    private val known = mapOf(
        Manifest.permission.CAMERA to camera,
        Manifest.permission.RECORD_AUDIO to microphone,
        Manifest.permission.ACCESS_FINE_LOCATION to PermissionVisual("Precise location", location.icon),
        Manifest.permission.ACCESS_COARSE_LOCATION to PermissionVisual("Approximate location", location.icon),
        Manifest.permission.ACCESS_BACKGROUND_LOCATION to PermissionVisual("Background location", location.icon),
        Manifest.permission.POST_NOTIFICATIONS to PermissionVisual("Notifications", Icons.Default.Notifications),
        Manifest.permission.READ_CONTACTS to contacts,
        Manifest.permission.WRITE_CONTACTS to contacts,
        Manifest.permission.GET_ACCOUNTS to PermissionVisual("Accounts", contacts.icon),
        Manifest.permission.READ_CALENDAR to calendar,
        Manifest.permission.WRITE_CALENDAR to calendar,
        Manifest.permission.READ_MEDIA_IMAGES to photos,
        Manifest.permission.READ_MEDIA_VIDEO to videos,
        Manifest.permission.READ_MEDIA_AUDIO to audio,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED to PermissionVisual("Selected photos and videos", photos.icon),
        Manifest.permission.ACCESS_MEDIA_LOCATION to PermissionVisual("Photo locations", location.icon),
        Manifest.permission.READ_EXTERNAL_STORAGE to PermissionVisual("Files and media", Icons.Default.Folder),
        Manifest.permission.WRITE_EXTERNAL_STORAGE to PermissionVisual("Files and media", Icons.Default.Folder),
        Manifest.permission.BLUETOOTH_SCAN to bluetooth,
        Manifest.permission.BLUETOOTH_CONNECT to bluetooth,
        Manifest.permission.BLUETOOTH_ADVERTISE to bluetooth,
        Manifest.permission.NEARBY_WIFI_DEVICES to PermissionVisual("Nearby Wi-Fi devices", Icons.Default.Wifi),
        Manifest.permission.BODY_SENSORS to PermissionVisual("Body sensors", Icons.Default.Favorite),
        Manifest.permission.BODY_SENSORS_BACKGROUND to PermissionVisual("Background body sensors", Icons.Default.Favorite),
        Manifest.permission.ACTIVITY_RECOGNITION to PermissionVisual("Physical activity", Icons.Default.DirectionsRun),
        Manifest.permission.READ_PHONE_STATE to phone,
        Manifest.permission.READ_PHONE_NUMBERS to phone,
        Manifest.permission.CALL_PHONE to phone,
        Manifest.permission.ANSWER_PHONE_CALLS to phone,
        Manifest.permission.READ_CALL_LOG to PermissionVisual("Call log", phone.icon),
        Manifest.permission.WRITE_CALL_LOG to PermissionVisual("Call log", phone.icon),
        Manifest.permission.READ_SMS to messages,
        Manifest.permission.SEND_SMS to messages,
        Manifest.permission.RECEIVE_SMS to messages,
        Manifest.permission.RECEIVE_MMS to messages,
        Manifest.permission.RECEIVE_WAP_PUSH to messages,
    )

    /** Resolve metadata for [permission], falling back to a humanized name and lock icon. */
    fun forPermission(permission: String): PermissionVisual = known[permission] ?: PermissionVisual(
        permission.substringAfterLast('.').replace('_', ' ').lowercase()
            .replaceFirstChar { it.titlecase() },
        Icons.Default.Lock,
    )
}
