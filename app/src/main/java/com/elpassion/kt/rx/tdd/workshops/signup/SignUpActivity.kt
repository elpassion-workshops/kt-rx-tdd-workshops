package com.elpassion.kt.rx.tdd.workshops.signup

import android.os.Bundle
import com.elpassion.kt.rx.tdd.workshops.R
import com.elpassion.kt.rx.tdd.workshops.signup.SignUpDI.api
import com.elpassion.kt.rx.tdd.workshops.signup.SignUpDI.camera
import com.elpassion.kt.rx.tdd.workshops.signup.SignUpDI.permissionRequester
import com.elpassion.kt.rx.tdd.workshops.utils.setImageFromStorage
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.trello.rxlifecycle2.components.RxActivity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observable.merge
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.sign_up_activity.*

class SignUpActivity : RxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_activity)
        SignUpReducer(api, camera, permissionRequester, { _, _ -> Completable.complete() })
                .invoke(uiEvents())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    when (it.loginValidation) {
                        SignUp.LoginValidation.State.IDLE -> loginValidationIndicator.setText(R.string.login_validation_idle)
                        SignUp.LoginValidation.State.LOADING -> loginValidationIndicator.setText(R.string.login_validation_loading)
                        is SignUp.LoginValidation.State.LoginAvailable -> loginValidationIndicator.setText(R.string.login_validation_available)
                        SignUp.LoginValidation.State.LOGIN_TAKEN -> loginValidationIndicator.setText(R.string.login_validation_taken)
                        SignUp.LoginValidation.State.ERROR -> loginValidationIndicator.setText(R.string.login_validation_error)
                    }
                    when (it.photo) {
                        is SignUp.Photo.State.Photo -> photo.setImageFromStorage(it.photo.uri)
                    }
                }
    }

    private fun uiEvents(): Observable<Any> =
            merge(loginInput.textChanges().map { SignUp.LoginValidation.LoginChangedEvent(it.toString()) },
                    takePhoto.clicks().map { SignUp.Photo.TakePhotoEvent })
}
