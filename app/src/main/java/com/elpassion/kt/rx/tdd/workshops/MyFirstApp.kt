package com.elpassion.kt.rx.tdd.workshops

import android.app.Application
import com.elpassion.kt.rx.tdd.workshops.common.CurrentActivityProvider
import com.elpassion.kt.rx.tdd.workshops.common.requestCameraPermission
import com.elpassion.kt.rx.tdd.workshops.common.requestPhoto
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp
import com.elpassion.kt.rx.tdd.workshops.signup.SignUpReducer
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MyFirstApp : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(CurrentActivityProvider)

        SignUp.camera = ::requestPhoto
        SignUp.cameraPermission = ::requestCameraPermission
        SignUp.loginApi = object : () -> Single<Boolean> {
            override fun invoke(): Single<Boolean> {
                return Single.just(true).delay(1, TimeUnit.SECONDS, Schedulers.io())
            }

        }

        SignUpReducer(SignUp.loginApi, SignUp.camera, SignUp.cameraPermission).invoke(SignUp.events).subscribe(SignUp.states)
    }
}
