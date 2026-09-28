package com.apexfission.android.permission

import android.Manifest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import com.apexfission.android.permission.ui.PermissionDescription
import com.apexfission.android.permission.ui.PermissionVisualDefaults
import org.junit.Assert.assertEquals
import org.junit.Test

class PermissionVisualDefaultsTest {
    @Test fun commonPermissionsHaveSpecificMetadata() {
        assertEquals("Camera", (PermissionDescription(Manifest.permission.CAMERA) {}).label)
        assertEquals(Icons.Default.PhotoCamera, (PermissionDescription(Manifest.permission.CAMERA) {}).icon)
        assertEquals("Microphone", (PermissionDescription(Manifest.permission.RECORD_AUDIO) {}).label)
        assertEquals(Icons.Default.Mic, (PermissionDescription(Manifest.permission.RECORD_AUDIO) {}).icon)
        assertEquals("Precise location", (PermissionDescription(Manifest.permission.ACCESS_FINE_LOCATION) {}).label)
        assertEquals("Notifications", (PermissionDescription(Manifest.permission.POST_NOTIFICATIONS) {}).label)
    }

    @Test fun unknownPermissionsKeepReadableFallback() {
        val description = PermissionDescription("com.example.permission.READ_SPECIAL_DATA") {}
        assertEquals("Read special data", description.label)
        assertEquals(Icons.Default.Lock, description.icon)
    }

    @Test fun callerCanOverrideEitherPartIndependently() {
        val customLabel = PermissionDescription(Manifest.permission.CAMERA, label = "Document scanner") {}
        assertEquals("Document scanner", customLabel.label)
        assertEquals(Icons.Default.PhotoCamera, customLabel.icon)

        val customIcon = PermissionDescription(Manifest.permission.CAMERA, icon = Icons.Default.Mic) {}
        assertEquals("Camera", customIcon.label)
        assertEquals(Icons.Default.Mic, customIcon.icon)
    }

    @Test fun defaultsAreAvailableToCustomPages() {
        val visual = PermissionVisualDefaults.forPermission(Manifest.permission.RECORD_AUDIO)
        assertEquals("Microphone", visual.label)
        assertEquals(Icons.Default.Mic, visual.icon)
    }
}
