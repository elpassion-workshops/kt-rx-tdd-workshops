package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.junit.Test

class SignUpReducerTest {

    private val events = PublishRelay.create<Any>()
    private val state = SignUpReducer().invoke(events).test()

    @Test
    fun shouldLoginValidationStateBeIdleAtTheBegging() {
        state.assertLastValueThat { loginValidation == SignUp.LoginValidation.State.IDLE }
    }

    @Test
    fun shouldLoginValidationStateBeInProgressWhenNotEmptyLoginArrives() {
        events.accept(SignUp.LoginValidation.LoginChangedEvent("login"))
        state.assertLastValueThat { loginValidation == SignUp.LoginValidation.State.LOADING }
    }
}

class SignUpReducer : Reducer<SignUp.State> {
    override fun invoke(events: Events): Observable<SignUp.State> {
        return events.map { SignUp.State(SignUp.LoginValidation.State.LOADING) }
                .startWith(SignUp.State(SignUp.LoginValidation.State.IDLE))
    }
}

interface SignUp {
    data class State(val loginValidation: LoginValidation.State)

    interface LoginValidation {
        enum class State {
            IDLE,
            LOADING,
        }

        data class LoginChangedEvent(val login: String)
    }
}

