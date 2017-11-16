package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.assertLastValueThat
import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.subjects.SingleSubject
import org.junit.Test

class SignUpReducerTest {

    private val events = PublishRelay.create<Any>()
    private val apiSubject = SingleSubject.create<Boolean>()
    private val state = SignUpReducer({ apiSubject }).invoke(events).test()

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
}

interface SignUp {
    data class State(val loginValidation: LoginValidation.State)

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
}

class SignUpReducer(private val api: () -> SingleSubject<Boolean>) : Reducer<SignUp.State> {
    override fun invoke(events: Events): Observable<SignUp.State> {
        return events.ofType(SignUp.LoginValidation.LoginChangedEvent::class.java)
                .switchMap {
                    if (it.login.isEmpty()) {
                        just(SignUp.LoginValidation.State.IDLE)
                    } else {
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
                }
                .startWith(SignUp.LoginValidation.State.IDLE)
                .map { SignUp.State(it) }
    }
}
