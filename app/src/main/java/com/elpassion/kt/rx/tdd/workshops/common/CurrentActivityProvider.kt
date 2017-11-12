package com.elpassion.kt.rx.tdd.workshops.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Bundle
import io.reactivex.Single
import io.reactivex.Single.just
import io.reactivex.subjects.SingleSubject

@SuppressLint("StaticFieldLeak")
object CurrentActivityProvider : Application.ActivityLifecycleCallbacks, () -> Single<Activity> {

    private val subjects = mutableSetOf<SingleSubject<Activity>>()
    private var activity: Activity? = null

    override fun invoke(): Single<Activity> {
        return if (activity != null) {
            just(activity)
        } else {
            SingleSubject.create<Activity>().apply {
                subjects.add(this)
            }
        }
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) = setActivity(activity)

    override fun onActivityStarted(activity: Activity?) = setActivity(activity)

    override fun onActivityResumed(activity: Activity?) = setActivity(activity)

    private fun setActivity(activity: Activity?) {
        CurrentActivityProvider.activity = activity
        subjects.forEach {
            it.onSuccess(activity!!)
        }
        subjects.clear()
    }

    override fun onActivityPaused(activity: Activity?) {
        CurrentActivityProvider.activity = null
    }

    override fun onActivityStopped(activity: Activity?) {
        CurrentActivityProvider.activity = null
    }

    override fun onActivityDestroyed(activity: Activity?) {
        CurrentActivityProvider.activity = null
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) = Unit
}