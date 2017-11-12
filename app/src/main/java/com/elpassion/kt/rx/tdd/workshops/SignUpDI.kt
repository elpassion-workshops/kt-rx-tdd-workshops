package com.elpassion.kt.rx.tdd.workshops

object SignUpDI {

    val signUpReducer by lazy(signUpModelProvider)

    lateinit var signUpModelProvider: () -> SignUpReducer
}
