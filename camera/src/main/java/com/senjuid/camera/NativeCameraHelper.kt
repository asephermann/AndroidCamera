package com.senjuid.camera

import android.app.Activity
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.invoke
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File


class NativeCameraHelper(private val imageFileManager: ImageFileManager) : ContextWrapper(imageFileManager.baseContext) {

    private lateinit var imageFile: File
    private var listener: CameraPluginListener? = null

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 2310
    }

    fun open(cameraPluginOptions: CameraPluginOptions) {
        try {
            imageFile = File.createTempFile(imageFileManager.generateFileName(cameraPluginOptions.name), ".png", imageFileManager.getDir())
        } catch (e: Exception) {
            return
        }

        val imageUri = if (Build.VERSION_CODES.N <= Build.VERSION.SDK_INT) {
            FileProvider.getUriForFile(baseContext, "${baseContext.packageName}.provider", imageFile)
        } else {
            Uri.fromFile(imageFile)
        }

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        intent.resolveActivity(packageManager)?.let {
            if (baseContext is AppCompatActivity) {
                val startForResult =
                        (imageFileManager.baseContext as AppCompatActivity).prepareCall(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                            onActivityResult(REQUEST_IMAGE_CAPTURE, result.resultCode, result.data)
                        }
                startForResult(intent)
            } else {
                (baseContext as Activity).startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    fun setCameraPluginListener(listener: CameraPluginListener?) {
        this.listener = listener
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (listener != null && imageFile != null && imageFile.exists()) {
            listener?.onSuccess(photoPath = imageFile.absolutePath, native = false, crash = false)
        }
    }
}
