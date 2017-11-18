package com.elpassion.kt.rx.tdd.workshops

import io.reactivex.Single

object SignUpDI {
    lateinit var api: (String) -> Single<Boolean>
    lateinit var cameraApi: () -> Single<String>
    lateinit var permission: () -> Single<Boolean>
}
