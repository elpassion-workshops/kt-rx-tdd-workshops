package com.elpassion.kt.rx.tdd.workshops

import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.reactivex.Observable
import io.reactivex.subjects.SingleSubject
import org.junit.Test

class SignUpPhotoReducerTest {

    private val permissionRequesterSubject = SingleSubject.create<Unit>()
    private val permissionRequester = mock<SignUp.Photo.PermissionRequester> { on { request() } doReturn permissionRequesterSubject.toMaybe() }
    private val photoRequesterSubject = SingleSubject.create<String>()
    private val photoRequester = mock<SignUp.Photo.PhotoRequester> { on { request() } doReturn photoRequesterSubject.toMaybe() }
    private val events = PublishRelay.create<Any>()
    private val state = PhotoReducer(permissionRequester, photoRequester).invoke(events).test()

    @Test
    fun shouldStartWithEmptyPhoto() {
        state.assertLastValue(SignUp.Photo.State.EMPTY)
    }

    @Test
    fun shouldHavePhotoWhenPhotoTaken() {
        events.accept(SignUp.Photo.TakePhotoEvent)
        permissionRequesterSubject.onSuccess(Unit)
        photoRequesterSubject.onSuccess("photo uri")
        state.assertLastValue(SignUp.Photo.State.Photo("photo uri"))
    }

    @Test
    fun shouldNotHavePhotoWhenPermissionDenied() {
        events.accept(SignUp.Photo.TakePhotoEvent)
        state.assertLastValue(SignUp.Photo.State.EMPTY)
    }

    class PhotoReducer(private val permissionRequester: SignUp.Photo.PermissionRequester,
                       private val photoRequester: SignUp.Photo.PhotoRequester) : (Observable<Any>) -> Observable<SignUp.Photo.State> {
        override fun invoke(events: Observable<Any>): Observable<SignUp.Photo.State> {
            return events.ofType(SignUp.Photo.TakePhotoEvent::class.java)
                    .switchMap {
                        permissionRequester.request().toObservable()
                    }
                    .map<SignUp.Photo.State> { SignUp.Photo.State.Photo("photo uri") }
                    .startWith(SignUp.Photo.State.EMPTY)
        }
    }
}