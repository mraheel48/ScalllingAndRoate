package com.example.scalllingandroate

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File
import java.io.FileInputStream

object Util {

    //******************this method return bitmapt to local path *******************//
    @JvmStatic
    fun getBitmapOrg(path: String): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val f = File(path)
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            bitmap = BitmapFactory.decodeStream(FileInputStream(f), null, options)
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        return bitmap
    }

    fun scaleDown(
        realImage: Bitmap?, maxImageSize: Float,
        filter: Boolean
    ): Bitmap? {

        if (realImage != null) {

            Log.e("myBitmapVal", "${realImage.width} -- ${realImage.height}")

            return if (realImage.width > maxImageSize || realImage.height > maxImageSize) {

                val ratio =
                    Math.min(maxImageSize / realImage.width, maxImageSize / realImage.height)
                val width = Math.round(ratio * realImage.width)
                val height = Math.round(ratio * realImage.height)

                return Bitmap.createScaledBitmap(
                    realImage, width,
                    height, filter
                )
            } else {
                realImage
            }
        } else {
            return null
        }

    }

    //This method Clear garbage collection
    //Developers can call System.gc()
    // anywhere in their code to instruct the JVM to prioritize garbage collection.
    // When a developer calls this method
    // -- and there isn't an extreme load on the JVM -- a Java GC cycle will happen within seconds
    @JvmStatic
    fun clearGarbageCollection() {
        try {
            System.gc()
            Runtime.getRuntime().gc()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

}