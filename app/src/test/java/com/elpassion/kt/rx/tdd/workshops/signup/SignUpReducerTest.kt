package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.MaybeSubject
import io.reactivex.subjects.SingleSubject
import org.junit.Test

class SignUpReducerTest {

    private val apiSubject = SingleSubject.create<Boolean>()
    private val permissionSubject = MaybeSubject.create<Unit>()
    private val cameraSubject = MaybeSubject.create<String>()
    private val camera = mock<() -> Maybe<String>> { on { invoke() } doReturn cameraSubject }
    private val api = mock<(String) -> Single<Boolean>> { on { invoke(any()) } doReturn apiSubject }
    private val events = PublishRelay.create<Any>()
    private val state = SignUpReducer(api, camera, { permissionSubject }).invoke(events).test()

    @Test
    fun shouldLoginValidationStateBeIdleAtTheBegging() {
        state.assertLastValueThat { loginValidation == SignUp.LoginValidation.State.IDLE }
    }

    @Test
    fun shouldLoginValidationStateBeInProgressWhenNotEmptyLoginArrives() {
        events.accept(SignUp.LoginValidation.LoginChangedEvent("login"))
        state.assertLastValueThat { loginValidation == SignUp.LoginValidation.State.LOADING }
    }

    @Test
    fun shouldLoginValidationStateBeIdleAfterErasingLogin() {
        events.accept(SignUp.LoginValidation.LoginChangedEvent("login"))
        events.accept(SignUp.LoginValidation.LoginChangedEvent(""))
        state.assertLastValueThat { loginValidation == SignUp.LoginValidation.State.IDLE }
    }

    @Test
    fun shouldLoginValidationStateBeAvailableWhenApiPasses() {
        events.accept(SignUp.LoginValidation.LoginChangedEvent("login"))
        apiSubject.onSuccess(true)
        state.assertLastValueThat { loginValidation == SignUp.LoginValidation.State.LOGIN_AVAILABLE }
    }

    @Test
    fun shouldLoginValidationStateBeNotAvailableWhenApiReturnsThatItIsTaken() {
        events.accept(SignUp.LoginValidation.LoginChangedEvent("login"))
        apiSubject.onSuccess(false)
        state.assertLastValueThat { loginValidation == SignUp.LoginValidation.State.LOGIN_TAKEN }
    }

    @Test
    fun shouldValidateLoginUsingPassedLogin() {
        events.accept(SignUp.LoginValidation.LoginChangedEvent("login"))
        verify(api).invoke("login")
    }

    @Test
    fun shouldShowErrorWhenApiReturnsError() {
        events.accept(SignUp.LoginValidation.LoginChangedEvent("login"))
        apiSubject.onError(RuntimeException())
        state.assertLastValueThat { loginValidation == SignUp.LoginValidation.State.ERROR }
    }

    @Test
    fun shouldPhotoStateBeEmptyAtTheBegging() {
        state.assertLastValueThat { photo == SignUp.Photo.State.EMPTY }
    }

    @Test
    fun shouldCallCameraWhenAddingPhoto() {
        events.accept(SignUp.Photo.TakePhotoEvent)
        permissionSubject.onSuccess(Unit)
        verify(camera).invoke()
    }

    @Test
    fun shouldNotCallCameraWithoutPermissionsWhenAddingPhoto() {
        events.accept(SignUp.Photo.TakePhotoEvent)
        permissionSubject.onComplete()
        verify(camera, never()).invoke()
    }

    @Test
    fun shouldShowPhotoFromCameraAfterTakingPhotoAndPermissionsGranted() {
        events.accept(SignUp.Photo.TakePhotoEvent)
        permissionSubject.onSuccess(Unit)
        cameraSubject.onSuccess("photo uri")
        state.assertLastValueThat { photo == SignUp.Photo.State.Photo("photo uri") }
    }
}

    }
}

