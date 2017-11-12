package com.elpassion.kt.rx.tdd.workshops.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import android.support.media.ExifInterface
import android.widget.ImageView
import java.io.InputStream

fun ImageView.setImageFromStorage(uriString: String) =
        setImageFromStorage(Uri.parse(uriString))

fun ImageView.setImageFromStorage(uri: Uri) =
        setImageBitmap(context.loadImageFromStorage(uri))

private fun Context.loadImageFromStorage(uri: Uri) =
        loadImageFromStorage({ contentResolver.openInputStream(uri) })

private fun loadImageFromStorage(inputStream: () -> InputStream): Bitmap {
    val rotation = ExifInterface(inputStream()).rotation()
    return BitmapFactory.decodeStream(inputStream(), Rect(), options()).rotate(rotation)
}

private fun options() = BitmapFactory.Options().apply { this.inSampleSize = 8 }

fun ExifInterface.rotation(): Float {
    val orientation = getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
}

private fun Bitmap.rotate(rotation: Float): Bitmap {
    return if (rotation == 0f) {
        this
    } else {
        val matrix = Matrix()
        matrix.postRotate(rotation)
        Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }
}