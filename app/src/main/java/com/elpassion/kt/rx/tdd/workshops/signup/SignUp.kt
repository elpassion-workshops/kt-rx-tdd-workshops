package com.elpassion.kt.rx.tdd.workshops.signup

import io.reactivex.Maybe
import io.reactivex.Scheduler
import io.reactivex.Single

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

    companion object {
        lateinit var api: (String) -> Single<Boolean>
        lateinit var debounceScheduler: Scheduler
        lateinit var camera: () -> Maybe<String>
        lateinit var permissions: () -> Single<Boolean>
    }
}