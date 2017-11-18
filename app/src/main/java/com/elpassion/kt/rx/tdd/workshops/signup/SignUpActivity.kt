package com.elpassion.kt.rx.tdd.workshops.signup

import android.os.Bundle
import com.elpassion.kt.rx.tdd.workshops.R
import com.elpassion.kt.rx.tdd.workshops.utils.setImageFromStorage
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.components.RxActivity
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.sign_up_activity.*

class SignUpActivity : RxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_activity)
        uiEvents()
                .bindToLifecycle(this)
                .subscribe(SignUp.events)

        SignUp.states
                .bindToLifecycle(this)
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
        when (state.photoState) {
            is SignUp.Photo.State.Taken -> photo.setImageFromStorage(state.photoState.uri)
        }
    }

    private fun loginChangesEvents(): Observable<Any> =
            loginInput.textChanges()
                    .skipInitialValue()
                    .map {
                SignUp.LoginValidation.LoginChangedEvent(it.toString())
            }

    private fun takePhotoEvents(): Observable<Any> =
            takePhotoButton.clicks().map {
                SignUp.Photo.TakePhotoEvent
            }
}
