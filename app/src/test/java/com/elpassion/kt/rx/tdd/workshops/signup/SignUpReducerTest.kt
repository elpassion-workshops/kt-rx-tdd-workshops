package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.*
import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import org.junit.Test

class SignUpReducerTest {

    private val events = PublishRelay.create<Any>()
    private val loginApiSubject = SingleSubject.create<Boolean>()
    private val loginApi: LoginApi = mock { on { checkLogin(any()) } doReturn loginApiSubject }
    private val camera: Camera = mock()
    private val systemSubject = SingleSubject.create<Boolean>()
    private val system: System = mock { on { cameraPermission() } doReturn systemSubject }
    private val state = SignUpReducer(loginApi, camera, system).invoke(events).test()

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
        events.accept(LoginValidation.LoginChangedEvent(""))
        state.assertLastValueThat { loginValidation == LoginValidation.State.IDLE }
    }

    @Test
    fun shouldLoginValidationStateBeAvailableWhenApiPasses() {
        events.accept(LoginValidation.LoginChangedEvent("a"))
        loginApiSubject.onSuccess(true)
        state.assertLastValueThat { loginValidation == LoginValidation.State.AVAILABLE }
    }

    @Test
    fun shouldLoginValidationStateBeNotAvailableWhenApiReturnsThatItIsTaken() {
        events.accept(LoginValidation.LoginChangedEvent("b"))
        loginApiSubject.onSuccess(false)
        state.assertLastValueThat { loginValidation == LoginValidation.State.NOT_AVAILABLE }
    }

    @Test
    fun shouldValidateLoginUsingPassedLogin() {
        events.accept(LoginValidation.LoginChangedEvent("b"))
        verify(loginApi).checkLogin("b")
    }

    @Test
    fun shouldShowErrorWhenApiReturnsError() {
        events.accept(LoginValidation.LoginChangedEvent("b"))
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
}

class SignUpReducer(val api: LoginApi, val camera: Camera, val system: System) : Reducer<SignUp.State> {
    override fun invoke(events: Events): Observable<SignUp.State> {
        events.ofType(AddPhoto.TakePhotoEvent::class.java)
                .flatMapSingle { system.cameraPermission() }
                .filter { it }
                .doOnNext {
                    camera.call()
                }
                .subscribe()

        return events
                .ofType(LoginValidation.LoginChangedEvent::class.java)
                .switchMap(this::processUserLogin)
                .startWith(LoginValidation.State.IDLE)
                .map { State(it, AddPhoto.State.EMPTY) }
    }

    private fun processUserLogin(event: LoginValidation.LoginChangedEvent) = with(event) {
        if (login.isEmpty()) {
            Observable.just(LoginValidation.State.IDLE)
        } else {
            api.checkLogin(login)
                    .map { if (it) LoginValidation.State.AVAILABLE else LoginValidation.State.NOT_AVAILABLE }
                    .onErrorReturn { LoginValidation.State.API_ERROR }
                    .toObservable()
                    .startWith(LoginValidation.State.IN_PROGRESS)
        }
    }
}

interface SignUp {
    data class State(val loginValidation: LoginValidation.State, val addPhoto: AddPhoto.State)

    interface LoginValidation {
        data class LoginChangedEvent(val login: String)

        enum class State {
            IDLE,
            IN_PROGRESS,
            AVAILABLE,
            NOT_AVAILABLE,
            API_ERROR
        }
    }

    interface AddPhoto {

        object TakePhotoEvent

        enum class State {
            EMPTY
        }
    }
}

interface LoginApi {
    fun checkLogin(login: String): Single<Boolean>
}


interface Camera {
    fun call()
}

interface System {
    fun cameraPermission(): Single<Boolean>
}
