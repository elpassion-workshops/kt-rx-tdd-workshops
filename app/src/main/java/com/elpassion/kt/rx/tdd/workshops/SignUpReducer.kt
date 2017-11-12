package com.elpassion.kt.rx.tdd.workshops

import io.reactivex.Observable

class SignUpReducer : (Observable<SignUp.Event>) -> Observable<SignUp.State> {
    override fun invoke(events: Observable<SignUp.Event>): Observable<SignUp.State> {
        throw RuntimeException()
    }
}