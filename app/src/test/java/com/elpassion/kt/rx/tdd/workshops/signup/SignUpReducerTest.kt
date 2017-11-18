package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.*
import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import org.junit.Test

class SignUpReducerTest {

    private val apiSubject = SingleSubject.create<Boolean>()
    private val api = mock<(String) -> Single<Boolean>>().apply {
        whenever(this.invoke(any())).thenReturn(apiSubject)
    }
    private val events = PublishRelay.create<Any>()
    private val state = SignUpReducer(api).invoke(events).test()

    @Test
    fun shouldLoginValidationStateBeIdleOnStart() {
        state.assertLastValueThat { loginValidation == LoginValidation.State.IDLE }
    }

    @Test
    fun shouldLoginValidationStateBeInProgressAfterUserTypeLogin() {
        events.accept(LoginValidation.LoginChangedEvent("a"))
        state.assertLastValueThat { loginValidation == LoginValidation.State.IN_PROGRESS }
    }

    @Test
    fun shouldLoginValidationStateBeIdleAfterErasingLogin() {
        events.accept(LoginValidation.LoginChangedEvent("a"))
        events.accept(LoginValidation.LoginChangedEvent(""))
        state.assertLastValueThat { loginValidation == LoginValidation.State.IDLE }
    }

    @Test
    fun shouldLoginValidationStateBeAvailableWhenApiPasses() {
        events.accept(LoginValidation.LoginChangedEvent("a"))
        apiSubject.onSuccess(true)
        state.assertLastValueThat { loginValidation == LoginValidation.State.AVAILABLE }
    }

    @Test
    fun shouldLoginValidationStateBeNotAvailableWhenApiReturnsThatItIsTaken() {
        events.accept(LoginValidation.LoginChangedEvent("a"))
        apiSubject.onSuccess(false)
        state.assertLastValueThat { loginValidation == LoginValidation.State.NOT_AVAILABLE }
    }

    @Test
    fun shouldValidateLoginUsingPassedLogin() {
        val loginEvent = LoginValidation.LoginChangedEvent("a")
        events.accept(loginEvent)
        verify(api).invoke(loginEvent.login)
    }
}

class SignUpReducer(private val loginValidationApi: (String) -> Single<Boolean>) : Reducer<SignUp.State> {
    override fun invoke(events: Events): Observable<SignUp.State> {
        return events
                .ofType(LoginValidation.LoginChangedEvent::class.java)
                .switchMap {
                    if (it.login.isNotEmpty()) {
                        loginValidationApi.invoke(it.login).toObservable().map {
                            if (it) {
                                LoginValidation.State.AVAILABLE
                            } else {
                                LoginValidation.State.NOT_AVAILABLE
                            }
                        }.startWith(LoginValidation.State.IN_PROGRESS)
                    } else {
                        Observable.just(LoginValidation.State.IDLE)
                    }
                }
                .startWith(LoginValidation.State.IDLE)
                .map(SignUp::State)
    }
}

interface SignUp {
    data class State(val loginValidation: LoginValidation.State)

    interface LoginValidation {
        data class LoginChangedEvent(val login: String)

        enum class State {
            IDLE,
            IN_PROGRESS,
            AVAILABLE,
            NOT_AVAILABLE
        }
    }
}
