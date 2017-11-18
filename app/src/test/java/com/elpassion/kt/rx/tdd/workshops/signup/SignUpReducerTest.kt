package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.*
import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.SingleSubject
import org.junit.Test

class SignUpReducerTest {

    private val apiSubject = SingleSubject.create<Boolean>()
    private val api = mock<(String) -> Single<Boolean>> { on { invoke(any()) } doReturn apiSubject }
    private val cameraSubject = SingleSubject.create<String>()
    private val cameraApi = mock<(() -> Single<String>)> { on { invoke() } doReturn cameraSubject }

    private val events = PublishRelay.create<Any>()

    private val permissionSubject = SingleSubject.create<Boolean>()
    private val permission = mock<(() -> Single<Boolean>)>().apply {
        whenever(this.invoke()).thenReturn(permissionSubject);
    }


    private val state = SignUpReducer(api, cameraApi, permission).invoke(events).test()

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
        events.accept(LoginValidation.LoginChangedEvent("a"))
        apiSubject.onSuccess(true)
        state.assertLastValueThat { loginValidation == LoginValidation.State.AVAILABLE }
    }

    @Test
    fun shouldLoginValidationStateBeNotAvailableWhenApiReturnsThatItIsTaken() {
        events.accept(LoginValidation.LoginChangedEvent("a"))
        apiSubject.onSuccess(false)
        state.assertLastValueThat { loginValidation == LoginValidation.State.NOT_AVAILABLE }
    }

    @Test
    fun shouldValidateLoginUsingPassedLogin() {
        val loginEvent = LoginValidation.LoginChangedEvent("a")
        events.accept(loginEvent)
        verify(api).invoke(loginEvent.login)
    }

    @Test
    fun shouldShowErrorWhenApiReturnsError() {
        val loginEvent = LoginValidation.LoginChangedEvent("a")
        events.accept(loginEvent)
        apiSubject.onError(Exception())
        state.assertLastValueThat { loginValidation == LoginValidation.State.ERROR }
    }

    @Test
    fun shouldPhotoStateBeEmptyAtTheBegging() {
        state.assertLastValueThat { photoState == Photo.State.Empty }
    }

    @Test
    fun shouldCallCameraWhenTakingPhoto() {
        events.accept(Photo.TakePhotoEvent)
        permissionSubject.onSuccess(true)
        verify(cameraApi).invoke()
    }

    @Test
    fun shouldNotCallCameraWithoutPermissionsWhenTakingPhoto() {
        events.accept(Photo.TakePhotoEvent)
        permissionSubject.onSuccess(false)
        verify(cameraApi, never()).invoke()
    }

    @Test
    fun shouldShowPhotoAfterTakingPhotoAndPermissionsGranted() {
        events.accept(Photo.TakePhotoEvent)
        permissionSubject.onSuccess(true)
        assert(cameraSubject.hasObservers())
//        cameraSubject.onSuccess("uri")
//        state.assertLastValueThat { photoState == Photo.State.Captured("uri") }
    }

    @Test
    fun shouldShowPhotoFromCameraAfterTakingPhotoAndPermissionsGranted(){
        events.accept(Photo.TakePhotoEvent)
        permissionSubject.onSuccess(true)
        val uri = "uri"
        cameraSubject.onSuccess(uri)
        state.assertLastValueThat {
            photoState == Photo.State.Captured("uri") }
    }
}

class SignUpReducer(private val loginValidationApi: (String) -> Single<Boolean>,
                    private val cameraApi: () -> Single<String>,
                    private val permission: () -> Single<Boolean>) : Reducer<SignUp.State> {

    override fun invoke(events: Events): Observable<SignUp.State> {
        return Observables.combineLatest(loginValidationReducer(events), photoReducer(), SignUp::State)
    }

    private fun photoReducer(): Observable<Photo.State> =
            permission()
                    .filter { it }
                    .flatMapSingle {
                        cameraApi
                                .invoke()
                                .map<Photo.State>(Photo.State::Captured)
                    }.toObservable()
                    .startWith(Photo.State.Empty)

    private fun loginValidationReducer(events: Events): Observable<LoginValidation.State> {
        return events
                .ofType(LoginValidation.LoginChangedEvent::class.java)
                .switchMap { (login) ->
                    if (login.isNotEmpty()) {
                        validateLogin(login)
                    } else {
                        Observable.just(LoginValidation.State.IDLE)
                    }
                }
                .startWith(LoginValidation.State.IDLE)
    }

    private fun validateLogin(login: String) = loginValidationApi.invoke(login)
            .toObservable()
            .map { isLoginAvailable ->
                if (isLoginAvailable) {
                    LoginValidation.State.AVAILABLE
                } else {
                    LoginValidation.State.NOT_AVAILABLE
                }
            }
            .onErrorReturnItem(LoginValidation.State.ERROR)
            .startWith(LoginValidation.State.IN_PROGRESS)
}

interface SignUp {
    data class State(val loginValidation: LoginValidation.State, val photoState: Photo.State)

    interface LoginValidation {
        data class LoginChangedEvent(val login: String)

        enum class State {
            IDLE,
            IN_PROGRESS,
            AVAILABLE,
            NOT_AVAILABLE,
            ERROR,
        }
    }

    interface Photo {
        sealed class State {
            object Empty : State()
            data class Captured(val photoUri: String) : State()
        }

        object TakePhotoEvent {
        }
    }
}
