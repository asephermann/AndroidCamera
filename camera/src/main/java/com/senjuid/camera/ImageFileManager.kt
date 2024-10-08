package com.senjuid.camera

import android.content.Context
import android.content.ContextWrapper
import java.io.File
import java.util.*


interface IImageFilerManager {
    fun createDir(): Boolean;
    fun getDir(): File
    fun generateFileName(prefix: String?): String
}

class ImageFileManager(context: Context) : IImageFilerManager, ContextWrapper(context) {

    private var dirPath: String = "${baseContext.externalCacheDir?.path}/GreatDayHR"

    override fun createDir(): Boolean {
        val dir = File(dirPath)
        if (!dir?.exists()) {
            dir?.mkdirs()
            return true
        }
        return false
    }

    override fun getDir(): File {
        return File(dirPath)
    }

    override fun generateFileName(prefix: String?): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = if (calendar.get(Calendar.MONTH) + 1 < 10) {
            "0" + (calendar.get(Calendar.MONTH) + 1)
        } else {
            "" + (calendar.get(Calendar.MONTH) + 1)
        }
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return if (prefix?.isNullOrEmpty() == true) {
            "${"img_default_"}$year$month$day${"_"}${System.currentTimeMillis()}"
        } else {
            "$prefix${"_"}$year$month$day${"_"}${System.currentTimeMillis()}"
        }
    }
}