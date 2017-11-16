package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.junit.Test

class SignUpReducerTest {

    private val events = PublishRelay.create<Any>()
    private val state = SignUpRelay().invoke(events).test()

    @Test
    fun shouldLoginValidationStateBeIdleAtTheBegging() {
        state.assertLastValueThat { loginValidation == SignUp.LoginValidation.State.IDLE }
    }

    @Test
    fun shouldChangeLoginValidationStateToLoadingAfterChangingLogin() {
        events.accept(SignUp.LoginValidation.LoginChangedEvent("a"))
        state.assertLastValueThat { loginValidation == SignUp.LoginValidation.State.LOADING }
    }

    @Test
    fun shouldReturnToIdleStateAfterClearingLogin() {
        events.accept(SignUp.LoginValidation.LoginChangedEvent(""))
        state.assertLastValueThat { loginValidation == SignUp.LoginValidation.State.IDLE }
    }
}

interface SignUp {
    data class State(val loginValidation: LoginValidation.State)

    interface LoginValidation {
        data class LoginChangedEvent(val login: String)

        enum class State {
            IDLE,
            LOADING,
        }
    }
}

class SignUpRelay : Reducer<SignUp.State> {
    override fun invoke(events: Events): Observable<SignUp.State> {
        return events.ofType(SignUp.LoginValidation.LoginChangedEvent::class.java)
                .map {
                    if (it.login.isEmpty()) {
                        SignUp.LoginValidation.State.IDLE
                    } else {
                        SignUp.LoginValidation.State.LOADING
                    }
                }
                .startWith(SignUp.LoginValidation.State.IDLE)
                .map { SignUp.State(it) }
    }
}
