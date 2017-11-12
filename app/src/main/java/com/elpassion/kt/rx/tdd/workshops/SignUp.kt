package com.elpassion.kt.rx.tdd.workshops

import io.reactivex.Maybe
import io.reactivex.Single

interface SignUp {
    data class State(val loginValidation: LoginValidation.State,
                     val takenPhoto: Photo.State)

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

    interface Photo {
        object TakePhotoEvent

        sealed class State {
            object EMPTY : State()
            data class Photo(val photoUri: String) : State()
        }

        interface PermissionRequester {
            fun request(): Maybe<Unit>
        }

        interface PhotoRequester {
            fun request(): Maybe<String>
        }
    }
}