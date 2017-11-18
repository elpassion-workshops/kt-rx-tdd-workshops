package com.elpassion.kt.rx.tdd.workshops.signup

import com.elpassion.kt.rx.tdd.workshops.common.Events
import com.elpassion.kt.rx.tdd.workshops.common.Reducer
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.Observables
import java.util.concurrent.TimeUnit

class SignUpReducer(private val api: LoginApi, private val camera: Camera, private val system: System, private val scheduler: Scheduler) : Reducer<SignUp.State> {
    override fun invoke(events: Events): Observable<SignUp.State> {
        return Observables.combineLatest(loginValidationReducer(events), photoValidationReducer(events), SignUp::State)
    }

    private fun loginValidationReducer(events: Events): Observable<SignUp.LoginValidation.State> {
        return events
                .ofType(SignUp.LoginValidation.LoginChangedEvent::class.java)
                .debounce(1, TimeUnit.SECONDS, scheduler)
                .switchMap(this::processUserLogin)
                .startWith(SignUp.LoginValidation.State.IDLE)
    }

    private fun photoValidationReducer(events: Events): Observable<SignUp.AddPhoto.State> {
        return events
                .ofType(SignUp.AddPhoto.TakePhotoEvent::class.java)
                .flatMapSingle { system.cameraPermission() }
                .filter { it }
                .switchMap {
                    camera.call()
                            .map { SignUp.AddPhoto.State.PhotoTaken(it) as SignUp.AddPhoto.State }
                            .toObservable()
                }
                .startWith(SignUp.AddPhoto.State.EMPTY)
    }

    private fun processUserLogin(event: SignUp.LoginValidation.LoginChangedEvent) = with(event) {
        if (login.isEmpty()) {
            Observable.just(SignUp.LoginValidation.State.IDLE)
        } else {
            api.checkLogin(login)
                    .toObservable()
                    .map { if (it) SignUp.LoginValidation.State.AVAILABLE else SignUp.LoginValidation.State.NOT_AVAILABLE }
                    .onErrorReturn { SignUp.LoginValidation.State.API_ERROR }
                    .startWith(SignUp.LoginValidation.State.IN_PROGRESS)
        }
    }
}