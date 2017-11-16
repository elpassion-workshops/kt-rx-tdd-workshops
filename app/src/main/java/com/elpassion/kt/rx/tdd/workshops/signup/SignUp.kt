package com.elpassion.kt.rx.tdd.workshops.signup

interface SignUp {
    object RegisterEvent

    data class State(
            val loginValidation: LoginValidation.State,
            val photo: Photo.State,
            val showLoader: Boolean)

    interface LoginValidation {
        sealed class State {
            object IDLE : LoginValidation.State()
            object LOADING : LoginValidation.State()
            data class LoginAvailable(val login: String) : LoginValidation.State()
            object LOGIN_TAKEN : LoginValidation.State()
            object ERROR : LoginValidation.State()
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