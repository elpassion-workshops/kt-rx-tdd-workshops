package com.elpassion.kt.rx.tdd.workshops.signup

import io.reactivex.Maybe
import io.reactivex.Single

interface SignUp {

    interface Photo {
        object TakePhotoEvent

        sealed class State {
            object Empty : State()
            data class Photo(val uri: String) : State()
        }
    }

    data class State(val loginValidation: LoginValidation.State, val photoState: Photo.State)


    interface LoginValidation {
        data class LoginChangedEvent(val login: String)

        enum class State {
            IDLE,
            IN_PROGRESS,
            AVAILABLE,
            ISTAKEN,
            APIFAIL
        }
    }

    companion object {

        lateinit var api : (String) -> Single<Boolean>
        lateinit var cameraApi : () -> Maybe<String>
        lateinit var permissionApi: () -> Single<Boolean>
    }
}