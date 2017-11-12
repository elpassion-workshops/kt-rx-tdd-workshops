package com.elpassion.kt.rx.tdd.workshops

import io.reactivex.Observable

class PhotoReducer(private val permissionRequester: SignUp.Photo.PermissionRequester,
                   private val photoRequester: SignUp.Photo.PhotoRequester) : Reducer<SignUp.Photo.State> {
    override fun invoke(events: Observable<Any>): Observable<SignUp.Photo.State> {
        return events.ofType(SignUp.Photo.TakePhotoEvent::class.java)
                .switchMap { permissionRequester.request().toObservable() }
                .switchMap { photoRequester.request().toObservable() }
                .map<SignUp.Photo.State> { SignUp.Photo.State.Photo(it) }
                .startWith(SignUp.Photo.State.EMPTY)
    }
}