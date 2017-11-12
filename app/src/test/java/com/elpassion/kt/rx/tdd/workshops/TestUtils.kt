package com.elpassion.kt.rx.tdd.workshops

import io.reactivex.observers.TestObserver
import junit.framework.Assert

fun <T> TestObserver<T>.assertLastValue(expected: T) {
    Assert.assertEquals(expected, values().last())
}