package com.elpassion.kt.rx.tdd.workshops.signup

import android.os.Bundle
import com.elpassion.kt.rx.tdd.workshops.R
import com.jakewharton.rxbinding2.widget.textChanges
import com.trello.rxlifecycle2.components.RxActivity
import io.reactivex.Maybe
import io.reactivex.Single
import kotlinx.android.synthetic.main.sign_up_activity.*

class SignUpActivity : RxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_activity)
        SignUpReducer(SignUp.api, { Maybe.never()}, {Single.never()}, SignUp.debounceScheduler)
                .invoke(singUpLogin.textChanges().map { SignUp.LoginValidation.LoginChangedEvent(it.toString()) })
                .subscribe {
                    when (it.loginValidation) {
                        SignUp.LoginValidation.State.IDLE -> signUpProgress.setText(R.string.idle)
                        SignUp.LoginValidation.State.IN_PROGRESS -> signUpProgress.setText(R.string.loading)
                        SignUp.LoginValidation.State.AVAILABLE -> signUpProgress.setText(R.string.available)
                        SignUp.LoginValidation.State.TAKEN -> signUpProgress.setText(R.string.taken)
                        SignUp.LoginValidation.State.ERROR -> signUpProgress.setText(R.string.api_error)
                    }
                }
    }
}
