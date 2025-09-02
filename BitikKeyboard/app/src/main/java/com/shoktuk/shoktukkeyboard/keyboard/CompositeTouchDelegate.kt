package com.shoktuk.shoktukkeyboard.keyboard

import android.graphics.Rect
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View

class CompositeTouchDelegate(private val host: View) : TouchDelegate(Rect(), host) {
    private val delegates = mutableListOf<TouchDelegate>()
    fun add(d: TouchDelegate) { delegates.add(d) }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        for (i in delegates.lastIndex downTo 0) if (delegates[i].onTouchEvent(event)) return true
        return false
    }
}

fun View.extendHorizontalHit(extraLeftPx: Int, extraRightPx: Int) {
    val parentView = parent as? View ?: return
    parentView.post {
        val rect = Rect()
        getHitRect(rect)
        rect.left -= extraLeftPx
        rect.right += extraRightPx

        val existing = parentView.touchDelegate
        val group = when (existing) {
            is CompositeTouchDelegate -> existing
            null -> CompositeTouchDelegate(parentView).also { parentView.touchDelegate = it } // CHANGED
            else -> CompositeTouchDelegate(parentView).also {                                // CHANGED
                it.add(existing)
                parentView.touchDelegate = it
            }
        }
        group.add(TouchDelegate(rect, this))
    }
}