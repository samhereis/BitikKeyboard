package com.shoktuk.shoktukkeyboard.keyboard

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

/**
 * "Space between" for the top row. While the centre slot (suggestions / transcription / glow) has nothing to show, it
 * collapses and the buttons, all at their normal size, share the free width equally: the first and last buttons sit at the row's edges
 * and the same gap goes between every pair ("space between"). As soon as the slot has content the gaps close again, so the
 * row looks exactly as it did before.
 */
object TopRowSpacing {
    /**
     * @param centre the slot that sits among the buttons (a child of [row]).
     * @param content the view whose children are the slot's content; it counts as empty when it has no child, or only blank text.
     * @param forceActive true while the slot must stay (e.g. the Auto Bitik glow is drawn on it).
     * @return a function that re-evaluates the layout, for changes [content]'s children don't show (the glow turning on or off).
     */
    /** True when [view] draws something: non-blank text, an image, or a descendant that does. */
    private fun shows(view: View): Boolean = when (view) {
        is TextView -> !view.text.isNullOrBlank()
        is android.widget.ImageView -> true
        is ViewGroup -> (0 until view.childCount).any { shows(view.getChildAt(it)) }
        else -> false
    }

    /** Gboard-style top row: the buttons keep their glyphs but lose their key background. */
    fun flatten(vararg buttons: View) = buttons.forEach { it.background = null }

    fun spreadEvenly(row: LinearLayout, centre: View, content: ViewGroup, forceActive: () -> Boolean = { false }): () -> Unit {
        val items = (0 until row.childCount).map { row.getChildAt(it) }
        val centreIndex = items.indexOf(centre)
        if (centreIndex < 0) return {}

        fun spacer() = View(row.context).apply { layoutParams = LinearLayout.LayoutParams(0, 1, 0f) }
        val gaps = ArrayList<View>()   // gaps[i] sits before items[i]; the last one after the final item
        row.removeAllViews()
        items.forEach { item ->
            gaps.add(spacer().also { row.addView(it) })
            row.addView(item)
        }
        gaps.add(spacer().also { row.addView(it) })

        fun hasContent(): Boolean {
            if (forceActive()) return true
            return shows(content)
        }

        fun apply() {
            val active = hasContent()
            centre.visibility = if (active) View.VISIBLE else View.GONE
            gaps.forEachIndexed { index, gap ->
                // Idle: every gap shares the free width; the one after the (hidden) centre would double up, so it goes.
                gap.visibility = if (!active && index == centreIndex + 1) View.GONE else View.VISIBLE
                (gap.layoutParams as LinearLayout.LayoutParams).weight = if (active || index == 0 || index == gaps.lastIndex) 0f else 1f
                gap.requestLayout()
            }
        }

        content.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View?, child: View?) { row.post { apply() } }
            override fun onChildViewRemoved(parent: View?, child: View?) { row.post { apply() } }
        })
        row.post { apply() }
        return { row.post { apply() } }
    }
}
