package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.Observables.combineLatest
import io.reactivex.rxkotlin.withLatestFrom

class SignUpReducer(private val api: (login: String) -> Single<Boolean>,
                    private val camera: () -> Maybe<String>,
                    private val permissionSubject: () -> Maybe<Unit>,
                    private val signUpApi: (String, String) -> Completable) : Reducer<SignUp.State> {

    override fun invoke(events: Events): Observable<SignUp.State> {
        val login = validateLogin(events).share()
        val photo = takePhoto(events, permissionSubject, camera).share()
        return Observables.combineLatest(
                login,
                photo,
                register(events, login, photo),
                SignUp::State)
    }

    private fun register(events: Events, login: Observable<SignUp.LoginValidation.State>, photo: Observable<SignUp.Photo.State>): Observable<Boolean> {
        return events.ofType(SignUp.RegisterEvent::class.java)
                .withLatestFrom(combineLatest(
                        login,
                        photo,
                        { a, b -> (a as SignUp.LoginValidation.State.LoginAvailable) to (b as SignUp.Photo.State.Photo) }),
                        { _, b -> b })
                .flatMap { (login, photo) ->
                    signUpApi.invoke(login.login, photo.uri)
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
                        SignUp.LoginValidation.State.LoginAvailable(login)
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