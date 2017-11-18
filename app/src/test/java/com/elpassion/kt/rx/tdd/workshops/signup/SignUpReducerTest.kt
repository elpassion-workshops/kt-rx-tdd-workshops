package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.*
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import org.junit.Test

class SignUpReducerTest {

    private val events = PublishRelay.create<Any>()
    private val loginValidationApiSubject = SingleSubject.create<Boolean>()
    private val state = SignUpReducer({ loginValidationApiSubject }).invoke(events).test()

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
    fun shouldLoginValidationStateBeLoginUnavailableAfterUserTypesTakenLogin() {
        events.accept(LoginValidation.LoginChangedEvent("taken login"))
        loginValidationApiSubject.onSuccess(false)
        state.assertLastValueThat { loginValidation == LoginValidation.State.LOGIN_TAKEN }
    }

    @Test
    fun shouldLoginValidationStateBeLoginAvailableAfterUserTypesAvailableLogin() {
        events.accept(LoginValidation.LoginChangedEvent("available login"))
        loginValidationApiSubject.onSuccess(true)
        state.assertLastValueThat { loginValidation == LoginValidation.State.LOGIN_AVAILABLE }
    }

}

class SignUpReducer(private val loginApi: () -> Single<Boolean>) : Reducer<SignUp.State> {
    override fun invoke(events: Events): Observable<SignUp.State> {
        return events
                .flatMap { callLoginValidationApi() }
                .startWith(LoginValidation.State.IDLE)
                .map(SignUp::State)
    }

    private fun callLoginValidationApi(): Observable<LoginValidation.State> {
        return loginApi()
                .map {
                    if (it) {
                        LoginValidation.State.LOGIN_AVAILABLE
                    } else {
                        LoginValidation.State.LOGIN_TAKEN
                    }
                }
                .toObservable()
                .startWith(LoginValidation.State.IN_PROGRESS)
    }
}

interface SignUp {
    data class State(val loginValidation: LoginValidation.State)


    interface LoginValidation {
        data class LoginChangedEvent(val login: String)

        enum class State {
            IDLE,
            IN_PROGRESS,
            LOGIN_TAKEN,
            LOGIN_AVAILABLE
        }
    }
}
