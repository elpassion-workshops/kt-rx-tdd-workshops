package com.elpassion.kt.rx.tdd.workshops

import android.app.Application
import com.elpassion.kt.rx.tdd.workshops.common.CurrentActivityProvider
import com.elpassion.kt.rx.tdd.workshops.common.requestCameraPermission
import com.elpassion.kt.rx.tdd.workshops.common.requestPhoto
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp
import io.reactivex.Single
import java.util.*
import java.util.concurrent.TimeUnit

class MyFirstApp : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(CurrentActivityProvider)
        SignUp.api= { Single.just(Random().nextBoolean()).delay(1,TimeUnit.SECONDS)}
        SignUp.cameraApi = ::requestPhoto
        SignUp.permissionApi = ::requestCameraPermission
    }
}
