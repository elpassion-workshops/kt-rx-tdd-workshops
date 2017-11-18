package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.LoginValidation
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.Photo
import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.subjects.MaybeSubject
import io.reactivex.subjects.SingleSubject
import org.junit.Test

class SignUpReducerTest {

    private val events = PublishRelay.create<Any>()
    private val apiSubject = SingleSubject.create<Boolean>()
    private val cameraSubject = MaybeSubject.create<String>()
    private val permissionsSubject = SingleSubject.create<Boolean>()
    private val apiMock = mock<(String) -> SingleSubject<Boolean>> {
        on {
            invoke(any())
        }.thenReturn(apiSubject)
    }
    private val cameraMock= mock<()->MaybeSubject<String>>{
        on{
            invoke()
        }.thenReturn(cameraSubject)
    }

    private val state = SignUpReducer(apiMock, cameraMock, { permissionsSubject }).invoke(events).test()

    @Test
    fun shouldLoginValidationStateBeIdleOnStart() {
        state.assertLastValueThat { loginValidation == LoginValidation.State.IDLE }
    }

    @Test
    fun shouldLoginValidationStateBeInProgressAfterUserTypeLogin() {
        events.accept(LoginValidation.LoginChangedEvent("a"))
        state.assertLastValueThat { loginValidation == LoginValidation.State.IN_PROGRESS }
    }

    @Test
    fun shouldLoginValidationStateAvailableWhenApiReturnsTrue() {
        events.accept(LoginValidation.LoginChangedEvent("a"))
        apiSubject.onSuccess(true)
        state.assertLastValueThat { loginValidation == LoginValidation.State.AVAILABLE }
    }

    @Test
    fun shouldLoginValidationStateBeIdleAfterErasingLogin() {
        events.accept(LoginValidation.LoginChangedEvent(""))
        state.assertLastValueThat { loginValidation == LoginValidation.State.IDLE }
    }

    @Test
    fun shouldLoginValidationStateBeNotAvailableWhenApiReturnsThatItIsTaken() {
        events.accept(LoginValidation.LoginChangedEvent("b"))
        apiSubject.onSuccess(false)
        state.assertLastValueThat { loginValidation == LoginValidation.State.ISTAKEN }
    }

    @Test
    fun shouldValidateLoginUsingPassedLogin() {
        events.accept(LoginValidation.LoginChangedEvent("a"))
        verify(apiMock).invoke("a")
    }

    @Test
    fun shouldShowErrorWhenApiReturnsError() {
        events.accept(LoginValidation.LoginChangedEvent("a"))
        apiSubject.onError(RuntimeException())
        state.assertLastValueThat { loginValidation == LoginValidation.State.APIFAIL }
    }

    @Test
    fun shouldPhotoStateBeEmptyAtTheBegging() {
        state.assertLastValueThat { photoState == SignUp.Photo.State.Empty }
    }

    @Test
    fun shouldCallCameraWhenTakingPhoto() {
        events.accept(Photo.TakePhotoEvent)
        permissionsSubject.onSuccess(true)
        verify(cameraMock).invoke()
    }

    @Test
    fun shouldNotCallCameraWithoutPermissionsWhenTakingPhoto() {
        events.accept(Photo.TakePhotoEvent)
        permissionsSubject.onSuccess(false)
        verify(cameraMock, never()).invoke()
    }

    @Test
    fun shouldShowPhotoAfterTakingPhotoAndPermissionsGranted(){
        events.accept(Photo.TakePhotoEvent)
        permissionsSubject.onSuccess(true)
        assert(cameraSubject.hasObservers())
    }

    @Test
    fun shouldShowPhotoFromCameraAfterTakingPhotoAndPermissionsGranted(){
        events.accept(Photo.TakePhotoEvent)
        permissionsSubject.onSuccess(true)
        cameraSubject.onSuccess("photoURI")
        state.assertLastValueThat { photoState == SignUp.Photo.State.Photo("photoURI") }
    }
}


