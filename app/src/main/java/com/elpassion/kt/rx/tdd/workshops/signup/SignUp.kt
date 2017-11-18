package com.elpassion.kt.rx.tdd.workshops.signup

interface SignUp {
    data class State(val loginValidation: LoginValidation.State, val photoValidation: PhotoValidation.State)

    interface LoginValidation {
        data class LoginChangedEvent(val login: String)

        enum class State {
            IDLE,
            IN_PROGRESS,
            AVAILABLE,
            TAKEN,
            ERROR
        }
    }

    interface PhotoValidation {

        class PhotoEvent

        sealed class State {
            object EMPTY : State()
            data class RETURNED(val path: String) : State()
        }
    }
}