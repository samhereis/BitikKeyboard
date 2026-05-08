package com.shoktuk.shoktukkeyboard.keyboard

import Haptics
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.InsetDrawable
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.toColorInt
import androidx.core.widget.TextViewCompat
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.vibrations
import com.shoktuk.shoktukkeyboard.project.data.SettingsManager.writingSystem
import com.shoktuk.shoktukkeyboard.project.data.Vibrations
import com.shoktuk.shoktukkeyboard.project.data.WritingSystem
import com.shoktuk.shoktukkeyboard.ui.theme.ButtonStyle
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme
import com.shoktuk.shoktukkeyboard.ui.theme.KeyboardTheme.dpToPx

object SystemKeyBuilder {
    private const val LONG_PRESS_DELAY = 250L
    var longPressRunnable: Runnable? = null
    fun systemButton_Text(
        service: InputMethodService,
        text: String,
        buttonHeight: Int,
        bald: Boolean = true,
        style: ButtonStyle = KeyboardTheme.getSystemButtonStyle(service),
        onClick: (() -> Unit)? = null,
        onLongClick: (() -> Unit)? = null,
        textId: Int? = null,
        weight: Float = 0f
    ): View {
        val root = newContainer(service, style, buttonHeight, weight).apply {
            isHapticFeedbackEnabled = true
        }

        root.addView(makeCenteredContent(service, style, buttonHeight, bald, textId, textToSet = text))

        root.setOnClickListener {
            onClick?.invoke()
        }

        root.setOnLongClickListener {
            onLongClick?.invoke()
            true
        }

        return root
    }

    fun systemButton_Icon(
        service: InputMethodService, assetPath: String, buttonHeight: Int, style: ButtonStyle = KeyboardTheme.getSystemButtonStyle(service), onClick: (() -> Unit)? = null, weight: Float = 0f
    ): View {
        val root = newContainer(service, style, buttonHeight, weight)
        root.addView(makeCenteredContent(service, style, buttonHeight, iconAssetPath = assetPath))
        root.setOnClickListener {
            onClick?.invoke()
        }
        return root
    }

    fun expandableSystemButton_Text(
        service: InputMethodService, text: String, buttonHeight: Int, textToCommit: String, onClick: (() -> Unit)? = null, style: ButtonStyle = KeyboardTheme.getLetterButtonStyle_Normal(service)
    ): View {
        val root = newContainer(service, style, buttonHeight, weight = 1f)
        root.addView(makeCenteredContent(service, style, buttonHeight, textToSet = text))
        root.setOnClickListener {
            onKeyPressed?.invoke(service.currentInputConnection, textToCommit, true)
        }
        root.setOnLongClickListener {
            onClick?.invoke()
            true
        }
        return root
    }

    fun expandableSystemButton_Icon(
        service: InputMethodService,
        assetPath: String,
        textToCommit: String,
        buttonHeight: Int,
        style: ButtonStyle = KeyboardTheme.getLetterButtonStyle_Normal(service),
        onLongClick: (() -> Unit)? = null
    ): View {
        val root = newContainer(service, style, buttonHeight, weight = 1f)
        root.addView(makeCenteredContent(service, style, buttonHeight, iconAssetPath = assetPath))
        root.setOnClickListener {
            onKeyPressed?.invoke(service.currentInputConnection, textToCommit, true)
        }

        root.setOnLongClickListener {
            onLongClick?.invoke()
            true
        }
        return root
    }

    fun forKey(
        service: InputMethodService,
        key: KeyEntry,
        isCaps: Boolean,
        buttonHeight: Int,
        maxKeyCount: Int,
        onCapsChange: (Boolean) -> Unit,
        baseStyle: ButtonStyle = KeyboardTheme.getSystemButtonStyle(service)
    ): View {
        val root = newContainer(service, baseStyle, buttonHeight, maxKeyCount = maxKeyCount)

        val icon = when (key.name) {
            "Shift" -> if (service.writingSystem == WritingSystem.Arab) {
                KeyboardTheme.BOOKMARK_ICON
            } else {
                if (isCaps) {
                    KeyboardTheme.SHIFT_ICON_FILE_Filled
                } else {
                    KeyboardTheme.SHIFT_ICON_FILE
                }
            }

            "Del" -> KeyboardTheme.DELETE_ICON_FILE
            else -> null
        }
        root.addView(makeCenteredContent(service, baseStyle, buttonHeight, textToSet = key.lowercase, iconAssetPath = icon))

        when (key.name) {
            "Shift" -> wireShift(root, onCapsChange)
            "Del" -> wireDelete(root, service)
            else -> root.setOnClickListener { Haptics.perform(it, HapticFeedbackConstants.KEYBOARD_TAP) }
        }
        return root
    }

