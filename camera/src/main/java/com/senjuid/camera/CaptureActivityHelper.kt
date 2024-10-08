package com.senjuid.camera

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Facing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


class CaptureActivityHelper(private val imageFileManager: ImageFileManager) {
    private var bitmapResult: Bitmap? = null

    fun pictureResultHandler(data: PictureResult, maxSize: Int, callback: (Bitmap?) -> Unit) {
        if (maxSize > 0) {
            data.toBitmap(maxSize, maxSize) {
                bitmapResult = it
                callback(bitmapResult)
            }
        } else {
            data.toBitmap {
                bitmapResult = it
                callback(bitmapResult)
            }
        }
    }

    fun saveBitmapAndFinish(intent: Intent, callback: (String?) -> Unit) {
        bitmapResult?.let {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val bmp = it

                    // Save picture to sdcard
                    val compress = intent.getIntExtra("quality", 100)
                    val prefix = intent.getStringExtra("name")
                    val fileName = imageFileManager.generateFileName(prefix)
                    val file = File(imageFileManager.getDir(), "$fileName.png")
                    val fileOutputStream = FileOutputStream(file)
                    bmp.compress(Bitmap.CompressFormat.JPEG, compress, fileOutputStream)

                    withContext(Dispatchers.Main) {
                        callback(file.absolutePath)
                    }
                } catch (e: Exception) {
                    // Handle any exceptions
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        callback(null)
                    }
                }
            }
        }
    }

    fun rotate(angle: Float, callback: (Bitmap?) -> Unit) {
        try {
            val matrix = Matrix()
            matrix.postRotate(angle)
            bitmapResult = Bitmap.createBitmap(bitmapResult!!, 0, 0, bitmapResult!!.width, bitmapResult!!.height, matrix, false)
            callback(bitmapResult)
        } catch (ex: Exception) {
            ex.printStackTrace()
            callback(null)
        }
    }

    fun flip(x: Float, y: Float, callback: (Bitmap?) -> Unit) {
        try {
            val matrix = Matrix().apply { postScale(x, y, bitmapResult!!.width / 2f, bitmapResult!!.height / 2f) }
            bitmapResult = Bitmap.createBitmap(bitmapResult!!, 0, 0, bitmapResult!!.width, bitmapResult!!.height, matrix, true)
            callback(bitmapResult)
        } catch (ex: Exception) {
            ex.printStackTrace()
            callback(null)
        }
    }

    private fun Bitmap.flip(x: Float, y: Float, cx: Float, cy: Float): Bitmap {
        val matrix = Matrix().apply { postScale(x, y, cx, cy) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }
}