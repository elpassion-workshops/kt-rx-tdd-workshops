package com.elpassion.kt.rx.tdd.workshops

import android.app.Application
import com.elpassion.kt.rx.tdd.workshops.common.CurrentActivityProvider
import com.elpassion.kt.rx.tdd.workshops.common.requestCameraPermission
import com.elpassion.kt.rx.tdd.workshops.common.requestPhoto
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.Companion.events
import com.elpassion.kt.rx.tdd.workshops.signup.SignUpReducer
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

        events.ofType(CreateReducer::class.java)
                .switchMap {
                    SignUpReducer(SignUp.api, SignUp.camera, SignUp.permissions, SignUp.debounceScheduler)
                            .invoke(SignUp.events)
                }
                .takeUntil(events.ofType(DestroyReducer::class.java))
                .subscribe(SignUp.state)
    }

    class CreateReducer

    class DestroyReducer
}
