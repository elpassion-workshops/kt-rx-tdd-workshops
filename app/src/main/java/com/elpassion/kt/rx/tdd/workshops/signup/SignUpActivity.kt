package com.elpassion.kt.rx.tdd.workshops.signup

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.elpassion.kt.rx.tdd.workshops.R
import com.trello.rxlifecycle2.components.RxActivity
import kotlinx.android.synthetic.main.sign_up_activity.*

class SignUpActivity : RxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_activity)

        loginInput.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loginValidationIndicator.text = SignUp.LoginValidation.State.IN_PROGRESS.toString()
            }
        })
    }
}
