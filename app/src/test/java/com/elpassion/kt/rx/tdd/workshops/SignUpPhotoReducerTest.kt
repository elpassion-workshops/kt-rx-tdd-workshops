package com.elpassion.kt.rx.tdd.workshops

import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
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

    @Test
    fun shouldNotHavePhotoWhenPhotoNotTaken() {
        events.accept(SignUp.Photo.TakePhotoEvent)
        permissionRequesterSubject.onSuccess(Unit)
        state.assertLastValue(SignUp.Photo.State.EMPTY)
    }

    @Test
    fun shouldHaveProperPhotoWhenPhotoTaken() {
        events.accept(SignUp.Photo.TakePhotoEvent)
        permissionRequesterSubject.onSuccess(Unit)
        photoRequesterSubject.onSuccess("other photo uri")
        state.assertLastValue(SignUp.Photo.State.Photo("other photo uri"))
    }

}
