package com.elpassion.kt.rx.tdd.workshops.signup

import android.os.Bundle
import com.elpassion.kt.rx.tdd.workshops.R
import com.jakewharton.rxbinding2.widget.textChanges
import com.trello.rxlifecycle2.components.RxActivity
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.sign_up_activity.*

class SignUpActivity : RxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_activity)
        SignUpReducer({ Single.never()}, { Maybe.never()}, {Single.never()}, AndroidSchedulers.mainThread())
                .invoke(singUpLogin.textChanges().map { SignUp.LoginValidation.LoginChangedEvent(it.toString()) })
                .subscribe {
                    when (it.loginValidation) {
                        SignUp.LoginValidation.State.IN_PROGRESS -> signUpProgress.setText(R.string.loading)
                    }
                }
    }
}
