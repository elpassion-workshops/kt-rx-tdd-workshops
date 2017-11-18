package com.elpassion.kt.rx.tdd.workshops

import io.reactivex.Maybe
import io.reactivex.Single

object SignUpDI {
    lateinit var api: (String) -> Single<Boolean>
    lateinit var cameraApi: () -> Maybe<String>
    lateinit var permission: () -> Single<Boolean>
}
