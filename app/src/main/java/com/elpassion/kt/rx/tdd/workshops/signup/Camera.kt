package com.elpassion.kt.rx.tdd.workshops.signup

import io.reactivex.Single

interface Camera {
    fun call(): Single<String>
}