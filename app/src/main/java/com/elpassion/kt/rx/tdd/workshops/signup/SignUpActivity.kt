package com.elpassion.kt.rx.tdd.workshops.signup

import android.os.Bundle
import android.widget.Toast
import com.elpassion.kt.rx.tdd.workshops.R
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.trello.rxlifecycle2.components.RxActivity
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observables.ConnectableObservable
import kotlinx.android.synthetic.main.sign_up_activity.*

class SignUpActivity : RxActivity() {

    val reducer = SignUpReducer(loginApi, camera, system, ioScheduler)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_activity)

        val clickEvent = takePhoto.clicks()
                .map { SignUp.AddPhoto.TakePhotoEvent }

        val events = loginInput.textChanges()
                .map<Any> { SignUp.LoginValidation.LoginChangedEvent(it.toString()) }
                .mergeWith(clickEvent)
                .share()

        val reducerStateEvents = reducer.invoke(events)
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(this)
                .publish()

        reducerStateEvents.loginSth()
        reducerStateEvents.photoSth()

        reducerStateEvents.connect()

    }

    private fun ConnectableObservable<SignUp.State>.loginSth() = map { it.loginValidation }
            .distinctUntilChanged()
            .subscribe { loginValidationIndicator.text = it.toString() }

    private fun ConnectableObservable<SignUp.State>.photoSth() = map { it.addPhoto }
            .distinctUntilChanged()
            .subscribe {
                if (it is SignUp.AddPhoto.State.PhotoTaken) {
                    Toast.makeText(this@SignUpActivity, it.uri, Toast.LENGTH_LONG).show()
                }
            }

    companion object {
        lateinit var loginApi: LoginApi
        lateinit var ioScheduler: Scheduler
        lateinit var camera: Camera
        lateinit var system: System
    }

}
