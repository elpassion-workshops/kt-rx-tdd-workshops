package com.elpassion.kt.rx.tdd.workshops.signup

interface SignUp {
    data class State(val loginValidation: LoginValidation.State, val addPhoto: AddPhoto.State)

    interface LoginValidation {
        data class LoginChangedEvent(val login: String)

        enum class State {
            IDLE,
            IN_PROGRESS,
            AVAILABLE,
            NOT_AVAILABLE,
            API_ERROR
        }
    }

    interface AddPhoto {

        object TakePhotoEvent

        sealed class State {
            object EMPTY : State()
            data class PhotoTaken(val uri: String) : State()
        }
    }
}