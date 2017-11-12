package com.elpassion.kt.rx.tdd.workshops

import io.reactivex.Single

interface SignUp {
    data class State(val loginValidation: LoginValidation.State)

    interface LoginValidation {
        data class LoginChangedEvent(val login: String)

        enum class State {
            IDLE,
            LOADING,
            LOGIN_OK,
        }

        interface Api {
            fun call(login: String): Single<Boolean>
        }
    }
}