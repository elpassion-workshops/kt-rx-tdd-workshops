package com.elpassion.kt.rx.tdd.workshops.signup

import android.os.Bundle
import com.elpassion.kt.rx.tdd.workshops.R
import com.elpassion.kt.rx.tdd.workshops.signup.SignUpDI.api
import com.jakewharton.rxbinding2.widget.textChanges
import com.trello.rxlifecycle2.components.RxActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.sign_up_activity.*

class SignUpActivity : RxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_activity)
        SignUpReducer(api, { throw RuntimeException() }, { throw RuntimeException() })
                .invoke(loginInput.textChanges().map { SignUp.LoginValidation.LoginChangedEvent(it.toString()) })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    when (it.loginValidation) {
                        SignUp.LoginValidation.State.IDLE -> loginValidationIndicator.setText(R.string.login_validation_idle)
                        SignUp.LoginValidation.State.LOADING -> loginValidationIndicator.setText(R.string.login_validation_loading)
                        SignUp.LoginValidation.State.LOGIN_AVAILABLE -> loginValidationIndicator.setText(R.string.login_validation_available)
                        SignUp.LoginValidation.State.LOGIN_TAKEN -> loginValidationIndicator.setText(R.string.login_validation_taken)
                        SignUp.LoginValidation.State.ERROR -> loginValidationIndicator.setText(R.string.login_validation_error)
                    }
                }
    }
}
