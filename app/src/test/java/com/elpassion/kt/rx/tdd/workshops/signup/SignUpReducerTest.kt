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
import io.reactivex.rxkotlin.Observables
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
            photoState is Photo.State.Taken &&
                    photoState.uri == imageUri
        }
    }
}

class SignUpReducer(private val loginApi: () -> Single<Boolean>,
                    private val camera: () -> Maybe<String>,
                    private val cameraPermission: () -> Single<Boolean>) : Reducer<SignUp.State> {


    override fun invoke(events: Events): Observable<SignUp.State> {
        return Observables.combineLatest(handleLoginChangedEvents(events), handleTakePhotoEvents(), SignUp::State)
    }

    private fun handleTakePhotoEvents(): Observable<Photo.State> =
            cameraPermission()
                    .filter { hasCameraPermission -> hasCameraPermission }
                    .flatMapObservable<Photo.State> {
                        camera()
                                .map { uri -> Photo.State.Taken(uri) }
                                .toObservable()
                    }
                    .startWith(Photo.State.Empty)

    private fun handleLoginChangedEvents(events: Events): Observable<LoginValidation.State> {
        return events
                .ofType(LoginValidation.LoginChangedEvent::class.java)
                .switchMap(this::handleEvent)
                .startWith(LoginValidation.State.IDLE)
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

        sealed class State {
            object Empty : Photo.State()
            class Taken(val uri: String) : Photo.State()
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
