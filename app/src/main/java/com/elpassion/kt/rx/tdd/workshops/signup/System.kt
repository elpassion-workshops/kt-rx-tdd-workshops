package com.elpassion.kt.rx.tdd.workshops.signup

import io.reactivex.Single

interface System {
    fun cameraPermission(): Single<Boolean>
}