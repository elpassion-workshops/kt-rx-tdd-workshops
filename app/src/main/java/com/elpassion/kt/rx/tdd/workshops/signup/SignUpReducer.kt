package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables

class SignUpReducer(val api: (login: String) -> Single<Boolean>,
                    val cameraApi: () -> Maybe<String>, val permisionApi: () -> Single<Boolean>
) : Reducer<SignUp.State> {
    override fun invoke(events: Events): Observable<SignUp.State> =
            Observables.combineLatest(loginChangedEvents(events), takePhotoEvents(events), SignUp::State)

    private fun takePhotoEvents(events: Events): Observable<SignUp.Photo.State> {
        return events.ofType(SignUp.Photo.TakePhotoEvent::class.java)
                .switchMapSingle {
                    permisionApi.invoke()
                }
                .filter { it }
                .flatMapMaybe {
                    cameraApi.invoke()
                }
                .map<SignUp.Photo.State> { SignUp.Photo.State.Photo(it) }
                .startWith(SignUp.Photo.State.Empty)
    }

    private fun loginChangedEvents(events: Events): Observable<SignUp.LoginValidation.State> {
        return events.ofType(SignUp.LoginValidation.LoginChangedEvent::class.java)
                .switchMap { (login) ->
                    if (login.isEmpty()) {
                        Observable.just(SignUp.LoginValidation.State.IDLE)
                    } else {
                        api.invoke(login)
                                .toObservable()
                                .map {
                                    if (it) SignUp.LoginValidation.State.AVAILABLE
                                    else SignUp.LoginValidation.State.ISTAKEN
                                }
                                .onErrorReturn { SignUp.LoginValidation.State.APIFAIL }
                                .startWith(SignUp.LoginValidation.State.IN_PROGRESS)
                    }
                }
                .startWith(SignUp.LoginValidation.State.IDLE)
    }

}