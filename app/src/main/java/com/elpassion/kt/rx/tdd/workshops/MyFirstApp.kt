package com.elpassion.kt.rx.tdd.workshops

import android.app.Application
import com.elpassion.kt.rx.tdd.workshops.common.CurrentActivityProvider
import com.elpassion.kt.rx.tdd.workshops.common.requestCameraPermission
import com.elpassion.kt.rx.tdd.workshops.common.requestPhoto
import com.elpassion.kt.rx.tdd.workshops.signup.Camera
import com.elpassion.kt.rx.tdd.workshops.signup.LoginApi
import com.elpassion.kt.rx.tdd.workshops.signup.SignUpActivity
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*

class MyFirstApp : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(CurrentActivityProvider)
        SignUpActivity.camera = object: Camera {
            override fun call(): Single<String> {
                return requestPhoto().toSingle()
            }
        }
        SignUpActivity.loginApi = object: LoginApi {
            override fun checkLogin(login: String): Single<Boolean> {
                return Single.just(Random().nextBoolean())
            }
        }
        SignUpActivity.system = object : com.elpassion.kt.rx.tdd.workshops.signup.System {
            override fun cameraPermission(): Single<Boolean> {
                return requestCameraPermission()
            }
        }
        SignUpActivity.ioScheduler = Schedulers.io()
    }

}
