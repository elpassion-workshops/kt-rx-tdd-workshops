package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.*
import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import org.junit.Test

class SignUpReducerTest {

    private val events = PublishRelay.create<Any>()
    private val loginApiSubject = SingleSubject.create<Boolean>()
    private val loginApi: LoginApi = mock { on { checkLogin(any()) } doReturn loginApiSubject }
    private val state = SignUpReducer(loginApi).invoke(events).test()

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
        events.accept(LoginValidation.LoginChangedEvent(""))
        state.assertLastValueThat { loginValidation == LoginValidation.State.IDLE }
    }

    @Test
    fun shouldLoginValidationStateBeAvailableWhenApiPasses() {
        events.accept(LoginValidation.LoginChangedEvent("a"))
        loginApiSubject.onSuccess(true)
        state.assertLastValueThat { loginValidation == LoginValidation.State.AVAILABLE }
    }

    @Test
    fun shouldLoginValidationStateBeNotAvailableWhenApiReturnsThatItIsTaken() {
        events.accept(LoginValidation.LoginChangedEvent("b"))
        loginApiSubject.onSuccess(false)
        state.assertLastValueThat { loginValidation == LoginValidation.State.NOT_AVAILABLE }
    }

    @Test
    fun shouldValidateLoginUsingPassedLogin() {
        events.accept(LoginValidation.LoginChangedEvent("b"))
        verify(loginApi).checkLogin("b")
    }
}

class SignUpReducer(val api: LoginApi) : Reducer<SignUp.State> {
    override fun invoke(events: Events): Observable<SignUp.State> {
        return events
                .ofType(LoginValidation.LoginChangedEvent::class.java)
                .switchMap {
                    if (it.login.isEmpty()) {
                        Observable.just(LoginValidation.State.IDLE)
                    } else {
                        api.checkLogin(it.login)
                                .map { if (it) LoginValidation.State.AVAILABLE else LoginValidation.State.NOT_AVAILABLE }
                                .toObservable()
                                .startWith(LoginValidation.State.IN_PROGRESS)
                    }
                }
                .startWith(LoginValidation.State.IDLE)
                .map { State(it) }
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

interface LoginApi {
    fun checkLogin(login: String): Single<Boolean>
}
