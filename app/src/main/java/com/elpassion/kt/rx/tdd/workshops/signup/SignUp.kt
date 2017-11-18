package com.elpassion.kt.rx.tdd.workshops.signup

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Maybe
import io.reactivex.Single

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

    companion object {
        val events = PublishRelay.create<Any>()
        val states = BehaviorRelay.create<State>()

        lateinit var loginApi: () -> Single<Boolean>
        lateinit var camera: () -> Maybe<String>
        lateinit var cameraPermission: () -> Single<Boolean>
    }
}