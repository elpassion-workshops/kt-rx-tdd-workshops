package com.elpassion.kt.rx.tdd.workshops.common

import android.Manifest
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Single

fun requestCameraPermission() = requestPermissions(Manifest.permission.CAMERA)

private fun requestPermissions(vararg permissions: String): Single<Boolean> =
        CurrentActivityProvider()
                .flatMapObservable {
                    RxPermissions(it).request(*permissions)
                }
                .firstOrError()