package com.elpassion.kt.rx.tdd.workshops.signup

import android.support.test.rule.ActivityTestRule
import com.elpassion.android.commons.espresso.hasText
import com.elpassion.kt.rx.tdd.workshops.R
import org.junit.Rule
import org.junit.Test
import com.elpassion.android.commons.espresso.onId
import com.elpassion.android.commons.espresso.replaceText
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.SingleSubject
import java.util.concurrent.TimeUnit


class SignUpActivityTest {

    private val apiSubject = SingleSubject.create<Boolean>()
    private val debounceScheduler = TestScheduler()

    @JvmField
    @Rule
    val rule = object : ActivityTestRule<SignUpActivity>(SignUpActivity::class.java) {
        override fun beforeActivityLaunched() {
            SignUp.api = { apiSubject }
            SignUp.debounceScheduler = debounceScheduler
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
}