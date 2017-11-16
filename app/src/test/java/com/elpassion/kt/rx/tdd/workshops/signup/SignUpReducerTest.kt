package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.MaybeSubject
import io.reactivex.subjects.SingleSubject
import org.junit.Test

class SignUpReducerTest {

    private val events = PublishRelay.create<Any>()
    private val cameraSubject = MaybeSubject.create<String>()
    private val apiSubject = SingleSubject.create<Boolean>()
    private val state: TestObserver<SignUp.State> = SignUpReducer({ apiSubject }, { cameraSubject }).invoke(events).test()

    @Test
    fun shouldLoginValidationStateBeIdleAtTheBegging() {
        state.assertLastValueThat { loginValidation == SignUp.LoginValidation.State.IDLE }
    }

    @Test
    fun shouldChangeLoginValidationStateToLoadingAfterChangingLogin() {
        events.accept(SignUp.LoginValidation.LoginChangedEvent("a"))
        state.assertLastValueThat { loginValidation == SignUp.LoginValidation.State.LOADING }
    }

    @Test
    fun shouldReturnToIdleStateAfterClearingLogin() {
        events.accept(SignUp.LoginValidation.LoginChangedEvent(""))
        state.assertLastValueThat { loginValidation == SignUp.LoginValidation.State.IDLE }
    }

    @Test
    fun shouldShowSuccessOnLoginAvailable() {
        events.accept(SignUp.LoginValidation.LoginChangedEvent("super awesome login"))
        apiSubject.onSuccess(true)
        state.assertLastValueThat { loginValidation == SignUp.LoginValidation.State.AVAILABLE }
    }

    @Test
    fun shouldShowLoginTakenOnLoginTaken() {
        events.accept(SignUp.LoginValidation.LoginChangedEvent("taken login"))
        apiSubject.onSuccess(false)
        state.assertLastValueThat { loginValidation == SignUp.LoginValidation.State.TAKEN }
    }

    @Test
    fun shouldShowErrorOnLoginApiError() {
        events.accept(SignUp.LoginValidation.LoginChangedEvent("taken login"))
        apiSubject.onError(RuntimeException())
        state.assertLastValueThat { loginValidation == SignUp.LoginValidation.State.ERROR }
    }


    @Test
    fun shouldTakePhotoOnTakePhoto() {
        events.accept(SignUp.Photo.TakePhotoEvent)
        cameraSubject.onSuccess("photo uri")
        state.assertLastValueThat { photo == SignUp.Photo.State.Taken("photo uri") }
    }
}

interface SignUp {
    data class State(val loginValidation: LoginValidation.State, val photo: Photo.State)

    interface LoginValidation {
        data class LoginChangedEvent(val login: String)

        enum class State {
            IDLE,
            LOADING,
            AVAILABLE,
            TAKEN,
            ERROR,
        }
    }

    interface Photo {
        object TakePhotoEvent
        sealed class State {
            data class Taken(val photo: String) : State()
        }
    }
}

class SignUpReducer(private val api: () -> SingleSubject<Boolean>, camera: () -> MaybeSubject<String>) : Reducer<SignUp.State> {
    override fun invoke(events: Events): Observable<SignUp.State> {
        return events.ofType(SignUp.LoginValidation.LoginChangedEvent::class.java)
                .switchMap {
                    if (it.login.isEmpty()) {
                        just(SignUp.LoginValidation.State.IDLE)
                    } else {
                        validateLoginWithApi()
                    }
                }
                .startWith(SignUp.LoginValidation.State.IDLE)
                .map { SignUp.State(it, SignUp.Photo.State.Taken("photo uri")) }
    }

    private fun validateLoginWithApi(): Observable<SignUp.LoginValidation.State> =
            api()
                    .map {
                        if (it) {
                            SignUp.LoginValidation.State.AVAILABLE
                        } else {
                            SignUp.LoginValidation.State.TAKEN
                        }
                    }
                    .toObservable()
                    .onErrorReturn { SignUp.LoginValidation.State.ERROR }
                    .startWith(SignUp.LoginValidation.State.LOADING)
}
