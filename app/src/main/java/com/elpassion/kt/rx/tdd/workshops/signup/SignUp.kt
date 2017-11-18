package com.elpassion.kt.rx.tdd.workshops.signup

interface SignUp {
    data class State(val loginValidation: LoginValidation.State, val photoState: Photo.State)

    interface Photo {

        sealed class State {
            object Empty : Photo.State()
            data class Taken(val uri: String) : Photo.State()
        }

        object TakePhotoEvent
    }

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
}