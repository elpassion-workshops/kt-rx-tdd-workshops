package com.elpassion.kt.rx.tdd.workshops

import android.os.Bundle
import com.elpassion.kt.rx.tdd.workshops.SignUpDI.signUpReducer
import com.trello.rxlifecycle2.components.RxActivity
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Observable

class SignUpActivity : RxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signUpReducer(events())
                .bindToLifecycle(this)
                .handleStates()
    }

    private fun events(): Observable<SignUp.Event> {
        throw RuntimeException()
    }

    private fun Observable<SignUp.State>.handleStates() {
        subscribe()
    }
}
