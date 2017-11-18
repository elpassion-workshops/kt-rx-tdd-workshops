package com.elpassion.kt.rx.tdd.workshops.signup

import android.os.Bundle
import com.elpassion.kt.rx.tdd.workshops.R
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.Companion.camera
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.Companion.cameraPermission
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.Companion.loginApi
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.trello.rxlifecycle2.components.RxActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.sign_up_activity.*

class SignUpActivity : RxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_activity)
        SignUpReducer(loginApi, camera, cameraPermission)
                .invoke(uiEvents())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleEvents)
    }

    private fun uiEvents() = Observable.merge(loginChangesEvents(), takePhotoEvents())

    private fun handleEvents(state: SignUp.State) {
        when (state.loginValidation) {
            SignUp.LoginValidation.State.IDLE -> loginIndicator.setText(R.string.login_indicator_idle)
            SignUp.LoginValidation.State.IN_PROGRESS -> loginIndicator.setText(R.string.login_indicator_loading)
            SignUp.LoginValidation.State.AVAILABLE -> loginIndicator.setText(R.string.login_indicator_available)
            SignUp.LoginValidation.State.TAKEN -> loginIndicator.setText(R.string.login_indicator_taken)
            SignUp.LoginValidation.State.ERROR -> loginIndicator.setText(R.string.login_indicator_error)
        }
    }

    private fun loginChangesEvents(): Observable<Any> =
            loginInput.textChanges().map {
                SignUp.LoginValidation.LoginChangedEvent(it.toString())
            }

    private fun takePhotoEvents(): Observable<Any> =
            takePhotoButton.clicks().map {
                SignUp.Photo.TakePhotoEvent
            }
}
