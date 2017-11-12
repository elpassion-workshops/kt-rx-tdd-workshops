package com.elpassion.kt.rx.tdd.workshops.signup

import android.support.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test

class SignUpActivityTest {

    @JvmField
    @Rule
    val rule = ActivityTestRule<SignUpActivity>(SignUpActivity::class.java)

    @Test
    fun shouldStartActivity() {
    }
}