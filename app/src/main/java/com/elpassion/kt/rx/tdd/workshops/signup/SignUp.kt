package com.elpassion.kt.rx.tdd.workshops.signup

interface SignUp {
    object RegisterEvent

    data class State(
            val loginValidation: LoginValidation.State,
            val photo: Photo.State,
            val showLoader: Boolean,
            val isRegisterEnabled: Boolean)

    interface LoginValidation {
        data class State(val login: String, val validationResult: ValidationResult)

        enum class ValidationResult {
            IDLE,
            LOADING,
            AVAILABLE,
            TAKEN,
            ERROR
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