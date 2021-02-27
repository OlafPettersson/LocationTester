package org.pettersson.locationtester.helper

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import android.view.ViewConfiguration

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout


class MySwipeRefreshLayout(context: Context, attrs: AttributeSet?) :
    SwipeRefreshLayout(context, attrs) {
    private val mTouchSlop: Int
    private var mPrevX = 0f

    // Indicate if we've already declined the move event
    private var mDeclined = false
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mPrevX = MotionEvent.obtain(event).x
                mDeclined = false // New action
            }
            MotionEvent.ACTION_MOVE -> {
                val eventX = event.x
                val xDiff = Math.abs(eventX - mPrevX)
                if (mDeclined || xDiff > mTouchSlop) {
                    mDeclined = true // Memorize
                    return false
                }
            }
        }
        return super.onInterceptTouchEvent(event)
    }

    init {
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }
}