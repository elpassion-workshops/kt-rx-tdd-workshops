package com.elpassion.kt.rx.tdd.workshops

import android.app.Application
import io.reactivex.Maybe
import io.reactivex.Single

class MyFirstApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SignUpDI.signUpModelProvider = {
            SignUpReducer(
                    LoginValidationReducer(
                            object : SignUp.LoginValidation.Api {
                                override fun call(login: String) = Single.never<Boolean>()
                            }
                    ),
                    PhotoReducer(
                            object : SignUp.Photo.PermissionRequester {
                                override fun request() = Maybe.never<Unit>()
                            },
                            object : SignUp.Photo.PhotoRequester {
                                override fun request() = Maybe.never<String>()
                            }
                    )
            )
        }
    }
}