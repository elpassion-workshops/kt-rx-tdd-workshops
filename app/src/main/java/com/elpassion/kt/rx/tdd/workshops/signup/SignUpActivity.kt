package com.elpassion.kt.rx.tdd.workshops.signup

import android.os.Bundle
import com.elpassion.kt.rx.tdd.workshops.R
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.Companion.events
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.Companion.state
import com.elpassion.kt.rx.tdd.workshops.utils.setImageFromStorage
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
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
                .subscribe(events)
        state
                .bindToLifecycle(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    when (it.loginValidation) {
                        SignUp.LoginValidation.State.IDLE -> signUpProgress.setText(R.string.idle)
                        SignUp.LoginValidation.State.IN_PROGRESS -> signUpProgress.setText(R.string.loading)
                        SignUp.LoginValidation.State.AVAILABLE -> signUpProgress.setText(R.string.available)
                        SignUp.LoginValidation.State.TAKEN -> signUpProgress.setText(R.string.taken)
                        SignUp.LoginValidation.State.ERROR -> signUpProgress.setText(R.string.api_error)
                    }
                    when (it.photoValidation) {
                        is SignUp.PhotoValidation.State.RETURNED -> {
                            signUpPhoto.setImageFromStorage(it.photoValidation.path)
                            signUpAddPhoto.setText(R.string.change_photo)
                        }
                    }
                }
    }

    private fun uiEvents() = Observable.merge(textChangesEvents(), takePhotoEvents())

    private fun textChangesEvents(): Observable<Any> =
            singUpLogin.textChanges().map { SignUp.LoginValidation.LoginChangedEvent(it.toString()) }

    private fun takePhotoEvents() = signUpAddPhoto.clicks().map { SignUp.PhotoValidation.PhotoEvent() }
}
