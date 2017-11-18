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
    private val state = SignUpReducer({ apiSubject }).invoke(events).test()
    private val apiSubject = SingleSubject.create<Boolean>()

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
}

class SignUpReducer(val api: () -> Single<Boolean>) : Reducer<SignUp.State> {
    override fun invoke(events: Events): Observable<SignUp.State> {
        return events
                .ofType(LoginValidation.LoginChangedEvent::class.java)
                .switchMap {
                    if (it.login.isNotEmpty()) {
                        api.invoke().toObservable().map {
                            LoginValidation.State.AVAILABLE
                        }.startWith(LoginValidation.State.IN_PROGRESS)
                    } else {
                        Observable.just(LoginValidation.State.IDLE)
                    }
                }
//                .map { e ->
//                    when (e.login) {
//                        "" -> LoginValidation.State.IDLE
//                        else -> {
//                            LoginValidation.State.IN_PROGRESS
//                        }
//                    }
//                }
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
        }
    }
}
