package com.elpassion.kt.rx.tdd.workshops.signup

import android.os.Bundle
import android.util.Log
import com.elpassion.kt.rx.tdd.workshops.R
import com.elpassion.kt.rx.tdd.workshops.SignUpDI
import com.jakewharton.rxbinding2.widget.textChanges
import com.trello.rxlifecycle2.components.RxActivity
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.sign_up_activity.*

class SignUpActivity : RxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_activity)
        setupReducer()
    }

    private fun setupReducer() {
        SignUpReducer(SignUpDI.api, SignUpDI.cameraApi, SignUpDI.permission)
                .invoke(loginInput.textChanges().map { SignUp.LoginValidation.LoginChangedEvent(it.toString()) })
                .subscribeBy {
                    when (it.loginValidation) {
                        SignUp.LoginValidation.State.IN_PROGRESS -> {
                            indicator.setText(R.string.loading)
                        }
                        SignUp.LoginValidation.State.AVAILABLE -> {
                            indicator.setText(R.string.available)
                        }
                    }
                }
    }
}
