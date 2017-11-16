package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables

class SignUpReducer(private val api: (login: String) -> Single<Boolean>,
                    private val camera: () -> Maybe<String>,
                    private val permissionSubject: () -> Maybe<Unit>,
                    private val signUpApi: (String, String) -> Completable) : Reducer<SignUp.State> {

    override fun invoke(events: Events): Observable<SignUp.State> {
        signUpApi.invoke("login", "photo uri")
        return Observables.combineLatest(validateLogin(events), takePhoto(events, permissionSubject, camera), SignUp::State)
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