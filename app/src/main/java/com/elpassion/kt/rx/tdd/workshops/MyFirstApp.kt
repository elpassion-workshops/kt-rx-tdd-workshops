package com.elpassion.kt.rx.tdd.workshops

import android.app.Application
import com.elpassion.kt.rx.tdd.workshops.common.CurrentActivityProvider
import com.elpassion.kt.rx.tdd.workshops.common.requestCameraPermission
import com.elpassion.kt.rx.tdd.workshops.common.requestPhoto
import io.reactivex.Observable
import io.reactivex.Single

class MyFirstApp : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(CurrentActivityProvider)
        SignUpDI.permission = ::requestCameraPermission
        SignUpDI.cameraApi = ::requestPhoto
        SignUpDI.api = { Single.just(true)}
    }


}
