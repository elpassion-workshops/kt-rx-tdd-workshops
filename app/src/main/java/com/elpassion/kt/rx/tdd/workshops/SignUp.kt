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
            LOGIN_TAKEN,
            API_ERROR,
        }

        interface Api {
            fun call(login: String): Single<Boolean>
        }
    }

    interface Photo{
        enum class State {
            EMPTY,
        }
    }
}