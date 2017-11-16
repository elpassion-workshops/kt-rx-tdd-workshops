package com.elpassion.kt.rx.tdd.workshops

import android.app.Application
import com.elpassion.kt.rx.tdd.workshops.common.CurrentActivityProvider
import com.elpassion.kt.rx.tdd.workshops.common.requestCameraPermission
import com.elpassion.kt.rx.tdd.workshops.common.requestPhoto
import com.elpassion.kt.rx.tdd.workshops.signup.SignUpDI
import io.reactivex.Single
import java.util.*
import java.util.concurrent.TimeUnit

class MyFirstApp : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(CurrentActivityProvider)
        SignUpDI.permissionRequester = ::requestCameraPermission
        SignUpDI.camera = ::requestPhoto
        SignUpDI.api = { createApi() }
    }

    private fun createApi(): Single<Boolean> {
        val random = Random()
        return if (random.nextInt() % 10 < 7) {
            Single.just(random.nextInt() % 10 <= 6)
                    .delay(random.nextLong() % 100 + 100, TimeUnit.MILLISECONDS)
        } else {
            Single.error<Boolean>(RuntimeException())
                    .delay(random.nextLong() % 100 + 100, TimeUnit.MILLISECONDS)
        }
    }
}
