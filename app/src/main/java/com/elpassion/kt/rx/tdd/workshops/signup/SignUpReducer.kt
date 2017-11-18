package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables

class SignUpReducer(private val loginApi: () -> Single<Boolean>,
                    private val camera: () -> Maybe<String>,
                    private val cameraPermission: () -> Single<Boolean>) : Reducer<SignUp.State> {


    override fun invoke(events: Events): Observable<SignUp.State> {
        return Observables.combineLatest(handleLoginChangedEvents(events), handleTakePhotoEvents(), SignUp::State)
    }

    private fun handleTakePhotoEvents(): Observable<SignUp.Photo.State> =
            cameraPermission()
                    .filter { hasCameraPermission -> hasCameraPermission }
                    .flatMapObservable<SignUp.Photo.State> {
                        camera()
                                .map { uri -> SignUp.Photo.State.Taken(uri) }
                                .toObservable()
                    }
                    .startWith(SignUp.Photo.State.Empty)

    private fun handleLoginChangedEvents(events: Events): Observable<SignUp.LoginValidation.State> {
        return events
                .ofType(SignUp.LoginValidation.LoginChangedEvent::class.java)
                .switchMap(this::handleEvent)
                .startWith(SignUp.LoginValidation.State.IDLE)
    }

    private fun handleEvent(event: SignUp.LoginValidation.LoginChangedEvent) =
            if (event.login.isEmpty()) {
                Observable.just(SignUp.LoginValidation.State.IDLE)
            } else {
                callApi()
                        .onErrorReturn { SignUp.LoginValidation.State.ERROR }
            }

    private fun callApi() =
            loginApi().map {
                if (it) {
                    SignUp.LoginValidation.State.AVAILABLE
                } else {
                    SignUp.LoginValidation.State.TAKEN
                }
            }.toObservable()
                    .startWith(SignUp.LoginValidation.State.IN_PROGRESS)

}