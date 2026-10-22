package com.shoktuk.shoktukkeyboard.keyboard

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.content.res.ColorStateList
import android.widget.SeekBar
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.shoktuk.shoktukkeyboard.MainActivity
import com.shoktuk.shoktukkeyboard.project.data.SettingScreens
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.savedStringsTextSize
import com.shoktuk.shoktukkeyboard.project.screens.settings.loadSavedStrings
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme
import java.util.Locale

object SavedStringsViewBuilder {
    fun create(
        service: InputMethodService, isCaps: Boolean, mode: KeyboardMode, onKeyPress: (String) -> Unit, onModeChange: (KeyboardMode) -> Unit
    ): LinearLayout {
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

        val accent = KeyboardTheme.getColorInt(4)
        val editRowHeight = dp(service, 40f)
        val chips = mutableListOf<TextView>()
        val minTextSp = 11
        val maxTextSp = 28

        val editRow = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, editRowHeight)
            setPadding(dp(service, 6f), 0, dp(service, 6f), 0)
        }
        val editButton = TextView(service).apply {
            text = "✎  ${editTitle()}"
            setTextColor(KeyboardTheme.getColor(2).toColorInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(dp(service, 14f), dp(service, 5f), dp(service, 14f), dp(service, 5f))
            background = tintedBackground(service, accent, 0.25f, 0, 20)
            setOnClickListener { openSavedStringsEditor(service) }
        }
        val sizeIcon = TextView(service).apply {
            text = "Aa"
            setTextColor(KeyboardTheme.getColor(2).toColorInt())
            alpha = 0.6f
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        }
        val sizeSlider = SeekBar(service).apply {
            max = maxTextSp - minTextSp
            progress = service.savedStringsTextSize.coerceIn(minTextSp, maxTextSp) - minTextSp
            progressTintList = ColorStateList.valueOf(accent)
            thumbTintList = ColorStateList.valueOf(accent)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(dp(service, 4f), 0, dp(service, 10f), 0)
            }
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar?, value: Int, fromUser: Boolean) {
                    chips.forEach { it.setTextSize(TypedValue.COMPLEX_UNIT_SP, (value + minTextSp).toFloat()) }
                }

                override fun onStartTrackingTouch(bar: SeekBar?) = Unit

                override fun onStopTrackingTouch(bar: SeekBar?) {
                    service.savedStringsTextSize = (bar?.progress ?: return) + minTextSp
                }
            })
        }
        editRow.addView(sizeIcon)
        editRow.addView(sizeSlider)
        editRow.addView(editButton)
        root.addView(editRow)

        val scroll = ScrollView(service).apply {
            isVerticalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, KeyboardTheme.getButtonHeight() * 3 - editRowHeight
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
                typeface = KeyboardTheme.bitikTypeface(service)
                setTextColor(KeyboardTheme.getColor(3).toColorInt())
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, KeyboardTheme.getButtonHeight()
                )
            }
            flow.addView(tv)
        } else {
            val textSp = service.savedStringsTextSize.coerceIn(minTextSp, maxTextSp).toFloat()
            items.forEach { s ->
                val chip = TextView(service).apply {
                    text = s
                    maxLines = 1
                    typeface = if (MyKeyboardService.current_writingSystem == com.shoktuk.shoktukkeyboard.project.data.WritingSystem.Bitik) {
                        KeyboardTheme.bitikTypeface(service) ?: Typeface.DEFAULT
                    } else {
                        Typeface.DEFAULT
                    }
                    setTextColor(KeyboardTheme.getColor(2).toColorInt())
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, textSp)
                    includeFontPadding = false
                    gravity = Gravity.CENTER
                    setPadding(dp(service, 8f), dp(service, 2f), dp(service, 8f), dp(service, 2f))
                    background = tintedBackground(service, accent, 0.25f, 1, 10)
                    setOnClickListener { onKeyPress(s) }
                }
                val lp = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(dp(service, 4f), dp(service, 4f), dp(service, 4f), dp(service, 4f))
                }
                chip.layoutParams = lp
                chips.add(chip)
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

    /** Accent-tinted fill with a thin accent outline, the same look as the iOS keyboard. */
    private fun tintedBackground(ctx: Context, accent: Int, fillAlpha: Float, strokeDp: Int, cornerDp: Int) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = dp(ctx, cornerDp.toFloat()).toFloat()
        setColor(ColorUtils.setAlphaComponent(accent, (fillAlpha * 255).toInt()))
        if (strokeDp > 0) setStroke(dp(ctx, strokeDp.toFloat()), ColorUtils.setAlphaComponent(accent, 140))
    }

    /** The keyboard doesn't load the app's localization files, so the one label lives here. */
    private fun editTitle(): String {
        val titles = mapOf("ky" to "Өзгөртүү", "tr" to "Düzenle", "kk" to "Өзгерту", "az" to "Redaktə et", "ru" to "Изменить")
        return titles[Locale.getDefault().language] ?: "Edit"
    }

    /** Opens the app straight on the saved strings editor (MainActivity reads [MainActivity.EXTRA_DESTINATION]). */
    private fun openSavedStringsEditor(service: InputMethodService) {
        val intent = Intent(service, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_DESTINATION, SettingScreens.SavedStrings.id)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        service.startActivity(intent)
    }

    private fun dp(ctx: Context, v: Float): Int = (ctx.resources.displayMetrics.density * v).toInt()
}