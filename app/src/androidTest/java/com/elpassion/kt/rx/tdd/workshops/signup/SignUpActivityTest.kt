package com.elpassion.kt.rx.tdd.workshops.signup

import android.support.test.rule.ActivityTestRule
import com.elpassion.android.commons.espresso.hasText
import com.elpassion.android.commons.espresso.onId
import com.elpassion.android.commons.espresso.typeText
import com.elpassion.kt.rx.tdd.workshops.R
import org.junit.Rule
import org.junit.Test

class SignUpActivityTest {

    @JvmField
    @Rule
    val rule = ActivityTestRule<SignUpActivity>(SignUpActivity::class.java)

    @Test
    fun shouldStartActivity() {
    }

    @Test
    fun shouldHaveLoginInput() {
        onId(R.id.login_input)
                .typeText("login")
                .hasText("login")
    }


}