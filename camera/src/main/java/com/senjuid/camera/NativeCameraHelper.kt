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


class NativeCameraHelper(private val imageFileManager: ImageFileManager, private val cordova: CordovaInterface? = null) : ContextWrapper(imageFileManager.baseContext) {

    private lateinit var imageFile: File
    private var listener: CameraPluginListener? = null

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 2310
    }

    fun open(cameraPluginOptions: CameraPluginOptions) {
        println("Babay 1")
        try {
            println("Babay 2")
            imageFile = File.createTempFile(imageFileManager.generateFileName(cameraPluginOptions.name), ".png", imageFileManager.getDir())
        } catch (e: Exception) {
            println("Babay 3")
            return
        }

        println("Babay 4")

        val imageUri = if (Build.VERSION_CODES.N <= Build.VERSION.SDK_INT) {
            println("Babay 5")
            FileProvider.getUriForFile(baseContext, "${baseContext.packageName}.provider", imageFile)
        } else {
            println("Babay 6")
            Uri.fromFile(imageFile)
        }

        println("Babay 7")

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        println("Babay 8")

        intent.resolveActivity(packageManager)?.let {
            println("Babay 9")
//            if (baseContext is AppCompatActivity) {
//                println("Babay 10")
//                val startForResult =
//                        (imageFileManager.baseContext as AppCompatActivity).prepareCall(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
//                            println("Babay 11")
//                            onActivityResult(REQUEST_IMAGE_CAPTURE, result.resultCode, result.data)
//                        }
//                println("Babay 12")
//                startForResult(intent)
//            } else {
                println("Babay 13 $baseContext ${(baseContext as Activity)}")
//                (baseContext as Activity).startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            cordova!!.getActivity().startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
//            }
        }
    }

    fun setCameraPluginListener(listener: CameraPluginListener?) {
        println("Babay 14")
        this.listener = listener
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        println("Babay 15 $listener $imageFile ${imageFile.absolutePath}")
        if (listener != null && imageFile != null && imageFile.exists()) {
            listener?.onSuccess(imageFile.absolutePath)
        }
    }
}
