package com.elpassion.kt.rx.tdd.workshops.signup

import android.support.test.rule.ActivityTestRule
import com.elpassion.android.commons.espresso.*
import com.elpassion.kt.rx.tdd.workshops.R
import com.elpassion.kt.rx.tdd.workshops.createTestBitmap
import com.elpassion.kt.rx.tdd.workshops.hasBitmap
import io.reactivex.subjects.MaybeSubject
import io.reactivex.subjects.SingleSubject
import org.junit.Rule
import org.junit.Test

class SignUpActivityTest {

    val apiSubject = SingleSubject.create<Boolean>()
    val cameraSubject = MaybeSubject.create<String>()
    val permissionSubject = SingleSubject.create<Boolean>()

    @JvmField
    @Rule
    val rule = object : ActivityTestRule<SignUpActivity>(SignUpActivity::class.java) {
        override fun beforeActivityLaunched() {
            SignUp.api = { apiSubject }
            SignUp.cameraApi = { cameraSubject }
            SignUp.permissionApi = { permissionSubject }
        }
    }

    @Test
    fun shouldStartActivity() {
    }

    @Test
    fun shouldHaveLoginInput() {
        onId(R.id.login_input)
                .typeText("login")
                .hasText("login")
    }

    @Test
    fun shouldShowIdleLoginValidationIndicatorOnStart() {
        onId(R.id.login_validation_label).hasText(R.string.loginValidationIdle)
    }

    @Test
    fun shouldShowLoadingValidationState() {
        onId(R.id.login_input).typeText("login")
        onId(R.id.login_validation_label).hasText(R.string.loginValidationLoading)
    }

    @Test
    fun shouldShowLoginAvailableValidationState() {
        onId(R.id.login_input).typeText("login")
        apiSubject.onSuccess(true)
        onId(R.id.login_validation_label).hasText(R.string.loginValidationAvailable)
    }

    @Test
    fun shouldShowLoginTakenWhenApiReturnsThatItIsTaken() {
        onId(R.id.login_input).typeText("login")
        apiSubject.onSuccess(false)
        onId(R.id.login_validation_label).hasText(R.string.loginValidationIsTaken)
    }

    @Test
    fun shouldShowLoginValidationErrorWhenApiReturnsError() {
        onId(R.id.login_input).typeText("login")
        apiSubject.onError(RuntimeException())
        onId(R.id.login_validation_label).hasText(R.string.loginValidationError)
    }

    @Test
    fun shouldShowIdleLoginValidationStateWhenLoginErased() {
        onId(R.id.login_input).typeText("login").replaceText("")
        onId(R.id.login_validation_label).hasText(R.string.loginValidationIdle)
    }

    @Test
    fun shouldTakePhotoOnTakePhotoClicked() {
        onId(R.id.photoButton).click()
        permissionSubject.onSuccess(true)
        val testBitmap = createTestBitmap()
        cameraSubject.onSuccess(testBitmap.first)
        onId(R.id.photoPreview).hasBitmap(testBitmap.second)
    }
}