package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Test

class SignUpReducerTest {

    private val state = Observable.just(SignUp.State(SignUp.LoginValidation.State.IDLE)).test()

    @Test
    fun shouldLoginValidationStateBeIdleAtTheBegging() {
        state.assertLastValueThat { loginValidation == SignUp.LoginValidation.State.IDLE }
    }

}

interface SignUp {
    data class State(val loginValidation: LoginValidation.State)

    interface LoginValidation {
        enum class State {
            IDLE,
        }
    }
}
