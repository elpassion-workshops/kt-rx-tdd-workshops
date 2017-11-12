package com.elpassion.kt.rx.tdd.workshops

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.junit.Test

class SignUpPhotoReducerTest {

    private val events = PublishRelay.create<Any>()
    private val state = PhotoReducer().invoke(events).test()

    @Test
    fun shouldStartWithEmptyPhoto() {
        state.assertLastValue(SignUp.Photo.State.EMPTY)
    }

    class PhotoReducer : (Observable<Any>) -> Observable<SignUp.Photo.State> {
        override fun invoke(events: Observable<Any>): Observable<SignUp.Photo.State> {
            return Observable.just(SignUp.Photo.State.EMPTY)
        }
    }
}