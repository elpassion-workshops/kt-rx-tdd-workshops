package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.withLatestFrom

class SignUpReducer(private val api: (login: String) -> Single<Boolean>,
                    private val camera: () -> Maybe<String>,
                    private val permissionSubject: () -> Maybe<Unit>,
                    private val signUpApi: (String, String) -> Completable) : Reducer<SignUp.State> {

    private val state = BehaviorRelay.create<SignUp.State>()

    override fun invoke(events: Events): Observable<SignUp.State> {
        Observables.combineLatest(
                validateLogin(events),
                takePhoto(events, permissionSubject, camera),
                register(events),
                SignUp::State).subscribe(state)
        return state
    }

    private fun register(events: Events): Observable<Boolean> {
        return events.ofType(SignUp.RegisterEvent::class.java)
                .withLatestFrom(events.ofType(SignUp.LoginValidation.LoginChangedEvent::class.java), { _, b -> b })
                .flatMap {
                    signUpApi.invoke(it.login, (state.value.photo as SignUp.Photo.State.Photo).uri)
                            .toSingleDefault(false)
                            .toObservable()
                            .startWith(true)
                }
                .startWith(false)
    }

    private fun validateLogin(events: Events): Observable<SignUp.LoginValidation.State> {
        return events
                .ofType(SignUp.LoginValidation.LoginChangedEvent::class.java)
                .switchMap {
                    if (it.login.isNotEmpty()) {
                        validateLoginWithApi(it.login)
                    } else {
                        Observable.just(SignUp.LoginValidation.State.IDLE)
                    }
                }
                .startWith(SignUp.LoginValidation.State.IDLE)
    }

    private fun validateLoginWithApi(login: String): Observable<SignUp.LoginValidation.State> {
        return api(login)
                .toObservable()
                .map {
                    if (it) {
                        SignUp.LoginValidation.State.LOGIN_AVAILABLE
                    } else {
                        SignUp.LoginValidation.State.LOGIN_TAKEN
                    }
                }
                .onErrorReturnItem(SignUp.LoginValidation.State.ERROR)
                .startWith(SignUp.LoginValidation.State.LOADING)
    }

    private fun takePhoto(events: Events, permissionSubject: () -> Maybe<Unit>, camera: () -> Maybe<String>): Observable<SignUp.Photo.State> {
        return events
                .ofType(SignUp.Photo.TakePhotoEvent::class.java)
                .flatMapMaybe {
                    permissionSubject()
                }
                .flatMapMaybe {
                    camera()
                }
                .map<SignUp.Photo.State> { SignUp.Photo.State.Photo(it) }
                .startWith(SignUp.Photo.State.EMPTY)
    }
}