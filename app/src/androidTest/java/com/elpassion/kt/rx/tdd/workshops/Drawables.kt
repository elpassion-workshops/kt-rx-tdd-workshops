package com.elpassion.kt.rx.tdd.workshops

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.test.InstrumentationRegistry
import com.elpassion.kt.rx.tdd.workshops.common.createImageFileUri
import com.elpassion.kt.rx.tdd.workshops.utils.loadImageFromStorage
import java.io.OutputStream

fun createTestBitmap(): Pair<String, Bitmap> {
    val context = InstrumentationRegistry.getTargetContext()
    val icon: Bitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
    val uriString = context.save(icon)
    val bitmap = context.loadImageFromStorage(Uri.parse(uriString))
    return Pair(uriString, bitmap)
}

private fun Context.save(bitmap: Bitmap): String {
    val fileUri = createImageFileUri()
    contentResolver.openOutputStream(fileUri).writeBitmap(bitmap)
    return fileUri.toString()
}

private fun OutputStream.writeBitmap(bitmap: Bitmap) = use {
    bitmap.compress(Bitmap.CompressFormat.JPEG, 60, it)
}