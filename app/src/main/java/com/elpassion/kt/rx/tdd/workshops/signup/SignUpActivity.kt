package com.elpassion.kt.rx.tdd.workshops.signup

import android.os.Bundle
import com.elpassion.kt.rx.tdd.workshops.R
import com.trello.rxlifecycle2.components.RxActivity

class SignUpActivity : RxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_activity)

    }
}
