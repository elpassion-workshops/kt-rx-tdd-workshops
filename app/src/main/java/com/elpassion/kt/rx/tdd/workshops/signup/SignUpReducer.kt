package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables

class SignUpReducer(private val loginValidationApi: (String) -> Single<Boolean>,
                    private val cameraApi: () -> Maybe<String>,
                    private val permission: () -> Single<Boolean>) : Reducer<SignUp.State> {

    override fun invoke(events: Events): Observable<SignUp.State> {
        return Observables.combineLatest(loginValidationReducer(events), photoReducer(), SignUp::State)
    }

    private fun photoReducer(): Observable<SignUp.Photo.State> =
            permission()
                    .filter { it }
                    .flatMap {
                        cameraApi
                                .invoke()
                                .map<SignUp.Photo.State>(SignUp.Photo.State::Captured)
                    }.toObservable()
                    .startWith(SignUp.Photo.State.Empty)

    private fun loginValidationReducer(events: Events): Observable<SignUp.LoginValidation.State> {
        return events
                .ofType(SignUp.LoginValidation.LoginChangedEvent::class.java)
                .switchMap { (login) ->
                    if (login.isNotEmpty()) {
                        validateLogin(login)
                    } else {
                        Observable.just(SignUp.LoginValidation.State.IDLE)
                    }
                }
                .startWith(SignUp.LoginValidation.State.IDLE)
    }

    private fun validateLogin(login: String) = loginValidationApi.invoke(login)
            .toObservable()
            .map { isLoginAvailable ->
                if (isLoginAvailable) {
                    SignUp.LoginValidation.State.AVAILABLE
                } else {
                    SignUp.LoginValidation.State.NOT_AVAILABLE
                }
            }
            .onErrorReturnItem(SignUp.LoginValidation.State.ERROR)
            .startWith(SignUp.LoginValidation.State.IN_PROGRESS)
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
