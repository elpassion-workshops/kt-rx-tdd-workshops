package com.elpassion.kt.rx.tdd.workshops.signup

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.elpassion.kt.rx.tdd.workshops.R
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.components.RxActivity
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.sign_up_activity.*

class SignUpActivity : RxActivity() {

    val reducer = SignUpReducer(loginApi, object : Camera {
        override fun call(): Single<String> {
            return Single.error(Throwable())
        }
    }, object : System {
        override fun cameraPermission(): Single<Boolean> {
            return Single.error(Throwable())
        }
    }, ioScheduler)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_activity)

        val events = loginInput.textChanges()
                .map<Any> { SignUp.LoginValidation.LoginChangedEvent(it.toString()) }
                .share()

        reducer.invoke(events)
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(this)
                .subscribe {
                    loginValidationIndicator.text = it.loginValidation.toString()
                }

    }

    companion object {
        lateinit var loginApi: LoginApi
        lateinit var ioScheduler: Scheduler
    }
}
