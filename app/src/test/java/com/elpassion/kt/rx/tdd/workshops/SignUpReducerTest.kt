package com.elpassion.kt.rx.tdd.workshops

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.junit.Test

class SignUpReducerTest {

    private val loginReducer: (Observable<Any>) -> Observable<SignUp.LoginValidation.State> = { Observable.just(SignUp.LoginValidation.State.IDLE)}
    private val events = PublishRelay.create<Any>()
    private val state = SignUpReducer(loginReducer).invoke(events).test()

    @Test
    fun shouldValidateLoginWithLoginReducer() {
        state.assertLastValueThat { loginValidation == SignUp.LoginValidation.State.IDLE }
    }
}