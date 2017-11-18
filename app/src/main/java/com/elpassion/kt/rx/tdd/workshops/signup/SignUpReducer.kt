package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import java.util.concurrent.TimeUnit

class SignUpReducer(
        val api: (String) -> Single<Boolean>,
        val camera: () -> Maybe<String>,
        val permission: () -> Single<Boolean>,
        val debounceScheduler: Scheduler) : Reducer<SignUp.State> {

    override fun invoke(events: Events): Observable<SignUp.State> =
            Observables.combineLatest(loginValidationReducer(events), photoValidationReducer(events), SignUp::State)

    private fun loginValidationReducer(events: Events): Observable<SignUp.LoginValidation.State> {
        return events
                .ofType(SignUp.LoginValidation.LoginChangedEvent::class.java)
                .switchMap { event ->
                    if (event.login.isEmpty()) {
                        Observable.just(SignUp.LoginValidation.State.IDLE)
                    } else {
                        Single.timer(5, TimeUnit.SECONDS, debounceScheduler)
                                .flatMapObservable {
                                    api.invoke(event.login)
                                            .map {
                                                if (it) SignUp.LoginValidation.State.AVAILABLE else SignUp.LoginValidation.State.TAKEN
                                            }
                                            .toObservable()
                                            .onErrorReturnItem(SignUp.LoginValidation.State.ERROR)
                                }
                                .startWith(SignUp.LoginValidation.State.IN_PROGRESS)
                    }
                }
                .startWith(SignUp.LoginValidation.State.IDLE)
    }

    private fun photoValidationReducer(events: Events): Observable<SignUp.PhotoValidation.State> {
        return events
                .ofType(SignUp.PhotoValidation.PhotoEvent::class.java)
                .flatMapMaybe<SignUp.PhotoValidation.State> {
                    permission.invoke()
                            .filter { it }
                            .flatMap {
                                camera.invoke().map { SignUp.PhotoValidation.State.RETURNED(it) }
                            }
                }
                .startWith(SignUp.PhotoValidation.State.EMPTY)
    }

}