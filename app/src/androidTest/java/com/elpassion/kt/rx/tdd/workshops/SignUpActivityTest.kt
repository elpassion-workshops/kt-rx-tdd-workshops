package com.elpassion.kt.rx.tdd.workshops

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.elpassion.android.commons.espresso.hasText
import com.elpassion.android.commons.espresso.isDisplayed
import com.elpassion.android.commons.espresso.onId
import com.elpassion.android.commons.espresso.typeText
import com.elpassion.kt.rx.tdd.workshops.signup.SignUpActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpActivityTest {

    @JvmField
    @Rule
    val rule = ActivityTestRule<SignUpActivity>(SignUpActivity::class.java)

    @Test
    fun shouldHaveLoginInput() {
        onId(R.id.loginInput).typeText("login").hasText("login").isDisplayed()
    }
}