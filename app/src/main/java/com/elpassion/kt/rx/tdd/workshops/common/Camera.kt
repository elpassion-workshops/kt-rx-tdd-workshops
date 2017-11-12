package com.elpassion.kt.rx.tdd.workshops.common

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import com.elpassion.kt.rx.tdd.workshops.BuildConfig
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import io.reactivex.Maybe
import io.reactivex.subjects.MaybeSubject
import java.io.File
import java.util.*
import kotlin.properties.Delegates

private const val TAG = "RxPhoto"
private const val START_FOR_PHOTO_CODE = 8325
private var photoSubject by Delegates.notNull<MaybeSubject<String>>()
private var uri by Delegates.notNull<Uri>()

fun requestPhoto(): Maybe<String> {
    photoSubject = MaybeSubject.create<String>()
    CurrentActivityProvider()
            .subscribe { it: Activity ->
                uri = it.createImageFileUri()
                takePhoto(it)
            }
    return photoSubject
}

private fun takePhoto(activity: Activity) {
    val rxCameraFragment = findFragment(activity) ?: crateAndAddFragment(activity)
    rxCameraFragment.takePhoto()
}

private fun findFragment(activity: Activity) =
        activity.fragmentManager.findFragmentByTag(TAG) as RxCameraFragment?

private fun crateAndAddFragment(activity: Activity): RxCameraFragment {
    return RxCameraFragment().apply {
        activity.fragmentManager.let {
            it
                    .beginTransaction()
                    .add(this, TAG)
                    .commitAllowingStateLoss()
            it.executePendingTransactions()
        }
    }
}

private fun Context.createImageFileUri(): Uri {
    val directory = File(filesDir, "photos").apply { mkdir() }
    val file = File(directory, UUID.randomUUID().toString())
    return FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, file)
}

class RxCameraFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    fun takePhoto() {
        val cameraIntent = createStartCameraIntent()
        startActivityForResult(cameraIntent, START_FOR_PHOTO_CODE)
    }

    private fun createStartCameraIntent() =
            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    .putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == START_FOR_PHOTO_CODE) {
            if (resultCode == RxAppCompatActivity.RESULT_OK) {
                photoSubject.onSuccess(uri.toString())
            } else {
                photoSubject.onComplete()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}