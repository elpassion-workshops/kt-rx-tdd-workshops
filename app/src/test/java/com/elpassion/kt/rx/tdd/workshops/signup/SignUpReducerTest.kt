package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.LoginValidation
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.Photo
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.MaybeSubject
import io.reactivex.subjects.SingleSubject
import org.junit.Assert
import org.junit.Test

class SignUpReducerTest {

    private val events = PublishRelay.create<Any>()
    private val loginApiSubject = SingleSubject.create<Boolean>()
    private val cameraSubject = MaybeSubject.create<String>()
    private val state = SignUpReducer({ loginApiSubject }, { cameraSubject }).invoke(events).test()

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
        state.assertLastValueThat { photoState == Photo.State.EMPTY }
    }

    @Test
    fun shouldCallCameraWhenTakingPhoto() {
        events.accept(Photo.TakePhotoEvent)
        Assert.assertTrue(cameraSubject.hasObservers())
    }
}

class SignUpReducer(private val loginApi: () -> Single<Boolean>, val camera: () -> Maybe<String>) : Reducer<SignUp.State> {
    override fun invoke(events: Events): Observable<SignUp.State> {
        camera().subscribe()
        return events
                .ofType(LoginValidation.LoginChangedEvent::class.java)
                .switchMap(this::handleEvent)
                .startWith(LoginValidation.State.IDLE)
                .map { validationState -> SignUp.State(validationState, Photo.State.EMPTY) }
    }

    private fun handleEvent(event: LoginValidation.LoginChangedEvent) =
            if (event.login.isEmpty()) {
                just(LoginValidation.State.IDLE)
            } else {
                callApi()
                        .onErrorReturn { LoginValidation.State.ERROR }
            }

    private fun callApi() =
            loginApi().map {
                if (it) {
                    LoginValidation.State.AVAILABLE
                } else {
                    LoginValidation.State.TAKEN
                }
            }.toObservable()
                    .startWith(LoginValidation.State.IN_PROGRESS)

}

interface SignUp {
    data class State(val loginValidation: LoginValidation.State, val photoState: Photo.State)

    interface Photo {

        enum class State {
            EMPTY,
        }

        object TakePhotoEvent
    }

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
}
