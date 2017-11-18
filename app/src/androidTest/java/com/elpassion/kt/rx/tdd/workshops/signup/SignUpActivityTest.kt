package com.elpassion.kt.rx.tdd.workshops.signup

import android.support.test.rule.ActivityTestRule
import com.elpassion.android.commons.espresso.*
import com.elpassion.kt.rx.tdd.workshops.R
import io.reactivex.subjects.MaybeSubject
import io.reactivex.subjects.SingleSubject
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class SignUpActivityTest {

    private val loginApiSubject = SingleSubject.create<Boolean>()
    private val cameraPermission = SingleSubject.create<Boolean>()
    private val camera = MaybeSubject.create<String>()

    @JvmField
    @Rule
    val rule = object : ActivityTestRule<SignUpActivity>(SignUpActivity::class.java) {
        override fun beforeActivityLaunched() {
            SignUp.loginApi = { loginApiSubject }
            SignUp.camera = { camera }
            SignUp.cameraPermission = { cameraPermission }
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

    @Test
    fun shouldTakePhotoOnTakePhotoClickedWhenPermissionsAreGranted() {
        onId(R.id.takePhotoButton).click()
        cameraPermission.onSuccess(true)
        Assert.assertTrue(camera.hasObservers())
    }
}