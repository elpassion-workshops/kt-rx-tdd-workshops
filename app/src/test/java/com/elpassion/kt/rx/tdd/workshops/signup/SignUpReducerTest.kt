package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.*
import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
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

    private val state = SignUpReducer(apiMock, { cameraSubject },{permissionsSubject}).invoke(events).test()

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
        cameraSubject.onSuccess("photoURI")
        state.assertLastValueThat { photoState == SignUp.Photo.State.Photo("photoURI") }
    }

    @Test
    fun shouldNotCallCameraWithoutPermissionsWhenTakingPhoto() {
        events.accept(Photo.TakePhotoEvent)
        permissionsSubject.onSuccess(false)
        cameraSubject.onSuccess("photoURI")
        state.assertLastValueThat { photoState == SignUp.Photo.State.Empty }
    }
}

class SignUpReducer(val api: (login: String) -> Single<Boolean>,
                    val cameraApi: () -> Maybe<String>, val permisionApi: () -> Single<Boolean>
) : Reducer<SignUp.State> {
    override fun invoke(events: Events): Observable<SignUp.State> =
            Observables.combineLatest(loginChangedEvents(events), takePhotoEvents(events), SignUp::State)

    private fun takePhotoEvents(events: Events): Observable<Photo.State> {
        return events.ofType(Photo.TakePhotoEvent::class.java)
                .switchMapSingle {
                    permisionApi.invoke()
                }
                .filter { it }
                .flatMapMaybe {
                    cameraApi.invoke()
                }
                .map<Photo.State> { Photo.State.Photo(it) }
                .startWith(Photo.State.Empty)
    }

    private fun loginChangedEvents(events: Events): Observable<LoginValidation.State> {
        return events.ofType(LoginValidation.LoginChangedEvent::class.java)
                .switchMap { (login) ->
                    if (login.isEmpty()) {
                        Observable.just(LoginValidation.State.IDLE)
                    } else {
                        api.invoke(login)
                                .toObservable()
                                .map {
                                    if (it) LoginValidation.State.AVAILABLE
                                    else LoginValidation.State.ISTAKEN
                                }
                                .onErrorReturn { LoginValidation.State.APIFAIL }
                                .startWith(LoginValidation.State.IN_PROGRESS)
                    }
                }
                .startWith(LoginValidation.State.IDLE)
    }

}


interface SignUp {

    interface Photo {
        object TakePhotoEvent

        sealed class State {
            object Empty : State()
            data class Photo(val uri: String) : State()
        }
    }

    data class State(val loginValidation: LoginValidation.State, val photoState: Photo.State)


    interface LoginValidation {
        data class LoginChangedEvent(val login: String)

        enum class State {
            IDLE,
            IN_PROGRESS,
            AVAILABLE,
            ISTAKEN,
            APIFAIL
        }
    }
}
