package com.elpassion.kt.rx.tdd.workshops.signup

import android.support.test.rule.ActivityTestRule
import com.elpassion.android.commons.espresso.hasText
import com.elpassion.android.commons.espresso.onId
import com.elpassion.android.commons.espresso.typeText
import com.elpassion.kt.rx.tdd.workshops.R
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import org.junit.Rule
import org.junit.Test

class SignUpActivityTest {

    val apiSubject = SingleSubject.create<Boolean>()

    @JvmField
    @Rule
    val rule = object : ActivityTestRule<SignUpActivity>(SignUpActivity::class.java) {
        override fun beforeActivityLaunched() {
            SignUp.api = { apiSubject }
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
    fun shouldShowLoginTakenWhenApiReturnsThatItIsTaken(){
        onId(R.id.login_input).typeText("login")
        apiSubject.onSuccess(false)
        onId(R.id.login_validation_label).hasText(R.string.loginValidationIsTaken)
    }
}