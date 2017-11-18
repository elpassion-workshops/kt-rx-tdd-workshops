package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.*
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import org.junit.Test
import java.util.concurrent.TimeoutException

class SignUpReducerTest {

    private val events = PublishRelay.create<Any>()
    private val state = SignUpReducer({ apiSubject }).invoke(events).test()
    private val apiSubject = SingleSubject.create<Boolean>()

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
        validatePassedLoginString("bsdfsdfsd", true, LoginValidation.State.AVAILABLE)
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


    private fun validatePassedLoginString(login: String, validated: Boolean, requiredState: LoginValidation.State) {
        events.accept(LoginValidation.LoginChangedEvent(login))
        apiSubject.onSuccess(validated)
        state.assertLastValueThat { loginValidation == requiredState }
    }
}

class SignUpReducer(val api: () -> Single<Boolean>) : Reducer<SignUp.State> {
    override fun invoke(events: Events): Observable<SignUp.State> {
        return loginValidationReducer(events)
                .map { SignUp.State(it, PhotoValidation.State.EMPTY) }
    }

    private fun loginValidationReducer(events: Events): Observable<LoginValidation.State> {
        return events
                .ofType(LoginValidation.LoginChangedEvent::class.java)
                .switchMap {
                    if (it.login.isEmpty()) {
                        Observable.just(LoginValidation.State.IDLE)
                    } else {
                        api.invoke()
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

        sealed class State {
            object EMPTY : State()
        }
    }
}
