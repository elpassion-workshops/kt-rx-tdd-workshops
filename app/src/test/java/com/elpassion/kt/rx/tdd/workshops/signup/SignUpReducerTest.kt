package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.*
import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockito_kotlin.*
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.SingleSubject
import org.junit.Test
import java.util.concurrent.TimeUnit

class SignUpReducerTest {

    private val events = PublishRelay.create<Any>()
    private val loginApiSubject = SingleSubject.create<Boolean>()
    private val loginApi: LoginApi = mock { on { checkLogin(any()) } doReturn loginApiSubject }
    private val cameraSubject = SingleSubject.create<String>()
    private val camera: Camera = mock { on { call() } doReturn cameraSubject }
    private val systemSubject = SingleSubject.create<Boolean>()
    private val system: System = mock { on { cameraPermission() } doReturn systemSubject }
    private val scheduler = TestScheduler()
    private val state = SignUpReducer(loginApi, camera, system, scheduler).invoke(events).test()

    @Test
    fun shouldLoginValidationStateBeIdleOnStart() {
        state.assertLastValueThat { loginValidation == LoginValidation.State.IDLE }
    }

    @Test
    fun shouldLoginValidationStateBeInProgressAfterUserTypeLogin() {
        events.accept(LoginValidation.LoginChangedEvent("a"))
        scheduler.advanceTimeBy(1001, TimeUnit.MILLISECONDS)
        state.assertLastValueThat { loginValidation == LoginValidation.State.IN_PROGRESS }
    }

    @Test
    fun shouldLoginValidationStateBeIdleAfterErasingLogin() {
        events.accept(LoginValidation.LoginChangedEvent(""))
        scheduler.advanceTimeBy(1001, TimeUnit.MILLISECONDS)
        state.assertLastValueThat { loginValidation == LoginValidation.State.IDLE }
    }

    @Test
    fun shouldLoginValidationStateBeAvailableWhenApiPasses() {
        events.accept(LoginValidation.LoginChangedEvent("a"))
        scheduler.advanceTimeBy(1001, TimeUnit.MILLISECONDS)
        loginApiSubject.onSuccess(true)
        state.assertLastValueThat { loginValidation == LoginValidation.State.AVAILABLE }
    }

    @Test
    fun shouldLoginValidationStateBeNotAvailableWhenApiReturnsThatItIsTaken() {
        events.accept(LoginValidation.LoginChangedEvent("b"))
        scheduler.advanceTimeBy(1001, TimeUnit.MILLISECONDS)
        loginApiSubject.onSuccess(false)
        state.assertLastValueThat { loginValidation == LoginValidation.State.NOT_AVAILABLE }
    }

    @Test
    fun shouldValidateLoginUsingPassedLogin() {
        events.accept(LoginValidation.LoginChangedEvent("b"))
        scheduler.advanceTimeBy(1001, TimeUnit.MILLISECONDS)
        verify(loginApi).checkLogin("b")
    }

    @Test
    fun shouldShowErrorWhenApiReturnsError() {
        events.accept(LoginValidation.LoginChangedEvent("b"))
        scheduler.advanceTimeBy(1001, TimeUnit.MILLISECONDS)
        loginApiSubject.onError(Throwable("Error"))
        state.assertLastValueThat { loginValidation == LoginValidation.State.API_ERROR }
    }

    @Test
    fun shouldPhotoStateBeEmptyAtTheBegging() {
        state.assertLastValueThat { addPhoto == AddPhoto.State.EMPTY }
    }

    @Test
    fun shouldCallCameraWhenTakingPhoto() {
        events.accept(AddPhoto.TakePhotoEvent)
        systemSubject.onSuccess(true)
        verify(camera).call()
    }

    @Test
    fun shouldNotCallCameraWithoutPermissionsWhenTakingPhoto() {
        events.accept(AddPhoto.TakePhotoEvent)
        systemSubject.onSuccess(false)
        verify(camera, never()).call()
    }

    @Test
    fun shouldShowPhotoFromCameraAfterTakingPhotoAndPermissionsGranted() {
        events.accept(AddPhoto.TakePhotoEvent)
        systemSubject.onSuccess(true)
        val uri = "Path"
        cameraSubject.onSuccess(uri)
        state.assertLastValueThat { addPhoto == AddPhoto.State.PhotoTaken(uri) }
    }

    @Test
    fun shouldCallApiOnceIfTwoLoginChangedEventsSentOneAfterAnother() {
        events.accept(LoginValidation.LoginChangedEvent("b"))
        events.accept(LoginValidation.LoginChangedEvent("bc"))
        scheduler.advanceTimeBy(1001, TimeUnit.MILLISECONDS)
        verify(loginApi).checkLogin(any())
    }
}


