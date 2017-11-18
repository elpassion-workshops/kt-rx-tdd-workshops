package com.elpassion.kt.rx.tdd.workshops.signup

import android.os.Bundle
import com.elpassion.kt.rx.tdd.workshops.R
import com.jakewharton.rxbinding2.widget.textChanges
import com.trello.rxlifecycle2.components.RxActivity
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.android.synthetic.main.sign_up_activity.*

class SignUpActivity : RxActivity() {

    val reducer = SignUpReducer({ Single.never()},{ Maybe.never()},{ Single.never()})

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_activity)
        reducer.invoke(uiEvents())
                .bindToLifecycle(this)
                .map { it.loginValidation }
                .subscribe {
                    if(it == SignUp.LoginValidation.State.IDLE){
                        login_validation_label.setText(R.string.loginValidationIdle)
                    }else{
                        login_validation_label.setText(R.string.loginValidationLoading)
                    }
                }

    }

    private fun uiEvents(): Observable<Any> {
        return login_input.textChanges().map { SignUp.LoginValidation.LoginChangedEvent(it.toString()) }
    }
}
