package com.elpassion.kt.rx.tdd.workshops

import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.elpassion.android.commons.espresso.*
import com.elpassion.kt.rx.tdd.workshops.signup.SignUpActivity
import com.elpassion.kt.rx.tdd.workshops.signup.SignUpDI
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.reactivex.subjects.MaybeSubject
import io.reactivex.subjects.SingleSubject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpActivityTest {

    private val apiSubject = SingleSubject.create<Boolean>()
    private val permissionSubject = MaybeSubject.create<Unit>()
    private val cameraSubject = MaybeSubject.create<String>()

    @JvmField
    @Rule
    val rule = object : ActivityTestRule<SignUpActivity>(SignUpActivity::class.java) {
        override fun beforeActivityLaunched() {
            SignUpDI.api = mock { on { invoke(any()) } doReturn apiSubject }
            SignUpDI.camera = { cameraSubject }
            SignUpDI.permissionRequester = { permissionSubject }
        }
    }

    @Test
    fun shouldHaveLoginInput() {
        onId(R.id.loginInput).typeText("login").hasText("login").isDisplayed()
    }

    @Test
    fun shouldShowIdleLoginValidationIndicatorOnStart() {
        onId(R.id.loginValidationIndicator).hasText(R.string.login_validation_idle)
    }

    @Test
    fun shouldShowLoadingValidationState() {
        onId(R.id.loginInput).typeText("a")
        onId(R.id.loginValidationIndicator).hasText(R.string.login_validation_loading)
    }

    @Test
    fun shouldShowLoginAvailableValidationState() {
        onId(R.id.loginInput).typeText("a")
        apiSubject.onSuccess(true)
        onId(R.id.loginValidationIndicator).hasText(R.string.login_validation_available)
    }

    @Test
    fun shouldShowLoginTakenWhenApiReturnsThatItIsTaken() {
        onId(R.id.loginInput).typeText("a")
        apiSubject.onSuccess(false)
        onId(R.id.loginValidationIndicator).hasText(R.string.login_validation_taken)
    }

    @Test
    fun shouldShowLoginValidationErrorWhenApiReturnsError() {
        onId(R.id.loginInput).typeText("a")
        apiSubject.onError(RuntimeException())
        onId(R.id.loginValidationIndicator).hasText(R.string.login_validation_error)
    }

    @Test
    fun shouldShowIdleLoginValidationStateWhenLoginErased() {
        onId(R.id.loginInput).typeText("a")
        onId(R.id.loginInput).replaceText("")
        onId(R.id.loginValidationIndicator).hasText(R.string.login_validation_idle)
    }

    @Test
    fun shouldTakePhotoOnTakePhotoClicked() {
        val (uriString, bitmap) = createTestBitmap()
        onId(R.id.takePhoto).click()
        permissionSubject.onSuccess(Unit)
        cameraSubject.onSuccess(uriString)
        onId(R.id.photo).check(matches(HasSameBitmap(bitmap)))
    }

    @Test
    fun shouldRegisterButtonBeVisible() {
        onId(R.id.registerButton).hasText(R.string.register_button).isDisplayed()
    }

    @Test
    fun shouldTakePhotoButtonHasProperText() {
        onId(R.id.takePhoto).hasText(R.string.take_photo_button)
    }

    @Test
    fun shouldRegisterButtonBeDisabledOnStart() {
        onId(R.id.registerButton).isDisabled()
    }
}