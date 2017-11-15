package com.elpassion.kt.rx.tdd.workshops.signup

import io.reactivex.Single

object SignUpDI {
    lateinit var api: (String) -> Single<Boolean>
}