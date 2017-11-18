package com.elpassion.kt.rx.tdd.workshops.signup

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.elpassion.kt.rx.tdd.workshops.R
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.trello.rxlifecycle2.components.RxActivity
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.sign_up_activity.*

class SignUpActivity : RxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_activity)
        val reducer = SignUpReducer(loginApi, object : Camera {
            override fun call(): Single<String> {
                return Single.error(Throwable())
            }
        }, object : System {
            override fun cameraPermission(): Single<Boolean> {
                return Single.error(Throwable())
            }
        }, ioScheduler)

        val events: Observable<Any> = loginInput.textChanges()
                .map { SignUp.LoginValidation.LoginChangedEvent(it.toString()) }

        reducer.invoke(events)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    loginValidationIndicator.text = it.loginValidation.toString()
                })

    }

    companion object {
        lateinit var loginApi: LoginApi
        lateinit var ioScheduler: Scheduler
    }
}
