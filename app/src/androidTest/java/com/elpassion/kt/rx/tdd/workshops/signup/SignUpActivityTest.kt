package com.elpassion.kt.rx.tdd.workshops.signup

import android.support.test.rule.ActivityTestRule
import com.elpassion.android.commons.espresso.*
import com.elpassion.kt.rx.tdd.workshops.R
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.SingleSubject
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

class SignUpActivityTest {

    private val testScheduler = TestScheduler()
    private val loginApiSubject = SingleSubject.create<Boolean>()
    private val loginApi = mock<LoginApi> {
        on { checkLogin(any()) } doReturn loginApiSubject
    }
    private val cameraSubject = SingleSubject.create<String>()
    private val camera = mock<Camera> { on { call() } doReturn cameraSubject }
    private val systemSubject = SingleSubject.create<Boolean>()
    private val system = mock<System> { on { cameraPermission() } doReturn systemSubject }

    @JvmField
    @Rule
    val rule = object : ActivityTestRule<SignUpActivity>(SignUpActivity::class.java) {
        override fun beforeActivityLaunched() {
            SignUpActivity.loginApi = loginApi
            SignUpActivity.ioScheduler = testScheduler
            SignUpActivity.camera = camera
            SignUpActivity.system = system
        }
    }

    @Test
    fun shouldHaveLoginInput() {
        onId(R.id.loginInput).typeText("login").hasText("login")
    }

    @Test
    fun shouldShowIdleLoginValidationIndicatorOnStart() {
        onId(R.id.loginValidationIndicator).hasText(SignUp.LoginValidation.State.IDLE.toString())
    }

    @Test
    fun shouldShowLoadingValidationState() {
        enterSampleLoginIntoLoginInputAndAdvanceTime()
        onId(R.id.loginValidationIndicator).hasText(SignUp.LoginValidation.State.IN_PROGRESS.toString())
    }

    @Test
    fun shouldShowLoginAvailableValidationState() {
        enterSampleLoginIntoLoginInputAndAdvanceTime()
        loginApiSubject.onSuccess(true)
        onId(R.id.loginValidationIndicator).hasText(SignUp.LoginValidation.State.AVAILABLE.toString())
    }

    @Test
    fun shouldShowLoginTakenWhenApiReturnsThatItIsTaken() {
        enterSampleLoginIntoLoginInputAndAdvanceTime()
        loginApiSubject.onSuccess(false)
        onId(R.id.loginValidationIndicator).hasText(SignUp.LoginValidation.State.NOT_AVAILABLE.toString())
    }

    @Test
    fun shouldShowLoginValidationErrorWhenApiReturnsError() {
        enterSampleLoginIntoLoginInputAndAdvanceTime()
        loginApiSubject.onError(Throwable())
        onId(R.id.loginValidationIndicator).hasText(SignUp.LoginValidation.State.API_ERROR.toString())
    }

    @Test
    fun shouldShowIdleLoginValidationStateWhenLoginErased() {
        onId(R.id.loginInput).typeText("a")
        onId(R.id.loginInput).replaceText("")
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)
        loginApiSubject.onSuccess(false)
        onId(R.id.loginValidationIndicator).hasText(SignUp.LoginValidation.State.IDLE.toString())
    }

    private fun enterSampleLoginIntoLoginInputAndAdvanceTime() {
        onId(R.id.loginInput).typeText("a")
        testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)
    }

    @Test
    fun shouldTakePhotoOnTakePhotoClicked() {
        onId(R.id.takePhoto).click()
        systemSubject.onSuccess(true)
        cameraSubject.onSuccess("test")
        verify(camera).call()
    }

    @Test
    fun shouldTakePhotoButtonHasProperText() {
        onId(R.id.takePhoto).hasText("Take Photo")
    }

    @Test
    fun shouldRegisterButtonBeVisible() {
        onId(R.id.register).isDisplayed()
    }
}