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

        loginInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable) {
                loginIndicator.setText(R.string.login_indicator_loading)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })
    }
}
