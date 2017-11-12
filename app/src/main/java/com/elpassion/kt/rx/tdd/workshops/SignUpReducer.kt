package com.elpassion.kt.rx.tdd.workshops

import io.reactivex.Observable

class SignUpReducer(private val loginReducer: (Observable<Any>) -> Observable<SignUp.LoginValidation.State>) : (Observable<Any>) -> Observable<SignUp.State> {

    override fun invoke(events: Observable<Any>): Observable<SignUp.State> {
        return loginReducer.invoke(events).map { SignUp.State(it) }
    }
}