package com.elpassion.kt.rx.tdd.workshops

import android.app.Application
import com.elpassion.kt.rx.tdd.workshops.common.CurrentActivityProvider
import com.elpassion.kt.rx.tdd.workshops.common.requestCameraPermission
import com.elpassion.kt.rx.tdd.workshops.common.requestPhoto
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp
import io.reactivex.Single

class MyFirstApp : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(CurrentActivityProvider)

        SignUp.camera = ::requestPhoto
        SignUp.cameraPermission = ::requestCameraPermission
        SignUp.loginApi = object : () -> Single<Boolean> {
            override fun invoke(): Single<Boolean> {
                return Single.just(true)
            }

        }

    }


}
