package com.elpassion.kt.rx.tdd.workshops

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.BoundedMatcher
import android.view.View
import android.widget.ImageView
import org.hamcrest.Description

fun ViewInteraction.hasBitmap(expectedBitmap: Bitmap) =
        check(matches(hasBitmapMatcher(expectedBitmap)))

private fun hasBitmapMatcher(expectedBitmap: Bitmap) = object : BoundedMatcher<View, ImageView>(ImageView::class.java) {
    override fun describeTo(description: Description) {
        description.appendText("has bitmap does not match expected bitmap")
    }

    override fun matchesSafely(item: ImageView) =
            (item.drawable as? BitmapDrawable)?.bitmap?.sameAs(expectedBitmap) ?: false
}
