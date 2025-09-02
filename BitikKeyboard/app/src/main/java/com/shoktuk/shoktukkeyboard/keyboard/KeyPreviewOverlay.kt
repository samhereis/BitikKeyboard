package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

class KeyPreviewOverlay(context: Context) : FrameLayout(context) {
    companion object {
        const val TAG_OVERLAY = "key_preview_overlay"
        const val TAG_HOST = "keyboard_root_host"

        fun ensureAttached(host: ViewGroup): KeyPreviewOverlay {
            val existing = host.findViewWithTag<KeyPreviewOverlay>(TAG_OVERLAY)
            if (existing != null) return existing
            val overlay = KeyPreviewOverlay(host.context).apply {
                tag = TAG_OVERLAY
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                isClickable = false
                isFocusable = false
                importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
                visibility = View.GONE
            }
            host.addView(overlay)
            return overlay
        }

        fun hide(host: ViewGroup?) {
            val o = host?.findViewWithTag<KeyPreviewOverlay>(TAG_OVERLAY)
            o?.visibility = View.GONE
            o?.removeAllViews()
        }
    }

    fun showFor(host: ViewGroup, anchor: View, content: View, aboveMarginPx: Int = 16) {
        // CHANGED: guard – must be descendant
        if (!isDescendantOf(host, anchor)) return

        removeAllViews()
        addView(content)

        val r = Rect()
        // CHANGED: convert anchor rect to host coords
        host.offsetDescendantRectToMyCoords(anchor, r)

        // Measure content
        val lp = content.layoutParams as LayoutParams? ?: LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
        )
        content.measure(MeasureSpec.makeMeasureSpec(host.width, MeasureSpec.AT_MOST),
            MeasureSpec.makeMeasureSpec(host.height, MeasureSpec.AT_MOST))
        val cw = content.measuredWidth
        val ch = content.measuredHeight

        val cx = r.centerX() - cw / 2
        val cy = r.top - ch - aboveMarginPx

        val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.START or Gravity.TOP
            leftMargin = cx.coerceIn(0, host.width - cw)
            topMargin = cy.coerceAtLeast(0)
        }
        content.layoutParams = params

        visibility = View.VISIBLE
        invalidate()
    }

    private fun isDescendantOf(parent: ViewGroup, child: View): Boolean {
        var cur: View? = child
        while (cur != null) {
            if (cur === parent) return true
            cur = (cur.parent as? View)
        }
        return false
    }
}
