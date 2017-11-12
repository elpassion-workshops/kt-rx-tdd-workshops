package com.elpassion.kt.rx.tdd.workshops

import io.reactivex.Observable

typealias Reducer<T> = (Observable<Any>) -> Observable<T>