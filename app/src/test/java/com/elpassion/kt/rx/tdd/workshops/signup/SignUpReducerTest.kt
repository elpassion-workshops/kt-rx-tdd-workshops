package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.*
import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.MaybeSubject
import io.reactivex.subjects.SingleSubject
import org.junit.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class SignUpReducerTest {

    private val events = PublishRelay.create<Any>()
    private val permissionSubject = SingleSubject.create<Boolean>()
    private val apiSubject = SingleSubject.create<Boolean>()
    private val api = mock<(String) -> Single<Boolean>> {
        on { invoke(any()) }.thenReturn(apiSubject)
    }
    private val cameraSubject = MaybeSubject.create<String>()
    private val camera = mock<() -> Maybe<String>> {
        on { invoke() }.thenReturn(cameraSubject)
    }
    private val debounceScheduler = TestScheduler()
    private val state = SignUpReducer(api, camera, { permissionSubject }, debounceScheduler).invoke(events).test()

    @Test
    fun shouldLoginValidationStateBeIdleOnStart() {
        state.assertLastValueThat { loginValidation == LoginValidation.State.IDLE }
    }

    @Test
    fun shouldLoginValidationStateBeInProgressAfterUserTypeLogin() {
        events.accept(LoginValidation.LoginChangedEvent("a"))
        debounceScheduler.advanceTimeBy(5, TimeUnit.SECONDS)
        state.assertLastValueThat { loginValidation == LoginValidation.State.IN_PROGRESS }
    }

    @Test
    fun shouldLoginValidationStateBeIdleAfterErasingLogin() {
        events.accept(LoginValidation.LoginChangedEvent("a"))
        events.accept(LoginValidation.LoginChangedEvent(""))
        state.assertLastValueThat { loginValidation == LoginValidation.State.IDLE }
    }

    @Test
    fun shouldLoginValidationStateBeAvailableWhenApiPasses() {
        validatePassedLoginString("s", true, LoginValidation.State.AVAILABLE)
    }

    @Test
    fun shouldLoginValidationStateBeTakenWhenApiFails() {
        validatePassedLoginString("a", false, LoginValidation.State.TAKEN)
    }

    @Test
    fun shouldValidateLoginUsingPassedLogin() {
        val login = "ssdf"
        events.accept(LoginValidation.LoginChangedEvent(login))
        debounceScheduler.advanceTimeBy(5, TimeUnit.SECONDS)
        verify(api).invoke(login)
    }

    @Test
    fun shouldShowErrorWhenApiReturnsError() {
        events.accept(LoginValidation.LoginChangedEvent("a"))
        debounceScheduler.advanceTimeBy(5, TimeUnit.SECONDS)
        apiSubject.onError(TimeoutException())
        state.assertLastValueThat { loginValidation == LoginValidation.State.ERROR }
    }

    @Test
    fun shouldNotCallApiAfterTypingLoginButBeforeTimePasses() {
        events.accept(LoginValidation.LoginChangedEvent("a"))
        verify(api, never()).invoke(any())
    }

    @Test
    fun shouldShowLoadingImmediatelyAfterUserTypesLogin() {
        events.accept(LoginValidation.LoginChangedEvent("a"))
        state.assertLastValueThat { loginValidation == LoginValidation.State.IN_PROGRESS }
    }

    @Test
    fun shouldPhotoStateBeEmptyAtTheBegging() {
        state.assertLastValueThat { photoValidation == PhotoValidation.State.EMPTY }
    }

    @Test
    fun shouldCallCameraWhenTakingPhoto() {
        events.accept(PhotoValidation.PhotoEvent())
        permissionSubject.onSuccess(true)
        verify(camera).invoke()
    }

    @Test
    fun shouldNotCallCameraBeforeEmittingEvent() {
        verify(camera, times(0)).invoke()
    }

    @Test
    fun shouldNotCallCameraWithoutPermissionsWhenTakingPhoto() {
        events.accept(PhotoValidation.PhotoEvent())
        permissionSubject.onSuccess(false)
        verify(camera, never()).invoke()
    }

    @Test
    fun shouldShowPhotoAfterTakingPhotoAndPermissionsGranted() {
        val photoPath = "different photo path"
        permissionSubject.onSuccess(true)
        cameraSubject.onSuccess(photoPath)
        events.accept(PhotoValidation.PhotoEvent())
        state.assertLastValueThat { photoValidation == PhotoValidation.State.RETURNED(photoPath) }
    }

    @Test
    fun shouldShowPhotoFromCameraAfterTakingPhotoAndPermissionsGranted() {
        permissionSubject.onSuccess(true)
        val photoPath = "photo path"
        events.accept(PhotoValidation.PhotoEvent())
        cameraSubject.onSuccess(photoPath)
        state.assertLastValueThat { photoValidation == PhotoValidation.State.RETURNED(photoPath) }
    }

    private fun validatePassedLoginString(login: String, validated: Boolean, requiredState: LoginValidation.State) {
        events.accept(LoginValidation.LoginChangedEvent(login))
        debounceScheduler.advanceTimeBy(5, TimeUnit.SECONDS)
        apiSubject.onSuccess(validated)
        state.assertLastValueThat { loginValidation == requiredState }
    }
}

