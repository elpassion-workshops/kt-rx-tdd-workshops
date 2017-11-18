package com.elpassion.kt.rx.tdd.workshops.signup

import android.os.Bundle
import android.widget.TextView
import com.elpassion.kt.rx.tdd.workshops.R
import com.elpassion.kt.rx.tdd.workshops.SignUpDI
import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.jakewharton.rxbinding2.widget.textChanges
import com.trello.rxlifecycle2.components.RxActivity
import io.reactivex.Observable
import kotlinx.android.synthetic.main.sign_up_activity.*

class SignUpActivity : RxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_activity)
        SignUpReducer(SignUpDI.api, SignUpDI.cameraApi, SignUpDI.permission)
                .invoke(loginInput.textChanges().map { SignUp.LoginValidation.LoginChangedEvent(it.toString()) })
                .subscribe {
                    if (it.loginValidation == SignUp.LoginValidation.State.IN_PROGRESS) {
                        indicator.text = "loading"
                    }
                }
    }
}
