package com.elpassion.kt.rx.tdd.workshops.signup

import android.os.Bundle
import android.view.View
import com.elpassion.kt.rx.tdd.workshops.R
import com.elpassion.kt.rx.tdd.workshops.SignUpDI
import com.elpassion.kt.rx.tdd.workshops.utils.setImageFromStorage
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.trello.rxlifecycle2.components.RxActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.sign_up_activity.*

class SignUpActivity : RxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_activity)
        setupReducer()


    }

    private fun setupReducer() {
        val loginTypedEvents = loginInput.textChanges().map { SignUp.LoginValidation.LoginChangedEvent(it.toString()) }
        val photoClickedEvents = takePhoto.clicks().map{SignUp.Photo.TakePhotoEvent}

        SignUpReducer(SignUpDI.api, SignUpDI.cameraApi, SignUpDI.permission)
                .invoke(Observable.merge(loginTypedEvents, photoClickedEvents))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy {
                    renderValidationForm(it)
                    renderPhotoState(it)
                }
    }

    private fun renderPhotoState(it: SignUp.State) {
        when (it.photoState) {
            is SignUp.Photo.State.Captured -> {
                takePhoto.visibility = View.GONE
                imageView.setImageFromStorage(it.photoState.photoUri)
            }
        }
    }

    private fun renderValidationForm(it: SignUp.State) {
        when (it.loginValidation) {
            SignUp.LoginValidation.State.IN_PROGRESS -> {
                indicator.setText(R.string.loading)
            }
            SignUp.LoginValidation.State.AVAILABLE -> {
                indicator.setText(R.string.available)
            }
            SignUp.LoginValidation.State.NOT_AVAILABLE -> {
                indicator.setText(R.string.taken)
            }
            SignUp.LoginValidation.State.ERROR -> {
                indicator.setText(R.string.error_message)
            }
            SignUp.LoginValidation.State.IDLE -> {
                indicator.setText(R.string.idle)
            }
        }
    }
}
