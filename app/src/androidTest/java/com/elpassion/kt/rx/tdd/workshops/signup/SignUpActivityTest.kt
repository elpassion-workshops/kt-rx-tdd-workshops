package com.elpassion.kt.rx.tdd.workshops.signup

import android.support.test.rule.ActivityTestRule
import com.elpassion.android.commons.espresso.hasText
import com.elpassion.kt.rx.tdd.workshops.R
import org.junit.Rule
import org.junit.Test
import com.elpassion.android.commons.espresso.onId
import com.elpassion.android.commons.espresso.replaceText


class SignUpActivityTest {

    @JvmField
    @Rule
    val rule = ActivityTestRule<SignUpActivity>(SignUpActivity::class.java)

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
}