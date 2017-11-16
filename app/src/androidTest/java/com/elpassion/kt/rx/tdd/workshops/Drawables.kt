package com.elpassion.kt.rx.tdd.workshops

import android.content.Context
import android.graphics.Bitmap
import com.elpassion.kt.rx.tdd.workshops.common.createImageFileUri
import java.io.OutputStream

fun Context.save(bitmap: Bitmap): String {
    val fileUri = createImageFileUri()
    contentResolver.openOutputStream(fileUri).writeBitmap(bitmap)
    return fileUri.toString()
}

private fun OutputStream.writeBitmap(bitmap: Bitmap) = use {
    bitmap.compress(Bitmap.CompressFormat.JPEG, 60, it)
}