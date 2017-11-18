package com.elpassion.kt.rx.tdd.workshops.signup

import android.support.test.espresso.ViewAssertion
import android.support.test.rule.ActivityTestRule
import com.elpassion.android.commons.espresso.*
import com.elpassion.kt.rx.tdd.workshops.R
import org.junit.Rule
import org.junit.Test
import com.elpassion.kt.rx.tdd.workshops.createTestBitmap
import com.elpassion.kt.rx.tdd.workshops.hasBitmap
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.MaybeSubject
import io.reactivex.subjects.SingleSubject
import java.util.concurrent.TimeUnit


class SignUpActivityTest {

    private val apiSubject = SingleSubject.create<Boolean>()
    private val debounceScheduler = TestScheduler()
    private val permissions = SingleSubject.create<Boolean>()
    private val camera = MaybeSubject.create<String>()

    @JvmField
    @Rule
    val rule = object : ActivityTestRule<SignUpActivity>(SignUpActivity::class.java) {
        override fun beforeActivityLaunched() {
            SignUp.api = { apiSubject }
            SignUp.debounceScheduler = debounceScheduler
            SignUp.camera = { camera }
            SignUp.permissions = { permissions }
        }
    }

    @Test
    fun shouldHaveLoginInput() {
        val text = "some example test"
        onId(R.id.singUpLogin).replaceText(text).hasText(text)
    }

    @Test
    fun shouldShowIdleLoginValidationIndicatorOnStart() {
        onId(R.id.signUpProgress).hasText(R.string.idle)
    }

    @Test
    fun shouldShowLoadingValidationState() {
        onId(R.id.singUpLogin).replaceText("a")
        onId(R.id.signUpProgress).hasText(R.string.loading)
    }

    @Test
    fun shouldShowLoginAvailableValidationState() {
        onId(R.id.singUpLogin).replaceText("a")
        debounceScheduler.advanceTimeBy(5, TimeUnit.SECONDS)
        apiSubject.onSuccess(true)
        onId(R.id.signUpProgress).hasText(R.string.available)
    }

    @Test
    fun shouldShowLoginTakenWhenApiReturnsThatItIsTaken() {
        onId(R.id.singUpLogin).replaceText("a")
        debounceScheduler.advanceTimeBy(5, TimeUnit.SECONDS)
        apiSubject.onSuccess(false)
        onId(R.id.signUpProgress).hasText(R.string.taken)
    }

    @Test
    fun shouldShowLoginValidationErrorWhenApiReturnsError() {
        onId(R.id.singUpLogin).replaceText("a")
        debounceScheduler.advanceTimeBy(5, TimeUnit.SECONDS)
        apiSubject.onError(RuntimeException())
        onId(R.id.signUpProgress).hasText(R.string.api_error)
    }

    @Test
    fun shouldShowIdleLoginValidationStateWhenLoginErased() {
        onId(R.id.singUpLogin).replaceText("a").replaceText("")
        onId(R.id.signUpProgress).hasText(R.string.idle)
    }

    @Test
    fun shouldTakePhotoOnTakePhotoClicked() {
        val testBitmap = createTestBitmap()
        assert(testBitmap.first.isNotEmpty())
        onId(R.id.signUpAddPhoto).click()
        permissions.onSuccess(true)
        camera.onSuccess(testBitmap.first)
        onId(R.id.signUpPhoto).hasBitmap(testBitmap.second)
    }

    @Test
    fun shouldTakePhotoButtonHasProperText() {
        val testBitmap = createTestBitmap()
        onId(R.id.signUpAddPhoto).hasText(R.string.add_photo)
        onId(R.id.signUpAddPhoto).click()
        permissions.onSuccess(true)
        camera.onSuccess(testBitmap.first)
        onId(R.id.signUpAddPhoto).hasText(R.string.change_photo)
    }

    @Test
    fun shouldRegisterButtonBeVisible() {
        onId(R.id.signUpRun).isDisplayed()
    }
}