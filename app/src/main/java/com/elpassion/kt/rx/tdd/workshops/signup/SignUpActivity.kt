package com.elpassion.kt.rx.tdd.workshops.signup

import android.os.Bundle
import com.elpassion.kt.rx.tdd.workshops.R
import com.elpassion.kt.rx.tdd.workshops.utils.setImageFromStorage
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.trello.rxlifecycle2.components.RxActivity
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.sign_up_activity.*

class SignUpActivity : RxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_activity)
        val reducer = SignUpReducer(SignUp.api, SignUp.cameraApi, SignUp.permissionApi)
        reducer.invoke(uiEvents())
                .bindToLifecycle(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    when (it.loginValidation) {
                        SignUp.LoginValidation.State.APIFAIL -> login_validation_label.setText(R.string.loginValidationError)
                        SignUp.LoginValidation.State.AVAILABLE -> login_validation_label.setText(R.string.loginValidationAvailable)
                        SignUp.LoginValidation.State.IDLE -> login_validation_label.setText(R.string.loginValidationIdle)
                        SignUp.LoginValidation.State.ISTAKEN -> login_validation_label.setText(R.string.loginValidationIsTaken)
                        SignUp.LoginValidation.State.IN_PROGRESS -> login_validation_label.setText(R.string.loginValidationLoading)
                    }
                    when(it.photoState){
                        is SignUp.Photo.State.Photo -> photoPreview.setImageFromStorage(it.photoState.uri)
                    }
                }

    }

    private fun uiEvents(): Observable<Any> {
        return Observable.merge(photoButton.clicks().map { SignUp.Photo.TakePhotoEvent }
                ,login_input.textChanges().map { SignUp.LoginValidation.LoginChangedEvent(it.toString()) })
    }


}
