package com.elpassion.kt.rx.tdd.workshops

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.BoundedMatcher
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.widget.ImageView
import com.elpassion.android.commons.espresso.*
import com.elpassion.kt.rx.tdd.workshops.signup.SignUpActivity
import com.elpassion.kt.rx.tdd.workshops.signup.SignUpDI
import com.elpassion.kt.rx.tdd.workshops.utils.loadImageFromStorage
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.reactivex.subjects.MaybeSubject
import io.reactivex.subjects.SingleSubject
import org.hamcrest.Description
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
        val icon: Bitmap = BitmapFactory.decodeResource(InstrumentationRegistry.getTargetContext().resources, R.mipmap.ic_launcher)
        val uriString = InstrumentationRegistry.getTargetContext().save(icon)
        val bitmap = InstrumentationRegistry.getTargetContext().loadImageFromStorage(Uri.parse(uriString))
        onId(R.id.takePhoto).click()
        permissionSubject.onSuccess(Unit)
        cameraSubject.onSuccess(uriString)
        onId(R.id.photo).check(matches(HasSameBitmap(bitmap)))
    }
}

class HasSameBitmap(private val expectedBitmap: Bitmap) : BoundedMatcher<View, ImageView>(ImageView::class.java) {
    override fun describeTo(description: Description) {
        description.appendText("has bitmap does not match expected bitmap")
    }

    override fun matchesSafely(item: ImageView): Boolean {
        return (item.drawable as? BitmapDrawable)?.bitmap?.sameAs(expectedBitmap) ?: false
    }
}