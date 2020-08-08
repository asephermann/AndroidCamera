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
            data?.toBitmap(maxSize!!, maxSize!!) {
                bitmapResult = it
                callback(bitmapResult)
            }
        } else {
            data?.toBitmap {
                bitmapResult = it
                callback(bitmapResult)
            }
        }
    }

    fun saveBitmapAndFinish(intent: Intent, facing: Facing, callback: (String?) -> Unit) {
        bitmapResult?.let {
            var bmp = it
            CoroutineScope(Dispatchers.IO).launch {
                // Mirroring option
                val snapshot = intent.getBooleanExtra("is_snapshot", true)
                var disableMirror = intent.getBooleanExtra("disable_mirror", true)
                if (facing == Facing.FRONT && disableMirror!! && !snapshot) {
                    bmp = it.flip(-1f, 1f, it.width / 2f, it.height / 2f)
                }

                // Save picture to sdcard
                var compress = intent.getIntExtra("quality", 100)
                val prefix = intent.getStringExtra("name")
                val fileName = imageFileManager.generateFileName(prefix)
                val file = File(imageFileManager.getDir(), "$fileName.png")
                val fileOutputStream = FileOutputStream(file)
                bmp.compress(Bitmap.CompressFormat.JPEG, compress!!, fileOutputStream)

                withContext(Dispatchers.Main) {
                    callback(file?.absolutePath)
                }
            }
        }
    }

    private fun Bitmap.flip(x: Float, y: Float, cx: Float, cy: Float): Bitmap {
        val matrix = Matrix().apply { postScale(x, y, cx, cy) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }
}