package com.elpassion.kt.rx.tdd.workshops.signup

import android.os.Bundle
import com.elpassion.kt.rx.tdd.workshops.R
import com.jakewharton.rxbinding2.widget.textChanges
import com.trello.rxlifecycle2.components.RxActivity
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.sign_up_activity.*

class SignUpActivity : RxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_activity)
        val reducer = SignUpReducer(SignUp.api,{ Maybe.never()},{ Single.never()})
        reducer.invoke(uiEvents())
                .bindToLifecycle(this)
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.loginValidation }
                .subscribe {
                    when (it) {
                        SignUp.LoginValidation.State.AVAILABLE -> login_validation_label.setText(R.string.loginValidationAvailable)
                        SignUp.LoginValidation.State.IDLE -> login_validation_label.setText(R.string.loginValidationIdle)
                        else -> login_validation_label.setText(R.string.loginValidationLoading)
                    }
                }

    }

    private fun uiEvents(): Observable<Any> {
        return login_input.textChanges().map { SignUp.LoginValidation.LoginChangedEvent(it.toString()) }
    }
}
