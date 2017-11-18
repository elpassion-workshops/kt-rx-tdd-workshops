package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import com.elpassion.kt.rx.tdd.workshops.signup.SignUpReducerTest.SignUp.LoginValidation.State
import io.reactivex.Observable
import org.junit.Test

class SignUpReducerTest {

    private val state = Observable.just(SignUp.State(State.IDLE)).test()

    @Test
    fun shouldLoginValidationStateBeIdleOnStart() {
        state.assertLastValueThat { loginValidation == State.IDLE }
    }

    interface SignUp {
        data class State(val loginValidation: LoginValidation.State)

        interface LoginValidation {
            enum class State {
                IDLE,
            }
        }
    }
}