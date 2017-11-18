package com.elpassion.kt.rx.tdd.workshops.signup

import io.reactivex.Single

interface LoginApi {
    fun checkLogin(login: String): Single<Boolean>
}