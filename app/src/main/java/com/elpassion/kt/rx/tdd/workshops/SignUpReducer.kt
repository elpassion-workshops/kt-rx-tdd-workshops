package com.elpassion.kt.rx.tdd.workshops

import io.reactivex.Observable

class SignUpReducer : (Observable<Any>) -> Observable<SignUp.State> {
    override fun invoke(events: Observable<Any>): Observable<SignUp.State> {
        throw RuntimeException()
    }
}