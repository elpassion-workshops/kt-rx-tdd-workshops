package com.elpassion.kt.rx.tdd.workshops

import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables.combineLatest

class SignUpReducer(
        private val loginReducer: Reducer<SignUp.LoginValidation.State>,
        private val photoReducer: Reducer<SignUp.Photo.State>
) : Reducer<SignUp.State> {

    override fun invoke(events: Observable<Any>) =
            combineLatest(loginReducer(events), photoReducer(events), SignUp::State)
}