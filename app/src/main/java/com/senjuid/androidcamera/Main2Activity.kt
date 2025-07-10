package com.senjuid.androidcamera

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.senjuid.androidcamera.databinding.ActivityMain2Binding
import com.senjuid.camera.CameraPlugin
import com.senjuid.camera.CameraPluginListener
import com.senjuid.camera.CameraPluginOptions
import java.io.File


class Main2Activity : AppCompatActivity() {

    private var cameraPlugin: CameraPlugin? = null
    private lateinit var binding: ActivityMain2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraPlugin = CameraPlugin(this)
        cameraPlugin?.setCameraPluginListener(object : CameraPluginListener {
            override fun onSuccess(photoPath: String, native: Boolean, crash: Boolean) {
                if (native) {
                    Toast.makeText(this@Main2Activity, "Canceled Native", Toast.LENGTH_LONG).show()
                } else {
                    if (photoPath != "") {
                        showImage(photoPath)
                    } else {
                        Toast.makeText(this@Main2Activity, "photoPath is Empty", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }

            override fun onCancel() {
                Toast.makeText(this@Main2Activity, "Canceled", Toast.LENGTH_LONG).show()
            }
        })

        binding.buttonClick.setOnClickListener {
            val quality = binding.etQuality.text.toString()
            val maxSize = binding.etMaxSize.text.toString()
            if (quality.isEmpty()) {
                Toast.makeText(
                    this@Main2Activity,
                    "Please input image quality (1 - 100).",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            if (maxSize.isEmpty()) {
                Toast.makeText(
                    this@Main2Activity,
                    "Please input image maximum size.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val options = CameraPluginOptions.Builder()
                .setCameraSource("Test Camera")
                .setMaxSize(maxSize.toInt())
                .setQuality(quality.toInt())
                .setDisableFacingBack(false)
                .setDisablePreview(true)
                .setDisableMirroring(true)
                .setIsFacingBack(false)
                .setShowFaceArea(true)
                .setShowNativeCameraButton(true)
                .build()
            cameraPlugin?.open(options)
        }
    }

    fun showImage(imagePath: String) {
        val imgFile = File(imagePath)
        if (imgFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
            binding.ivPreview.setImageBitmap(bitmap)
        }
    }

    /** Uncomment if the activity isn't androidx.appcompat.app.AppCompatActivity */
    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        cameraPlugin?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }*/
}
