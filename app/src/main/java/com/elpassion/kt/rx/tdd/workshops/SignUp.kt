package com.elpassion.kt.rx.tdd.workshops

interface SignUp {
    interface Event : LoginValidation.Event
    data class State(val loginValidation: LoginValidation.State)

    interface LoginValidation {
        interface Event
        object State
    }
}