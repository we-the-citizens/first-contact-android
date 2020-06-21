// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.utils

import android.Manifest
import android.content.Context
import pub.devrel.easypermissions.EasyPermissions

object PermissionUtils {

    fun cameraRequiredPermissions() = arrayOf(Manifest.permission.CAMERA)

    fun hasCameraPermission(context: Context): Boolean {
        val cameraPermission = cameraRequiredPermissions()
        return EasyPermissions.hasPermissions(context, *cameraPermission)
    }
}