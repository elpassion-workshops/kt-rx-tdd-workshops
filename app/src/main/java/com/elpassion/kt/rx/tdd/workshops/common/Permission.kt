package com.elpassion.kt.rx.tdd.workshops.common

import android.Manifest
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Maybe

fun requestCameraPermission() = requestPermissions(Manifest.permission.CAMERA)

private fun requestPermissions(vararg permissions: String): Maybe<Unit> =
        CurrentActivityProvider()
                .flatMapObservable {
                    RxPermissions(it).request(*permissions)
                }
                .take(1)
                .filter { it }
                .map { Unit }
                .firstElement()