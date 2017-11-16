package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import com.elpassion.kt.rx.tdd.workshops.signup.SignUp.LoginValidation.ValidationResult.IDLE
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
        val loginAndPhoto = combineLatest(login, photo, { a, b -> a to b })
        val registerButton = registerButtonReducer(loginAndPhoto)
        return Observables.combineLatest(
                login,
                photo,
                register(events, loginAndPhoto),
                registerButton,
                SignUp::State)
    }

    private fun registerButtonReducer(loginAndPhoto: Observable<Pair<SignUp.LoginValidation.State, SignUp.Photo.State>>): Observable<Boolean> {
        return loginAndPhoto
                .map { (login, _) -> login.validationResult != IDLE }
                .startWith(false)
    }

    private fun register(events: Events, loginAndPhoto: Observable<Pair<SignUp.LoginValidation.State, SignUp.Photo.State>>): Observable<Boolean> {
        return events.ofType(SignUp.RegisterEvent::class.java)
                .withLatestFrom(loginAndPhoto,
                        { _, b -> b })
                .flatMap { (login, photo) ->
                    signUpApi.invoke(login.login, (photo as SignUp.Photo.State.Photo).uri)
                            .toSingleDefault(false)
                            .toObservable()
                            .startWith(true)
                }
                .startWith(false)
    }

    private fun validateLogin(events: Events): Observable<SignUp.LoginValidation.State> {
        return events
                .ofType(SignUp.LoginValidation.LoginChangedEvent::class.java)
                .switchMap { event ->
                    if (event.login.isNotEmpty()) {
                        validateLoginWithApi(event.login)
                                .map { SignUp.LoginValidation.State(event.login, it) }
                    } else {
                        Observable.just(SignUp.LoginValidation.State(login = event.login, validationResult = IDLE))
                    }
                }
                .startWith(SignUp.LoginValidation.State(login = "", validationResult = IDLE))
    }

    private fun validateLoginWithApi(login: String): Observable<SignUp.LoginValidation.ValidationResult> {
        return api(login)
                .toObservable()
                .map {
                    if (it) {
                        SignUp.LoginValidation.ValidationResult.AVAILABLE
                    } else {
                        SignUp.LoginValidation.ValidationResult.TAKEN
                    }
                }
                .onErrorReturnItem(SignUp.LoginValidation.ValidationResult.ERROR)
                .startWith(SignUp.LoginValidation.ValidationResult.LOADING)
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