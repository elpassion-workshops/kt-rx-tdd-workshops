package com.elpassion.kt.rx.tdd.workshops

import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables.combineLatest

class SignUpReducer(
        private val loginReducer: (Observable<Any>) -> Observable<SignUp.LoginValidation.State>,
        private val photoReducer: (Observable<Any>) -> Observable<SignUp.Photo.State>
) : (Observable<Any>) -> Observable<SignUp.State> {

    override fun invoke(events: Observable<Any>) =
            combineLatest(loginReducer(events), photoReducer(events), SignUp::State)
}