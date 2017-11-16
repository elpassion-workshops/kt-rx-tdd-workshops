package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.subjects.MaybeSubject
import io.reactivex.subjects.SingleSubject
import org.junit.Test

class SignUpReducerTest {

    private val apiSubject = SingleSubject.create<Boolean>()
    private val permissionSubject = MaybeSubject.create<Unit>()
    private val permissionRequester = mock<() -> Maybe<Unit>> { on { invoke() } doReturn permissionSubject }
    private val cameraSubject = MaybeSubject.create<String>()
    private val camera = mock<() -> Maybe<String>> { on { invoke() } doReturn cameraSubject }
    private val api = mock<(String) -> Single<Boolean>> { on { invoke(any()) } doReturn apiSubject }
    private val signUpApi = mock<(String, String) -> Completable> { on { invoke(any(), any()) } doReturn Completable.never() }
    private val events = PublishRelay.create<Any>()
    private val state = SignUpReducer(api, camera, permissionRequester, signUpApi).invoke(events).test()

    @Test
    fun shouldLoginValidationStateBeIdleAtTheBegging() {
        state.assertLastValueThat { loginValidation.validationResult == SignUp.LoginValidation.ValidationResult.IDLE }
    }

    @Test
    fun shouldLoginValidationStateBeInProgressWhenNotEmptyLoginArrives() {
        events.accept(SignUp.LoginValidation.LoginChangedEvent("login"))
        state.assertLastValueThat { loginValidation.validationResult == SignUp.LoginValidation.ValidationResult.LOADING }
    }

    @Test
    fun shouldLoginValidationStateBeIdleAfterErasingLogin() {
        events.accept(SignUp.LoginValidation.LoginChangedEvent("login"))
        events.accept(SignUp.LoginValidation.LoginChangedEvent(""))
        state.assertLastValueThat { loginValidation.validationResult == SignUp.LoginValidation.ValidationResult.IDLE }
    }

    @Test
    fun shouldLoginValidationStateBeAvailableWhenApiPasses() {
        events.accept(SignUp.LoginValidation.LoginChangedEvent("login"))
        apiSubject.onSuccess(true)
        state.assertLastValueThat { loginValidation.validationResult == SignUp.LoginValidation.ValidationResult.AVAILABLE }
    }

    @Test
    fun shouldLoginValidationStateBeNotAvailableWhenApiReturnsThatItIsTaken() {
        events.accept(SignUp.LoginValidation.LoginChangedEvent("login"))
        apiSubject.onSuccess(false)
        state.assertLastValueThat { loginValidation.validationResult == SignUp.LoginValidation.ValidationResult.TAKEN }
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
        state.assertLastValueThat { loginValidation.validationResult == SignUp.LoginValidation.ValidationResult.ERROR }
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

    @Test
    fun shouldNotRequestPermissionAfterLoginChanges() {
        events.accept(SignUp.LoginValidation.LoginChangedEvent("login"))
        verify(permissionRequester, never()).invoke()
    }

    @Test
    fun shouldSendLoginAndPhotoToSignUpApiOnSend() {
        typeLoginAndTakePhoto()
        events.accept(SignUp.RegisterEvent)
        verify(signUpApi).invoke("login", "photo uri")
    }

    @Test
    fun shouldNotInvokeSignUpApiWithoutExplicitAction() {
        typeLoginAndTakePhoto()
        verify(signUpApi, never()).invoke(any(), any())
    }

    @Test
    fun shouldShowLoaderAfterSendingSignUpData() {
        typeLoginAndTakePhoto()
        events.accept(SignUp.RegisterEvent)
        state.assertLastValueThat { showLoader }
    }

    @Test
    fun shouldNotShowLoaderByDefault() {
        state.assertLastValueThat { !showLoader }
    }

    @Test
    fun shouldUseProperLoginAndUri() {
        typeLoginAndTakePhoto(login = "other login", photoUri = "other photo")
        events.accept(SignUp.RegisterEvent)
        verify(signUpApi).invoke("other login", "other photo")
    }

    @Test
    fun shouldCallApiEvenWhenValidationApiReturnsErrorAfterRegisterEvent() {
        events.accept(SignUp.LoginValidation.LoginChangedEvent("login"))
        apiSubject.onError(RuntimeException())
        takePhoto("photoUri")
        events.accept(SignUp.RegisterEvent)
        verify(signUpApi).invoke(any(), any())
    }

    @Test
    fun shouldRegisterButtonBeDisabledBeforeLoginIsAdded() {
        state.assertLastValueThat { !isRegisterEnabled }
    }

    private fun typeLoginAndTakePhoto(login: String = "login", photoUri: String = "photo uri") {
        events.accept(SignUp.LoginValidation.LoginChangedEvent(login))
        apiSubject.onSuccess(true)
        takePhoto(photoUri)
    }

    private fun takePhoto(photoUri: String = "photoUri") {
        events.accept(SignUp.Photo.TakePhotoEvent)
        permissionSubject.onSuccess(Unit)
        cameraSubject.onSuccess(photoUri)
    }
}
