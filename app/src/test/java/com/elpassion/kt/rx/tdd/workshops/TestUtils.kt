package com.elpassion.kt.rx.tdd.workshops

import io.reactivex.observers.TestObserver
import junit.framework.Assert

fun <T> TestObserver<T>.assertLastValue(expected: T) {
    Assert.assertEquals(expected, values().last())
}

fun <T> TestObserver<T>.assertLastValueThat(predicate: T.() -> Boolean) {
    Assert.assertTrue("Last value does not match predicate. ${values().last()}", values().last().predicate())
}