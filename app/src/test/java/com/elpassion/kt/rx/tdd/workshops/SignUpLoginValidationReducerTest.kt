package com.elpassion.kt.rx.tdd.workshops

import com.elpassion.kt.rx.tdd.workshops.SignUp.LoginValidation.LoginChangedEvent
import com.elpassion.kt.rx.tdd.workshops.SignUp.LoginValidation.State
import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.subjects.SingleSubject
import org.junit.Test

class SignUpLoginValidationReducerTest {

    private val apiSubject = SingleSubject.create<Boolean>()
    private val events = PublishRelay.create<Any>()
    private val api = mock<SignUp.LoginValidation.Api> { on { call("login") } doReturn (apiSubject) }
    private val state = LoginValidationReducer(api).invoke(events).test()

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

    @Test
    fun shouldReturnLoginTakenWhenApiReturnsFalse() {
        events.accept(LoginChangedEvent("login"))
        apiSubject.onSuccess(false)
        state.assertLastValue(State.LOGIN_TAKEN)
    }

    @Test
    fun shouldReturnErrorWhenApiCallFailed() {
        events.accept(LoginChangedEvent("login"))
        apiSubject.onError(RuntimeException())
        state.assertLastValue(State.API_ERROR)
    }

}
