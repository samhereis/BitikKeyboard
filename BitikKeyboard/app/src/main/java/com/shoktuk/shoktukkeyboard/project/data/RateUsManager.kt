package com.shoktuk.shoktukkeyboard.project.data

import android.app.Activity
import android.content.Context
import androidx.core.content.edit
import com.google.android.play.core.review.ReviewManagerFactory
import java.util.Calendar

/**
 * Shows the native Google Play in-app review popup on every 5th app open,
 * but at most once per calendar month: once shown in a given month, the next
 * prompt can only appear in a later month.
 */
object RateUsManager {
    private const val PREFS = "rate_prefs"
    private const val KEY_OPEN_COUNT = "appOpenCount"
    private const val KEY_LAST_SHOWN_MONTH = "rateLastShownMonth"
    private const val SHOW_EVERY = 5

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // Monotonic month identifier so comparisons work across year boundaries.
    private fun currentMonthKey(): Int {
        val c = Calendar.getInstance()
        return c.get(Calendar.YEAR) * 12 + c.get(Calendar.MONTH)
    }

    /** Call once per real app launch (not on configuration-change recreation). */
    fun onAppOpened(activity: Activity) {
        val p = prefs(activity)

        val count = p.getInt(KEY_OPEN_COUNT, 0) + 1
        p.edit { putInt(KEY_OPEN_COUNT, count) }

        // Only consider showing on every 5th open.
        if (count % SHOW_EVERY != 0) return

        // Already shown this calendar month -> wait until next month.
        val thisMonth = currentMonthKey()
        if (p.getInt(KEY_LAST_SHOWN_MONTH, -1) == thisMonth) return

        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow().addOnCompleteListener { request ->
            if (!request.isSuccessful) return@addOnCompleteListener
            manager.launchReviewFlow(activity, request.result).addOnCompleteListener {
                // Record once we've launched the flow (Play may silently no-op due to
                // its own quota, but we still treat this month as "asked").
                p.edit { putInt(KEY_LAST_SHOWN_MONTH, thisMonth) }
            }
        }
    }
}
