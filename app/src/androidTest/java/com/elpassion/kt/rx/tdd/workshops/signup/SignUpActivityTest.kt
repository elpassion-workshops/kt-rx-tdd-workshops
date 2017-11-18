package com.elpassion.kt.rx.tdd.workshops.signup

import android.support.test.rule.ActivityTestRule
import com.elpassion.android.commons.espresso.hasText
import com.elpassion.android.commons.espresso.onId
import com.elpassion.android.commons.espresso.typeText
import com.elpassion.kt.rx.tdd.workshops.R
import com.elpassion.kt.rx.tdd.workshops.SignUpDI
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.reactivex.subjects.SingleSubject
import org.junit.Rule
import org.junit.Test

class SignUpActivityTest {

    private val cameraSubject = SingleSubject.create<String>()

    private val permissionSubject = SingleSubject.create<Boolean>()

    private val apiSubject =  SingleSubject.create<Boolean>()

    @JvmField
    @Rule
    val rule = object : ActivityTestRule<SignUpActivity>(SignUpActivity::class.java) {
        override fun beforeActivityLaunched() {
            super.beforeActivityLaunched()
            SignUpDI.api = mock { on { invoke(any())} doReturn apiSubject}
            SignUpDI.cameraApi = mock { on { invoke() } doReturn cameraSubject }
            SignUpDI.permission = mock { on { invoke() } doReturn permissionSubject }
        }
    }

    @Test
    fun shouldHaveLoginInput() {
        onId(R.id.loginInput).typeText("login").hasText("login")
    }

    @Test
    fun shouldShowIdleLoginValidationIndicatorOnStart() {
        onId(R.id.indicator).hasText("idle")
    }

    @Test
    fun shouldShowLoadingValidationState(){
        onId(R.id.loginInput).typeText("login")
        onId(R.id.indicator).hasText("loading")
    }
}
