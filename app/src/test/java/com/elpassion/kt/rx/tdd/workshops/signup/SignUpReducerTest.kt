package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.*
import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables.combineLatest
import io.reactivex.subjects.MaybeSubject
import io.reactivex.subjects.SingleSubject
import org.junit.Test
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
    private val state = SignUpReducer(api, camera, { permissionSubject }).invoke(events).test()

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
        verify(api).invoke(login)
    }

    @Test
    fun shouldShowErrorWhenApiReturnsError() {
        events.accept(LoginValidation.LoginChangedEvent("a"))
        apiSubject.onError(TimeoutException())
        state.assertLastValueThat { loginValidation == LoginValidation.State.ERROR }
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
        apiSubject.onSuccess(validated)
        state.assertLastValueThat { loginValidation == requiredState }
    }
}

class SignUpReducer(val api: (String) -> Single<Boolean>, val camera: () -> Maybe<String>, val permission: () -> Single<Boolean>) : Reducer<SignUp.State> {
    override fun invoke(events: Events): Observable<SignUp.State> =
            combineLatest(loginValidationReducer(events), photoValidationReducer(events), SignUp::State)

    private fun loginValidationReducer(events: Events): Observable<LoginValidation.State> {
        return events
                .ofType(LoginValidation.LoginChangedEvent::class.java)
                .switchMap {
                    if (it.login.isEmpty()) {
                        Observable.just(LoginValidation.State.IDLE)
                    } else {
                        api.invoke(it.login)
                                .map {
                                    if (it) LoginValidation.State.AVAILABLE else LoginValidation.State.TAKEN
                                }
                                .toObservable()
                                .onErrorReturnItem(LoginValidation.State.ERROR)
                                .startWith(LoginValidation.State.IN_PROGRESS)
                    }
                }
                .startWith(LoginValidation.State.IDLE)
    }

    private fun photoValidationReducer(events: Events): Observable<PhotoValidation.State> {
        return events
                .ofType(PhotoValidation.PhotoEvent::class.java)
                .flatMapMaybe<SignUp.PhotoValidation.State> {
                    permission.invoke()
                            .filter { it }
                            .flatMap {
                                camera.invoke().map { PhotoValidation.State.RETURNED(it) }
                            }
                }
                .startWith(PhotoValidation.State.EMPTY)
    }

}

interface SignUp {
    data class State(val loginValidation: LoginValidation.State, val photoValidation: PhotoValidation.State)

    interface LoginValidation {
        data class LoginChangedEvent(val login: String)

        enum class State {
            IDLE,
            IN_PROGRESS,
            AVAILABLE,
            TAKEN,
            ERROR
        }
    }

    interface PhotoValidation {

        class PhotoEvent

        sealed class State {
            object EMPTY : State()
            data class RETURNED(val path: String) : State()
        }
    }
}
