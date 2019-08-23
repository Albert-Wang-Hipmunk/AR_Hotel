/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ar.sceneform.samples.videorecording

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import com.google.ar.sceneform.ux.ArFragment

/**
 * Writing Ar Fragment extends the ArFragment class to include the WRITER_EXTERNAL_STORAGE
 * permission. This adds this permission to the list of permissions presented to the user for
 * granting.
 */
class WritingArFragment : ArFragment() {
    override fun getAdditionalPermissions(): Array<String?> {
        val additionalPermissions = super.getAdditionalPermissions()
        val permissionLength = additionalPermissions?.size ?: 0
        val permissions = arrayOfNulls<String>(permissionLength + 2)
        permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE
        permissions[1] = Manifest.permission.RECORD_AUDIO
        if (permissionLength > 0) {
            System.arraycopy(additionalPermissions!!, 0, permissions, 1, additionalPermissions.size)
        }
        return permissions
    }

    fun hasWritePermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
                this.requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    /** Launch Application Setting to grant permissions.  */
    fun launchPermissionSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", requireActivity().packageName, null)
        requireActivity().startActivity(intent)
    }
}
