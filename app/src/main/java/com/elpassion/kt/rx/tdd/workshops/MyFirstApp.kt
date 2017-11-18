package com.elpassion.kt.rx.tdd.workshops

import android.app.Application
import com.elpassion.kt.rx.tdd.workshops.common.CurrentActivityProvider
import com.elpassion.kt.rx.tdd.workshops.common.requestCameraPermission
import com.elpassion.kt.rx.tdd.workshops.common.requestPhoto
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

class MyFirstApp : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(CurrentActivityProvider)
        SignUp.debounceScheduler = AndroidSchedulers.mainThread()
        SignUp.api = { Single.just(true) }
        SignUp.camera = { requestPhoto() }
        SignUp.permissions = { requestCameraPermission() }
    }
}
