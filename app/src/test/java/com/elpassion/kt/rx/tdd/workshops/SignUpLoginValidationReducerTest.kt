package com.elpassion.kt.rx.tdd.workshops

import com.elpassion.kt.rx.tdd.workshops.SignUp.LoginValidation.LoginChangedEvent
import com.elpassion.kt.rx.tdd.workshops.SignUp.LoginValidation.State
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import junit.framework.Assert
import org.junit.Test

class SignUpLoginValidationReducerTest {

    private val events = PublishRelay.create<Any>()
    private val reducer = LoginValidationReducer()
    private val state = reducer.invoke(events).test()

    @Test
    fun shouldBeIdleAtTheBegging() {
        state.assertValue(State.IDLE)
    }

    @Test
    fun shouldBeInProgressWhenNotEmptyLoginArrives() {
        events.accept(LoginChangedEvent("login"))
        state.assertLastValue(State.LOADING)
    }

    @Test
    fun shouldBeIdleAfterErasingLogin() {
        events.accept(LoginChangedEvent("login"))
        events.accept(LoginChangedEvent(""))
        state.assertLastValue(State.IDLE)
    }

    class LoginValidationReducer : (Observable<Any>) -> Observable<State> {
        override fun invoke(events: Observable<Any>): Observable<State> {
            return events
                    .ofType(LoginChangedEvent::class.java)
                    .map { if (it.login.isEmpty()) State.IDLE else State.LOADING }
                    .startWith(State.IDLE)
        }
    }

    fun <T> TestObserver<T>.assertLastValue(expected: T) {
        Assert.assertEquals(expected, values().last())
    }
}