    private fun newContainer(
        service: InputMethodService, style: ButtonStyle, buttonHeight: Int, weight: Float = 0f, maxKeyCount: Int = 10
    ): FrameLayout {
        return FrameLayout(service).apply {
            isClickable = true
            isFocusable = true
            foregroundGravity = Gravity.CENTER
            layoutParams = if (weight == 0f) {
                LinearLayout.LayoutParams(
                    KeyboardTheme.getSystemButtonWidth(service), buttonHeight
                )
            } else {
                LinearLayout.LayoutParams(0, buttonHeight, weight)
            }
            val inset_h = dpToPx(service, KeyboardTheme.KEY_MARGIN_DP_OnlyVisual_H * 2)
            val inset_v = dpToPx(service, KeyboardTheme.KEY_MARGIN_DP_OnlyVisual_V)
            val pill = KeyboardTheme.createDrawableFromStyle(service, style)
            background = InsetDrawable(pill, inset_h, inset_v, inset_h, inset_v)
        }
    }

    private fun makeCenteredContent(
        service: InputMethodService, style: ButtonStyle, buttonHeight: Int, bald: Boolean = true, textId: Int? = null, textToSet: String? = null, iconAssetPath: String? = null
    ): View {
        val container = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER
            )
        }
        val textColorInt = style.textColor.toColorInt()

        if (iconAssetPath != null) {
            KeyboardTheme.loadAssetDrawable(service, iconAssetPath)?.let { d ->
                val icon = DrawableCompat.wrap(d).mutate()
                DrawableCompat.setTint(icon, textColorInt)
                val size = (buttonHeight / 2.5).toInt()
                val iv = ImageView(service).apply {
                    layoutParams = LinearLayout.LayoutParams(size, size).apply { gravity = Gravity.CENTER }
                    setImageDrawable(icon)
                }
                container.addView(iv)
            }
        }

        if (!textToSet.isNullOrEmpty()) {
            val tv = TextView(service).apply {
                gravity = Gravity.CENTER
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                isAllCaps = false
                includeFontPadding = false
                setTextColor(textColorInt)
                setSingleLine(false)
                maxLines = 1
                ellipsize = null
                textSize = buttonHeight / 6f

                if (bald) {
                    typeface = Typeface.DEFAULT_BOLD
                    paint.isFakeBoldText = true
                    paint.strokeWidth = 0.5f
                    paint.style = Paint.Style.FILL_AND_STROKE
                }

                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { gravity = Gravity.CENTER }

                text = textToSet
                if (textId != null) {
                    id = textId
                }
            }

            val maxSp = KeyboardTheme.getLetterButtonStyle_Normal(service).textSizeSp.toInt()
            val minSp = 1
            val stepSp = 1
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                tv, minSp, maxSp, stepSp, TypedValue.COMPLEX_UNIT_SP
            )

            container.addView(tv)
        }
        return container
    }

    private fun wireShift(root: View, onCapsChange: (Boolean) -> Unit) {
        root.setOnClickListener {
            try {
                if (MyKeyboardService.context.vibrations == Vibrations.On) {
                    Haptics.perform(root, HapticFeedbackConstants.KEYBOARD_TAP)
                }
            } catch (e: Exception) {
                print(e.message)
            }

            onCapsChange(!MyKeyboardService.isCaps)
        }
    }

    private fun wireDelete(root: View, service: InputMethodService) {
        val handler = Handler(Looper.getMainLooper())
        val interval = 100L
        val repeater = object : Runnable {
            override fun run() {
                performDelete(service)
                try {
                    if (MyKeyboardService.context.vibrations == Vibrations.On) {
                        Haptics.perform(root, HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                } catch (e: Exception) {
                    print(e.message)
                }
                handler.postDelayed(this, interval)
            }
        }
        root.setOnTouchListener { v, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    try {
                        if (MyKeyboardService.context.vibrations == Vibrations.On) {
                            Haptics.perform(root, HapticFeedbackConstants.KEYBOARD_TAP)
                        }
                    } catch (e: Exception) {
                        print(e.message)
                    }

                    performDelete(service)
                    handler.postDelayed(repeater, interval)
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(repeater)
                    true
                }

                else -> false
            }
        }
    }

    fun performDelete(service: InputMethodService) {
        val ic = service.currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(2, 0)
        val deleteCount = when {
            before.isNullOrEmpty() -> 1
            before.length >= 2 && Character.isSurrogatePair(before[0], before[1]) -> 2
            before.toString().contains("\\uD803".toRegex()) -> 2
            else -> 1
        }
        ic.deleteSurroundingText(deleteCount, 0)
        ic.getTextAfterCursor(2, 0)?.let { after ->
            if (after.toString().matches("[\\uD800-\\uDFFF]".toRegex())) {
                ic.deleteSurroundingText(1, 0)
            }
        }
        TopRowBuilder_Old.onTypedListener?.invoke()
    }
}