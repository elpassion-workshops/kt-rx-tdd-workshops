package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.LoginValidation
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.Photo
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.subjects.MaybeSubject
import io.reactivex.subjects.SingleSubject
import org.junit.Assert
import org.junit.Test

class SignUpReducerTest {

    private val events = PublishRelay.create<Any>()
    private val loginApiSubject = SingleSubject.create<Boolean>()
    private val cameraSubject = MaybeSubject.create<String>()
    private val permissionProvider = SingleSubject.create<Boolean>()
    private val state = SignUpReducer({ loginApiSubject }, { cameraSubject }, { permissionProvider }).invoke(events).test()

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
    fun shouldLoginValidationStateBeIdleWhenUserClearsLogin() {
        events.accept(LoginValidation.LoginChangedEvent(""))
        state.assertLastValueThat { loginValidation == LoginValidation.State.IDLE }
    }

    @Test
    fun shouldLoginValidationStateBeAvailableWhenLoginIsAvailable() {
        events.accept(LoginValidation.LoginChangedEvent("login"))
        loginApiSubject.onSuccess(true)
        state.assertLastValueThat { loginValidation == LoginValidation.State.AVAILABLE }
    }

    @Test
    fun shouldLoginValidationStateBeTakenWhenLoginIsTaken() {
        events.accept(LoginValidation.LoginChangedEvent("login"))
        loginApiSubject.onSuccess(false)
        state.assertLastValueThat { loginValidation == LoginValidation.State.TAKEN }
    }

    @Test
    fun shouldShowErrorWhenApiReturnsError() {
        events.accept(LoginValidation.LoginChangedEvent("login"))
        loginApiSubject.onError(RuntimeException())
        state.assertLastValueThat { loginValidation == LoginValidation.State.ERROR }
    }

    @Test
    fun shouldPhotoStateBeEmptyAtTheBegging() {
        state.assertLastValueThat { photoState is Photo.State.Empty }
    }

    @Test
    fun shouldCallCameraWhenTakingPhoto() {
        events.accept(Photo.TakePhotoEvent)
        permissionProvider.onSuccess(true)
        Assert.assertTrue(cameraSubject.hasObservers())
    }

    @Test
    fun shouldNotCallCameraWithoutPermissionsWhenTakingPhoto() {
        events.accept(Photo.TakePhotoEvent)
        permissionProvider.onSuccess(false)
        Assert.assertFalse(cameraSubject.hasObservers())
    }

    @Test
    fun shouldShowPhotoFromCameraAfterTakingPhotoAndPermissionsGranted() {
        val imageUri = "image Url"
        events.accept(Photo.TakePhotoEvent)
        permissionProvider.onSuccess(true)
        cameraSubject.onSuccess(imageUri)
        state.assertLastValueThat {
                    photoState == Photo.State.Taken(imageUri)
        }
    }
}

