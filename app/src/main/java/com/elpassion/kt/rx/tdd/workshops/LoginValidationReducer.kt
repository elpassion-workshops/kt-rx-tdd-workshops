package com.elpassion.kt.rx.tdd.workshops

import io.reactivex.Observable

class LoginValidationReducer(private val api: SignUp.LoginValidation.Api) : Reducer<SignUp.LoginValidation.State> {
    override fun invoke(events: Observable<Any>): Observable<SignUp.LoginValidation.State> {
        return events
                .ofType(SignUp.LoginValidation.LoginChangedEvent::class.java)
                .switchMap {
                    if (it.login.isEmpty()) {
                        Observable.just(SignUp.LoginValidation.State.IDLE)
                    } else {
                        api.call(it.login)
                                .map { if (it) SignUp.LoginValidation.State.LOGIN_OK else SignUp.LoginValidation.State.LOGIN_TAKEN }
                                .toObservable()
                                .onErrorReturnItem(SignUp.LoginValidation.State.API_ERROR)
                                .startWith(SignUp.LoginValidation.State.LOADING)
                    }
                }
                .startWith(SignUp.LoginValidation.State.IDLE)
    }
}