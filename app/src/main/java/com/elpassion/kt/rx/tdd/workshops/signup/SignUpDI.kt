package com.elpassion.kt.rx.tdd.workshops.signup

import io.reactivex.Maybe
import io.reactivex.Single

object SignUpDI {
    lateinit var api: (String) -> Single<Boolean>
    lateinit var camera: () -> Maybe<String>
    lateinit var permissionRequester: ()-> Maybe<Unit>
}