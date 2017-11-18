package com.elpassion.kt.rx.tdd.workshops.signup

import android.support.test.rule.ActivityTestRule
import com.elpassion.android.commons.espresso.*
import com.elpassion.kt.rx.tdd.workshops.R
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import org.junit.Rule
import org.junit.Test

class SignUpActivityTest {

    private val loginApiSubject = SingleSubject.create<Boolean>()

    @JvmField
    @Rule
    val rule = object : ActivityTestRule<SignUpActivity>(SignUpActivity::class.java) {
        override fun beforeActivityLaunched() {
            SignUp.loginApi = { loginApiSubject }
            SignUp.camera = { Maybe.empty() }
            SignUp.cameraPermission = { Single.never() }
        }
    }

    @Test
    fun shouldHaveLoginInput() {
        onId(R.id.loginInput).isDisplayed()
    }

    @Test
    fun shouldShowIdleLoginValidationIndicatorOnStart() {
        onId(R.id.loginIndicator).hasText(R.string.login_indicator_idle)
    }

    @Test
    fun shouldShowLoadingValidationStateOnLoginInput() {
        onId(R.id.loginInput).typeText("login")
        onId(R.id.loginIndicator).hasText(R.string.login_indicator_loading)
    }

    @Test
    fun shouldShowLoginAvailableValidationState() {
        onId(R.id.loginInput).typeText("login")
        loginApiSubject.onSuccess(true)
        onId(R.id.loginIndicator).hasText(R.string.login_indicator_available)
    }

    @Test
    fun shouldShowLoginTakenWhenApiReturnsThatItIsTaken() {
        onId(R.id.loginInput).typeText("login")
        loginApiSubject.onSuccess(false)
        onId(R.id.loginIndicator).hasText(R.string.login_indicator_taken)
    }

    @Test
    fun shouldShowLoginValidationErrorWhenApiReturnsError() {
        onId(R.id.loginInput).typeText("login")
        loginApiSubject.onError(RuntimeException())
        onId(R.id.loginIndicator).hasText(R.string.login_indicator_error)
    }

    @Test
    fun shouldShowIdleLoginValidationStateWhenLoginErased() {
        onId(R.id.loginInput).typeText("login")
        onId(R.id.loginInput).replaceText("")
        onId(R.id.loginIndicator).hasText(R.string.login_indicator_idle)
    }
}