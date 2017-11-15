package com.elpassion.kt.rx.tdd.workshops.signup

interface SignUp {
    data class State(
            val loginValidation: LoginValidation.State,
            val photo: Photo.State)

    interface LoginValidation {
        enum class State {
            IDLE,
            LOADING,
            LOGIN_AVAILABLE,
            LOGIN_TAKEN,
            ERROR,
        }

        data class LoginChangedEvent(val login: String)
    }

    interface Photo {
        sealed class State {
            object EMPTY : State()
            data class Photo(val uri: String) : State()
        }

        object TakePhotoEvent
    }
}