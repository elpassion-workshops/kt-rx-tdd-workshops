package com.elpassion.kt.rx.tdd.workshops

import com.elpassion.kt.rx.tdd.workshops.SignUp.LoginValidation.LoginChangedEvent
import com.elpassion.kt.rx.tdd.workshops.SignUp.LoginValidation.State
import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.SingleSubject
import junit.framework.Assert
import org.junit.Test

class SignUpLoginValidationReducerTest {

    private val apiSubject = SingleSubject.create<Boolean>()
    private val events = PublishRelay.create<Any>()
    private val api = mock<SignUp.LoginValidation.Api> { on { call("login") } doReturn (apiSubject) }
    private val reducer = LoginValidationReducer(api)
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

    @Test
    fun shouldReturnLoginOkWhenApiPasses() {
        events.accept(LoginChangedEvent("login"))
        apiSubject.onSuccess(true)
        state.assertLastValue(State.LOGIN_OK)
    }

    @Test
    fun shouldCallApiWithProperArgument() {
        events.accept(LoginChangedEvent("login"))
        verify(api).call("login")
    }

    class LoginValidationReducer(private val api: SignUp.LoginValidation.Api) : (Observable<Any>) -> Observable<State> {
        override fun invoke(events: Observable<Any>): Observable<State> {
            return events
                    .ofType(LoginChangedEvent::class.java)
                    .switchMap {
                        if (it.login.isEmpty()) {
                            Observable.just(State.IDLE)
                        } else {
                            api.call(it.login)
                                    .map { State.LOGIN_OK }
                                    .toObservable()
                                    .startWith(State.LOADING)
                        }
                    }
                    .startWith(State.IDLE)
        }
    }

    fun <T> TestObserver<T>.assertLastValue(expected: T) {
        Assert.assertEquals(expected, values().last())
    }
}
