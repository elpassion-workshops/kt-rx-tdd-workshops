package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import com.jakewharton.rxrelay2.BehaviorRelay
import org.junit.Test

class SignUpReducerTest {

    private val state = BehaviorRelay.createDefault(SignUp.State(SignUp.LoginValidation.State.IDLE)).test()

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

