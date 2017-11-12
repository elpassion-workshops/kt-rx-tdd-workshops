package com.elpassion.kt.rx.tdd.workshops

import com.elpassion.kt.rx.tdd.workshops.SignUp.LoginValidation.Event
import com.elpassion.kt.rx.tdd.workshops.SignUp.LoginValidation.State
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.junit.Test

class SignUpLoginValidationReducerTest {

    private val events = PublishRelay.create<Event>()
    private val reducer = LoginValidationReducer()
    private val state = reducer.invoke(events).test()

    @Test
    fun shouldBeIdleAtTheBegging() {
        state.assertValue(State)
    }

    class LoginValidationReducer : (Observable<Event>) -> Observable<State> {
        override fun invoke(events: Observable<Event>): Observable<State> {
            return Observable.just(State)
        }
    }
}
