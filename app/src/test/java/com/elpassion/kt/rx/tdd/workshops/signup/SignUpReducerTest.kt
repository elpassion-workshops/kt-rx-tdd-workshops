package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.*
import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiConsumer
import io.reactivex.subjects.SingleSubject
import org.junit.Test
import org.reactivestreams.Subscriber

class SignUpReducerTest {

    private val apiSubject = SingleSubject.create<Boolean>()
    private val api = mock<(String) -> Single<Boolean>>().apply {
        whenever(this.invoke(any())).thenReturn(apiSubject)
    }
    private val cameraMock = mock<() -> Single<String>>()
    private val events = PublishRelay.create<Any>()

    private val permissionSubject = SingleSubject.create<Boolean>()
    private val permission = mock<(() -> Single<Boolean>)>().apply {
        whenever(this.invoke()).thenReturn(permissionSubject);
    }
    private val state = SignUpReducer(api, cameraMock, permission).invoke(events).test()

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
        state.assertLastValueThat { photoState == Photo.State.EMPTY }
    }

    @Test
    fun shouldCallCameraWhenTakingPhoto(){
        events.accept(Photo.TakePhotoEvent)
        permissionSubject.onSuccess(true)
        verify(cameraMock).invoke()
    }

    @Test
    fun shouldNotCallCameraWithoutPermissionsWhenTakingPhoto() {
        events.accept(Photo.TakePhotoEvent)
        permissionSubject.onSuccess(false)
        verify(cameraMock, never()).invoke()
    }
}

class SignUpReducer(private val loginValidationApi: (String) -> Single<Boolean>,
                    private val cameraApi: () -> Single<String>,
                    private val permission: () -> Single<Boolean>) : Reducer<SignUp.State> {
    override fun invoke(events: Events): Observable<SignUp.State> {
        permission().subscribe { hasPermission ->
                if(hasPermission) {
                    cameraApi.invoke()
                }
        }

        return loginValidationReducer(events)
                .map {State(it)}
    }

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
    data class State(val loginValidation: LoginValidation.State, val photoState: Photo.State = Photo.State.EMPTY)

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

    interface Photo{
        enum class State{
            EMPTY,
        }

        object TakePhotoEvent {
        }

    }
}
