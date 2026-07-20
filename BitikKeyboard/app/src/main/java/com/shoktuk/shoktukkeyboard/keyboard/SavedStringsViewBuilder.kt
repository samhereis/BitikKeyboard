package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.graphics.Typeface
import android.inputmethodservice.InputMethodService
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.shoktuk.shoktukkeyboard.project.screens.settings.loadSavedStrings
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme

object SavedStringsViewBuilder {
    fun create(
        service: InputMethodService, isCaps: Boolean, mode: KeyboardMode, onKeyPress: (String) -> Unit, onModeChange: (KeyboardMode) -> Unit
    ): LinearLayout {
        var systemKeybHeight = (KeyboardTheme.getButtonHeight() / 1.5f).toInt()
        val screenWidthPx = service.resources.displayMetrics.widthPixels
        val baseDesignWidthDp = 360f
        val density = service.resources.displayMetrics.density
        val maxWidthPx = (baseDesignWidthDp * density).toInt()
        val sideMarginPx = if (screenWidthPx > maxWidthPx) (screenWidthPx - maxWidthPx) / 2 else 0

        val root = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                if (screenWidthPx > maxWidthPx) maxWidthPx else ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                leftMargin = sideMarginPx
                rightMargin = sideMarginPx
            }
            setBackgroundColor(KeyboardTheme.containerBg())
        }

        val scroll = ScrollView(service).apply {
            isVerticalScrollBarEnabled = true
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, KeyboardTheme.getButtonHeight() * 3
            )
        }

        val flow = FlexboxLayout(service).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = com.google.android.flexbox.JustifyContent.FLEX_START
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val items = loadSavedStrings(service, MyKeyboardService.current_writingSystem)
        if (items.isEmpty()) {
            val tv = TextView(service).apply {
                text = "𐰀𐰯𐰘, 𐰅𐰲 𐰤𐰼𐰾𐰅 𐰳𐰹 𐰅𐰚𐰤 🥸"
                setTextColor(KeyboardTheme.getColor(3).toColorInt())
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, KeyboardTheme.getButtonHeight()
                )
            }
            flow.addView(tv)
        } else {
            val textSp = KeyboardTheme.getSystemButtonStyle(service).textSizeSp
            items.forEach { s ->
                val chip = TextView(service).apply {
                    text = s
                    maxLines = 1
                    typeface = Typeface.DEFAULT
                    setTextColor(KeyboardTheme.getColor(2).toColorInt())
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, textSp)
                    setPadding(dp(service, 10f), dp(service, 6f), dp(service, 10f), dp(service, 6f))
                    background = KeyboardTheme.createDrawableFromStyle(
                        service, com.shoktuk.shoktukkeyboard.ui.theme.ButtonStyle(
                            fillColor = KeyboardTheme.getColor(1),
                            borderColor = KeyboardTheme.getColor(5),
                            borderWidthDp = 1,
                            cornerRadiusDp = 8,
                            textColor = KeyboardTheme.getColor(2),
                            textSizeSp = KeyboardTheme.getSystemButtonStyle(service).textSizeSp
                        )
                    )
                    setOnClickListener { onKeyPress(s) }
                }
                val lp = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, systemKeybHeight
                ).apply {
                    setMargins(dp(service, 4f), dp(service, 4f), dp(service, 4f), dp(service, 4f))
                }
                chip.layoutParams = lp
                flow.addView(chip)
            }
        }

        scroll.addView(flow)
        root.addView(scroll)

        root.addView(
            BottomRowBuilder.createBottomRow(
                service, isCaps, mode, KeyboardTheme.getButtonHeight(), onModeChange
            )
        )
        return root
    }

    private fun dp(ctx: Context, v: Float): Int = (ctx.resources.displayMetrics.density * v).toInt()
}